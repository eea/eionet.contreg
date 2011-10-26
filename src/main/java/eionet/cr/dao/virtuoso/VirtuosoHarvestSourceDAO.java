package eionet.cr.dao.virtuoso;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.readers.HarvestSourceDTOReader;
import eionet.cr.dao.readers.NewSourcesReaderWriter;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.harvest.statistics.dto.HarvestedUrlCountDTO;
import eionet.cr.util.Bindings;
import eionet.cr.util.Hashes;
import eionet.cr.util.Pair;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.YesNoBoolean;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.util.sesame.SesameUtil;
import eionet.cr.util.sql.SQLUtil;
import eionet.cr.util.sql.SingleObjectReader;

/**
 * Methods operating with harvest sources. Implementation for Virtuoso.
 *
 * @author kaido
 */

public class VirtuosoHarvestSourceDAO extends VirtuosoBaseDAO implements HarvestSourceDAO {

    private static final String GET_SOURCES_SQL =
        "SELECT<pagingParams> * FROM HARVEST_SOURCE WHERE URL NOT IN (SELECT URL FROM REMOVE_SOURCE_QUEUE) ";
    private static final String SEARCH_SOURCES_SQL =
        "SELECT<pagingParams> * FROM HARVEST_SOURCE WHERE URL like (?) AND URL NOT IN (SELECT URL FROM REMOVE_SOURCE_QUEUE) ";
    private static final String getHarvestSourcesFailedSQL =
        "SELECT<pagingParams> * FROM HARVEST_SOURCE WHERE LAST_HARVEST_FAILED = 'Y' AND URL NOT IN (SELECT URL FROM REMOVE_SOURCE_QUEUE)";
    private static final String searchHarvestSourcesFailedSQL =
        "SELECT<pagingParams> * FROM HARVEST_SOURCE WHERE LAST_HARVEST_FAILED = 'Y' AND URL LIKE(?) AND"
        + " URL NOT IN (SELECT URL FROM REMOVE_SOURCE_QUEUE)";
    private static final String getHarvestSourcesUnavailableSQL = "SELECT * FROM HARVEST_SOURCE WHERE COUNT_UNAVAIL > "
        + HarvestSourceDTO.COUNT_UNAVAIL_THRESHOLD + " AND URL NOT IN (SELECT URL FROM REMOVE_SOURCE_QUEUE)";
    private static final String searchHarvestSourcesUnavailableSQL =
        "SELECT<pagingParams> * FROM HARVEST_SOURCE WHERE URL LIKE (?) AND COUNT_UNAVAIL > "
        + HarvestSourceDTO.COUNT_UNAVAIL_THRESHOLD + " AND URL NOT IN (SELECT URL FROM REMOVE_SOURCE_QUEUE)";
    private static final String getPrioritySources =
        "SELECT<pagingParams> * FROM HARVEST_SOURCE WHERE PRIORITY_SOURCE = 'Y' AND URL NOT IN (SELECT URL FROM REMOVE_SOURCE_QUEUE)  ";
    private static final String searchPrioritySources =
        "SELECT<pagingParams> * FROM HARVEST_SOURCE WHERE PRIORITY_SOURCE = 'Y' and URL like(?) AND "
        + "URL NOT IN (SELECT URL FROM REMOVE_SOURCE_QUEUE) ";

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#getHarvestSources(java.lang.String, eionet.cr.util.PagingRequest,
     * eionet.cr.util.SortingRequest)
     */
    @Override
    public Pair<Integer, List<HarvestSourceDTO>> getHarvestSources(String searchString, PagingRequest pagingRequest,
            SortingRequest sortingRequest) throws DAOException {

        return getSources(StringUtils.isBlank(searchString) ? GET_SOURCES_SQL : SEARCH_SOURCES_SQL, searchString, pagingRequest,
                sortingRequest);
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#getHarvestSourcesFailed(java.lang.String, eionet.cr.util.PagingRequest,
     * eionet.cr.util.SortingRequest)
     */
    @Override
    public Pair<Integer, List<HarvestSourceDTO>> getHarvestSourcesFailed(String searchString, PagingRequest pagingRequest,
            SortingRequest sortingRequest) throws DAOException {

        return getSources(StringUtils.isBlank(searchString) ? getHarvestSourcesFailedSQL : searchHarvestSourcesFailedSQL,
                searchString, pagingRequest, sortingRequest);
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#getHarvestSourcesUnavailable(java.lang .String, eionet.cr.util.PagingRequest,
     * eionet.cr.util.SortingRequest)
     */
    @Override
    public Pair<Integer, List<HarvestSourceDTO>> getHarvestSourcesUnavailable(String searchString, PagingRequest pagingRequest,
            SortingRequest sortingRequest) throws DAOException {

        return getSources(
                StringUtils.isBlank(searchString) ? getHarvestSourcesUnavailableSQL : searchHarvestSourcesUnavailableSQL,
                        searchString, pagingRequest, sortingRequest);
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#getPrioritySources(java.lang.String, eionet.cr.util.PagingRequest,
     * eionet.cr.util.SortingRequest)
     */
    @Override
    public Pair<Integer, List<HarvestSourceDTO>> getPrioritySources(String searchString, PagingRequest pagingRequest,
            SortingRequest sortingRequest) throws DAOException {

        return getSources(StringUtils.isBlank(searchString) ? getPrioritySources : searchPrioritySources, searchString,
                pagingRequest, sortingRequest);
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#getInferenceSources(java.lang.String, eionet.cr.util.PagingRequest,
     * eionet.cr.util.SortingRequest)
     */
    @Override
    public Pair<Integer, List<HarvestSourceDTO>> getInferenceSources(String searchString, PagingRequest pagingRequest,
            SortingRequest sortingRequest, String sourceUris) throws DAOException {

        if (StringUtils.isBlank(sourceUris)) {
            return new Pair<Integer, List<HarvestSourceDTO>>(0, new ArrayList<HarvestSourceDTO>());
        }

        String query =
            "SELECT<pagingParams> * FROM HARVEST_SOURCE WHERE URL IN (<sources>) AND URL NOT IN (SELECT URL FROM REMOVE_SOURCE_QUEUE) ";
        if (!StringUtils.isBlank(searchString)) {
            query =
                "SELECT<pagingParams> * FROM HARVEST_SOURCE WHERE URL like (?) AND URL IN (<sources>) AND "
                + " URL NOT IN (SELECT URL FROM REMOVE_SOURCE_QUEUE) ";
        }
        query = query.replace("<sources>", sourceUris);

        return getSources(query, searchString, pagingRequest, sortingRequest);
    }

    /**
     *
     * @param sql
     * @param searchString
     * @param pagingRequest
     * @param sortingRequest
     * @return
     * @throws DAOException
     */
    private Pair<Integer, List<HarvestSourceDTO>> getSources(String sql, String searchString, PagingRequest pagingRequest,
            SortingRequest sortingRequest) throws DAOException {

        List<Object> inParams = new LinkedList<Object>();
        if (!StringUtils.isBlank(searchString)) {
            inParams.add(searchString);
        }
        String queryWithoutOrderAndLimit = sql.replace("<pagingParams>", "");
        List<Object> inParamsWithoutOrderAndLimit = new LinkedList<Object>(inParams);

        if (sortingRequest != null && sortingRequest.getSortingColumnName() != null) {
            sql += " ORDER BY " + sortingRequest.getSortingColumnName() + " " + sortingRequest.getSortOrder().toSQL();
        } else {
            sql += " ORDER BY URL ";
        }

        if (pagingRequest != null) {
            String pp = " TOP " + pagingRequest.getOffset() + ", " + pagingRequest.getItemsPerPage();
            sql = sql.replace("<pagingParams>", pp);
        } else {
            sql = sql.replace("<pagingParams>", "");
        }

        int rowCount = 0;
        List<HarvestSourceDTO> list = executeSQL(sql, inParams, new HarvestSourceDTOReader());
        if (list != null && !list.isEmpty()) {

            StringBuffer buf = new StringBuffer("select count(*) from (").append(queryWithoutOrderAndLimit).append(") as foo");

            rowCount =
                Integer.parseInt(executeUniqueResultSQL(buf.toString(), inParamsWithoutOrderAndLimit,
                        new SingleObjectReader<Object>()).toString());
        }

        return new Pair<Integer, List<HarvestSourceDTO>>(rowCount, list);
    }

    /**
     * Calculation of number of sources needed to be harvested in VirtuosoSQL syntax.
     */
    private static final String URGENCY_SOURCES_COUNT = "select count(*) from HARVEST_SOURCE where"
        + " INTERVAL_MINUTES > 0 AND -datediff('second', now(), coalesce(LAST_HARVEST,"
        + " dateadd('minute', -INTERVAL_MINUTES, TIME_CREATED))) / (INTERVAL_MINUTES*60) >= 1.0";

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#getUrgencySourcesCount()
     */
    @Override
    public Long getUrgencySourcesCount() throws DAOException {

        Connection conn = null;
        try {
            conn = getSQLConnection();
            Object o = SQLUtil.executeSingleReturnValueQuery(URGENCY_SOURCES_COUNT, conn);
            return o == null ? new Long(0) : Long.valueOf(o.toString());
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }

    /**
     * Insert a record into the the table of harvest sources in VirtuosoSQL syntax. INSERT SOFT means that if such source already
     * exists then don't insert (like MySQL INSERT IGNORE)
     */
    private static final String ADD_SOURCE_SQL = "insert soft HARVEST_SOURCE"
        + " (URL,URL_HASH,EMAILS,TIME_CREATED,INTERVAL_MINUTES,PRIORITY_SOURCE,SOURCE_OWNER)" + " VALUES (?,?,?,NOW(),?,?,?)";

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#addSource(HarvestSourceDTO source)
     */
    @Override
    public Integer addSource(HarvestSourceDTO source) throws DAOException {

        if (source == null) {
            throw new IllegalArgumentException("harvest source must not be null");
        }

        if (StringUtils.isBlank(source.getUrl())) {
            throw new IllegalArgumentException("url must not be null");
        }

        // harvest sources where URL has fragment part, are not allowed
        String url = StringUtils.substringBefore(source.getUrl(), "#");
        long urlHash = Hashes.spoHash(url);

        List<Object> values = new ArrayList<Object>();
        values.add(url);
        values.add(Long.valueOf(urlHash));
        values.add(source.getEmails());
        values.add(Integer.valueOf(source.getIntervalMinutes()));
        values.add(YesNoBoolean.format(source.isPrioritySource()));
        if (source.getOwner() == null || source.getOwner().length() == 0) {
            values.add("harvester");
        } else {
            values.add(source.getOwner());
        }

        Connection conn = null;
        try {
            // execute the insert statement
            conn = getSQLConnection();
            int o = SQLUtil.executeUpdateReturnAutoID(ADD_SOURCE_SQL, values, conn);
            return new Integer(o);
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#addSourceIgnoreDuplicate(HarvestSourceDTO source)
     */
    @Override
    public void addSourceIgnoreDuplicate(HarvestSourceDTO source) throws DAOException {
        addSource(source);
    }

    /** */
    private static final String DELETE_HARVEST_SOURCES = "delete from HARVEST_SOURCE where URL_HASH=?";
    /** */
    private static final String DELETE_FROM_URGENT_HARVEST_QUEUE = "delete from URGENT_HARVEST_QUEUE where URL=?";
    /** */
    private static final String DELETE_FROM_RULESET = "DB.DBA.rdfs_rule_set (?, ?, 1)";

    /**
     *
     * @param sourceUrls
     * @param conn
     * @throws SQLException
     */
    private void removeHarvestSources(Collection<String> sourceUrls, Connection conn) throws SQLException {

        // We need to clear all references in:
        // - harvest messages table (cascade deleted)
        // - harvests table (cascade deleted)
        // - harvest sources table
        // - urgent harvest queue
        // - inference rule-set

        PreparedStatement sourcesDeleteStatement = null;
        PreparedStatement urgentQueueDeleteStatement = null;
        PreparedStatement rulesetDeleteStatement = null;
        try {
            sourcesDeleteStatement = conn.prepareStatement(DELETE_HARVEST_SOURCES);
            urgentQueueDeleteStatement = conn.prepareStatement(DELETE_FROM_URGENT_HARVEST_QUEUE);
            rulesetDeleteStatement = conn.prepareStatement(DELETE_FROM_RULESET);

            for (String sourceUrl : sourceUrls) {

                long urlHash = Hashes.spoHash(sourceUrl);

                sourcesDeleteStatement.setLong(1, urlHash);
                urgentQueueDeleteStatement.setString(1, sourceUrl);
                rulesetDeleteStatement.setString(1, GeneralConfig.getRequiredProperty(GeneralConfig.VIRTUOSO_CR_RULESET_NAME));
                rulesetDeleteStatement.setString(2, sourceUrl);

                sourcesDeleteStatement.addBatch();
                urgentQueueDeleteStatement.addBatch();
                rulesetDeleteStatement.addBatch();
            }

            sourcesDeleteStatement.executeBatch();
            urgentQueueDeleteStatement.executeBatch();
            rulesetDeleteStatement.executeBatch();
        } finally {
            SQLUtil.close(sourcesDeleteStatement);
            SQLUtil.close(urgentQueueDeleteStatement);
            SQLUtil.close(rulesetDeleteStatement);
        }
    }

    /**
     *
     * @param sourceUrls
     * @param conn
     * @throws RepositoryException
     */
    private void removeHarvestSources(Collection<String> sourceUrls, RepositoryConnection conn) throws RepositoryException {

        ValueFactory valueFactory = conn.getValueFactory();
        Resource harvesterContext = valueFactory.createURI(GeneralConfig.HARVESTER_URI);

        // We need to clear all triples in the graph, and remove the graph's metadata (in harvester context) as well.

        for (String sourceUrl : sourceUrls) {

            Resource graphResource = valueFactory.createURI(sourceUrl);
            conn.clear(graphResource);
            conn.remove(graphResource, null, null, harvesterContext);
        }
    }

    /** */
    private static final String EDIT_SOURCE_SQL = "update HARVEST_SOURCE set URL=?, URL_HASH=?, EMAILS=?, INTERVAL_MINUTES=?,"
        + " PRIORITY_SOURCE=?, SOURCE_OWNER=?, MEDIA_TYPE=? where HARVEST_SOURCE_ID=?";

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#editSource(eionet.cr.dto.HarvestSourceDTO)
     */
    @Override
    public void editSource(HarvestSourceDTO source) throws DAOException {

        List<Object> values = new ArrayList<Object>();
        values.add(source.getUrl());
        values.add(Long.valueOf(source.getUrl() == null ? 0 : Hashes.spoHash(source.getUrl())));
        values.add(source.getEmails());
        values.add(source.getIntervalMinutes());
        values.add(YesNoBoolean.format(source.isPrioritySource()));
        values.add(source.getOwner());
        values.add(source.getMediaType());
        values.add(source.getSourceId());

        Connection conn = null;
        try {
            conn = getSQLConnection();
            SQLUtil.executeUpdate(EDIT_SOURCE_SQL, values, conn);
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }

    /** */
    private static final String getSourcesByIdSQL = "select * from HARVEST_SOURCE where HARVEST_SOURCE_ID=?";

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#getHarvestSourceById(java.lang.Integer)
     */
    @Override
    public HarvestSourceDTO getHarvestSourceById(Integer harvestSourceID) throws DAOException {

        List<Object> values = new ArrayList<Object>();
        values.add(harvestSourceID);
        List<HarvestSourceDTO> list = executeSQL(getSourcesByIdSQL, values, new HarvestSourceDTOReader());
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }

    /** */
    private static final String getSourcesByUrlSQL = "select * from HARVEST_SOURCE where URL_HASH=?";

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#getHarvestSourceByUrl(java.lang.String)
     */
    @Override
    public HarvestSourceDTO getHarvestSourceByUrl(String url) throws DAOException {

        List<Object> values = new ArrayList<Object>();
        values.add(Long.valueOf(Hashes.spoHash(url)));
        List<HarvestSourceDTO> list = executeSQL(getSourcesByUrlSQL, values, new HarvestSourceDTOReader());
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }

    /** */
    private static final String GET_NEXT_SCHEDULED_SOURCES_SQL = "select top <limit> * from HARVEST_SOURCE where"
        + " INTERVAL_MINUTES > 0 and <seconds_since_last_harvest> >= <harvest_interval_seconds>"
        + " order by (<seconds_since_last_harvest> / <harvest_interval_seconds>) desc";

    /** */
    private static final String SECONDS_SINCE_LAST_HARVEST_EXPR = "cast("
        + "abs(datediff('second', now(), coalesce(LAST_HARVEST, dateadd('second', -1*INTERVAL_MINUTES, TIME_CREATED)))) "
        + "as float)";

    /** */
    private static final String HARVEST_INTERVAL_SECONDS_EXPR = "cast(INTERVAL_MINUTES*60 as float)";

    /**
     * @see eionet.cr.dao.HarvestSourceDAO#getNextScheduledSources(int)
     */
    @Override
    public List<HarvestSourceDTO> getNextScheduledSources(int limit) throws DAOException {

        if (limit < 1) {
            throw new IllegalArgumentException("Limit must be >=1 ");
        }

        String query = GET_NEXT_SCHEDULED_SOURCES_SQL.replace("<limit>", String.valueOf(limit));
        query = query.replace("<seconds_since_last_harvest>", SECONDS_SINCE_LAST_HARVEST_EXPR);
        query = query.replace("<harvest_interval_seconds>", HARVEST_INTERVAL_SECONDS_EXPR);
        return executeSQL(query, Collections.EMPTY_LIST, new HarvestSourceDTOReader());
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#getScheduledForDeletion()
     */
    @Override
    public List<String> getScheduledForDeletion() throws DAOException {

        return executeSQL("select URL from REMOVE_SOURCE_QUEUE", null, new SingleObjectReader<String>());
    }

    /** */
    private static final String INCREASE_UNAVAIL_COUNT =
        "update HARVEST_SOURCE set COUNT_UNAVAIL=(COUNT_UNAVAIL+1) where URL_HASH=?";

    /**
     * @see eionet.cr.dao.HarvestSourceDAO#increaseUnavailableCount(int)
     */
    @Override
    public void increaseUnavailableCount(String sourceUrl) throws DAOException {
        Connection conn = null;
        try {
            conn = getSQLConnection();
            ArrayList<Object> values = new ArrayList<Object>();
            values.add(Long.valueOf(Hashes.spoHash(sourceUrl)));
            SQLUtil.executeUpdate(INCREASE_UNAVAIL_COUNT, values, conn);
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#getLatestHarvestedURLs()
     */
    @Override
    public Pair<Integer, List<HarvestedUrlCountDTO>> getLatestHarvestedURLs(int days) throws DAOException {

        StringBuffer buf =
            new StringBuffer().append("select left (datestring(LAST_HARVEST), 10) AS HARVESTDAY,")
            .append(" COUNT(HARVEST_SOURCE_ID) AS HARVESTS FROM CR.cr3user.HARVEST_SOURCE where")
            .append(" LAST_HARVEST IS NOT NULL AND dateadd('day', " + days + ", LAST_HARVEST) > now()")
            .append(" GROUP BY (HARVESTDAY) ORDER BY HARVESTDAY DESC");

        List<HarvestedUrlCountDTO> result = new ArrayList<HarvestedUrlCountDTO>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            conn = getSQLConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(buf.toString());
            while (rs.next()) {
                HarvestedUrlCountDTO resultRow = new HarvestedUrlCountDTO();
                try {
                    resultRow.setHarvestDay(sdf.parse(rs.getString("HARVESTDAY")));
                } catch (ParseException ex) {
                    throw new DAOException(ex.toString(), ex);
                }
                resultRow.setHarvestCount(rs.getLong("HARVESTS"));
                result.add(resultRow);
            }
        } catch (SQLException e) {
            throw new DAOException(e.toString(), e);
        } finally {
            SQLUtil.close(rs);
            SQLUtil.close(stmt);
            SQLUtil.close(conn);
        }

        return new Pair<Integer, List<HarvestedUrlCountDTO>>(result.size(), result);
    }

    /** */
    private static final String GET_MOST_URGENT_HARVEST_SOURCES = "select top <limit> * from HARVEST_SOURCE where"
        + " INTERVAL_MINUTES > 0 order by (<seconds_since_last_harvest> / <harvest_interval_seconds>) desc";

    /**
     * @see eionet.cr.dao.HelperDAO#getUrgencyOfComingHarvests()
     */
    @Override
    public List<HarvestSourceDTO> getMostUrgentHarvestSources(int limit) throws DAOException {

        if (limit < 1) {
            throw new IllegalArgumentException("Limit must be >=1 ");
        }

        String query = GET_MOST_URGENT_HARVEST_SOURCES.replace("<limit>", String.valueOf(limit));
        query = query.replace("<seconds_since_last_harvest>", SECONDS_SINCE_LAST_HARVEST_EXPR);
        query = query.replace("<harvest_interval_seconds>", HARVEST_INTERVAL_SECONDS_EXPR);
        return executeSQL(query, Collections.EMPTY_LIST, new HarvestSourceDTOReader());

        //
        // StringBuffer buf =
        // new StringBuffer()
        // .append("select top " + limit + " url, last_harvest, interval_minutes,")
        // .append(" (-datediff('second', now(), coalesce(LAST_HARVEST, dateadd('minute', -INTERVAL_MINUTES, TIME_CREATED)))")
        // .append(" / (INTERVAL_MINUTES*60)) AS urgency").append(" from CR.cr3user.HARVEST_SOURCE")
        // .append(" where INTERVAL_MINUTES > 0 ORDER BY urgency DESC");
        //
        // List<HarvestUrgencyScoreDTO> result = new ArrayList<HarvestUrgencyScoreDTO>();
        // Connection conn = null;
        // Statement stmt = null;
        // ResultSet rs = null;
        // try {
        // SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        //
        // conn = getSQLConnection();
        // stmt = conn.createStatement();
        // rs = stmt.executeQuery(buf.toString());
        // while (rs.next()) {
        // HarvestUrgencyScoreDTO resultRow = new HarvestUrgencyScoreDTO();
        // resultRow.setUrl(rs.getString("url"));
        // try {
        // resultRow.setLastHarvest(sdf.parse(rs.getString("last_harvest") + ""));
        // } catch (ParseException ex) {
        // resultRow.setLastHarvest(null);
        // // throw new DAOException(ex.toString(), ex);
        // }
        // resultRow.setIntervalMinutes(rs.getLong("interval_minutes"));
        // resultRow.setUrgency(rs.getDouble("urgency"));
        // result.add(resultRow);
        // }
        // } catch (SQLException e) {
        // throw new DAOException(e.toString(), e);
        // } finally {
        // SQLUtil.close(rs);
        // SQLUtil.close(stmt);
        // SQLUtil.close(conn);
        // }
        //
        // return new Pair<Integer, List<HarvestUrgencyScoreDTO>>(result.size(), result);

    }

    /** */
    private static final String UPDATE_SOURCE_HARVEST_FINISHED_SQL =
        "update HARVEST_SOURCE set EMAILS=?, STATEMENTS=?, COUNT_UNAVAIL=?, LAST_HARVEST=?, INTERVAL_MINUTES=?,"
        + " LAST_HARVEST_FAILED=?, PRIORITY_SOURCE=?, SOURCE_OWNER=?," + " PERMANENT_ERROR=? where URL_HASH=?";

    /**
     *
     */
    @Override
    public void updateSourceHarvestFinished(HarvestSourceDTO sourceDTO) throws DAOException {

        if (sourceDTO == null) {
            throw new IllegalArgumentException("Source DTO must not be null!");
        } else if (sourceDTO.getUrl() == null) {
            throw new IllegalArgumentException("Source URL must not be null!");
        }

        ArrayList<Object> values = new ArrayList<Object>();
        values.add(sourceDTO.getEmails());
        values.add(sourceDTO.getStatements() == null ? (int) 0 : sourceDTO.getStatements());
        values.add(sourceDTO.getCountUnavail() == null ? (int) 0 : sourceDTO.getCountUnavail());
        values.add(sourceDTO.getLastHarvest() == null ? null : new Timestamp(sourceDTO.getLastHarvest().getTime()));
        values.add(sourceDTO.getIntervalMinutes() == null ? (int) 0 : sourceDTO.getIntervalMinutes());
        values.add(YesNoBoolean.format(sourceDTO.isLastHarvestFailed()));
        values.add(YesNoBoolean.format(sourceDTO.isPrioritySource()));
        values.add(sourceDTO.getOwner());
        values.add(YesNoBoolean.format(sourceDTO.isPermanentError()));
        values.add(Hashes.spoHash(sourceDTO.getUrl()));

        Connection conn = null;
        try {
            conn = getSQLConnection();
            SQLUtil.executeUpdate(UPDATE_SOURCE_HARVEST_FINISHED_SQL, values, conn);
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }

    /**
     * @see eionet.cr.dao.HarvestSourceDAO#removeHarvestSources(Collection)
     */
    @Override
    public void removeHarvestSources(Collection<String> sourceUrls) throws DAOException {

        if (sourceUrls == null || sourceUrls.isEmpty()) {
            return;
        }

        // We need to do removals both in triple store and relational tables,
        // so prepare connections for both.

        Connection sqlConn = null;
        RepositoryConnection repoConn = null;
        try {
            sqlConn = getSQLConnection();
            repoConn = SesameUtil.getRepositoryConnection();

            sqlConn.setAutoCommit(false);
            repoConn.setAutoCommit(false);

            // Perform removals in triple store.
            removeHarvestSources(sourceUrls, repoConn);
            // Perform removals in relational tables.
            removeHarvestSources(sourceUrls, sqlConn);

            // Commit removals.
            repoConn.commit();
            sqlConn.commit();

        } catch (RepositoryException e) {
            SesameUtil.rollback(repoConn);
            SQLUtil.rollback(sqlConn);
            throw new DAOException("Repository exception when deleting sources", e);
        } catch (SQLException e) {
            SesameUtil.rollback(repoConn);
            SQLUtil.rollback(sqlConn);
            throw new DAOException("SQLException when deleting sources", e);
        } finally {
            SesameUtil.close(repoConn);
            SQLUtil.close(sqlConn);
        }
    }

    /**
     * @see eionet.cr.dao.HarvestSourceDAO#clearGraph(java.lang.String)
     */
    @Override
    public void clearGraph(String graphUri) throws DAOException {

        RepositoryConnection conn = null;
        try {
            conn = SesameUtil.getRepositoryConnection();
            conn.setAutoCommit(false);
            conn.clear(conn.getValueFactory().createURI(graphUri));
            conn.commit();
        } catch (RepositoryException e) {
            SesameUtil.rollback(conn);
            throw new DAOException("Repository exception when clearing graph " + graphUri, e);
        } finally {
            SesameUtil.close(conn);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#getSourcesInInferenceRules()
     */
    @Override
    public String getSourcesInInferenceRules() throws DAOException {

        Connection conn = null;
        ResultSet rs = null;
        String ret = "";
        PreparedStatement stmt = null;
        try {
            conn = SesameUtil.getSQLConnection();
            stmt = conn.prepareStatement("SELECT RS_URI FROM DB.DBA.sys_rdf_schema where RS_NAME = ?");
            stmt.setString(1, GeneralConfig.getRequiredProperty(GeneralConfig.VIRTUOSO_CR_RULESET_NAME));
            rs = stmt.executeQuery();

            StringBuffer sb = new StringBuffer();
            while (rs.next()) {
                String graphUri = rs.getString("RS_URI");
                if (!StringUtils.isBlank(graphUri)) {
                    sb.append("'").append(graphUri).append("'");
                    sb.append(",");
                }
            }

            ret = sb.toString();
            // remove last comma
            if (!StringUtils.isBlank(ret)) {
                ret = ret.substring(0, ret.lastIndexOf(","));
            }
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
            SQLUtil.close(rs);
            SQLUtil.close(stmt);
        }

        return ret;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#isSourceInInferenceRule()
     */
    @Override
    public boolean isSourceInInferenceRule(String url) throws DAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        boolean ret = false;
        try {
            conn = SesameUtil.getSQLConnection();
            stmt = conn.prepareStatement("SELECT RS_NAME FROM DB.DBA.sys_rdf_schema where RS_NAME = ? AND RS_URI = ?");
            stmt.setString(1, GeneralConfig.getRequiredProperty(GeneralConfig.VIRTUOSO_CR_RULESET_NAME));
            stmt.setString(2, url);
            rs = stmt.executeQuery();
            while (rs.next()) {
                ret = true;
            }
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
            SQLUtil.close(rs);
            SQLUtil.close(stmt);
        }
        return ret;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#addSourceIntoInferenceRule()
     */
    @Override
    public boolean addSourceIntoInferenceRule(String url) throws DAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        boolean ret = false;

        try {
            conn = SesameUtil.getSQLConnection();
            stmt = conn.prepareStatement("DB.DBA.rdfs_rule_set (?, ?)");
            stmt.setString(1, GeneralConfig.getRequiredProperty(GeneralConfig.VIRTUOSO_CR_RULESET_NAME));
            stmt.setString(2, url);
            ret = stmt.execute();

        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
            SQLUtil.close(stmt);
        }
        return ret;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#removeSourceFromInferenceRule()
     */
    @Override
    public boolean removeSourceFromInferenceRule(String url) throws DAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        boolean ret = false;

        try {
            conn = SesameUtil.getSQLConnection();
            stmt = conn.prepareStatement("DB.DBA.rdfs_rule_set (?, ?, 1)");
            stmt.setString(1, GeneralConfig.getRequiredProperty(GeneralConfig.VIRTUOSO_CR_RULESET_NAME));
            stmt.setString(2, url);
            ret = stmt.execute();
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
            SQLUtil.close(stmt);
        }
        return ret;
    }

    /**
     * @see eionet.cr.dao.virtuoso.VirtuosoHarvestSourceDAO#loadIntoRepository(java.io.File, RDFFormat, java.lang.String, boolean)
     */
    @Override
    public int loadIntoRepository(File file, RDFFormat rdfFormat, String graphUrl, boolean clearPreviousGraphContent)
    throws IOException, OpenRDFException {

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            return loadIntoRepository(inputStream, rdfFormat, graphUrl, clearPreviousGraphContent);

        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * @see eionet.cr.dao.virtuoso.VirtuosoHarvestSourceDAO#loadIntoRepository(java.io.InputStream, RDFFormat, java.lang.String,
     *      boolean)
     */
    @Override
    public int
    loadIntoRepository(InputStream inputStream, RDFFormat rdfFormat, String graphUrl, boolean clearPreviousGraphContent)
    throws IOException, OpenRDFException {

        int storedTriplesCount = 0;
        boolean isSuccess = false;
        RepositoryConnection conn = null;
        try {

            conn = SesameUtil.getRepositoryConnection();

            // convert graph URL into OpenRDF Resource
            org.openrdf.model.Resource graphResource = conn.getValueFactory().createURI(graphUrl);

            // start transaction
            conn.setAutoCommit(false);

            // if required, clear previous triples of this graph
            if (clearPreviousGraphContent) {
                conn.clear(graphResource);
            }

            // add the stream content into repository under the given graph
            conn.add(inputStream, graphUrl, rdfFormat == null ? RDFFormat.RDFXML : rdfFormat, graphResource);

            long tripleCount = conn.size(graphResource);

            // commit transaction
            conn.commit();

            // set total stored triples count
            storedTriplesCount = Long.valueOf(tripleCount).intValue();

            // no transaction rollback needed, when reached this point
            isSuccess = true;
        } finally {
            if (!isSuccess) {
                SesameUtil.rollback(conn);
            }
            SesameUtil.close(conn);
        }

        return storedTriplesCount;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#addSourceMetadata(SubjectDTO)
     */
    @Override
    public void addSourceMetadata(SubjectDTO sourceMetadata) throws DAOException, RDFParseException, RepositoryException,
    IOException {

        if (sourceMetadata.getPredicateCount() > 0) {

            boolean isSuccess = false;
            RepositoryConnection conn = null;

            try {
                conn = SesameUtil.getRepositoryConnection();
                conn.setAutoCommit(false);

                // The contextURI is always the harvester URI
                URI harvesterContext = conn.getValueFactory().createURI(GeneralConfig.HARVESTER_URI);

                if (sourceMetadata != null) {
                    URI subject = conn.getValueFactory().createURI(sourceMetadata.getUri());

                    // Remove old predicates
                    conn.remove(subject, null, null, harvesterContext);

                    if (sourceMetadata.getPredicateCount() > 0) {
                        insertMetadata(sourceMetadata, conn, harvesterContext, subject);
                    }
                }
                // commit transaction
                conn.commit();

                // no transaction rollback needed, when reached this point
                isSuccess = true;
            } finally {
                if (!isSuccess && conn != null) {
                    try {
                        conn.rollback();
                    } catch (RepositoryException e) {
                    }
                }
                SesameUtil.close(conn);
            }
        }
    }

    /**
     * @param subjectDTO
     * @param conn
     * @param contextURI
     * @param subject
     * @throws RepositoryException
     */
    private void insertMetadata(SubjectDTO subjectDTO, RepositoryConnection conn, URI contextURI, URI subject)
    throws RepositoryException {

        for (String predicateUri : subjectDTO.getPredicates().keySet()) {

            Collection<ObjectDTO> objects = subjectDTO.getObjects(predicateUri);
            if (objects != null && !objects.isEmpty()) {

                URI predicate = conn.getValueFactory().createURI(predicateUri);
                for (ObjectDTO object : objects) {
                    if (object.isLiteral()) {
                        Literal literalObject = conn.getValueFactory().createLiteral(object.toString(), object.getDatatype());
                        conn.add(subject, predicate, literalObject, contextURI);
                    } else {
                        URI resourceObject = conn.getValueFactory().createURI(object.toString());
                        conn.add(subject, predicate, resourceObject, contextURI);
                    }
                }
            }
        }
    }

    /**
     * SPARQL for getting source metadata.
     */
    private static final String SOURCE_METADATA_SPARQL = "select ?o where {graph ?g { ?subject ?predicate ?o . "
        + "filter (?g = ?deploymentHost)}}";

    /**
     * @see eionet.cr.dao.HarvestSourceDAO#getHarvestSourceMetadata(java.lang.String, java.lang.String)
     */
    @Override
    public String getHarvestSourceMetadata(String harvestSourceUri, String predicateUri) throws DAOException {

        Bindings bindings = new Bindings();
        bindings.setURI("deploymentHost", GeneralConfig.HARVESTER_URI);
        bindings.setURI("subject", harvestSourceUri);
        bindings.setURI("predicate", predicateUri);

        Object resultObject = executeUniqueResultSPARQL(SOURCE_METADATA_SPARQL, bindings, new SingleObjectReader<Long>());
        return resultObject == null ? null : resultObject.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#insertUpdateSourceMetadata(String, String, ObjectDTO)
     */
    @Override
    public void insertUpdateSourceMetadata(String subject, String predicate, ObjectDTO object) throws DAOException,
    RepositoryException, IOException {
        RepositoryConnection conn = null;
        try {

            conn = SesameUtil.getRepositoryConnection();

            URI harvesterContext = conn.getValueFactory().createURI(GeneralConfig.HARVESTER_URI);
            URI sub = conn.getValueFactory().createURI(subject);
            URI pred = conn.getValueFactory().createURI(predicate);

            conn.remove(sub, pred, null, harvesterContext);
            if (object.isLiteral()) {
                Literal literalObject = conn.getValueFactory().createLiteral(object.toString(), object.getDatatype());
                conn.add(sub, pred, literalObject, harvesterContext);
            } else {
                URI resourceObject = conn.getValueFactory().createURI(object.toString());
                conn.add(sub, pred, resourceObject, harvesterContext);
            }

        } finally {
            SesameUtil.close(conn);
        }
    }

    /**
     *
     */
    @Override
    public void deleteSubjectTriplesInSource(String subjectUri, String sourceUri) throws DAOException {

        if (!StringUtils.isBlank(subjectUri)) {
            RepositoryConnection conn = null;
            try {

                conn = SesameUtil.getRepositoryConnection();
                ValueFactory valueFactory = conn.getValueFactory();
                conn.remove(valueFactory.createURI(subjectUri), null, null, valueFactory.createURI(sourceUri));

            } catch (RepositoryException e) {
                throw new DAOException(e.getMessage(), e);
            } finally {
                SesameUtil.close(conn);
            }
        }
    }

    /** */
    private static final String FILTER_BY_SUBSTRING_SQL =
        "select top(@offset,@limit) URL from HARVEST_SOURCE where URL like (?) order by URL asc";

    /**
     * @see eionet.cr.dao.HarvestSourceDAO#filter(java.lang.String, int, int)
     */
    @Override
    public List<String> filter(String substring, int limit, int offset) throws DAOException {

        if (StringUtils.isBlank(substring) || limit <= 0) {
            return Collections.emptyList();
        }

        offset = Math.max(offset, 0);

        String sql = StringUtils.replace(FILTER_BY_SUBSTRING_SQL, "@offset", String.valueOf(offset));
        sql = StringUtils.replace(sql, "@limit", String.valueOf(limit));
        ArrayList<String> values = new ArrayList<String>();
        values.add("%" + substring + "%");
        return executeSQL(sql, values, new SingleObjectReader<String>());
    }

    @Override
    public int getTotalStatementsCount() throws DAOException {
        String sql = "SELECT SUM(h.statements) FROM harvest_source as h";
        return Integer.parseInt(executeUniqueResultSQL(sql, null, new SingleObjectReader<Object>()).toString());
    }

    /**
     * SPARQL for getting new sources based on the given source.
     */
    private static final String NEW_SOURCES_SPARQL = "DEFINE input:inference 'CRInferenceRule' PREFIX cr: "
        + "<http://cr.eionet.europa.eu/ontologies/contreg.rdf#> SELECT ?s FROM ?sourceUrl FROM ?deploymentHost WHERE "
        + "{ ?s a cr:File . OPTIONAL { ?s cr:lastRefreshed ?refreshed } FILTER( !BOUND(?refreshed)) }";

    /**
     * @see eionet.cr.dao.HarvestSourceDAO#deriveNewHarvestSources(java.lang.String)
     */
    @Override
    public int deriveNewHarvestSources(String sourceUrl) throws DAOException {

        if (StringUtils.isBlank(sourceUrl)) {
            throw new IllegalArgumentException("Source url must not be blank!");
        }

        Bindings bindings = new Bindings();
        bindings.setURI("sourceUrl", sourceUrl);
        bindings.setURI("deploymentHost", GeneralConfig.getRequiredProperty(GeneralConfig.APPLICATION_HOME_URL));

        Connection sqlConn = null;
        RepositoryConnection sparqlConn = null;
        NewSourcesReaderWriter reader = null;
        try {
            sparqlConn = SesameUtil.getRepositoryConnection();
            sparqlConn.setAutoCommit(false);

            sqlConn = SesameUtil.getSQLConnection();
            sqlConn.setAutoCommit(false);

            reader = new NewSourcesReaderWriter(sqlConn);
            reader.setBlankNodeUriPrefix(VirtuosoBaseDAO.BNODE_URI_PREFIX);
            SesameUtil.executeQuery(NEW_SOURCES_SPARQL, bindings, reader, sparqlConn);
            reader.finish();

            sparqlConn.commit();
            sqlConn.commit();

            return reader.getSourceCount();

        } catch (Exception e) {
            SesameUtil.rollback(sparqlConn);
            SQLUtil.rollback(sqlConn);
            if (e instanceof DAOException) {
                throw (DAOException) e;
            } else {
                throw new DAOException(e.getMessage(), e);
            }
        } finally {
            SesameUtil.close(sparqlConn);
            if (reader != null) {
                reader.closeResources();
            }
            SQLUtil.close(sqlConn);
        }
    }

    /** */
    private static final String SOURCES_ABOVE_URGENCY_THRESHOLD_SQL = "select count(*) from HARVEST_SOURCE where"
        + " INTERVAL_MINUTES > 0 and (<seconds_since_last_harvest> / <harvest_interval_seconds>) > ?";

    /**
     * @see eionet.cr.dao.HarvestSourceDAO#getNumberOfSourcesAboveUrgencyThreshold(double)
     */
    @Override
    public int getNumberOfSourcesAboveUrgencyThreshold(double threshold) throws DAOException {

        // Currently cannot see any reason for a negative threshold.
        if (threshold < 0) {
            throw new IllegalArgumentException("Urgency threshold must not be negative!");
        }

        ArrayList<Object> params = new ArrayList<Object>();
        params.add(Double.valueOf(threshold));

        String query =
            SOURCES_ABOVE_URGENCY_THRESHOLD_SQL.replace("<seconds_since_last_harvest>", SECONDS_SINCE_LAST_HARVEST_EXPR);
        query = query.replace("<harvest_interval_seconds>", HARVEST_INTERVAL_SECONDS_EXPR);

        Object o = executeUniqueResultSQL(query, params, new SingleObjectReader<Object>());
        return o == null ? 0 : Integer.parseInt(o.toString());
    }
}
