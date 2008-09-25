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
import eionet.cr.dto.HarvestQueueItemDTO;
import eionet.cr.harvest.scheduled.HarvestQueue;
import eionet.cr.search.SearchException;
import eionet.cr.util.Util;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
@UrlBinding("/harvestQueue.action")
public class HarvestQueueActionBean extends AbstractCRActionBean{
	
	/** */
	private static Log logger = LogFactory.getLog(HarvestQueueActionBean.class);
	
	/** */
	private static List<Map<String, String>> priorities;
	
	/** */
	private String priority;
	
	/** */
	private List<HarvestQueueItemDTO> list;
	
	/**
	 * 
	 */
	public HarvestQueueActionBean(){
		setPriority(HarvestQueue.PRIORITY_NORMAL);
	}
	
	/**
	 * 
	 * @return
	 * @throws DAOException 
	 */
	@DefaultHandler
	public Resolution view() throws DAOException{
		
		if (!Util.isNullOrEmpty(getPriority())){
			if (getPriority().equals(HarvestQueue.PRIORITY_NORMAL))
				list = DAOFactory.getDAOFactory().getHarvestQueueDAO().getNormalHarvestQueue();
			else
				list = DAOFactory.getDAOFactory().getHarvestQueueDAO().getUrgentHarvestQueue();
		}
		
		return new ForwardResolution("/pages/harvestQueue.jsp");
	}


	/**
	 * @return the priority
	 */
	public String getPriority() {
		return priority;
	}

	/**
	 * @param priority the priority to set
	 */
	public void setPriority(String priority) {
		this.priority = priority;
	}

	/**
	 * @return the list
	 */
	public List<HarvestQueueItemDTO> getList() {
		return list;
	}
	
	/**
	 * 
	 * @return
	 */
	public List<Map<String, String>> getPriorities(){
		
		if (priorities==null){
			
			priorities = new ArrayList<Map<String,String>>();
			
			Map<String,String> priorityMap = new HashMap<String,String>();
			priorityMap.put("title", "Normal queue");
			priorityMap.put("priority", HarvestQueue.PRIORITY_NORMAL);
			priorities.add(priorityMap);
			
			priorityMap = new HashMap<String,String>();
			priorityMap.put("title", "Urgent queue");
			priorityMap.put("priority", HarvestQueue.PRIORITY_URGENT);
			priorities.add(priorityMap);
		}
		
		return priorities;
	}
}
