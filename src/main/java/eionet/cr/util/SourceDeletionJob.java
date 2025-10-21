package eionet.cr.util;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang3.StringUtils;

import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.StatefulJob;

import eionet.cr.common.JobScheduler;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.SourceDeletionsDAO;
import eionet.cr.harvest.CurrentHarvests;
import eionet.cr.harvest.Harvest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A background job that deletes harvest sources that have been scheduled for background deletion.
 * Source deletion is delegated to {@link HarvestSourceDAO#removeHarvestSources(java.util.Collection, boolean).} See also
 * {@link SourceDeletionsDAO} for more background information.
 *
 * @author Jaanus
 */
public class SourceDeletionJob implements StatefulJob, ServletContextListener {

    /** Static logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(SourceDeletionJob.class);

    /** This job's running interval in milliseconds. Default is every 20000 ms, i.e. every 20 seconds. */
    public static int INTERVAL_MILLIS;

    /** Hours when the job should be active. */
    public static HashSet<Integer> ACTIVE_HOURS;

    /** Number of sources that the job should delete during one run. Default is 20. */
    private static int BATCH_SIZE;

    /** Simple name of this class. */
    private static final String CLASS_SIMPLE_NAME = SourceDeletionJob.class.getSimpleName();

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextInitialized(ServletContextEvent contextEvent) {

        ACTIVE_HOURS = getConfiguredActiveHours();
        BATCH_SIZE = GeneralConfig.getIntProperty(GeneralConfig.SOURCE_DELETION_JOB_BATCH_SIZE, 20);
        INTERVAL_MILLIS = GeneralConfig.getTimePropertyMilliseconds(GeneralConfig.SOURCE_DELETION_JOB_INTERVAL, 20000);

        JobDetail jobDetails = new JobDetail(CLASS_SIMPLE_NAME, getClass().getName(), getClass());
        Exception exception = null;
        try {
            LOGGER.debug("Scheduling " + CLASS_SIMPLE_NAME + " with interval " + INTERVAL_MILLIS + " ms");
            JobScheduler.scheduleIntervalJob(Long.valueOf(INTERVAL_MILLIS), jobDetails);
        } catch (SchedulerException e) {
            exception = e;
        } catch (ParseException e) {
            exception = e;
        }

        if (exception != null) {
            LOGGER.error("Failed to schedule " + CLASS_SIMPLE_NAME, exception);
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    @Override
    public void execute(JobExecutionContext executionContext) throws JobExecutionException {

        LOGGER.trace(CLASS_SIMPLE_NAME + " executing...");

        Integer currentHour = Integer.valueOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
        if (!ACTIVE_HOURS.contains(currentHour)) {
            LOGGER.trace(currentHour + " is not an active hour, exiting!");
            return;
        }

        long startTime = System.currentTimeMillis();
        try {
            performDeletions();
        } catch (DAOException e) {
            LOGGER.error("Failure when accessing and/or performing the deletion queue", e);
        }

        LOGGER.trace(CLASS_SIMPLE_NAME + " finished! Total time: " + (System.currentTimeMillis() - startTime) + " ms");
    }

    /**
     * Performs the deletion of next {@link #BATCH_SIZE} sources.
     *
     * @throws DAOException If database error occurs.
     */
    private void performDeletions() throws DAOException {

        SourceDeletionsDAO sourceDeletionsDao = DAOFactory.get().getDao(SourceDeletionsDAO.class);
        HarvestSourceDAO harvestSourceDao = DAOFactory.get().getDao(HarvestSourceDAO.class);

        int countFoundUrls = 0;
        for (int i = 1; i <= BATCH_SIZE; i++) {

            String sourceUrl = sourceDeletionsDao.pickForDeletion();
            if (StringUtils.isNotBlank(sourceUrl)) {
                countFoundUrls++;
                Harvest currHarvest = CurrentHarvests.getQueuedHarvest();
                if (currHarvest != null && currHarvest.isBeingHarvested(sourceUrl)) {
                    LOGGER.debug("Postponing the deletion of currently harvested " + sourceUrl);
                    break;
                } else {
                    LOGGER.debug("Deleting " + sourceUrl);
                    harvestSourceDao.removeHarvestSources(Collections.singletonList(sourceUrl), false);
                }
            } else if (sourceUrl != null && sourceUrl.trim().length() == 0) {
                LOGGER.warn("Found a blank URL in deletion queue!");
            }
        }

        if (countFoundUrls == 0) {
            LOGGER.trace("Found no URLs in deletion queue!");
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextDestroyed(ServletContextEvent contextEvent) {
        LOGGER.debug(this.getClass().getName() + " context destroyed");
    }

    /**
     * Get the job's configured active hours. If the configuration value is empty, the job should not run at all.
     * If it's not empty, but cannot be parsed into valid values, then {@link #DEFAULT_ACTIVE_HOURS} is used.
     *
     * @return The job's active hours (valid values from 0-23).
     */
    private static HashSet<Integer> getConfiguredActiveHours() {

        String str = GeneralConfig.getProperty(GeneralConfig.SOURCE_DELETION_JOB_ACTIVE_HOURS);

        // Empty value means no active hours, i.e. the job is disabled. Non-parseable value means use default.

        HashSet<Integer> hours = new HashSet<Integer>();
        if (StringUtils.isNotBlank(str)) {

            String[] split = StringUtils.split(str, ',');
            for (int i = 0; i < split.length; i++) {
                try {
                    int hour = Integer.parseInt(split[i].trim());
                    if (hour >= 0 && hour <= 23) {
                        hours.add(hour);
                    }
                } catch (NumberFormatException e) {
                    // Do nothing.
                }
            }

            // If no legal values found, fall back to default.
            if (hours.isEmpty()) {
                int[] defaults = {19, 20, 21, 22, 23, 0, 1, 2, 3, 4, 5, 6, 7, 15, 16, 17, 18};
                for (int i = 0; i < defaults.length; i++) {
                    hours.add(defaults[i]);
                }
            }
        }

        return hours;
    }
}
