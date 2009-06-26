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

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import eionet.cr.config.GeneralConfig;
import eionet.cr.harvest.util.InputStreamBasedARPSource;
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

		boolean doDownload = true;
		
		File file = fullFilePathForSourceUrl(sourceUrlString);;
		if (file.exists()){
			doDownload = !Boolean.parseBoolean(GeneralConfig.getProperty(GeneralConfig.HARVESTER_USE_DOWNLOADED_FILES, "false"));
			if (doDownload){
				file.delete();
			}
		}
		
		String contentType = null;
		InputStream inputStream = null;
		try{
			
			if (doDownload){
				
				logger.debug("Downloading");
				
				sourceAvailable = Boolean.FALSE;
				
				URL url = new URL(sourceUrlString);
				URLConnection urlConnection = url.openConnection();				
				
				urlConnection.setRequestProperty("Accept", "application/rdf+xml, text/xml, */*");
				urlConnection.setRequestProperty("User-Agent", getUserAgent());
				if (lastHarvest!=null){
					urlConnection.setIfModifiedSince(lastHarvest.getTime());
				}
				
				inputStream = urlConnection.getInputStream();
				
				// having reached this point, we assume the URL is not broken, ie the source is available
				// (if it's not then we shouldn't reach this point, but instead we should be in the catch exception block)
				sourceAvailable = Boolean.TRUE; 
				
				contentType = urlConnection.getContentType();				
				if (contentType!=null
						&& !contentType.startsWith("text/xml")
						&& !contentType.startsWith("application/xml")
						&& !contentType.startsWith("application/rdf+xml")){

					logger.debug("Skipping because of unsupported content type: " + contentType);
					return;
				}
				else{
					if (urlConnection instanceof HttpURLConnection){
						if (((HttpURLConnection)urlConnection).getResponseCode()==HttpURLConnection.HTTP_NOT_MODIFIED){
							
							if (previousHarvest!=null){
								setStoredTriplesCount(previousHarvest.getTotalStatements());
								setDistinctSubjectsCount(previousHarvest.getTotalResources());
							}
							
							String msg = "Source not modified since " + lastHarvest.toString();
							logger.debug(msg);
							infos.add(msg);
							return;
						}
					}

					// save the stream to file
					FileUtil.streamToFile(inputStream, file);					
				}
			}
			else{
				sourceAvailable = Boolean.TRUE;
				logger.debug("Harvesting the already downloaded file");
			}
		}
		catch (IOException e){
			throw new HarvestException(e.toString(), e);
		}
		finally{
			try{
				if (inputStream!=null)
					inputStream.close();
			}
			catch (IOException e){}
		}
		
		/* harvest the downloaded file */
		
		harvest(file, contentType);
	}

	/**
	 * Harvest the given file.
	 * 
	 * @param file
	 * @throws HarvestException 
	 */
	private void harvest(File file, String contentType) throws HarvestException{
		
		InputStream inputStream = null;
		try{
			
			/* pre-process the file and if it's still valid then open input stream */
			
			try{
				if ((file=preProcess(file, contentType))==null)
					return;
				else
					inputStream = new FileInputStream(file);
			}
			catch (Exception e){
				throw new HarvestException(e.toString(), e);
			}

			/* create ARPSource based on the file's input stream and harvest it */
			
			harvest(new InputStreamBasedARPSource(inputStream));
		}
		finally{
			
			// close input stream
			if (inputStream!=null){
				try{ inputStream.close(); } catch (Exception e){ logger.error(e.toString(), e);}
			}
			
			// delete the file
			deleteDownloadedFile(file);
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
	 * @param file
	 */
	private void deleteDownloadedFile(File file){
		
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
