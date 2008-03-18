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

/**
 * 
 * @author heinljab
 *
 */
public class PullHarvest extends Harvest{
	
	/** */
	private static Log logger = LogFactory.getLog(PullHarvest.class);

	/**
	 * 
	 * @param sourceUrlString
	 */
	public PullHarvest(String sourceUrlString, HarvestDAOWriter harvestDAOWriter, HarvestNotificationSender notificationSender) {
		super(sourceUrlString, harvestDAOWriter, notificationSender);
	}
	
	/**
	 * @throws HarvestException 
	 * 
	 */
	public void doExecute() throws HarvestException{

		File toFile = fullFilePathForSourceUrl(sourceUrlString);;
		if (toFile.exists())
			toFile.delete();
		
		downloadUrlToFile(sourceUrlString, toFile);
		harvestFile(toFile);
	}

	/**
	 * 
	 * @param urlString
	 * @param toFile
	 * @throws HarvestException
	 */
	private static void downloadUrlToFile(String urlString, File toFile) throws HarvestException{
		
		logger.debug("Downloading from URL: " + urlString);

		InputStream istream = null;
		FileOutputStream fos = null;
		try{
			URL url = new URL(urlString);
			URLConnection httpConn = url.openConnection();
			httpConn.setRequestProperty("Accept", "application/rdf+xml");
			
			istream = httpConn.getInputStream();
			fos = new FileOutputStream(toFile);
	        
	        int i = -1;
	        byte[] bytes = new byte[1024];
	
	        while ((i = istream.read(bytes, 0, bytes.length)) != -1)
	        	fos.write(bytes, 0, i);
		}
		catch (IOException e){
			throw new HarvestException(e.toString(), e);
		}
		finally{
			try{
				logger.debug("Closing URL input stream: " + urlString);
		        if (istream!=null) istream.close();
			}
			catch (IOException e){
				logger.error("Failed to close URL input stream: " + e.toString(), e);
			}
			try{
				logger.debug("Closing file output stream: " + toFile.getAbsolutePath());
				if (fos!=null) fos.close();
			}
			catch (IOException e){
				logger.error("Failed to close file output stream: " + e.toString(), e);
			}
		}
	}
}
