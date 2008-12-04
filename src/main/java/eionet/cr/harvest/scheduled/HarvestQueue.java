package eionet.cr.harvest.scheduled;

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
	
//	public static void main(String[] args){
//		
//		try{
//			ConnectionUtil.setReturnSimpleConnection(true);
//			
//			HarvestQueue.addPullHarvest("http://pull1.org", HarvestQueue.PRIORITY_NORMAL);
//			Thread.sleep(1000);
//			HarvestQueue.addPullHarvest("http://pull1.org", HarvestQueue.PRIORITY_URGENT);
//			Thread.sleep(1000);
//			HarvestQueue.addPushHarvest("push content 1", "http://push1.org", HarvestQueue.PRIORITY_NORMAL);
//			Thread.sleep(1000);
//			HarvestQueue.addPushHarvest("push content 1", "http://push1.org", HarvestQueue.PRIORITY_URGENT);
//			Thread.sleep(1000);
//			
//			HarvestQueueItemDTO dto = HarvestQueue.poll(HarvestQueue.PRIORITY_NORMAL);
//			if (dto!=null)
//				System.out.println(dto.getUrl());
//			dto = HarvestQueue.poll(HarvestQueue.PRIORITY_URGENT);
//			if (dto!=null)
//				System.out.println(dto.getUrl());
//		}
//		catch (Throwable t){
//			t.printStackTrace();
//		}
//	}
}
