package eionet.cr.harvest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

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

		File toFile = fullFilePathForSourceUrl(sourceUrlString);;
		if (toFile.exists())
			toFile.delete();
		
		logger.debug("Downloading");
		
		InputStream inputStream = null;
		try{
			sourceAvailable = new Boolean(false);
			
			URL url = new URL(sourceUrlString);
			URLConnection urlConnection = url.openConnection();
			
			urlConnection.setRequestProperty("Accept", "application/rdf+xml, text/xml, */*");
			if (lastHarvest!=null){
				urlConnection.setIfModifiedSince(lastHarvest.getTime());
			}
			
			inputStream = urlConnection.getInputStream();
			sourceAvailable = new Boolean(true);
			
			if (urlConnection instanceof HttpURLConnection){
				if (((HttpURLConnection)urlConnection).getResponseCode()==HttpURLConnection.HTTP_NOT_MODIFIED){
					String msg = "Source not modified since " + lastHarvest.toString();
					logger.debug(msg);
					infos.add(msg);
					return;
				}
			}
			
			FileUtil.streamToFile(inputStream, toFile);
		}
		catch (IOException e){
			throw new HarvestException(e.toString(), e);
		}
		finally{
			try{
				if (inputStream!=null) inputStream.close();
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
