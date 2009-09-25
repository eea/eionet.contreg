/*
 * The contents of this file are subject to the Mozilla Public
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
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Aleksandr Ivanov, Tieto Eesti
 */
package eionet.cr.web.util;

import java.text.ParseException;
import java.util.Collection;

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
import eionet.cr.common.Predicates;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.HelperDao;
import eionet.cr.dao.mysql.MySQLDAOFactory;
import eionet.cr.search.DataflowPicklistSearch;

/**
 * @author Aleksandr Ivanov
 * <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public class DataflowSearchPicklistCacheUpdater  implements StatefulJob, ServletContextListener{

	/** */
	private static Log logger = LogFactory.getLog(DataflowSearchPicklistCacheUpdater.class);
			
	/*
	 * (non-Javadoc)
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		try {
			DataflowPicklistSearch search = new DataflowPicklistSearch();
			search.execute();
			DataflowSearchPicklistCache cache = DataflowSearchPicklistCache.getInstance();
			Collection<String> localities = MySQLDAOFactory
					.get()
					.getDao(HelperDao.class)
					.getPicklistForPredicate(Predicates.ROD_LOCALITY_PROPERTY);
			
			cache.updateCache(search.getResultMap(), localities);
			logger.debug("Dataflow picklist cache updated");
		}
		catch (Exception e) {
			logger.error("Error when updating dataflow picklist cache cache: ", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		
		JobDetail jobDetails = new JobDetail(getClass().getSimpleName(), JobScheduler.class.getName(), DataflowSearchPicklistCacheUpdater.class);
		try{
			JobScheduler.scheduleIntervalJob(
					Long.parseLong(GeneralConfig.getProperty(
							GeneralConfig.DATAFLOW_PICKLIST_CACHE_UPDATE_INTERVAL)), jobDetails);
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
