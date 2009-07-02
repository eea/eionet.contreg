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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import eionet.cr.common.Predicates;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.harvest.util.arp.ARPSource;
import eionet.cr.harvest.util.arp.ATriple;
import eionet.cr.harvest.util.arp.InputStreamBasedARPSource;
import eionet.cr.util.FileUtil;
import eionet.cr.util.URLUtil;
import eionet.cr.util.Util;
import eionet.cr.util.xml.ConversionsParser;
import eionet.cr.util.xml.XmlAnalysis;

/**
 * 
 * @author heinljab
 *
 */
public class PullHarvest extends Harvest{
	
	/** */
	private static String userAgent;
	
	/** */
	private Boolean sourceAvailable = null;
	private Date lastHarvest = null;

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
				URL url = new URL(sourceUrlString);
				URLConnection urlConnection = url.openConnection();				
				urlConnection.setRequestProperty("Accept", "application/rdf+xml, text/xml, */*");
				urlConnection.setRequestProperty("User-Agent", getUserAgent());
				if (lastHarvest!=null){
					urlConnection.setIfModifiedSince(lastHarvest.getTime());
				}

				// open connection stream				
				logger.debug("Downloading");
				try{
					sourceAvailable = Boolean.FALSE;
					inputStream = urlConnection.getInputStream();
					sourceAvailable = Boolean.TRUE;					
				}
				catch (IOException e){
					logger.warn(e.toString(), e);
				}
				
				// if source not available (i.e. link broken) then just set the last-refreshed metadata
				if (sourceAvailable.booleanValue()==false){
					setLastRefreshed(urlConnection);
				}
				// source is avaialable, so continue to extract it's contents and metadata
				else{
					
					// extract various metadata about this harvest source from url connection object
					setSourceMetadata(urlConnection);

					// skip if unsupported content type
					contentType = sourceMetadata.getObjectValue(Predicates.CR_MEDIA_TYPE);
					if (contentType!=null
							&& !contentType.startsWith("text/xml")
							&& !contentType.startsWith("application/xml")
							&& !contentType.startsWith("application/rdf+xml")){
	
						logger.debug("Unsupported content type: " + contentType);
					}
					else{
						// content type OK, but skip if not modified since last harvest
						if (urlConnection instanceof HttpURLConnection
							&& ((HttpURLConnection)urlConnection).getResponseCode()==HttpURLConnection.HTTP_NOT_MODIFIED){
								
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
							// content type OK, source modified since last harvest, so save the stream to file
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
		catch (IOException e){
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
	 * Harvest the given file.
	 * 
	 * @param file
	 * @throws HarvestException 
	 */
	private void harvest(File file, String contentType) throws HarvestException{
		
		// remember the file's absolute path, so we can later detect if a new file was created during the pre-processing
		String originalPath = file.getAbsolutePath();
		
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
			
			// delete the file we harvested
			deleteDownloadedFile(file);
			
			// delete the original file if a new one was created during the pre-processing
			if (!originalPath.equals(file.getAbsolutePath())){
				deleteDownloadedFile(file);
			}
		}
	}

	/**
	 * 
	 * @param file
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	private File preProcess(File file, String contentType) throws ParserConfigurationException, SAXException, IOException{

		if (contentType!=null && contentType.startsWith("application/rdf+xml"))
			return file;
		
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
			
			/* get the URL of the conversion service method that returns the list of available conversions */
			
			String listConversionsUrl = GeneralConfig.getRequiredProperty(GeneralConfig.XMLCONV_LIST_CONVERSIONS_URL);
			listConversionsUrl = MessageFormat.format(listConversionsUrl, Util.toArray(URLEncoder.encode(schemaOrDtd)));

			/* open connection to the list-conversions URL */
			
			URL url = new URL(listConversionsUrl);
			URLConnection httpConn = url.openConnection();
			
			/* parse the returned input stream with eionet.cr.util.xml.ConversionsParser */
			
			InputStream inputStream = null;
			try{
				inputStream = httpConn.getInputStream();				
				ConversionsParser conversionsParser = new ConversionsParser();
				conversionsParser.parse(inputStream);
				
				/* see if ConversionsParser found any conversions available */
				
				String conversionId = conversionsParser.getRdfConversionId();
				if (conversionId!=null && conversionId.length()>0){
					
					logger.debug("Extracted schema or DTD has an RDF conversion, going to run it");
					
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
					logger.debug("Extracted schema or DTD has no RDF conversion, no parsing will be done");
					return null;
				}
			}
			finally{
				try{
					if (inputStream!=null) inputStream.close();
				}
				catch (IOException e){}
			}
		}
		else{
			logger.debug("No schema or DTD declared, going to parse as RDF");
		}
		
		return file;
	}

	/**
	 * 
	 * @param urlConnection
	 */
	private void setLastRefreshed(URLConnection urlConnection){
		
		String lastRefreshed = Util.dateToString(new Date(System.currentTimeMillis()), "yyyy-MM-dd'T'HH:mm:ss");
		sourceMetadata.addObject(Predicates.CR_LAST_REFRESHED, new ObjectDTO(String.valueOf(lastRefreshed), true));
	}
	
	/**
	 * 
	 * @param urlConnetion
	 */
	private void setSourceMetadata(URLConnection urlConnection){
		
		setLastRefreshed(urlConnection);

		long lastModified = urlConnection.getLastModified();
		if (lastModified>0){
			String s = Util.dateToString(new Date(lastModified), "yyyy-MM-dd'T'HH:mm:ss");
			sourceMetadata.addObject(Predicates.CR_LAST_MODIFIED, new ObjectDTO(s, true));
		}
		
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
	
	/**
	 * 
	 * @return
	 */
	private static final String getUserAgent(){
		
		if (PullHarvest.userAgent==null){
			
			String ua = GeneralConfig.getRequiredProperty(GeneralConfig.APPLICATION_USERAGENT);
			Object[] args = new String[1];
			args[0] = GeneralConfig.getRequiredProperty(GeneralConfig.APPLICATION_VERSION);
			PullHarvest.userAgent = MessageFormat.format(ua, args);
		}
		
		return PullHarvest.userAgent;
	}
}
