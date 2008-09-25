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
public class HarvestingJobListener implements JobListener{
	
	/** */
	private static Log logger = LogFactory.getLog(HarvestingJobListener.class);

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
		logger.info("Execution vetoed for job " + context.getJobDetail().getName());
	}

	/*
	 * (non-Javadoc)
	 * @see org.quartz.JobListener#jobToBeExecuted(org.quartz.JobExecutionContext)
	 */
	public void jobToBeExecuted(JobExecutionContext context) {
		logger.info("Going to execute job " + context.getJobDetail().getName());
	}

	/*
	 * (non-Javadoc)
	 * @see org.quartz.JobListener#jobWasExecuted(org.quartz.JobExecutionContext, org.quartz.JobExecutionException)
	 */
	public void jobWasExecuted(JobExecutionContext context, JobExecutionException exception) {
		
		if (exception!=null){
			logger.error("Exception thrown when executing job " + context.getJobDetail().getName() + ": " + exception.toString(), exception);
			return;
		}
		
		logger.info("Executed job " + context.getJobDetail().getName());
	}
}
