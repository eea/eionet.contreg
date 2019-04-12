package eionet.cr.util.cleanup;

import eionet.cr.common.JobScheduler;
import eionet.cr.dao.DAOException;
import eionet.cr.harvest.HarvestException;
import eionet.cr.harvest.ResponseCodeUtil;
import eionet.cr.harvest.scheduled.HarvestingJob;
import eionet.cr.util.Pair;
import eionet.cr.util.URLUtil;
import eionet.cr.util.sesame.SesameUtil;
import eionet.cr.util.sql.PairReader;
import eionet.cr.util.sql.SQLUtil;
import eionet.cr.util.sql.SingleObjectReader;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;

public class HarvestSourcesCleanupJob implements StatefulJob, ServletContextListener {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(HarvestSourcesCleanupJob.class);
    private static final long INTERVAL_MINUTES = 30 * 24 * 60;
    private static final String HTTPS = "https://";
    public static final String CLEANUP_USERNAME = "cleanup";

    /**
     *
     */
    private void executeInternal() throws DAOException, HarvestException {

        LOGGER.info("Executing {} ... ", getClass().getSimpleName());
        cleanupHarvestSources();
        LOGGER.info("Exiting ... ");
    }

    /**
     *
     */
    private void cleanupHarvestSources() throws DAOException, HarvestException {

        List<Pair<String, Integer>> sourcePairs = findHarvestSourcesWithLastHarvestCode();
        if (sourcePairs == null || sourcePairs.isEmpty()) {
            return;
        }

        Set<String> sourcesInUrgentHarvestQueue = findSourcesInUrgentHarvestQueue();
        Set<String> redirectedResources = findRedirectedResources();

        Set<String> agnosticSources = new LinkedHashSet<>();
        Set<String> httpsSources = new LinkedHashSet<>();
        Set<String> redirectedSources = new LinkedHashSet<>();

        LOGGER.debug("Processing found sources ...");
        for (Pair<String, Integer> sourcePair : sourcePairs) {

            String url = sourcePair.getLeft();
            Integer httpCode = NumberUtils.toInt(Objects.toString(sourcePair.getRight(), ""), 0);

            // Skip space-containing harvest sources (must be very few by accident), they jeopardizing whole cleanup.
            if (url.contains(" ")) {
                continue;
            }

            if (StringUtils.startsWithIgnoreCase(url, HTTPS) && !sourcesInUrgentHarvestQueue.contains(url)) {
                httpsSources.add(url);
                agnosticSources.add(URLUtil.httpsToHttp(url));
            } else if ((isRedirect(httpCode) || redirectedResources.contains(url)) && !sourcesInUrgentHarvestQueue.contains(url)) {
                redirectedSources.add(url);
                agnosticSources.add(url);
            }
        }

        int total = httpsSources.size() + redirectedSources.size();
        LOGGER.info("Found {} sources applicable for cleanup ({} HTTPS sources and {} redirected non-HTTPS sources)",
                total, httpsSources.size(), redirectedSources.size());
        LOGGER.info("Number of distinct protocol-agnostic sources: {}", agnosticSources.size());

        if (total == 0) {
            return;
        } else {
            LOGGER.info("Scheduling the found applicable sources for cleanup harvest...");
        }

        HashSet<String> urls = new HashSet<>(httpsSources);
        urls.addAll(redirectedSources);
        int urgentHarvests = addUrgentHarvests(urls);
        LOGGER.info("A total of {} sources added to urgent harvest queue for cleanup!", urgentHarvests);
    }

    /**
     *
     * @param urls
     * @throws DAOException
     */
    private int addUrgentHarvests(Collection<String> urls) throws DAOException {

        String sql = "INSERT INTO urgent_harvest_queue (url,\"timestamp\",username) VALUES (?,now(),'" + CLEANUP_USERNAME + "')";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = SesameUtil.getSQLConnection();
            pstmt = conn.prepareStatement(sql);

            int counter = 0;
            int batchSize = 1000;
            for (String url : urls) {
                pstmt.setString(1, url);
                pstmt.addBatch();

                counter++;
                if (counter % batchSize == 0) {
                    pstmt.executeBatch();
                }
            }

            if (counter % batchSize != 0) {
                pstmt.executeBatch();
            }

            return counter;
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(pstmt);
            SQLUtil.close(conn);
        }
    }

    /**
     *
     * @param httpCode
     * @return
     */
    private boolean isRedirect(Integer httpCode) {
        return httpCode != null && ResponseCodeUtil.isRedirect(httpCode);
    }

    /**
     *
     * @return
     * @throws DAOException
     */
    private List<Pair<String, Integer>> findHarvestSourcesWithLastHarvestCode() throws DAOException {

        LOGGER.debug("Querying harvest sources ...");

        String sql = "SELECT hs.url AS LCOL, h.http_code AS RCOL FROM harvest_source hs " +
                "LEFT OUTER JOIN harvest h ON hs.last_harvest_id = h.harvest_id ORDER BY hs.url";

        PairReader<String,Integer> reader = new PairReader<>();

        Connection conn = null;
        try {
            conn = SesameUtil.getSQLConnection();
            SQLUtil.executeQuery(sql, reader, conn);
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
        }

        List<Pair<String, Integer>> resultList = reader.getResultList();
        LOGGER.info("Found a total of {} harvest sources", resultList.size());
        return resultList;
    }

    /**
     *
     * @return
     * @throws DAOException
     */
    private Set<String> findSourcesInUrgentHarvestQueue() throws DAOException {

        LOGGER.debug("Querying urgent harvest queue ...");

        String sql = "SELECT DISTINCT url FROM urgent_harvest_queue";
        SingleObjectReader<String> reader = new SingleObjectReader<>();

        Connection conn = null;
        try {
            conn = SesameUtil.getSQLConnection();
            SQLUtil.executeQuery(sql, reader, conn);
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
        }

        List<String> resultList = reader.getResultList();
        LOGGER.info("Found a total of {} sources in urgent harvest queue", resultList.size());
        return new HashSet<>(resultList);
    }

    /**
     *
     * @return
     * @throws DAOException
     */
    private Set<String> findRedirectedResources() throws DAOException {

//        LOGGER.debug("Finding redirecting resources from triple store ...");
//
//        SingleObjectReader<String> reader = new SingleObjectReader<>();
//
//        String sparql = "select distinct ?s1 where {\n" +
//                "  ?s1 <http://cr.eionet.europa.eu/ontologies/contreg.rdf#redirectedTo> ?s2\n" +
//                "}";
//
        SingleObjectReader<String> reader = new SingleObjectReader<>();
//
//        RepositoryConnection conn = null;
//        try {
//            conn = SesameUtil.getRepositoryConnection();
//            SesameUtil.executeQuery(sparql, reader, conn);
//        } catch (Exception e) {
//            throw new DAOException(e.toString(), e);
//        } finally {
//            SesameUtil.close(conn);
//        }
//
//        LOGGER.debug("Found a total of {} redirecting resources in triple store", resultList.size());
        List<String> resultList = reader.getResultList();
        return new HashSet<>(resultList);
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            executeInternal();
        } catch (Exception e) {
            throw new JobExecutionException(getClass().getSimpleName() + " execution threw exception:", e);
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        try {
            JobDetail jobDetails = new JobDetail(getClass().getSimpleName(), JobScheduler.class.getName(), HarvestSourcesCleanupJob.class);

            RedirectionsCleanupJobListener listener = new RedirectionsCleanupJobListener();
            jobDetails.addJobListener(listener.getName());
            JobScheduler.registerJobListener(listener);

            JobScheduler.scheduleIntervalJob(INTERVAL_MINUTES * 60L * 1000L, jobDetails);
            LOGGER.debug(String.format("%s scheduled with interval %d min", getClass().getSimpleName(), INTERVAL_MINUTES));
        } catch (Exception e) {
            LOGGER.error("Error when scheduling " + getClass().getSimpleName() + " with interval minutes " + INTERVAL_MINUTES, e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }

    /**
     *
     */
    public static class RedirectionsCleanupJobListener implements JobListener {

        /** */
        private static final Logger LOGGER = LoggerFactory.getLogger(RedirectionsCleanupJobListener.class);

        @Override
        public String getName() {
            return getClass().getSimpleName();
        }

        @Override
        public void jobToBeExecuted(JobExecutionContext jobExecutionContext) {
            LOGGER.trace("Going to execute job " + jobExecutionContext.getJobDetail().getName());
        }

        @Override
        public void jobExecutionVetoed(JobExecutionContext jobExecutionContext) {
            LOGGER.error("Execution vetoed for job " + jobExecutionContext.getJobDetail().getName());
        }

        @Override
        public void jobWasExecuted(JobExecutionContext jobExecutionContext, JobExecutionException e) {

            JobDetail jobDetail = jobExecutionContext.getJobDetail();
            jobDetail.getJobDataMap().put(HarvestingJob.JobStateAttrs.LAST_FINISH.toString(), new Date());

            if (e != null) {
                LOGGER.error("Exception thrown when executing job " + jobDetail.getName() + ": " + e, e);
            }
        }
    }
}
