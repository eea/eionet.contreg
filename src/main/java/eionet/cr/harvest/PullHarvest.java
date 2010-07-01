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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
import eionet.cr.dao.postgre.PostgreSQLHarvestSourceDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.harvest.util.HarvestUrlConnection;
import eionet.cr.harvest.util.MimeTypeConverter;
import eionet.cr.harvest.util.arp.ARPSource;
import eionet.cr.harvest.util.arp.InputStreamBasedARPSource;
import eionet.cr.util.FileUtil;
import eionet.cr.util.GZip;
import eionet.cr.util.URLUtil;
import eionet.cr.util.UrlRedirectAnalyzer;
import eionet.cr.util.UrlRedirectionInfo;
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
	
	/**
	 * 
	 */
	public static final int maxRedirectionsAllowed = 4;
	private boolean fullSetupMode = false;
	private boolean fullSetupModeUrgent = false;
	
	/** */
	private Boolean sourceAvailable = null;	
	private Date lastHarvest = null;
	
	/** */
	private ConversionsParser convParser;
	
	/** 
	 * In case of redirected sources, each redirected source will be harvested without additional recursive redirection.
	 * */
	private boolean recursiveHarvestDisabled = false;

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
		
		HarvestUrlConnection harvestUrlConnection = null;
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
				harvestUrlConnection = HarvestUrlConnection.getConnection(sourceUrlString);

				setConversionModified(harvestUrlConnection.getUrlConnection());

				// open connection stream				
				logger.debug("Downloading");
				try{
					harvestUrlConnection.openInputStream();
					sourceAvailable = harvestUrlConnection.isSourceAvailable();
				}
				catch (Exception e){
					logger.warn(e.toString());
				}
				
				if (!harvestUrlConnection.isSourceAvailable()){	
					logger.debug(harvestUrlConnection.getSourceNotExistMessage() + ", going to delete the source");
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
				if (harvestUrlConnection.isSourceAvailable() == false){
					setLastRefreshed(harvestUrlConnection.getConnection(), System.currentTimeMillis());
				}
				// source is available, so continue to extract it's contents and metadata
				else{
					
					// extract various metadata about this harvest source from url connection object
					setSourceMetadata(harvestUrlConnection.getConnection());

					// if Url is redirected, take action. 
					if (harvestUrlConnection.getRedirectionInfo().isRedirected() && harvestUrlConnection.isHttpConnection() && recursiveHarvestDisabled == false){
						redirectedSourceHarvest(harvestUrlConnection);
					} 
					// NOTE: If URL is redirected, content type is null.
					// skip if unsupported content type
					
					if (!harvestUrlConnection.isContentTypeValid(sourceMetadata.getObjectValue(Predicates.CR_MEDIA_TYPE))){
						logger.debug("Unsupported content type: " + harvestUrlConnection.getContentType());
					}
					else{
						if (harvestUrlConnection.isHttpConnection() && harvestUrlConnection.getResponseCode()==HttpURLConnection.HTTP_NOT_MODIFIED){
							
							clearPreviousContent = false;
							
							if (previousHarvest!=null){
								// copy the number of triples and distinct subjects from previous harvest
								setStoredTriplesCount(previousHarvest.getTotalStatements());
								setDistinctSubjectsCount(previousHarvest.getTotalResources());
							}
							
							String msg = "Source not modified since " + lastHarvest.toString();
							logger.debug(msg);
							infos.add(msg);
						} else{
							// save the stream to file
							int totalBytes = FileUtil.streamToFile(harvestUrlConnection.getInputStream(), file);

							// if content-length for source metadata was previously not found, then set it to file size
							if (sourceMetadata.getObject(Predicates.CR_BYTE_SIZE)==null){
								sourceMetadata.addObject(Predicates.CR_BYTE_SIZE, new ObjectDTO(String.valueOf(totalBytes), true));
							}
						}
					} 
				} // if Source is available.
			}
		}
		catch (Exception e){
			throw new HarvestException(e.toString(), e);
		}
		finally{
			// close input stream
			try{ 
				if (harvestUrlConnection != null && harvestUrlConnection.getInputStream()!=null) 
					harvestUrlConnection.getInputStream().close(); 
			} catch (IOException e){}
		}
		
		// perform the harvest
		harvest(file, contentType);
	}
	
	
	/**
	 * Performs the harvesting for redirected harvests.
	 */
	private void redirectedSourceHarvest(HarvestUrlConnection harvestUrlConnection) throws HarvestException{
		
		int redirectionsFound = -1;
		
		HarvestSourceDTO originalSource = new HarvestSourceDTO();
		// Loading full information about the initial source
		try {
			try {
				originalSource = DAOFactory.get().getDao(HarvestSourceDAO.class).getHarvestSourceByUrl(sourceUrlString);
			} catch (DAOException e){
				throw new HarvestException("Original source not found in HARVEST_SOURCE. Cannot continue parsing redirected sources.");
			}
			
			// The case when the original URL has been redirected to a new source.
			// Before continuing with harvesting, the potential chain of further redirections
			// is going to be analyzed and limited if necessary.
			
			String directedUrl = new URL(StringUtils.substringBefore(harvestUrlConnection.getRedirectionInfo().getTargetURL(), "#")).toString();
			
			List <UrlRedirectionInfo> redirectedUrls = new ArrayList<UrlRedirectionInfo>();
			
			UrlRedirectionInfo lastUrl = new UrlRedirectionInfo();
			lastUrl = UrlRedirectAnalyzer.analyzeUrlRedirection(directedUrl);
			redirectedUrls.add(lastUrl);
			
			while (lastUrl.isRedirected() == true){
				lastUrl = UrlRedirectAnalyzer.analyzeUrlRedirection(lastUrl.getTargetURL());
				redirectedUrls.add(lastUrl);
				
				redirectionsFound = redirectedUrls.size();
				// Checking the count of redirects.
				if (redirectionsFound>PullHarvest.maxRedirectionsAllowed){
					throw new HarvestException("Too many redirections for url: " + sourceUrlString+". Found "+ redirectionsFound+", allowed "+PullHarvest.maxRedirectionsAllowed);
				}
			}
			
			// Going to harvest all directed url's
			for (int i = 0; i < redirectedUrls.size(); i++){
	
				UrlRedirectionInfo current = redirectedUrls.get(i);

				HarvestSourceDTO directedSource;
				// Loading information about the source
				directedSource = DAOFactory.get().getDao(HarvestSourceDAO.class).getHarvestSourceByUrl(current.getSourceURL());
				
				if (directedSource != null){
					// Checking if directedSource has larger interval_minutes value compared to original and updating accordingly
					if (directedSource.getIntervalMinutes() > originalSource.getIntervalMinutes()){
						directedSource.setIntervalMinutes(originalSource.getIntervalMinutes());
						// Saving the updated source to database
						DAOFactory.get().getDao(HarvestSourceDAO.class).editSource(directedSource);
					}
					
				} else {
					directedSource = new HarvestSourceDTO();
					directedSource.setIntervalMinutes(originalSource.getIntervalMinutes());
					directedSource.setUrl(current.getSourceURL());
					DAOFactory.get().getDao(HarvestSourceDAO.class).addSource(directedSource, null);
				}
				
				Date now = new Date();
				
				// The conditions applies to current url only. If "current" is not harvested, the one following the "current" is still attempted.
				
				if (
						directedSource.getLastHarvest() == null || 
						this instanceof InstantHarvest ||
						(now.getTime() - directedSource.getLastHarvest().getTime()) > directedSource.getIntervalMinutes() * 60 * 1000
					){
					//PullHarvest harvest = new PullHarvest(directedSource.getUrl(), null);
					PullHarvest harvest = null;
					// The flag for fullSetupMode is set in createFullSetup when initializing the harvest that way.
					// The value for Urgent is also received there.
					
					if (this.isFullSetupMode()){
						harvest = createFullSetup(directedSource, this.isFullSetupModeUrgent());
					}
					else {
						harvest = new PullHarvest(directedSource.getUrl(), null);
					}
					
					// setting the flag to not allow it recursively harvest their followers.
					// During the harvest of this instance, the code won't reach this block.
					harvest.setRecursiveHarvestDisabled(true);
					harvest.execute();
				}
				
			}
		} catch (Exception ex){
			throw new HarvestException("Exception during harvesting redirected sources: "+ex.getMessage(), ex);
		}
	}
	
	/**
	 * 
	 * @return
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 * @throws IOException 
	 * @throws DAOException 
	 */
	
	private void setConversionModified(HttpURLConnection urlConnection) throws DAOException, IOException, ParserConfigurationException, SAXException{
		
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
			String rdfTypeOfMediaType = MimeTypeConverter.getRdfTypeFor(contentType);
			if (!StringUtils.isBlank(rdfTypeOfMediaType)){
				sourceMetadata.addObject(Predicates.RDF_TYPE, new ObjectDTO(String.valueOf(rdfTypeOfMediaType), false));
			}
			
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
		
		harvest.setFullSetupMode(true);
		harvest.setFullSetupModeUrgent(urgent);
		harvest.setPreviousHarvest(DAOFactory.get().getDao(HarvestDAO.class).getLastHarvest(
				dto.getSourceId().intValue()));
		harvest.setDaoWriter(new HarvestDAOWriter(
				dto.getSourceId().intValue(), Harvest.TYPE_PULL, numOfResources, CRUser.application.getUserName()));
		harvest.setNotificationSender(new HarvestNotificationSender());
		
		return harvest;
	}

	public boolean isRecursiveHarvestDisabled() {
		return recursiveHarvestDisabled;
	}

	public void setRecursiveHarvestDisabled(boolean recursiveHarvestDisabled) {
		this.recursiveHarvestDisabled = recursiveHarvestDisabled;
	}

	public boolean isFullSetupMode() {
		return fullSetupMode;
	}

	public void setFullSetupMode(boolean fullSetupMode) {
		this.fullSetupMode = fullSetupMode;
	}

	public boolean isFullSetupModeUrgent() {
		return fullSetupModeUrgent;
	}

	public void setFullSetupModeUrgent(boolean fullSetupModeUrgent) {
		this.fullSetupModeUrgent = fullSetupModeUrgent;
	}
}
