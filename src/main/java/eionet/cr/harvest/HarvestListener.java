package eionet.cr.harvest;

import eionet.cr.harvest.util.RDFResource;
import eionet.cr.harvest.util.RDFResourceProperty;

/**
 * 
 * @author heinljab
 *
 */
public interface HarvestListener {

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
	 * @throws HarvestException
	 */
	public void harvestCancelled() throws HarvestException;	
}
