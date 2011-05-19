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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SimpleTrigger;
import org.quartz.StatefulJob;

import eionet.cr.common.JobScheduler;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.UrgentHarvestQueueItemDTO;
import eionet.cr.harvest.CurrentHarvests;
import eionet.cr.harvest.Harvest;
import eionet.cr.harvest.HarvestDAOWriter;
import eionet.cr.harvest.HarvestException;
import eionet.cr.harvest.HarvestNotificationSender;
import eionet.cr.harvest.PullHarvest;
import eionet.cr.harvest.PushHarvest;
import eionet.cr.harvest.persist.PersisterFactory;
import eionet.cr.util.EMailSender;
import eionet.cr.util.Util;
import eionet.cr.web.security.CRUser;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class HarvestingJob implements StatefulJob, ServletContextListener {

    /** */
    public static final String NAME = HarvestingJob.class.getClass().getSimpleName();

    /**
     * Number of minutes in an hour.
     */
    private static final int MINUTES = 60;
    /** */
    private static Log logger;

    /** */
    private static List<HarvestSourceDTO> batchHarvestingQueue;
    private static List<HarvestSourceDTO> nextScheduledSources;

    /** */
    private static List<HourSpan> batchHarvestingHours;
    private static Integer intervalSeconds;
    private static Integer harvesterUpperLimit;
    private static Integer dailyActiveMinutes;
    private static boolean firstRunMade = false;

    /** */
    private static SimpleTrigger trigger = null;

    // private static Harvest currentHarvest = null;

    /**
     *
     */
    static {
        logger = LogFactory.getLog(HarvestingJob.class);
        refreshNextScheduledSources();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    public void execute(JobExecutionContext jobExecContext) throws JobExecutionException {

        if (firstRunMade == false) {
            firstRunMade = true;
            logger.debug("First run of " + getClass().getName());
        }

        try {
            PersisterFactory.getPersister().rollbackUnfinishedHarvests();
            deleteSourcesQueuedForRemoval();
            harvestUrgentQueue();

            if (isBatchHarvestingEnabled()) {

                loadBatchHarvestingQueue();
                harvestBatchQueue();
            }
        } catch (Exception e) {
            try {
                EMailSender.sendToSysAdmin(getClass().getName() + " encountered the following error", Util.getStackTrace(e));
            } catch (Exception ee) {
                logger.error("Exception when sending error notification to system administrator", ee);
            }
            throw new JobExecutionException(e.toString(), e);
        } finally {
            refreshNextScheduledSources();
            CurrentHarvests.setQueuedHarvest(null);
            resetBatchHarvestingQueue();
        }
    }

    /**
     *
     * @throws DAOException
     */
    private void harvestBatchQueue() throws DAOException {

        if (isBatchHarvestingEnabled()) {

            if (batchHarvestingQueue != null && !batchHarvestingQueue.isEmpty()) {

                for (Iterator<HarvestSourceDTO> i = batchHarvestingQueue.iterator(); i.hasNext();) {

                    HarvestSourceDTO harvestSource = i.next();
                    // For sources where interval is less than 8 hours, the
                    // batch harvesting hours doesn't apply. They are always
                    // harvested.
                    boolean lessThan8Hours = harvestSource.getIntervalMinutes().intValue() < 480;
                    if (lessThan8Hours || isBatchHarvestingHour()) {
                        i.remove();
                        if (batchHarvestingQueue.isEmpty()) {
                            refreshNextScheduledSources();
                        }

                        pullHarvest(harvestSource, false);

                        if (!isBatchHarvestingEnabled()) {
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * deletes all sources, which are queued for deletion.
     */
    private void deleteSourcesQueuedForRemoval() throws DAOException {
        HarvestSourceDAO sourceDao = DAOFactory.get().getDao(HarvestSourceDAO.class);
        List<String> doomed = sourceDao.getScheduledForDeletion();
        if (doomed != null && !doomed.isEmpty()) {
            for (String url : doomed) {
                if (!CurrentHarvests.contains(url)) {
                    sourceDao.deleteSourceByUrl(url);

                    // Also remove from ruleset
                    boolean isInRuleset = sourceDao.isSourceInInferenceRule(url);
                    if (isInRuleset) {
                        sourceDao.removeSourceFromInferenceRule(url);
                    }
                }
            }
        }
    }

    /**
     *
     * @throws DAOException
     */
    private void loadBatchHarvestingQueue() throws DAOException {

        int numOfSegments = getNumberOfSegments();
        int limit = getSourcesLimitForInterval();
        batchHarvestingQueue = DAOFactory.get().getDao(HarvestSourceDAO.class).getNextScheduledSources(limit);

        logger.debug(batchHarvestingQueue.size() + " sources added to batch harvesting queue (numOfSegments=" + numOfSegments + ")");
    }

    /**
     *
     */
    private static void refreshNextScheduledSources() {

        if (isBatchHarvestingEnabled()) {

            try {
                nextScheduledSources = DAOFactory.get().getDao(HarvestSourceDAO.class)
                        .getNextScheduledSources(getSourcesLimitForInterval());
            } catch (DAOException e) {
                logger.error("Error loading next scheduled sources: " + e.toString(), e);
            }
        }
    }

    /**
     *
     * @return List<HarvestSourceDTO>
     */
    public static List<HarvestSourceDTO> getNextScheduledSources() {

        return nextScheduledSources;
    }

    /**
     *
     */
    private void resetBatchHarvestingQueue() {
        batchHarvestingQueue = null;
    }

    /**
     *
     * @return List<HarvestSourceDTO>
     */
    public static List<HarvestSourceDTO> getBatchHarvestingQueue() {

        return HarvestingJob.batchHarvestingQueue;
    }

    /**
     *
     */
    private void harvestUrgentQueue() {

        try {
            int counter = 0;
            UrgentHarvestQueueItemDTO queueItem = null;
            for (queueItem = UrgentHarvestQueue.poll(); queueItem != null; queueItem = UrgentHarvestQueue.poll()) {

                counter++;

                String url = queueItem.getUrl();
                if (!StringUtils.isBlank(url)) {

                    if (queueItem.isPushHarvest()) {
                        pushHarvest(url, queueItem.getPushedContent());
                    } else {
                        HarvestSourceDTO src = DAOFactory.get().getDao(HarvestSourceDAO.class).getHarvestSourceByUrl(url);
                        if (src != null) {
                            pullHarvest(src, true);
                        } else {
                            logger.trace("Could not find harvest source [" + url + "]");
                        }
                    }
                }
            }
        } catch (DAOException e) {
            logger.error(e.toString(), e);
        }
    }

    /**
     *
     * @param url
     * @param pushedContent
     */
    private void pushHarvest(String url, String pushedContent) {

        // if the source is currently being harvested then return
        if (url != null && CurrentHarvests.contains(url)) {
            logger.debug("The source is currently being harvested, so skipping it");
            return;
        }

        try {
            Integer sourceId = null;

            HarvestSourceDAO harvestSourceDAO = DAOFactory.get().getDao(HarvestSourceDAO.class);
            HarvestSourceDTO harvestSource = harvestSourceDAO.getHarvestSourceByUrl(url);
            if (harvestSource == null) {
                harvestSource = new HarvestSourceDTO();
                harvestSource.setUrl(url);

                sourceId = harvestSourceDAO.addSource(harvestSource);
            } else {
                sourceId = harvestSource.getSourceId();
            }

            Harvest harvest = new PushHarvest(pushedContent, url);
            if (sourceId != null && sourceId.intValue() > 0) {
                harvest.setDaoWriter(new HarvestDAOWriter(sourceId.intValue(), Harvest.TYPE_PUSH, CRUser.APPLICATION.getUserName()));
            }

            harvest.setNotificationSender(new HarvestNotificationSender());
            executeHarvest(harvest);
        } catch (DAOException e) {
            logger.error(e.toString(), e);
        }
    }

    /**
     *
     * @param harvestSource
     * @throws DAOException
     */
    private void pullHarvest(HarvestSourceDTO harvestSource, boolean urgent) throws DAOException {

        if (harvestSource != null) {

            // if the source is currently being harvested then return
            if (CurrentHarvests.contains(harvestSource.getUrl())) {
                logger.debug("The source is currently being harvested, so skipping it");
                return;
            }

            Harvest harvest = PullHarvest.createFullSetup(harvestSource, urgent);
            executeHarvest(harvest);
        }
    }

    /**
     *
     * @param harvest
     */
    private void executeHarvest(Harvest harvest) {

        if (harvest != null) {
            CurrentHarvests.setQueuedHarvest(harvest);
            try {
                harvest.execute();
            } catch (HarvestException e) {
                // exception already logged
            } finally {
                CurrentHarvests.setQueuedHarvest(null);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet .ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        try {
            JobDetail jobDetails = new JobDetail(HarvestingJob.NAME, JobScheduler.class.getName(), HarvestingJob.class);

            HarvestingJobListener listener = new HarvestingJobListener();
            jobDetails.addJobListener(listener.getName());
            JobScheduler.registerJobListener(listener);

            JobScheduler.scheduleIntervalJob((long) getIntervalSeconds().intValue() * (long) 1000, jobDetails);

            logger.debug(getClass().getSimpleName() + " scheduled with interval seconds " + getIntervalSeconds()
                    + ", batch harvesting hours = " + getBatchHarvestingHours());
        } catch (Exception e) {
            logger.fatal("Error when scheduling " + getClass().getSimpleName() + " with interval seconds " + getIntervalSeconds(),
                    e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet. ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }

    // /**
    // *
    // * @return
    // */
    // public static synchronized Harvest getCurrentHarvest() {
    // return currentHarvest;
    // }
    //
    // /**
    // *
    // * @return
    // */
    // public static synchronized String getCurrentHarvestUrl() {
    // return currentHarvest == null ? null :
    // currentHarvest.getSourceUrlString();
    // }
    //
    // /**
    // *
    // * @param item
    // */
    // public static synchronized void setCurrentHarvest(Harvest harvest) {
    // currentHarvest = harvest;
    // }

    /**
     * Read the hours the harvester is allowed to do batch harvesting from the general configuration file. The clock hours (0-23)
     * when batch harvesting should be active are written as comma separated from-to spans (e.g 10-15, 19-23), where in every span
     * both from and to are inclusive and there must be from &lt;= to (so, to say from 18.00 to 9.00 you must write 18-23,0-8).
     * (leave the field completely empty to disable batch harvesting)
     *
     * @return list containing the activeHours
     */
    public static List<HourSpan> getBatchHarvestingHours() {

        if (batchHarvestingHours == null) {

            batchHarvestingHours = new ArrayList<HourSpan>();
            String hoursString = GeneralConfig.getProperty(GeneralConfig.HARVESTER_BATCH_HARVESTING_HOURS);
            if (!StringUtils.isBlank(hoursString)) {

                String[] spans = hoursString.trim().split(",");
                for (int i = 0; i < spans.length; i++) {

                    String span = spans[i].trim();
                    if (span.length() > 0) {

                        String[] spanBoundaries = span.split("-");

                        int from = Integer.parseInt(spanBoundaries[0].trim());
                        int to = Integer.parseInt(spanBoundaries[1].trim());

                        from = Math.max(0, Math.min(23, from));
                        to = Math.max(0, Math.min(23, to));
                        if (to < from)
                            to = from;

                        batchHarvestingHours.add(new HourSpan(from, to));
                    }
                }
            }
        }

        return batchHarvestingHours;
    }

    /**
     * Returns the interval in seconds where the harvester checks for checks for new urgent or scheduled tasks. The interval can't
     * be more than 3600 seconds or less than 5 seconds. The value is retrieved from the general configuration file.
     *
     * @return the interval in seconds
     */
    public static Integer getIntervalSeconds() {

        if (intervalSeconds == null) {

            int seconds = Integer.parseInt(GeneralConfig.getRequiredProperty(GeneralConfig.HARVESTER_JOB_INTERVAL_SECONDS).trim());
            seconds = Math.min(3600, seconds);
            seconds = Math.max(5, seconds);

            intervalSeconds = new Integer(seconds);
        }

        return intervalSeconds;
    }

    /**
     * Returns the upper limit on the number of sources that are harvested in each interval. The value is retrieved from the general
     * configuration file.
     *
     * @return the upper limit
     */
    public static Integer getHarvesterUpperLimit() {

        if (harvesterUpperLimit == null) {
            String upperLimitStr = GeneralConfig.getProperty(GeneralConfig.HARVESTER_SOURCES_UPPER_LIMIT).trim();
            if (upperLimitStr != null && upperLimitStr.length() > 0) {
                harvesterUpperLimit = Integer.parseInt(upperLimitStr);
            } else {
                harvesterUpperLimit = new Integer(0);
            }
        }

        return harvesterUpperLimit;
    }

    /**
     * Returns the interval in minutes where the harvester checks for checks for new urgent or scheduled tasks. Value can be less
     * than 1.0.
     *
     * @return interval in minutes
     */
    public static float getIntervalMinutes() {

        return getIntervalSeconds().floatValue() / (float) MINUTES;
    }

    /**
     * Calculates how many minutes a day the batch harvester is active. If the batch harvesting is from 5-6, then the return value
     * is 120.
     *
     * @return the dailyActiveMinutes
     */
    public static Integer getDailyActiveMinutes() {

        if (dailyActiveMinutes == null) {

            /* determine the amount of total active minutes in a day */

            int minutes = 0;
            List<HourSpan> activeHours = getBatchHarvestingHours();
            for (HourSpan hourSpan : activeHours) {
                minutes += ((hourSpan.length()) + 1) * (int) MINUTES;
            }

            dailyActiveMinutes = minutes > 1440 ? new Integer(1440) : new Integer(minutes);
        }

        return dailyActiveMinutes;
    }

    /**
     * Calculates how many harvesting segments there is in a day. If the harvester is active 120 minutes and the interval is 15
     * seconds (i.e. 1/4 minute, then there are 480 harvesting segments in the day.
     *
     * @return the number of harvesting segments
     */
    private static int getNumberOfSegments() {

        return Math.round(getDailyActiveMinutes().floatValue() / getIntervalMinutes());
    }

    /**
     * Calculates how many sources we need to harvest in this round, but if the amount is over the limit we lower it to the limit.
     * The purpose is to avoid tsunamis of harvesting.
     * <p>
     * Example: If there are 4320 time segments, and there are 216 sources with a score of 1.0 or above, the number of sources to
     * harvest in this round is 216 / 4320 = 0.05. This we then round up to one.
     *
     * @return the limit of sources returned
     */
    private static int getSourcesLimitForInterval() {
        int limit = 0;
        try {
            int numOfSegments = getNumberOfSegments();
            Long numberOfSources = DAOFactory.get().getDao(HarvestSourceDAO.class).getUrgencySourcesCount();
            int upperLimit = getHarvesterUpperLimit();

            // Round up to 1 if there is something at all to harvest
            limit = (int) Math.ceil((double) numberOfSources / (double) numOfSegments);
            if (upperLimit > 0 && limit > upperLimit) {
                limit = upperLimit;
            }
        } catch (DAOException e) {
            logger.error(e.toString(), e);
        }

        return limit;
    }

    /**
     *
     * @return
     */
    private boolean isBatchHarvestingHour() {

        return isBatchHarvestingHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
    }

    /**
     *
     * @param calendar
     * @return
     */
    private static boolean isBatchHarvestingHour(Calendar calendar) {

        boolean result = false;

        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        List<HourSpan> activeHours = getBatchHarvestingHours();
        for (HourSpan hourSpan : activeHours) {
            if (hourSpan.includes(hour)) {
                result = true;
                break;
            }
        }

        return result;
    }

    /**
     * Checks if the argument is an hour where batch harvesting is active.
     *
     * @param hour
     *            values can be from 0-23
     * @return true if the hour is a batch harvesting hour.
     */
    private static boolean isBatchHarvestingHour(int hour) {

        boolean result = false;
        List<HourSpan> activeHours = getBatchHarvestingHours();
        for (HourSpan hourSpan : activeHours) {
            if (hourSpan.includes(hour)) {
                result = true;
                break;
            }
        }

        return result;
    }

    /**
     * Checks if batch harvesting is enabled.
     *
     * @return true if batch harvesting is enabled
     */
    private static boolean isBatchHarvestingEnabled() {
        return getDailyActiveMinutes().intValue() > 0;
    }

    /**
     *
     * @return long
     */
    public static long getNextBatchHarvestTime() {

        long result = 0;
        if (trigger != null) {

            Date nextFireTime = trigger.getNextFireTime();
            if (nextFireTime != null) {

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(nextFireTime);

                int i = 1;
                for (; !isBatchHarvestingHour(calendar) && i <= 24; i++) {
                    calendar.add(Calendar.HOUR_OF_DAY, 1);
                }

                // if i>25 then we've tried 24 hours and none of them was
                // a batch harvesting hour
                if (i <= 24) {
                    calendar.set(Calendar.MINUTE, 0);
                    result = calendar.getTimeInMillis();
                }
            }
        }

        return result;
    }
}
