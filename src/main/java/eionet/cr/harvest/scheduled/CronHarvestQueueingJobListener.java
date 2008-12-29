package eionet.cr.harvest.scheduled;

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
public class CronHarvestQueueingJobListener implements JobListener{

	/** */
	private static Log logger = LogFactory.getLog(CronHarvestQueueingJobListener.class);
	
	/*
	 * (non-Javadoc)
	 * @see org.quartz.JobListener#getName()
	 */
	public String getName() {
		return this.getClass().getSimpleName();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.quartz.JobListener#jobExecutionVetoed(org.quartz.JobExecutionContext)
	 */
	public void jobExecutionVetoed(JobExecutionContext context) {
		logger.info("CronHarvestQueueingJob execution was vetoed for cron expression [" +
				context.getJobDetail().getJobDataMap().getString(CronHarvestQueueingJob.CRON_ATTR) + "]");
	}

	/*
	 * (non-Javadoc)
	 * @see org.quartz.JobListener#jobToBeExecuted(org.quartz.JobExecutionContext)
	 */
	public void jobToBeExecuted(JobExecutionContext context) {
//		logger.debug("Going to execute CronHarvestQueueingJob for cron expression [" +
//				context.getJobDetail().getJobDataMap().getString(CronHarvestQueueingJob.CRON_ATTR) + "]");
	}

	/*
	 * (non-Javadoc)
	 * @see org.quartz.JobListener#jobWasExecuted(org.quartz.JobExecutionContext, org.quartz.JobExecutionException)
	 */
	public void jobWasExecuted(JobExecutionContext context, JobExecutionException exception) {
		
		if (exception!=null){
			logger.error("There was a CronHarvestQueueingJob execution exception for cron expression [" +
					context.getJobDetail().getJobDataMap().getString(CronHarvestQueueingJob.CRON_ATTR) + "]: " + exception.toString(), exception);
			return;
		}
		
//		logger.debug("CronHarvestQueueingJob was executed for cron expression [" +
//				context.getJobDetail().getJobDataMap().getString(CronHarvestQueueingJob.CRON_ATTR) + "]");
	}
}
