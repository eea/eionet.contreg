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
public class PullHarvestQueueingJobListener implements JobListener{

	/** */
	private static Log logger = LogFactory.getLog(PullHarvestQueueingJobListener.class);
	
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
		logger.info("Job execution was vetoed for cron expression [" +
				context.getJobDetail().getJobDataMap().getString(PullHarvestQueueingJob.CRON_ATTR) + "]");
	}

	/*
	 * (non-Javadoc)
	 * @see org.quartz.JobListener#jobToBeExecuted(org.quartz.JobExecutionContext)
	 */
	public void jobToBeExecuted(JobExecutionContext context) {
		logger.info("Going to execute job for cron expression [" +
				context.getJobDetail().getJobDataMap().getString(PullHarvestQueueingJob.CRON_ATTR) + "]");
	}

	/*
	 * (non-Javadoc)
	 * @see org.quartz.JobListener#jobWasExecuted(org.quartz.JobExecutionContext, org.quartz.JobExecutionException)
	 */
	public void jobWasExecuted(JobExecutionContext context, JobExecutionException exception) {
		
		if (exception!=null){
			logger.error("There was a job execution exception for cron expression [" +
					context.getJobDetail().getJobDataMap().getString(PullHarvestQueueingJob.CRON_ATTR) + "]: " + exception.toString(), exception);
			return;
		}
		
		logger.info("Job was executed for cron expression [" +
				context.getJobDetail().getJobDataMap().getString(PullHarvestQueueingJob.CRON_ATTR) + "]");
	}
}
