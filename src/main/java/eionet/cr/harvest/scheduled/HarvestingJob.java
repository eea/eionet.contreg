package eionet.cr.harvest.scheduled;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.StatefulJob;

import eionet.cr.common.JobScheduler;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dto.UrgentHarvestQueueItemDTO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.harvest.Harvest;
import eionet.cr.harvest.HarvestDAOWriter;
import eionet.cr.harvest.HarvestException;
import eionet.cr.harvest.PullHarvest;
import eionet.cr.harvest.PushHarvest;
import eionet.cr.harvest.RDFHandler;
import eionet.cr.util.Util;
import eionet.cr.util.sql.ConnectionUtil;
import eionet.cr.web.security.CRUser;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class HarvestingJob implements StatefulJob, ServletContextListener{
	
	/** */
	private static Log logger = LogFactory.getLog(HarvestingJob.class);
	
	/** */
	private static Harvest currentHarvest = null;
	private static List<HarvestSourceDTO> batchHarvestingQueue; 
	
	/** */
	private List<HourSpan> batchHarvestingHours;
	private Integer intervalSeconds;
	private Integer dailyActiveMinutes;
	
	/*
	 * (non-Javadoc)
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	public void execute(JobExecutionContext jobExecContext) throws JobExecutionException {
		
		try{
			RDFHandler.rollbackUnfinishedHarvests();
			harvestUrgentQueue();

			if (!isBatchHarvestingEnabled() || !isBatchHarvestingHour())
				return;

			updateBatchHarvestingQueue();
			if (batchHarvestingQueue!=null && !batchHarvestingQueue.isEmpty()){
				for (Iterator<HarvestSourceDTO> iter=batchHarvestingQueue.iterator(); iter.hasNext(); harvestUrgentQueue()){
					pullHarvest(iter.next());
				}
			}
		}
		catch (Exception e){
			throw new JobExecutionException(e.toString(), e);
		}
		finally{
			setCurrentHarvest(null);
			resetBatchHarvestingQueue();
		}
	}

	/**
	 * 
	 * @return
	 * @throws DAOException 
	 */
	private void updateBatchHarvestingQueue() throws DAOException{
		
		if (isBatchHarvestingEnabled()){
			
			int numOfSegments = Math.round((float)getDailyActiveMinutes() / getIntervalMinutes());
			batchHarvestingQueue = DAOFactory.getDAOFactory().getHarvestSourceDAO().getNextScheduledSources(numOfSegments);
			
			logger.debug(batchHarvestingQueue.size() + " sources added to batch harvesting queue (numOfSegments=" + numOfSegments + ")");
		}
	}
	
	/**
	 * 
	 */
	private void resetBatchHarvestingQueue(){
		batchHarvestingQueue = null;
	}
	
	/**
	 * 
	 * @return
	 */
	public static List<HarvestSourceDTO> getBatchHarvestingQueue(){
		return HarvestingJob.batchHarvestingQueue;
	}

	/**
	 * 
	 */
	private void harvestUrgentQueue(){
		
		try{
			UrgentHarvestQueueItemDTO queueItem = null;
			for (queueItem = UrgentHarvestQueue.poll(); queueItem!=null; queueItem = UrgentHarvestQueue.poll()){
				
				String url = queueItem.getUrl();
				if (!StringUtils.isBlank(url)){
					
					if (queueItem.isPushHarvest())
						pushHarvest(url, queueItem.getPushedContent());
					else
						pullHarvest(DAOFactory.getDAOFactory().getHarvestSourceDAO().getHarvestSourceByUrl(url));
				}
			}
		}
		catch (DAOException e){
			logger.error(e.toString(), e);
		}
	}

	/**
	 * 
	 * @param url
	 * @param pushedContent
	 */
	private void pushHarvest(String url, String pushedContent){
		
		HarvestSourceDTO sourceDTO = new HarvestSourceDTO();
		sourceDTO.setUrl(url);
		sourceDTO.setName(url);
		sourceDTO.setType("data");
		
		try{
			Integer sourceId = DAOFactory.getDAOFactory().getHarvestSourceDAO().addSourceIgnoreDuplicate(
																sourceDTO, CRUser.application.getUserName());
			Harvest harvest = new PushHarvest(pushedContent, url);
			if (sourceId!=null && sourceId.intValue()>0){
				harvest.setDaoWriter(new HarvestDAOWriter(sourceId.intValue(), Harvest.TYPE_PUSH, CRUser.application.getUserName()));
			}
			executeHarvest(harvest);
		}
		catch (DAOException e){
			logger.error(e.toString(), e);
		}
	}

	/**
	 * 
	 * @param harvestSource
	 */
	private void pullHarvest(HarvestSourceDTO harvestSource){
		
		if (harvestSource!=null){
			Harvest harvest = new PullHarvest(harvestSource.getUrl(), harvestSource.getLastHarvest());
			harvest.setDaoWriter(new HarvestDAOWriter(
					harvestSource.getSourceId().intValue(), Harvest.TYPE_PULL, CRUser.application.getUserName()));
			executeHarvest(harvest);
		}
	}
	
	/**
	 * 
	 * @param harvest
	 */
	private void executeHarvest(Harvest harvest){
		
		if (harvest!=null){
			setCurrentHarvest(harvest);
			try {
				harvest.execute();
			}
			catch (HarvestException e){
				// exception already logged
			}
			finally{
				setCurrentHarvest(null);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		
		try{
			JobDetail jobDetails = new JobDetail(getClass().getSimpleName(), JobScheduler.class.getName(), HarvestingJob.class);
			
			HarvestingJobListener listener = new HarvestingJobListener();
			jobDetails.addJobListener(listener.getName());
			JobScheduler.registerJobListener(listener);
			
			JobScheduler.scheduleIntervalJob((long)getIntervalSeconds().intValue()*(long)1000, jobDetails);
			
			logger.debug(getClass().getSimpleName() + " scheduled with interval seconds " + getIntervalSeconds()
					+ ", batch harvesting hours = " + getBatchHarvestingHours());
		}
		catch (Exception e){
			logger.fatal("Error when scheduling " + getClass().getSimpleName() + " with interval seconds " + getIntervalSeconds(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
	}
	
	/**
	 * 
	 * @return
	 */
	public static synchronized Harvest getCurrentHarvest() {
		return currentHarvest;
	}

	/**
	 * 
	 * @param item
	 */
	public static synchronized void setCurrentHarvest(Harvest harvest) {
		currentHarvest = harvest;
	}
	
	/**
	 * @return the activeHours
	 */
	public List<HourSpan> getBatchHarvestingHours() {
		
		if (batchHarvestingHours==null){
			
			batchHarvestingHours = new ArrayList<HourSpan>();
			String hoursString = GeneralConfig.getProperty(GeneralConfig.HARVESTER_BATCH_HARVESTING_HOURS);
			if (!StringUtils.isBlank(hoursString)){
				
				String[] spans = hoursString.trim().split(",");
				for (int i=0; i<spans.length; i++){
					
					String span = spans[i].trim();
					if (span.length()>0){
						
						String[] spanBoundaries = span.split("-");
						
						int from = Integer.parseInt(spanBoundaries[0].trim());
						int to = Integer.parseInt(spanBoundaries[1].trim());
						
						from = Math.max(0, Math.min(23, from));
						to = Math.max(0, Math.min(23, to));
						if (to<from)
							to = from;
						
						batchHarvestingHours.add(new HourSpan(from, to));
					}
				}
			}
		}
		
		return batchHarvestingHours;
	}

	/**
	 * @return the intervalSeconds
	 */
	public Integer getIntervalSeconds() {
		if (intervalSeconds==null){
			intervalSeconds = Integer.parseInt(GeneralConfig.getRequiredProperty(GeneralConfig.HARVESTER_JOB_INTERVAL_SECONDS).trim());
			intervalSeconds = Math.min(3600, intervalSeconds.intValue());
			intervalSeconds = Math.max(5, intervalSeconds.intValue());
		}
		return intervalSeconds;
	}

	/**
	 * 
	 * @return
	 */
	public float getIntervalMinutes(){
		
		return (float)getIntervalSeconds().intValue()/(float)60;
	}

	/**
	 * @return the dailyActiveMinutes
	 */
	public Integer getDailyActiveMinutes() {

		if (this.dailyActiveMinutes==null){

			/* determine the amount of total active minutes in a day */

			int dailyActiveMinutes = 0;
			List<HourSpan> activeHours = getBatchHarvestingHours();
			for (Iterator<HourSpan> iter=activeHours.iterator(); iter.hasNext();){
				dailyActiveMinutes += ((iter.next().length())+1)*(int)60;
			}

			this.dailyActiveMinutes = dailyActiveMinutes>1440 ? new Integer(1440) : new Integer(dailyActiveMinutes);
		}
		
		return this.dailyActiveMinutes;
	}

	/**
	 * 
	 * @return
	 */
	private boolean isBatchHarvestingHour(){
		
		boolean result = false;
		
		int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		List<HourSpan> activeHours = getBatchHarvestingHours();
		for (Iterator<HourSpan> iter=activeHours.iterator(); iter.hasNext();){
			if (iter.next().includes(currentHour)){
				result = true;
				break;
			}
		}
		
		return result;
	}
	
	/**
	 * 
	 * @return
	 */
	private boolean isBatchHarvestingEnabled(){
		return getDailyActiveMinutes().intValue()>0;
	}
	
	/**
	 * 
	 */
	class HourSpan{
		
		/** */
		private int from;
		private int to;

		/**
		 * 
		 * @param from
		 * @param to
		 */
		HourSpan(int from, int to){
			this.from = from;
			this.to = to;
		}
		/**
		 * @return the from
		 */
		public int getFrom() {
			return from;
		}
		/**
		 * @return the to
		 */
		public int getTo() {
			return to;
		}
		/**
		 * 
		 * @return
		 */
		public int length(){
			return to-from; // we assume the creator has made sure that to>=from
		}
		/**
		 * 
		 * @param hour
		 * @return
		 */
		public boolean includes(int hour){
			return hour>=from && hour<=to;
		}
		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString(){
			return new StringBuffer().append(from).append("-").append(to).toString();
		}
	}
}
