package eionet.cr.dao;

import java.util.List;

import eionet.cr.dto.UrgentHarvestQueueItemDTO;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public interface UrgentHarvestQueueDAO {

	/**
	 * 
	 * @throws DAOException
	 */
	public abstract void addPullHarvests(List<UrgentHarvestQueueItemDTO> queueItems) throws DAOException;
	
	/**
	 * 
	 * @param queueItem
	 * @throws DAOException
	 */
	public abstract void addPushHarvest(UrgentHarvestQueueItemDTO queueItem) throws DAOException;
	
	/**
	 * 
	 * @return
	 */
	public abstract List<UrgentHarvestQueueItemDTO> getUrgentHarvestQueue() throws DAOException;
	
	/**
	 * 
	 * @return
	 * @throws DAOException
	 */
	public abstract UrgentHarvestQueueItemDTO poll() throws DAOException;
}
