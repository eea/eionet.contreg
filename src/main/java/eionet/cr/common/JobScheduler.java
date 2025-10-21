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
package eionet.cr.common;

import eionet.cr.config.GeneralConfig;
import eionet.cr.util.Pair;
import eionet.cr.web.util.job.DeliverySearchPicklistCacheUpdater;
import eionet.cr.web.util.job.TagCloudCacheUpdater;
import eionet.cr.web.util.job.TypeCacheUpdater;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.text.ParseException;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
@SuppressWarnings("unchecked")
public class JobScheduler implements ServletContextListener {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(JobScheduler.class);

    /** */
    private static Scheduler quartzScheduler = null;

    /** */
    private static final Pair<String, JobDetail>[] INTERVAL_JOBS;

    /**
     *
     */
    static {
        INTERVAL_JOBS =
                new Pair[] {
                new Pair(GeneralConfig.DELIVERY_SEARCH_PICKLIST_CACHE_UPDATE_INTERVAL, new JobDetail(
                        DeliverySearchPicklistCacheUpdater.class.getSimpleName(), JobScheduler.class.getName(),
                        DeliverySearchPicklistCacheUpdater.class)),
                        // TODO: This has temporarily been removed because it's being affected by
                        // https://github.com/openlink/virtuoso-opensource/issues/118
//                        new Pair(GeneralConfig.RECENT_DISCOVERED_FILES_CACHE_UPDATE_INTERVAL, new JobDetail(
//                                RecentResourcesCacheUpdater.class.getSimpleName(), JobScheduler.class.getName(),
//                                RecentResourcesCacheUpdater.class)),
                                new Pair(GeneralConfig.TAG_CLOUD_CACHE_UPDATE_INTERVAL, new JobDetail(
                                        TagCloudCacheUpdater.class.getSimpleName(), JobScheduler.class.getName(),
                                        TagCloudCacheUpdater.class)),
                                        new Pair(GeneralConfig.TYPE_CACHE_UPDATE_INTERVAL, new JobDetail(TypeCacheUpdater.class.getSimpleName(),
                                                JobScheduler.class.getName(), TypeCacheUpdater.class))};

    }

    /**
     *
     * @return
     * @throws SchedulerException
     */
    private static void init() throws SchedulerException {

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
    public static synchronized void scheduleCronJob(String cronExpression, JobDetail jobDetails) throws SchedulerException,
    ParseException {

        CronTrigger trigger = new CronTrigger(jobDetails.getName(), jobDetails.getGroup());
        trigger.setCronExpression(cronExpression);

        if (quartzScheduler == null) {
            init();
        }

        quartzScheduler.scheduleJob(jobDetails, trigger);
    }

    /**
     *
     * @param repeatInterval
     * @param jobDetails
     * @throws SchedulerException
     * @throws ParseException
     */
    public static synchronized void scheduleIntervalJob(long repeatInterval, JobDetail jobDetails) throws SchedulerException,
    ParseException {

        SimpleTrigger trigger = new SimpleTrigger(jobDetails.getName(), jobDetails.getGroup());
        trigger.setRepeatInterval(repeatInterval);
        trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);

        if (quartzScheduler == null) {
            init();
        }

        quartzScheduler.scheduleJob(jobDetails, trigger);
    }

    /**
     *
     * @param jobListener
     * @throws SchedulerException
     */
    public static synchronized void registerJobListener(JobListener jobListener) throws SchedulerException {

        if (jobListener == null) {
            return;
        }

        if (quartzScheduler == null) {
            init();
        }

        quartzScheduler.addJobListener(jobListener);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

        if (quartzScheduler != null) {
            try {
                quartzScheduler.shutdown(false);
            } catch (SchedulerException e) {
                // Ignore deliberately.
            }
        }
    }

    /**
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent) {@inheritDoc}
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {

        // schedule interval jobs

        for (Pair<String, JobDetail> job : INTERVAL_JOBS) {

            try {
                String intervString = GeneralConfig.getProperty(job.getLeft());

                // if interval specified, then schedule the job, otherwise
                // consider it administrator's will to not schedule this particular job
                if (!StringUtils.isBlank(intervString)) {
                    int intervMillis = GeneralConfig.getTimePropertyMilliseconds(job.getLeft(), -1);
                    scheduleIntervalJob(intervMillis, job.getRight());
                    LOGGER.debug(job.getRight().getName() + " scheduled, interval=" + intervString);
                }
            } catch (Exception e) {
                LOGGER.error("Error when scheduling " + job.getRight().getName(), e);
            }
        }
    }
}
