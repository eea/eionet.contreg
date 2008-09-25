package eionet.cr.harvest.scheduled;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import eionet.cr.common.CRException;
import eionet.cr.common.JobScheduler;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dto.HarvestQueueItemDTO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.harvest.Harvest;
import eionet.cr.harvest.HarvestDAOWriter;
import eionet.cr.harvest.HarvestException;
import eionet.cr.harvest.PullHarvest;
import eionet.cr.harvest.PushHarvest;
import eionet.cr.util.URLUtil;
import eionet.cr.util.Util;
import eionet.cr.util.sql.ConnectionUtil;
import eionet.cr.web.security.CRUser;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class HarvestingJob implements Job, ServletContextListener{
	
	/** */
	private static Log logger = LogFactory.getLog(HarvestingJob.class);
	
	/** */
	private static String urlHarvestingNow = null;

	/*
	 * (non-Javadoc)
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	public void execute(JobExecutionContext jobExecContext) throws JobExecutionException {

		try{
			HarvestQueueItemDTO queueItem = HarvestQueue.poll(HarvestQueue.PRIORITY_URGENT);
			if (queueItem==null || queueItem.getUrl()==null || queueItem.getUrl().length()==0)
				queueItem = HarvestQueue.poll(HarvestQueue.PRIORITY_NORMAL);
			if (queueItem==null || queueItem.getUrl()==null || queueItem.getUrl().length()==0)
				return;
			
			Harvest harvest = null;
			String pushedContent = queueItem.getPushedContent();
			if (Util.isNullOrEmpty(pushedContent)){
				
				logger.info("Going to harvest url: " + queueItem.getUrl());
				
				HarvestSourceDTO harvestSource = DAOFactory.getDAOFactory().getHarvestSourceDAO().getHarvestSourceByUrl(queueItem.getUrl());
				harvest = new PullHarvest(harvestSource.getUrl(), null); // TODO - use proper lastHarvestTimestamp instead of null
				harvest.setDaoWriter(new HarvestDAOWriter(
						harvestSource.getSourceId().intValue(), Harvest.TYPE_PULL, CRUser.application.getUserName()));
			}
			else{
				logger.info("Going to push content from url: " + queueItem.getUrl());
				
				HarvestSourceDTO sourceDTO = new HarvestSourceDTO();
				sourceDTO.setUrl(queueItem.getUrl());
				sourceDTO.setName(queueItem.getUrl());
				sourceDTO.setType("data");
				Integer sourceId = DAOFactory.getDAOFactory().getHarvestSourceDAO().addSourceIgnoreDuplicate(sourceDTO, CRUser.application.getUserName());
				
				harvest = new PushHarvest(pushedContent, queueItem.getUrl());
				if (sourceId!=null && sourceId.intValue()>0){
					harvest.setDaoWriter(new HarvestDAOWriter(sourceId.intValue(), Harvest.TYPE_PUSH, CRUser.application.getUserName()));
				}
			}
		
			setUrlHarvestingNow(queueItem.getUrl());
			harvest.execute();
		}
		catch (Throwable t){
			throw new JobExecutionException(t.toString(), t);
		}
		finally{
			setUrlHarvestingNow(null);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		
		try{
			JobDetail jobDetails = new JobDetail(HarvestingJob.class.getSimpleName(), JobScheduler.class.getName(), HarvestingJob.class);
			
			HarvestingJobListener listener = new HarvestingJobListener();
			jobDetails.addJobListener(listener.getName());
			JobScheduler.registerJobListener(listener);
			
			JobScheduler.scheduleCronJob(GeneralConfig.getRequiredProperty(GeneralConfig.HARVESTER_JOB_CRON_EXPRESSION), jobDetails);
		}
		catch (SchedulerException e){
			logger.fatal("Error when scheduling " + HarvestingJob.class.getSimpleName() + ": " + e.toString(), e);
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
	 * @param args
	 */
	public static void main(String[] args){
		
		ConnectionUtil.setReturnSimpleConnection(true);

		PullHarvestQueueingJob queueingJob = new PullHarvestQueueingJob();
		queueingJob.contextInitialized(null);

		HarvestingJob harvestingJob = new HarvestingJob();
		harvestingJob.contextInitialized(null);
	}

	/**
	 * @return the urlHarvestingNow
	 */
	public static synchronized String getUrlHarvestingNow() {
		return urlHarvestingNow;
	}

	/**
	 * @param urlHarvestingNow the urlHarvestingNow to set
	 */
	public static synchronized void setUrlHarvestingNow(String urlHarvestingNow) {
		HarvestingJob.urlHarvestingNow = urlHarvestingNow;
	}
}
