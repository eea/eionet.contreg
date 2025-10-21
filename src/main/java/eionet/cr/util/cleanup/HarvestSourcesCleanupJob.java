package eionet.cr.util.cleanup;

import eionet.cr.common.CRRuntimeException;
import eionet.cr.common.JobScheduler;
import eionet.cr.common.Predicates;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.harvest.HarvestException;
import eionet.cr.harvest.ResponseCodeUtil;
import eionet.cr.harvest.scheduled.HarvestingJob;
import eionet.cr.util.Pair;
import eionet.cr.util.URLUtil;
import eionet.cr.util.sesame.SesameUtil;
import eionet.cr.util.sql.PairReader;
import eionet.cr.util.sql.SQLUtil;
import eionet.cr.util.sql.SingleObjectReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class HarvestSourcesCleanupJob implements StatefulJob, ServletContextListener {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(HarvestSourcesCleanupJob.class);
    private static final long INTERVAL_MINUTES = 30 * 24 * 60;
    private static final String HTTPS = "https://";
    public static final String CLEANUP_USERNAME = "cleanup";
    private static final Set<String> EXCEPT_PREDICATES = new HashSet<>(Arrays.asList(Predicates.CR_REDIRECTED_TO, Predicates.CR_LAST_REFRESHED));
    public static final int DELETE_BATCH_SIZE = 50;
    public static final long DELETE_REST_MILLISECONDS = 10 * 1000;
    public static final long SLEEP_MILLIS = 10 * 1000;
    public static final int REDIRECTED_SOURCES_DELETION_BATCH = 100;

    /**
     *
     */
    private void executeInternal() throws DAOException, HarvestException, SQLException {

        LOGGER.info("Executing {} ... ", getClass().getSimpleName());
        cleanupWithoutReharvesting();
        LOGGER.info("Exiting ... ");
    }

    /**
     *
     * @throws DAOException
     * @throws HarvestException
     */
    private void cleanupWithoutReharvesting() throws DAOException, HarvestException, SQLException {

        // Delete redirected sources.
        deleteRedirectedSources();

        // TODO: Find all non-redirecting HTTPS and rename them to HTTP.
    }

    /**
     *
     * @throws DAOException
     * @throws SQLException
     */
    private void deleteRedirectedSources() throws DAOException, SQLException {

        int i = 0;
        Connection conn = null;
        List<String> sources = null;
        try {
            conn = SesameUtil.getSQLConnection();
            conn.setAutoCommit(true);

            do {
                sources = findTop100RedirectedSources();
                if (sources == null || sources.isEmpty()) {
                    break;
                }

                i++;
                LOGGER.debug("Deleting redirected sources batch {}", i);
                try {
                    deleteSourcesById(conn, sources);
                } catch (Exception e) {
                    LOGGER.error("Failed deleting redirected sources batch " + i, e);
                }

                if (i % REDIRECTED_SOURCES_DELETION_BATCH == 0) {
                    sleep(SLEEP_MILLIS);
                }
            } while (sources != null && !sources.isEmpty());
        } finally {
            SQLUtil.close(conn);
        }
    }

    /**
     *
     * @param sleepMillis
     */
    private void sleep(long sleepMillis) {
        LOGGER.debug("Sleeping for {} ms", sleepMillis);
        try {
            Thread.sleep(sleepMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     */
    private void cleanupWithReharvesting() throws DAOException, HarvestException {

        List<Pair<String, Integer>> sourcePairs = findHarvestSourcesWithLastHarvestCode();
        if (sourcePairs == null || sourcePairs.isEmpty()) {
            return;
        }

        removeUrgentHarvestsOfNonExistingSources();

        Set<String> sourcesInUrgentHarvestQueue = findSourcesInUrgentHarvestQueue();
        Set<String> redirectedResources = findRedirectedResources();

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
            } else if ((isRedirect(httpCode) || redirectedResources.contains(url)) && !sourcesInUrgentHarvestQueue.contains(url)) {
                redirectedSources.add(url);
            }
        }

        int total = httpsSources.size() + redirectedSources.size();
        LOGGER.info("Found {} sources applicable for cleanup ({} HTTPS sources and {} redirected non-HTTPS sources)",
                total, httpsSources.size(), redirectedSources.size());

        if (total == 0) {
            return;
        } else {
            LOGGER.info("Scheduling the found applicable sources for cleanup harvest...");
        }

        HashSet<String> urls = new HashSet<>(httpsSources);
        urls.addAll(redirectedSources);

        Set<String> agnosticsRemovedSources = new LinkedHashSet<>();
        for (String url : urls) {
            agnosticsRemovedSources.add(URLUtil.httpsToHttp(url));
        }
        LOGGER.info("Number of applicable sources after removing protocol-agnostic: " + agnosticsRemovedSources.size());

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

    /**
     *
     * @throws DAOException
     */
    private void removeUrgentHarvestsOfNonExistingSources() throws DAOException {

        LOGGER.info("Removing urgent harvests of sources not existing any more...");

        String sql = String.format(
                "DELETE FROM urgent_harvest_queue WHERE username='%s' AND url NOT IN (SELECT url FROM harvest_source)",
                CLEANUP_USERNAME);

        Connection conn = null;
        try {
            conn = SesameUtil.getSQLConnection();
            SQLUtil.executeUpdate(sql, conn);
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }

    /**
     *
     */
    private void removeRedirectingHarvestSources() throws DAOException {

        String sql = "SELECT DISTINCT " +
                "hs.url AS LCOL, hs.statements AS RCOL FROM harvest_source hs LEFT JOIN harvest h ON hs.last_harvest_id=h.harvest_id " +
                "WHERE h.http_code IN (301,302,303,307,308)";

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
        LOGGER.info("Found {} redirecting harvest sources. Going to delete them ...", resultList.size());

        Set<String> batch = new HashSet<>();
        Set<String> clearGraphBatch = new HashSet<>();
        HarvestSourceDAO harvestSourceDAO = DAOFactory.get().getDao(HarvestSourceDAO.class);

        for (Pair<String, Integer> pair : resultList) {

            String url = pair.getLeft();
            Integer statements = NumberUtils.toInt(Objects.toString(pair.getRight(), ""), 0);

            batch.add(url);
            if (statements != null && statements > 0) {
                clearGraphBatch.add(url);
            }

            if (batch.size() % DELETE_BATCH_SIZE == 0) {

                Set<String> set1 = new HashSet<>(batch);
                Set<String> set2 = new HashSet<>(clearGraphBatch);
                batch.clear();
                clearGraphBatch.clear();

                deleteBatch(set1, set2, harvestSourceDAO);

                LOGGER.debug("Resting before deleting next redirecting sources batch...");
                try {
                    Thread.sleep(DELETE_REST_MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        if (!batch.isEmpty() || !clearGraphBatch.isEmpty()) {
            deleteBatch(new HashSet<>(batch), new HashSet<>(clearGraphBatch), harvestSourceDAO);
        }
    }

    /**
     *
     * @param batch
     * @param clearGraphBatch
     * @param harvestSourceDAO
     * @throws DAOException
     */
    private void deleteBatch(Set<String> batch, Set<String> clearGraphBatch, HarvestSourceDAO harvestSourceDAO) throws DAOException {

        LOGGER.debug("Deleting next {} redirecting sources...", batch.size());
        harvestSourceDAO.removeHarvestSources(batch, EXCEPT_PREDICATES, true, false);
        for (String s : clearGraphBatch) {
            LOGGER.debug("Clearing graph of " + s);
            harvestSourceDAO.clearGraph(s);
        }
    }

    /**
     *
     * @return
     * @throws DAOException
     */
    private List<String> findTop100RedirectedSources() throws DAOException {

        String sql = "select top 100 concat('', harvest_source_id) as srcid from CR.cr3user.harvest_source\n" +
                "where last_harvest_code in (301,302,303,307,308)\n" +
                "order by harvest_source_id asc";

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
        return resultList;
    }

    /**
     *
     * @param sourceIds
     * @throws DAOException
     */
    private void deleteSourcesById(Connection conn, Collection<String> sourceIds) throws DAOException {

        if (sourceIds == null || sourceIds.isEmpty()) {
            return;
        }

        String sql = String.format(
                "DELETE FROM harvest_source WHERE harvest_source_id IN (%s)", StringUtils.join(sourceIds, ','));

        try {
            SQLUtil.executeUpdate(sql, conn);
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        }
    }

    private static void tst() {

        String sqlTemplate = "select '%s' as period, count(*) as cnt from CR.cr3user.harvest_source where last_harvest_code=301 \n" +
                "and time_created >= cast('%s' as date) and time_created < cast('%s' as date)";

        int[][] daySpans = {{1,11}, {11,21}, {21,100}};
        int counter = 0;
        int startYear = 2008;
        int endYear = 2019;
        for (int year = startYear; year <= endYear; year++) {
            for (int month = 1; month <= 12; month++) {
                for (int d = 0; d < daySpans.length; d++) {

                    int[] daySpan = daySpans[d];
                    int startDay = daySpan[0];
                    int endDay = daySpan[1];

                    int endMonth = endDay == 100 ? month + 1 : month;
                    endDay = endDay == 100 ? 1 : endDay;

                    String periodStartIncl = String.format("%d-%02d-%02d", year, month, startDay);
                    String periodEndExcl = endMonth <= 12 ? String.format("%d-%02d-%02d", year, endMonth, endDay) :
                            String.format("%d-%02d-%02d", year + 1, 1, endDay);

                    if (counter++ > 0) {
                        System.out.println("union");
                    }

                    String periodStr = periodStartIncl + " " + periodEndExcl;
                    System.out.println(String.format(sqlTemplate, periodStr, periodStartIncl, periodEndExcl));

//                    String monthStr = String.format("%d-%02d", year, month);
//                    String periodStartIncl = String.format("%d-%02d-01", year, month);
//                    String periodEndExcl = month < 12 ? String.format("%d-%02d-01", year, month + 1) :
//                            String.format("%d-01-01", year + 1);
//
//                    System.out.println(String.format(sqlTemplate, monthStr, periodStartIncl, periodEndExcl));
                }
            }
        }

        System.out.println("counter = " + counter);
    }

    /**
     *
     * @return
     * @throws SQLException
     */
    private Connection getConnection() throws SQLException {
        String drv = "virtuoso.jdbc4.Driver";
        try {
            Class.forName(drv);
            return DriverManager.getConnection("jdbc:virtuoso://localhost:1111/charset=UTF-8/log_enable=2/DATABASE=CR", "cr3user", "togefedu");
        } catch (ClassNotFoundException e) {
            throw new CRRuntimeException("Failed to get connection, driver class not found: " + drv, e);
        }
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
