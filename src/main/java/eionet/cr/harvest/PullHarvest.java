package eionet.cr.harvest;

import java.io.File;
import java.net.HttpURLConnection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eionet.cr.util.Util;

/**
 * 
 * @author heinljab
 *
 */
public class PullHarvest extends Harvest{
	
	/** */
	static Log logger = LogFactory.getLog(PullHarvest.class);

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
		
		downloadUrlToFile(sourceUrlString, toFile);
		harvestFile(toFile);
	}
}
