package eionet.cr.harvest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
			URLConnection httpConn = url.openConnection();
// FIXME - use IfModifiedSince, but keep in mind that if the source has not been modified
// indeed then you get a 304 response instead of any content and you need to handle this situation
//			if (lastHarvest!=null)
//				httpConn.setIfModifiedSince(lastHarvest.getTime());
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
		harvest(toFile);
	}

	/**
	 * @return the sourceAvailable
	 */
	public Boolean getSourceAvailable() {
		return sourceAvailable;
	}
}
