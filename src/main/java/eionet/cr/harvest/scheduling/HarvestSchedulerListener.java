package eionet.cr.harvest.scheduling;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class HarvestSchedulerListener implements JobListener{

	/** */
	private static Log logger = LogFactory.getLog(HarvestSchedulerListener.class);
	
	/** */
	private String name = HarvestSchedulerListener.class.getSimpleName();

	/**
	 * 
	 */
	public HarvestSchedulerListener(){
	}

	/**
	 * 
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * 
	 */
	public void setName(String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * @see org.quartz.JobListener#jobExecutionVetoed(org.quartz.JobExecutionContext)
	 */
	public void jobExecutionVetoed(JobExecutionContext context) {
		logger.error("Job execution was vetoed for cron expression [" +
				context.getJobDetail().getJobDataMap().getString(HarvestScheduler.CRON_ATTR) + "]");
	}

	/*
	 * (non-Javadoc)
	 * @see org.quartz.JobListener#jobToBeExecuted(org.quartz.JobExecutionContext)
	 */
	public void jobToBeExecuted(JobExecutionContext context) {
		logger.info("Going to execute job for cron expression [" +
				context.getJobDetail().getJobDataMap().getString(HarvestScheduler.CRON_ATTR) + "]");
	}

	/*
	 * (non-Javadoc)
	 * @see org.quartz.JobListener#jobWasExecuted(org.quartz.JobExecutionContext, org.quartz.JobExecutionException)
	 */
	public void jobWasExecuted(JobExecutionContext context, JobExecutionException exception) {
		
		if (exception!=null){
			logger.error("There was a jop execution exception for cron expression [" +
					context.getJobDetail().getJobDataMap().getString(HarvestScheduler.CRON_ATTR) + "]: " + exception.toString(), exception);
			return;
		}
		
		logger.info("Job was executed for cron expression [" +
				context.getJobDetail().getJobDataMap().getString(HarvestScheduler.CRON_ATTR) + "]");
		
		logger.info("Current *normal* priority harvest queue:\n" + HarvestQueue.getNormal().toString());
		logger.info("Current *urgent* priority harvest queue:\n" + HarvestQueue.getUrgent().toString());
	}
}
