package eionet.cr.harvest.scheduling;

import java.text.ParseException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.harvest.HarvestException;
import eionet.cr.util.sql.ConnectionUtil;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class HarvestScheduler implements Job{
	
	/** */
	private static Log logger = LogFactory.getLog(HarvestScheduler.class);
	
	/** */
	protected static final String CRON_ATTR = "cron";

	/** */
	private static Scheduler scheduler = null;

	/*
	 * (non-Javadoc)
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		String cronExpression = context.getJobDetail().getJobDataMap().getString(CRON_ATTR);

		try{
			List<HarvestSourceDTO> harvestSources = DAOFactory.getDAOFactory().getHarvestSourceDAO().getHarvestSourcesBySchedule(cronExpression);
			if (harvestSources!=null && !harvestSources.isEmpty()){
				for (int i=0; i<harvestSources.size(); i++){
					String sourceUrl = harvestSources.get(i).getUrl();
					HarvestQueue.getNormal().add(sourceUrl);
				}
			}
		}
		catch (DAOException e){
			throw new JobExecutionException(e.toString(), e);
		}
	}

	/**
	 * 
	 * @return
	 * @throws SchedulerException 
	 */
	private static void start() throws SchedulerException{
		
		SchedulerFactory schedFact = new StdSchedulerFactory();
		scheduler = schedFact.getScheduler();
		scheduler.start();
	}
	
	/**
	 * 
	 * @param sourceUrl
	 * @throws HarvestException 
	 */
	private static synchronized void schedule(String cronExpression) throws HarvestException{
		
		try{
			JobDetail jobDetails = new JobDetail("HarvestScheduler", "CR Schedulers", HarvestScheduler.class);
			jobDetails.getJobDataMap().put(CRON_ATTR, cronExpression);
			CronTrigger trigger = new CronTrigger("HarvestScheduler", "CR Schedulers");
			trigger.setCronExpression(cronExpression);
	
			if (scheduler==null)
				start();
			scheduler.scheduleJob(jobDetails, trigger);
		}
		catch (ParseException e){
			logger.error("Error parsing cron expression (" + cronExpression + "): " + e.toString(), e);
		}
		catch (SchedulerException e){
			throw new HarvestException(e.toString(), e);
		}
	}
	
	/**
	 * @throws HarvestException 
	 * 
	 */
	public static synchronized void startup() throws HarvestException{
		
		try{
			List<String> schedules = DAOFactory.getDAOFactory().getHarvestSourceDAO().getDistinctSchedules();
			if (schedules!=null && !schedules.isEmpty()){
				for (int i=0; i<schedules.size(); i++){
					schedule(schedules.get(i));
				}
			}
		}
		catch (DAOException e){
			throw new HarvestException(e.toString(), e);
		}
	}
}
