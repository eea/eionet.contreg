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
package eionet.cr.web.util.job;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HelperDAO;
import eionet.cr.web.util.ApplicationCache;

/**
 * Job updates cache that contains recent resources.
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 */
public class RecentResourcesCacheUpdater implements StatefulJob {

    /**
     * Count of resources shown in the cache.
     */
    private static final int LASTRESOURCES_COUNT = 10;
    /**
     * Internal logger.
     */
    private static Log logger = LogFactory.getLog(RecentResourcesCacheUpdater.class);

    /**
     * Executes the job.
     *
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     * @param context current context.
     * @throws JobExecutionException if execution fails.
     */
    public void execute(final JobExecutionContext context) throws JobExecutionException {

        try {
            HelperDAO dao = DAOFactory.get().getDao(HelperDAO.class);
            ApplicationCache.updateRecentResourceCache(dao.getLatestFiles(LASTRESOURCES_COUNT));
            logger.debug("Recently discovered resources cache updated!");
        } catch (DAOException e) {
            logger.error("Error when updating recently discovered files cache: ", e);
        }
    }
}
