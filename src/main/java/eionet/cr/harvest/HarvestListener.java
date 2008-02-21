package eionet.cr.harvest;

import eionet.cr.harvest.util.RDFResource;

/**
 * 
 * @author heinljab
 *
 */
public interface HarvestListener {

	/**
	 * 
	 * @param resource
	 */
	public void resourceHarvested(RDFResource resource);
	
	/**
	 * 
	 */
	public void harvestStarted();
	
	/**
	 * 
	 */
	public void harvestFinished();
	
	/**
	 * 
	 */
	public boolean hasFatalError();
}
