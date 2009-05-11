package eionet.cr.harvest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
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
			doDownload = !Boolean.parseBoolean(GeneralConfig.getProperty(GeneralConfig.HARVESTER_DEBUG_USE_DOWNLOADED_FILES, "false"));
			if (doDownload){
				toFile.delete();
			}
		}
		
		InputStream inputStream = null;
		try{
			
			if (doDownload){
				
				logger.debug("Downloading");
				
				sourceAvailable = Boolean.FALSE;
				
				URL url = new URL(sourceUrlString);
				URLConnection urlConnection = url.openConnection();
				
				urlConnection.setRequestProperty("Accept", "application/rdf+xml, text/xml, */*");
				if (lastHarvest!=null){
					urlConnection.setIfModifiedSince(lastHarvest.getTime());
				}
				
				inputStream = urlConnection.getInputStream();
				sourceAvailable = Boolean.TRUE;
				
				if (urlConnection instanceof HttpURLConnection){
					if (((HttpURLConnection)urlConnection).getResponseCode()==HttpURLConnection.HTTP_NOT_MODIFIED){
						String msg = "Source not modified since " + lastHarvest.toString();
						logger.debug(msg);
						infos.add(msg);
						return;
					}
				}
			
				// harvest the downloaded file
				FileUtil.streamToFile(inputStream, toFile);
			}
			else{
				sourceAvailable = Boolean.TRUE;
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
		
		logger.debug("Parsing local file: " + toFile.getAbsolutePath());
		harvest(toFile);
	}

	/**
	 * @return the sourceAvailable
	 */
	public Boolean getSourceAvailable() {
		return sourceAvailable;
	}
}
