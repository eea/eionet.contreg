package eionet.cr.harvest;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eionet.cr.util.HttpUtil;
import eionet.cr.util.Util;

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
	protected void doExecute() throws HarvestException{

		File toFile = fullFilePathForSourceUrl(sourceUrlString);;
		if (toFile.exists())
			toFile.delete();
		
		logger.debug("Downloading from URL: " + sourceUrlString);
		try{
			HttpUtil.downloadUrlToFile(sourceUrlString, toFile);
		}
		catch (IOException e){
			throw new HarvestException(e.toString(), e);
		}
		
		logger.debug("Parsing local file: " + toFile.getAbsolutePath());
		harvestFile(toFile);
	}
}
