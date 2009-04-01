package eionet.cr.common;

import java.text.ParseException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.quartz.impl.StdSchedulerFactory;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class JobScheduler implements ServletContextListener{
	
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
	 * @param jobDetails
	 * @throws SchedulerException 
	 * @throws ParseException 
	 */
	public static synchronized void scheduleCronJob(String cronExpression, JobDetail jobDetails) throws SchedulerException, ParseException{
		
		CronTrigger trigger = new CronTrigger(jobDetails.getName(), jobDetails.getGroup());
		trigger.setCronExpression(cronExpression);

		if (quartzScheduler==null)
			init();
		
		quartzScheduler.scheduleJob(jobDetails, trigger);
	}

	/**
	 * 
	 * @param repeatInterval
	 * @param jobDetails
	 * @throws SchedulerException
	 * @throws ParseException
	 */
	public static synchronized void scheduleIntervalJob(long repeatInterval, JobDetail jobDetails) throws SchedulerException, ParseException{
		
		SimpleTrigger trigger = new SimpleTrigger(jobDetails.getName(), jobDetails.getGroup());
		trigger.setRepeatInterval(repeatInterval);
		trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);

		if (quartzScheduler==null)
			init();
		
		quartzScheduler.scheduleJob(jobDetails, trigger);
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

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		
		if (quartzScheduler!=null){
			try{
				quartzScheduler.shutdown(false);
			}
			catch (SchedulerException e){
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent servletContextEvent) {
	}
}
