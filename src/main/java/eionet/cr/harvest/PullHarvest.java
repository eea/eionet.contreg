/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.cr.harvest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import eionet.cr.common.Predicates;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestDAO;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.harvest.util.arp.ARPSource;
import eionet.cr.harvest.util.arp.InputStreamBasedARPSource;
import eionet.cr.util.FileUtil;
import eionet.cr.util.GZip;
import eionet.cr.util.URLUtil;
import eionet.cr.util.Util;
import eionet.cr.util.xml.ConversionsParser;
import eionet.cr.util.xml.XmlAnalysis;
import eionet.cr.web.security.CRUser;

/**
 * 
 * @author heinljab
 *
 */
public class PullHarvest extends Harvest{
	
	/** */
	private Boolean sourceAvailable = null;	
	private Date lastHarvest = null;
	
	/** */
	private ConversionsParser convParser;

	/**
	 * 
	 * @param sourceUrlString
	 */
	public PullHarvest(String sourceUrlString, Date lastHarvest) {
		super(sourceUrlString);
		this.lastHarvest = lastHarvest;
	}
	
	/**
	 * @throws HarvestException 
	 * 
	 */
	protected void doExecute() throws HarvestException{

		File file = fullFilePathForSourceUrl(sourceUrlString);
		
		String contentType = null;
		InputStream inputStream = null;
		try{
			// reuse the file if it exists and the configuration allows to do it (e.g. for debugging purposes) */			
			if (file.exists() && Boolean.parseBoolean(GeneralConfig.getProperty(GeneralConfig.HARVESTER_USE_DOWNLOADED_FILES, "false"))){
				
				sourceAvailable = Boolean.TRUE;
				logger.debug("Harvesting the already downloaded file");
			}
			else{
				// delete the old file, should it exist
				if (file.exists()){
					file.delete();
				}
				
				// prepare URL connection
				URL url = new URL(StringUtils.substringBefore(sourceUrlString, "#"));
				URLConnection urlConnection = url.openConnection();				
				urlConnection.setRequestProperty("Accept", "application/rdf+xml, text/xml, */*;q=0.6");
				urlConnection.setRequestProperty("User-Agent", URLUtil.userAgentHeader());
				if (lastHarvest!=null){
					
					Boolean conversionModified = isConversionModifiedSinceLastHarvest();
					if (conversionModified==null || conversionModified.booleanValue()==false){
						
						urlConnection.setIfModifiedSince(lastHarvest.getTime());
						if (conversionModified!=null){
							logger.debug("The source's RDF conversion not modified since" + lastHarvest.toString());
						}
					}
					else{
						logger.debug("The source has an RDF conversion that has been modified since last harvest");
					}
				}

				// open connection stream				
				logger.debug("Downloading");
				IOException connectException = null;
				try{
					sourceAvailable = Boolean.FALSE;
					inputStream = urlConnection.getInputStream();
					sourceAvailable = Boolean.TRUE;					
				}
				catch (IOException e){
					connectException = e;
					logger.warn(e.toString());
				}
				
				String sourceNotExistMessage = null; 
				if (connectException!=null && connectException instanceof UnknownHostException){
					sourceNotExistMessage = "IP address of the host could not be determined";
				}
				else if (urlConnection instanceof HttpURLConnection){
					int responseCode = ((HttpURLConnection)urlConnection).getResponseCode();
					if ((responseCode>=400 && responseCode<=499) || responseCode==501 || responseCode==505){
						sourceNotExistMessage = "Got HTTP response code " + responseCode;
					}
				}

				// if we see that the source for surely doesn't exist, we delete it and return right away
				if (sourceNotExistMessage!=null){
					logger.debug(sourceNotExistMessage + ", going to delete the source");
					try {
						daoWriter = null; // we dont't want finishing actions to be done
						DAOFactory.get().getDao(HarvestSourceDAO.class).queueSourcesForDeletion(Collections.singletonList(sourceUrlString));
					}
					catch (DAOException e){
						logger.warn("Failure when deleting the source", e);
					}
					return;
				}
						
				// if source not available (i.e. link broken) then just set the last-refreshed metadata
				if (sourceAvailable.booleanValue()==false){
					setLastRefreshed(urlConnection, System.currentTimeMillis());
				}
				// source is available, so continue to extract it's contents and metadata
				else{
					
					// extract various metadata about this harvest source from url connection object
					setSourceMetadata(urlConnection);

					// skip if unsupported content type
					contentType = sourceMetadata.getObjectValue(Predicates.CR_MEDIA_TYPE);
					if (contentType!=null
							&& !contentType.startsWith("text/xml")
							&& !contentType.startsWith("application/xml")
							&& !contentType.startsWith("application/rdf+xml")
							&& !contentType.startsWith("application/octet-stream")
							&& !contentType.startsWith("application/x-gzip")){
	
						logger.debug("Unsupported content type: " + contentType);
					}
					else{
						// set the field indicating if source has been modified since last harvest
						if (urlConnection instanceof HttpURLConnection
							&& ((HttpURLConnection)urlConnection).getResponseCode()==HttpURLConnection.HTTP_NOT_MODIFIED){
							
							clearPreviousContent = false;
							
							if (previousHarvest!=null){
								// copy the number of triples and distinct subjects from previous harvest
								setStoredTriplesCount(previousHarvest.getTotalStatements());
								setDistinctSubjectsCount(previousHarvest.getTotalResources());
							}
							
							String msg = "Source not modified since " + lastHarvest.toString();
							logger.debug(msg);
							infos.add(msg);
						}
						else{
							// save the stream to file
							int totalBytes = FileUtil.streamToFile(inputStream, file);

							// if content-length for source metadata was previously not found, then set it to file size
							if (sourceMetadata.getObject(Predicates.CR_BYTE_SIZE)==null){
								sourceMetadata.addObject(Predicates.CR_BYTE_SIZE, new ObjectDTO(String.valueOf(totalBytes), true));
							}
						}
					}
				}
			}
		}
		catch (Exception e){
			throw new HarvestException(e.toString(), e);
		}
		finally{
			// close input stream
			try{ if (inputStream!=null) inputStream.close(); } catch (IOException e){}
		}
		
		// perform the harvest
		harvest(file, contentType);
	}
	
	/**
	 * 
	 * @return
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 * @throws IOException 
	 * @throws DAOException 
	 */
	private Boolean isConversionModifiedSinceLastHarvest() throws IOException, SAXException, ParserConfigurationException, DAOException{
		
		Boolean result = null;
		
		String schemaUri = daoFactory.getDao(HelperDAO.class).getSubjectSchemaUri(sourceUrlString);
		if (!StringUtils.isBlank(schemaUri)){
			
			// see if schema has RDF conversion
			convParser = ConversionsParser.parseForSchema(schemaUri);
			if (!StringUtils.isBlank(convParser.getRdfConversionId())){

				// see if the conversion XSL has changed since last harvest
				String xsl = convParser.getRdfConversionXslFileName();
				if (!StringUtils.isBlank(xsl)){

					String xslUrl = GeneralConfig.getRequiredProperty(GeneralConfig.XMLCONV_XSL_URL);
					xslUrl = MessageFormat.format(xslUrl, Util.toArray(xsl));
					result = URLUtil.isModifiedSince(xslUrl, lastHarvest.getTime());
				}
			}
		}
		
		return result;
	}

	/**
	 * Harvest the given file.
	 * 
	 * @param file
	 * @throws HarvestException 
	 */
	private void harvest(File file, String contentType) throws HarvestException{
		
		// remember the file's absolute path, so we can later detect if a new file was created during the pre-processing
		String originalPath = file.getAbsolutePath();
		
		
		// Testing whether the input file is zipped. If true, it is uncompressed and used as original source file.
		
		File unGZipped = unCompressGZip(file);
		if (unGZipped != null){
			file = unGZipped;
		}
		
		InputStream inputStream = null;
		try{
			
			ARPSource arpSource = null;
			
			/* pre-process the file; if it's still valid then open input stream and create ARP source object */
			/* (the file may not exist, if content type was unsupported or other reasons (see caller)*/
			if (file.exists()){
				try{
					if ((file=preProcess(file, contentType))!=null){
						
						inputStream = new FileInputStream(file);
						arpSource = new InputStreamBasedARPSource(inputStream);
					}
				}
				catch (Exception e){
					throw new HarvestException(e.toString(), e);
				}
			}

			harvest(arpSource);
		}
		finally{
			
			// close input stream
			if (inputStream!=null){
				try{ inputStream.close(); } catch (Exception e){ logger.error(e.toString(), e);}
			}
			
			// delete the file we harvested and the original one (in case a new file was created during the pre-processing)
			// the method is safe against situation where file or original file is actually null or doesn't exist
			deleteDownloadedFile(file);
			deleteDownloadedFile(originalPath);
			deleteDownloadedFile(unGZipped);
		}
	}

	private File unCompressGZip(File file){
		
		File unPackedFile = null;
		
		// Testing whether the input file is GZip or not.
		if (GZip.isFileGZip(file)){
			try {
				unPackedFile = GZip.unPack(file);
			} catch (Exception ex){
				System.out.println(ex.getMessage());
			}
		}
		return unPackedFile;
	}
	
	/**
	 * 
	 * @param file
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	private File preProcess(File file, String contentType) throws ParserConfigurationException, SAXException, IOException{

		if (contentType!=null && contentType.startsWith("application/rdf+xml")){
			return file;
		}

		String conversionId = convParser==null ? null : convParser.getRdfConversionId();
		if (StringUtils.isBlank(conversionId)){
			
			logger.debug("Response content type was " + contentType + ", trying to extract schema or DTD");
			
			XmlAnalysis xmlAnalysis = new XmlAnalysis();
			xmlAnalysis.parse(file);
			
			// get schema uri, if it's not found then fall back to dtd 
			String schemaOrDtd = xmlAnalysis.getSchemaLocation();
			if (schemaOrDtd==null || schemaOrDtd.length()==0){
				schemaOrDtd = xmlAnalysis.getSystemDtd();
				if (schemaOrDtd==null || !URLUtil.isURL(schemaOrDtd)){
					schemaOrDtd = xmlAnalysis.getPublicDtd();
				}
			}
			
			// if this file has a conversion to RDF, run it and return the reference to the resulting file
			if (schemaOrDtd!=null && schemaOrDtd.length()>0){
				
				sourceMetadata.addObject(Predicates.CR_SCHEMA, new ObjectDTO(schemaOrDtd, false));
				convParser = ConversionsParser.parseForSchema(schemaOrDtd);
				if (convParser!=null){
					conversionId = convParser.getRdfConversionId();
				}
			}
			else{
				logger.debug("No schema or DTD declared, going to parse as RDF");
				return file;
			}
		}
		
		if (!StringUtils.isBlank(conversionId)){
			
			logger.debug("Going to run RDF conversion");
			
			/* prepare conversion URL */

			String convertUrl = GeneralConfig.getRequiredProperty(GeneralConfig.XMLCONV_CONVERT_URL);
			Object[] args = new String[2];
			args[0] = URLEncoder.encode(conversionId);
			args[1] = URLEncoder.encode(sourceUrlString);
			convertUrl = MessageFormat.format(convertUrl, args);

			/* run conversion and save the response to file */

			File convertedFile = new File(file.getAbsolutePath() + ".converted");
			FileUtil.downloadUrlToFile(convertUrl, convertedFile);

			// delete the original file
			deleteDownloadedFile(file);

			// return converted file
			return convertedFile;
		}
		else{
			logger.debug("No RDF conversion found, no parsing will be done");
			return null;
		}
	}

	/**
	 * 
	 * @param urlConnection
	 */
	private void setLastRefreshed(URLConnection urlConnection, long lastRefreshedTime){
		
		String lastRefreshed = Util.dateToString(new Date(lastRefreshedTime), "yyyy-MM-dd'T'HH:mm:ss");
		sourceMetadata.addObject(Predicates.CR_LAST_REFRESHED, new ObjectDTO(String.valueOf(lastRefreshed), true));
	}
	
	/**
	 * 
	 * @param urlConnetion
	 */
	private void setSourceMetadata(URLConnection urlConnection){
		
		// set last-refreshed predicate
		long lastRefreshed = System.currentTimeMillis();
		setLastRefreshed(urlConnection, lastRefreshed);

		// detect the last-modified-date from HTTP response, if it's not >0, then take the value of last-refreshed 
		sourceLastModified = urlConnection.getLastModified();
		if (sourceLastModified<=0){
			sourceLastModified = lastRefreshed;
		}

		// set the last-modified predicate
		String s = Util.dateToString(new Date(sourceLastModified), "yyyy-MM-dd'T'HH:mm:ss");
		sourceMetadata.addObject(Predicates.CR_LAST_MODIFIED, new ObjectDTO(s, true));
		
		int contentLength = urlConnection.getContentLength();
		if (contentLength>=0){
			sourceMetadata.addObject(Predicates.CR_BYTE_SIZE, new ObjectDTO(String.valueOf(contentLength), true));
		}
		
		String contentType = urlConnection.getContentType();
		if (contentType!=null && contentType.length()>0){
			String charset = null;
			int i = contentType.indexOf(";");
			if (i>0){
				int j = contentType.indexOf("charset=", i);
				if (j>i){
					int k = contentType.indexOf(";", j);
					k = k<0 ? contentType.length() : k;
					charset = contentType.substring(j + "charset=".length(), k).trim(); 
				}
				contentType = contentType.substring(0, i).trim();
			}
			
			sourceMetadata.addObject(Predicates.CR_MEDIA_TYPE, new ObjectDTO(String.valueOf(contentType), true));
			if (charset!=null && charset.length()>0){
				sourceMetadata.addObject(Predicates.CR_CHARSET, new ObjectDTO(String.valueOf(charset), true));
			}
		}
	}
	
	/**
	 * 
	 * @param path
	 */
	private void deleteDownloadedFile(String path){
		deleteDownloadedFile(new File(path));
	}

	/**
	 * 
	 * @param file
	 */
	private void deleteDownloadedFile(File file){
		
		if (file==null || !file.exists())
			return;
		
		try{
			// delete unless the configuration requires otherwise
			if (GeneralConfig.getProperty(GeneralConfig.HARVESTER_DELETE_DOWNLOADED_FILES, "true").equals("true")){
				file.delete();
			}
		}
		catch (RuntimeException e){
			logger.error(e.toString(), e);
		}
	}

	/**
	 * @return the sourceAvailable
	 */
	public Boolean getSourceAvailable() {
		return sourceAvailable;
	}
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.harvest.Harvest#doHarvestStartedActions()
	 */
	protected void doHarvestStartedActions() throws HarvestException{
		
		logger.debug("Pull harvest started");
		super.doHarvestStartedActions();
	}

	/**
	 * 
	 * @param sourceUrl
	 * @return
	 * @throws DAOException 
	 */
	public static PullHarvest createFullSetup(String sourceUrl, boolean urgent) throws DAOException{
		
		return createFullSetup(DAOFactory.get().getDao(
				HarvestSourceDAO.class).getHarvestSourceByUrl(sourceUrl), urgent);
	}
	
	/**
	 * 
	 * @param dto
	 * @param urgent
	 * @return
	 * @throws DAOException
	 */
	public static PullHarvest createFullSetup(HarvestSourceDTO dto, boolean urgent) throws DAOException{
		
		int numOfResources = dto.getResources()==null ? 0 : dto.getResources().intValue();
		
		PullHarvest harvest = new PullHarvest(dto.getUrl(), urgent ? null : dto.getLastHarvest());
		
		harvest.setPreviousHarvest(DAOFactory.get().getDao(HarvestDAO.class).getLastHarvest(
				dto.getSourceId().intValue()));
		harvest.setDaoWriter(new HarvestDAOWriter(
				dto.getSourceId().intValue(), Harvest.TYPE_PULL, numOfResources, CRUser.application.getUserName()));
		harvest.setNotificationSender(new HarvestNotificationSender());
		
		return harvest;
	}
}
