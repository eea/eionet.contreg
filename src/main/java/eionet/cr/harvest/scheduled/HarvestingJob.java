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

import eionet.cr.common.JobScheduler;
import eionet.cr.config.GeneralConfig;
import eionet.cr.harvest.HarvestException;
import eionet.cr.util.URLUtil;
import eionet.cr.util.sql.ConnectionUtil;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class HarvestingJob implements Job, ServletContextListener{
	
	/** */
	private static Log logger = LogFactory.getLog(HarvestingJob.class);

	/*
	 * (non-Javadoc)
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	public void execute(JobExecutionContext jobExecContext) throws JobExecutionException {
		
		if (true){
			throw new JobExecutionException("just testing");
		}
		String urlToHarvest = HarvestQueue.getUrgent().remove();
		if (urlToHarvest==null)
			urlToHarvest = HarvestQueue.getNormal().remove();
		if (urlToHarvest==null || !URLUtil.isURL(urlToHarvest))
			return;
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
		HarvestingJob job = new HarvestingJob();
		job.contextInitialized(null);
	}
}
