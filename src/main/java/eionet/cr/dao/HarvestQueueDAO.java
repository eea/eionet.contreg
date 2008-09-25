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
	 * @return
	 * @throws DAOException 
	 */
	public abstract List<HarvestQueueItemDTO> getNormalHarvestQueue() throws DAOException;
	
	/**
	 * 
	 * @return
	 */
	public abstract List<HarvestQueueItemDTO> getUrgentHarvestQueue() throws DAOException;

}
