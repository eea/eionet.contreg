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
	 * @throws HarvestException
	 */
	public void resourceHarvested(RDFResource resource) throws HarvestException;
	
	/**
	 * @throws HarvestException
	 * 
	 */
	public void harvestStarted() throws HarvestException;
	
	/**
	 * @throws HarvestException
	 * 
	 */
	public void harvestFinished() throws HarvestException;
	
	/**
	 * 
	 */
	public HarvestException getFatalException();
}
