package eionet.cr.harvest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eionet.cr.util.FileUtil;
import eionet.cr.util.Util;

/**
 * 
 * @author heinljab
 *
 */
public class PullHarvest extends Harvest{
	
	/** */
	private static Log logger = LogFactory.getLog(PullHarvest.class);
	
	/** */
	private Boolean sourceAvailable = null;
	private Long lastHarvestTimestamp = null;

	/**
	 * 
	 * @param sourceUrlString
	 */
	public PullHarvest(String sourceUrlString, Long lastHarvestTimestamp) {
		super(sourceUrlString);
		this.lastHarvestTimestamp = lastHarvestTimestamp;
	}
	
	/**
	 * @throws HarvestException 
	 * 
	 */
	protected void doExecute() throws HarvestException{

		File toFile = fullFilePathForSourceUrl(sourceUrlString);;
		if (toFile.exists())
			toFile.delete();
		
		logger.debug("Downloading from URL: " + sourceUrlString);
		
		InputStream inputStream = null;
		try{
			sourceAvailable = new Boolean(false);
			URL url = new URL(sourceUrlString);
			URLConnection httpConn = url.openConnection();
			if (lastHarvestTimestamp!=null && lastHarvestTimestamp.longValue()>0)
				httpConn.setIfModifiedSince(lastHarvestTimestamp.longValue());
			httpConn.setRequestProperty("Accept", "application/rdf+xml, text/xml, */*");
			inputStream = httpConn.getInputStream();
			sourceAvailable = new Boolean(true);
			
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
		harvestFile(toFile);
	}

	/**
	 * @return the sourceAvailable
	 */
	public Boolean getSourceAvailable() {
		return sourceAvailable;
	}
}
