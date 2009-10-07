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
package eionet.cr.web.util.job;

import java.util.List;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.StatefulJob;

import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.mysql.MySQLDAOFactory;

/**
 * background job to perform garbage collection on the database.
 * Pauses all other jobs
 * 
 * @author Aleksandr Ivanov
 * <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public class GarbageCollectorJob implements StatefulJob {
	
	private static final Logger logger = Logger.getLogger(GarbageCollectorJob.class);
	private static final int NEEDED_TO_REMAIN = 10;

	/** 
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			logger.debug("garbage collector has started");
			Scheduler scheduler = context.getScheduler();
			scheduler.pauseAll();
			try {
				List jobs = null;
				do {
					jobs = scheduler.getCurrentlyExecutingJobs();
					if (jobs != null && jobs.size() > 1) {
						Thread.sleep(1000);
					}
				} while (jobs != null && jobs.size() != 1); 
			} catch (Exception ignored) {
				logger.error("Exception was raised while sheduling garbage collector job", ignored);
				return;
			}
			
			HarvestSourceDAO harvestSourceDAO = MySQLDAOFactory.get().getDao(HarvestSourceDAO.class);
			harvestSourceDAO.deleteOrphanSources();
			harvestSourceDAO.deleteHarvestHistory(NEEDED_TO_REMAIN);
			logger.debug("garbage collector has finished its job");
		} catch (Exception ignored) {
			logger.error("error in garbage collector", ignored);
		} finally {
			try {
				context.getScheduler().resumeAll();
			} catch (Exception fatal) {
				throw new RuntimeException("couldn't resume the scheduler");
			}
		}
	}

}
