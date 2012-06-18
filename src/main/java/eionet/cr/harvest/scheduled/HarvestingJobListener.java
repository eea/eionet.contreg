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
 * Jaanus Heinlaid, Tieto Eesti
 */
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
public class HarvestingJobListener implements JobListener {

    public static final String NAME = HarvestingJobListener.class.getClass().getSimpleName();

    /** */
    private static Log logger = LogFactory.getLog(HarvestingJobListener.class);

    /*
     * (non-Javadoc)
     * 
     * @see org.quartz.JobListener#getName()
     */
    public String getName() {
        return NAME;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.quartz.JobListener#jobExecutionVetoed(org.quartz.JobExecutionContext)
     */
    public void jobExecutionVetoed(JobExecutionContext context) {
        logger.error("Execution vetoed for job " + context.getJobDetail().getName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.quartz.JobListener#jobToBeExecuted(org.quartz.JobExecutionContext)
     */
    public void jobToBeExecuted(JobExecutionContext context) {
        // logger.debug("Going to execute job " + context.getJobDetail().getName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.quartz.JobListener#jobWasExecuted(org.quartz.JobExecutionContext, org.quartz.JobExecutionException)
     */
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException exception) {

        if (exception != null) {
            logger.error("Exception thrown when executing job " + context.getJobDetail().getName() + ": " + exception.toString(),
                    exception);
            return;
        }

        // logger.debug("Executed job " + context.getJobDetail().getName());
    }
}
