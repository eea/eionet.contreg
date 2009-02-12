package eionet.cr.dao;

import java.util.List;

import eionet.cr.dto.HarvestQueueItemDTO;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public interface HarvestQueueDAO {

	/**
	 * 
	 * @throws DAOException
	 */
	public abstract void addPullHarvest(HarvestQueueItemDTO queueItem) throws DAOException;
	
	/**
	 * 
	 * @param queueItem
	 * @throws DAOException
	 */
	public abstract void addPushHarvest(HarvestQueueItemDTO queueItem) throws DAOException;
	
	/**
	 * 
	 * @return
	 * @throws DAOException 
	 */
	public abstract List<HarvestQueueItemDTO> getNormalHarvestQueue() throws DAOException;
	
	/**
	 * 
	 * @return
	 */
	public abstract List<HarvestQueueItemDTO> getUrgentHarvestQueue() throws DAOException;
	
	/**
	 * 
	 * @return
	 * @throws DAOException
	 */
	public abstract HarvestQueueItemDTO pollNormal() throws DAOException;
	
	/**
	 * 
	 * @return
	 * @throws DAOException
	 */
	public abstract HarvestQueueItemDTO pollUrgent() throws DAOException;

	/**
	 * 
	 * @return
	 * @throws DAOException
	 */
	public abstract HarvestQueueItemDTO peekNormal() throws DAOException;
	
	/**
	 * 
	 * @return
	 * @throws DAOException
	 */
	public abstract HarvestQueueItemDTO peekUrgent() throws DAOException;

	/**
	 * 
	 * @return
	 * @throws DAOException
	 */
	public abstract void deleteQueueItem(HarvestQueueItemDTO harvestQueueItemDTO) throws DAOException;
}
