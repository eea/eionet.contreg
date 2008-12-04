package eionet.cr.harvest.scheduled;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;

import eionet.cr.common.CRException;
import eionet.cr.common.JobScheduler;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.util.Util;
import eionet.cr.util.sql.ConnectionUtil;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class CronHarvestQueueingJob implements Job, ServletContextListener{
	
	/** */
	private static Log logger = LogFactory.getLog(CronHarvestQueueingJob.class);
	
	/** */
	protected static final String CRON_ATTR = "cron";
	
	/** */
	private static CronHarvestQueueingJobListener listener;
	
	/** */
	private static Set<String> scheduledCrons = null;

	/*
	 * (non-Javadoc)
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	public void execute(JobExecutionContext jobExecContext) throws JobExecutionException {
		
		String cronExpression = jobExecContext.getJobDetail().getJobDataMap().getString(CRON_ATTR);
		
		try{
			List<HarvestSourceDTO> harvestSources = DAOFactory.getDAOFactory().getHarvestSourceDAO().getHarvestSourcesBySchedule(cronExpression);
			if (harvestSources!=null && !harvestSources.isEmpty()){
				for (int i=0; i<harvestSources.size(); i++){
					String sourceUrl = harvestSources.get(i).getUrl();
					HarvestQueue.addPullHarvest(sourceUrl, HarvestQueue.PRIORITY_NORMAL);
				}
			}
		}
		catch (CRException e){
			throw new JobExecutionException(e.toString(), e);
		}
	}

	/**
	 * 
	 * @param cronExpression
	 * @throws SchedulerException 
	 */
	public static void scheduleCronHarvest(String cronExpression) throws SchedulerException{
		
		if (!Util.isValidQuartzCronExpression(cronExpression))
			return;
		else if (getScheduledCrons().contains(cronExpression))
			return;
		
		JobDetail jobDetails = new JobDetail(CronHarvestQueueingJob.class.getSimpleName() + " for cron expression [" + cronExpression + "]", JobScheduler.class.getName(), CronHarvestQueueingJob.class);
		jobDetails.getJobDataMap().put(CRON_ATTR, cronExpression);
		
		addListener(jobDetails);
		JobScheduler.scheduleCronJob(cronExpression, jobDetails);
		getScheduledCrons().add(cronExpression);
	}

	/**
	 * 
	 * @return
	 * @throws SchedulerException 
	 */
	private synchronized static void addListener(JobDetail jobDetails) throws SchedulerException{
		
		if (listener==null){
			listener = new CronHarvestQueueingJobListener();
			JobScheduler.registerJobListener(listener);
		}
		jobDetails.addJobListener(listener.getName());
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		
		String cronExpression = null;
		try{
			List<String> schedules = DAOFactory.getDAOFactory().getHarvestSourceDAO().getDistinctSchedules();
			if (schedules!=null && !schedules.isEmpty()){
				for (int i=0; i<schedules.size(); i++){
					cronExpression = schedules.get(i);
					scheduleCronHarvest(cronExpression);
				}
			}
		}
		catch (DAOException e){
			logger.fatal("Error when getting distinct schedules from database: " + e.toString(), e);
		}
		catch (SchedulerException e){
			logger.fatal("Error when scheduling cron [" + cronExpression + "]: " + e.toString(), e);
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
	 */
	private static synchronized Set<String> getScheduledCrons(){
		
		if (scheduledCrons==null)
			scheduledCrons = new HashSet<String>();
		return scheduledCrons;
	}
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args){
		ConnectionUtil.setReturnSimpleConnection(true);
		CronHarvestQueueingJob job = new CronHarvestQueueingJob();
		job.contextInitialized(null);
	}
}
