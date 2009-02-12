package eionet.cr.harvest.scheduled;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dto.HarvestQueueItemDTO;
import eionet.cr.harvest.HarvestException;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class HarvestQueue{
	
	/** */
	private static Log logger = LogFactory.getLog(HarvestQueue.class);

	/** */
	public static final String PRIORITY_NORMAL = "normal";
	public static final String PRIORITY_URGENT = "urgent";
	
	/**
	 * 
	 * @param priority
	 * @throws HarvestException 
	 */
	public static synchronized void addPullHarvest(String url, String priority) throws HarvestException{
		
		HarvestQueueItemDTO dto = new HarvestQueueItemDTO();
		dto.setUrl(url);
		dto.setPriority(priority);
		
		try {
			DAOFactory.getDAOFactory().getHarvestQueueDAO().addPullHarvest(dto);
			logger.debug("Pull harvest added to the " + priority + " queue, url = " + url);
		}
		catch (DAOException e) {
			throw new HarvestException(e.toString(), e);
		}
	}

	/**
	 * 
	 * @param pushContent
	 * @param url
	 * @param priority
	 * @throws HarvestException 
	 */
	public static synchronized void addPushHarvest(String pushContent, String url, String priority) throws HarvestException{
		
		HarvestQueueItemDTO dto = new HarvestQueueItemDTO();
		dto.setUrl(url);
		dto.setPriority(priority);
		dto.setPushedContent(pushContent);
		
		try {
			DAOFactory.getDAOFactory().getHarvestQueueDAO().addPushHarvest(dto);
			logger.debug("Push harvest added to the " + priority + " queue, url = " + url);
		}
		catch (DAOException e) {
			throw new HarvestException(e.toString(), e);
		}
	}
	
	/**
	 * 
	 * @param priority
	 * @return
	 * @throws HarvestException 
	 */
	public static synchronized HarvestQueueItemDTO poll(String priority) throws HarvestException{
		
		try {
			if (priority.equals(HarvestQueue.PRIORITY_NORMAL))
				return DAOFactory.getDAOFactory().getHarvestQueueDAO().pollNormal();
			else
				return DAOFactory.getDAOFactory().getHarvestQueueDAO().pollUrgent();
		}
		catch (DAOException e) {
			throw new HarvestException(e.toString(), e);
		}
	}

	/**
	 * 
	 * @param priority
	 * @return
	 * @throws HarvestException 
	 */
	public static synchronized HarvestQueueItemDTO peek(String priority) throws HarvestException{
		
		try {
			if (priority.equals(HarvestQueue.PRIORITY_NORMAL))
				return DAOFactory.getDAOFactory().getHarvestQueueDAO().peekNormal();
			else
				return DAOFactory.getDAOFactory().getHarvestQueueDAO().peekUrgent();
		}
		catch (DAOException e) {
			throw new HarvestException(e.toString(), e);
		}
	}
	
	/**
	 * 
	 * @param harvestQueueItemDTO
	 * @throws HarvestException
	 */
	public static synchronized void deleteQueueItem(HarvestQueueItemDTO harvestQueueItemDTO) throws HarvestException{
		
		try {
			DAOFactory.getDAOFactory().getHarvestQueueDAO().deleteQueueItem(harvestQueueItemDTO);
		}
		catch (DAOException e) {
			throw new HarvestException(e.toString(), e);
		}
	}
}
