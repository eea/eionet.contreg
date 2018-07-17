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

import java.util.Date;


import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Quartz job listener to be used for listening to {@link HarvestingJob}.
 *
 * @author Jaanus
 */
public class HarvestingJobListener implements JobListener {

    /** Static logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(HarvestingJobListener.class);

    /** Simple name of this class. Made constant because we need to return it via interface method. */
    public static final String CLASS_NAME = HarvestingJobListener.class.getSimpleName();

    /*
     * (non-Javadoc)
     *
     * @see org.quartz.JobListener#getName()
     */
    @Override
    public String getName() {
        return CLASS_NAME;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.quartz.JobListener#jobExecutionVetoed(org.quartz.JobExecutionContext)
     */
    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        LOGGER.error("Execution vetoed for job " + context.getJobDetail().getName());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.quartz.JobListener#jobToBeExecuted(org.quartz.JobExecutionContext)
     */
    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        // LOGGER.debug("Going to execute job " + context.getJobDetail().getName());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.quartz.JobListener#jobWasExecuted(org.quartz.JobExecutionContext, org.quartz.JobExecutionException)
     */
    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException exception) {

        JobDetail jobDetail = context.getJobDetail();
        jobDetail.getJobDataMap().put(HarvestingJob.JobStateAttrs.LAST_FINISH.toString(), new Date());

        if (exception != null) {
            LOGGER.error("Exception thrown when executing job " + jobDetail.getName() + ": " + exception.toString(), exception);
        }

        // LOGGER.debug("Executed job " + context.getJobDetail().getName());
    }
}
