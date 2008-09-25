package eionet.cr.common;

import java.text.ParseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import eionet.cr.harvest.HarvestException;
import eionet.cr.harvest.scheduled.CronHarvestQueueingJob;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class JobScheduler {
	
	/** */
	private static Log logger = LogFactory.getLog(JobScheduler.class);

	/** */
	private static Scheduler quartzScheduler = null;
	
	/**
	 * 
	 * @return
	 * @throws SchedulerException 
	 */
	private static void init() throws SchedulerException{
		
		SchedulerFactory schedFact = new StdSchedulerFactory();
		quartzScheduler = schedFact.getScheduler();
		quartzScheduler.start();
	}
	
	/**
	 * 
	 * @param cronExpression
	 * @throws SchedulerException 
	 */
	public static synchronized void scheduleCronJob(String cronExpression, JobDetail jobDetails) throws SchedulerException{
		
		try{
			CronTrigger trigger = new CronTrigger(jobDetails.getName(), jobDetails.getGroup());
			trigger.setCronExpression(cronExpression);
	
			if (quartzScheduler==null)
				init();
			
			quartzScheduler.scheduleJob(jobDetails, trigger);
		}
		catch (ParseException e){
			logger.error("Error parsing cron expression (" + cronExpression + "): " + e.toString(), e);
		}
	}

	/**
	 * 
	 * @param jobListener
	 * @throws SchedulerException 
	 */
	public static synchronized void registerJobListener(JobListener jobListener) throws SchedulerException{
		
		if (jobListener==null)
			return;
		
		if (quartzScheduler==null)
			init();
		
		quartzScheduler.addJobListener(jobListener);
	}
}
