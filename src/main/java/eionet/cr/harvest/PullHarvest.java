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
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.Date;

import eionet.cr.config.GeneralConfig;
import eionet.cr.util.FileUtil;

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
		
		File toFile = fullFilePathForSourceUrl(sourceUrlString);;
		if (toFile.exists()){
			doDownload = !Boolean.parseBoolean(GeneralConfig.getProperty(GeneralConfig.HARVESTER_USE_DOWNLOADED_FILES, "false"));
			if (doDownload){
				toFile.delete();
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
				logger.debug("Content type in response header = " + contentType);
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
					FileUtil.streamToFile(inputStream, toFile);					
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
		
		try{
			harvest(toFile, contentType);
		}
		finally{
		
			// delete the downloaded file, unless the configuration doesn't require to do so
			try{
				if (GeneralConfig.getProperty(GeneralConfig.HARVESTER_DELETE_DOWNLOADED_FILES, "true").equals("true")){
					toFile.delete();
				}
			}
			catch (Exception e){}
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
