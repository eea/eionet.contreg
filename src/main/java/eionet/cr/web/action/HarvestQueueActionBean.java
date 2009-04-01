package eionet.cr.web.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.UrgentHarvestQueueItemDTO;
import eionet.cr.harvest.Harvest;
import eionet.cr.harvest.scheduled.HarvestingJob;
import eionet.cr.harvest.scheduled.UrgentHarvestQueue;
import eionet.cr.util.Util;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
@UrlBinding("/harvestQueue.action")
public class HarvestQueueActionBean extends AbstractActionBean{
	
	/** */
	private static final String TYPE_BATCH = "batch";
	private static final String TYPE_URGENT = "urgent";

	/** */
	private static Log logger = LogFactory.getLog(HarvestQueueActionBean.class);
	
	/** */
	private static List<Map<String, String>> queueTypes;
	
	/** */
	private String queueType;
	
	/** */
	private List<UrgentHarvestQueueItemDTO> urgentQueue;
	private List<HarvestSourceDTO> batchQueue;
	
	/**
	 * 
	 */
	public HarvestQueueActionBean(){
		setQueueType(TYPE_URGENT);
	}
	
	/**
	 * 
	 * @return
	 * @throws DAOException 
	 */
	@DefaultHandler
	public Resolution view() throws DAOException{
		
		if (getQueueType().equals(TYPE_BATCH)){
			batchQueue = HarvestingJob.getBatchHarvestingQueue();
		}
		else{
			urgentQueue = DAOFactory.getDAOFactory().getUrgentHarvestQueueDAO().getUrgentHarvestQueue();
		}
		
		return new ForwardResolution("/pages/harvestQueue.jsp");
	}


	/**
	 * @return the queueType
	 */
	public String getQueueType() {
		
		if (queueType==null){
			queueType = TYPE_URGENT;
		}
		return queueType;
	}

	/**
	 * @param queueType the queueType to set
	 */
	public void setQueueType(String queueType) {
		this.queueType = queueType;
	}

	/**
	 * @return the list
	 */
	public List<UrgentHarvestQueueItemDTO> getUrgentQueue() {
		return urgentQueue;
	}

	/**
	 * @return the batchQueue
	 */
	public List<HarvestSourceDTO> getBatchQueue() {
		return batchQueue;
	}

	/**
	 * 
	 * @return
	 */
	public List<Map<String, String>> getQueueTypes(){
		
		if (queueTypes==null){
			
			queueTypes = new ArrayList<Map<String,String>>();
			
			Map<String,String> queueType = new HashMap<String,String>();
			queueType.put("title", "Urgent queue");
			queueType.put("queueType", TYPE_URGENT);
			queueTypes.add(queueType);

			queueType = new HashMap<String,String>();
			queueType.put("title", "Batch queue");
			queueType.put("queueType", TYPE_BATCH);
			queueTypes.add(queueType);
		}
		
		return queueTypes;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isTypeUrgent(){
		return getQueueType().equals(TYPE_URGENT);
	}

	/**
	 * 
	 * @return
	 */
	public boolean isTypeBatch(){
		return getQueueType().equals(TYPE_BATCH);
	}
}
