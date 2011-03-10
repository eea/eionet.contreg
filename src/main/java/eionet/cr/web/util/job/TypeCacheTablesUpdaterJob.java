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
 * Agency.  Portions created by Tieto Estonia are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Enriko Käsper, Tieto Estonia
 */
package eionet.cr.web.util.job;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HelperDAO;

/**
 * background job to perform database updates on type cache tables
 *
 * @author Enriko Käsper
 * <a href="mailto:enriko.kasper@tieto.com">Enriko Käsper</a>
 */
public class TypeCacheTablesUpdaterJob implements StatefulJob {

    private static final Logger logger = Logger.getLogger(TypeCacheTablesUpdaterJob.class);

    /**
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     * {@inheritDoc}
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            logger.debug("Executing database update on type cache tables");

            DAOFactory.get().getDao(
                    HelperDAO.class).updateTypeDataCache();
            logger.debug("type cache tables update job finished");

        }catch (Exception ignored) {
            logger.error("Exception is thrown while updating type cache tables", ignored);
        } finally {
            try {
                context.getScheduler().resumeAll();
            } catch (Exception fatal) {
                throw new RuntimeException("couldn't resume the scheduler");
            }
        }
    }

}
