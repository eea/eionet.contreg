/*
* The contents of this file are subject to the Mozilla Public
* 
* License Version 1.1 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of
* the License at http://www.mozilla.org/MPL/
* 
* Software distributed under the License is distributed on an "AS
* IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
* implied. See the License for the specific language governing
* rights and limitations under the License.
* 
* The Original Code is Content Registry 2.0.
* 
* The Initial Owner of the Original Code is European Environment
* Agency. Portions created by Tieto Eesti are Copyright
* (C) European Environment Agency. All Rights Reserved.
* 
* Contributor(s):
* Jaanus Heinlaid, Tieto Eesti*/
package eionet.cr.web.util;

import java.text.ParseException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.StatefulJob;

import eionet.cr.common.JobScheduler;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.HelperDao;
import eionet.cr.dao.mysql.MySQLDAOFactory;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class RecentResourcesCacheUpdater implements StatefulJob, ServletContextListener{

	/** */
	private static final int UPDATE_INTERVAL_MINUTES = 5;
	
	/** */
	private static Log logger = LogFactory.getLog(RecentResourcesCacheUpdater.class);
			
	/*
	 * (non-Javadoc)
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		try {
			HelperDao dao = MySQLDAOFactory.get().getDao(HelperDao.class);
			RecentResourcesCache.getInstance().updateCache(dao.getRecentlyDiscoveredFiles(10));
			logger.debug("Recently discovered resources cache updated!");
		}
		catch (DAOException e) {
			logger.error("Error when updating recently discovered files cache: ", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		
		JobDetail jobDetails = new JobDetail(getClass().getSimpleName(), JobScheduler.class.getName(), RecentResourcesCacheUpdater.class);
		try{
			JobScheduler.scheduleIntervalJob(UPDATE_INTERVAL_MINUTES * 60 * 1000, jobDetails);
			logger.debug(getClass().getSimpleName() + " scheduled");
		}
		catch (ParseException e){
			logger.fatal("Error when scheduling " + getClass().getSimpleName(), e);
		}
		catch (SchedulerException e){
			logger.fatal("Error when scheduling " + getClass().getSimpleName(), e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
	}
}
