package eionet.cr.dao.virtuoso;

import eionet.cr.common.CRException;
import eionet.cr.common.Predicates;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.readers.HarvestSourceDTOReader;
import eionet.cr.dao.readers.NewSourcesReaderWriter;
import eionet.cr.dao.readers.UrlAuthenticationDTOReader;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.dto.UrlAuthenticationDTO;
import eionet.cr.harvest.BaseHarvest;
import eionet.cr.harvest.load.ContentLoader;
import eionet.cr.harvest.load.RDFFormatLoader;
import eionet.cr.harvest.statistics.dto.HarvestedUrlCountDTO;
import eionet.cr.util.*;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.util.sesame.SesameUtil;
import eionet.cr.util.sql.SQLUtil;
import eionet.cr.util.sql.SingleObjectReader;
import eionet.cr.web.sparqlClient.helpers.QueryResult;
import eionet.cr.web.sparqlClient.helpers.ResultValue;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

/**
 * Methods operating with harvest sources. Implementation for Virtuoso.
 *
 * @author jaanus
 * @author George Sofianos
 */

public class VirtuosoHarvestSourceDAO extends VirtuosoBaseDAO implements HarvestSourceDAO {

    /** Static logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(VirtuosoHarvestSourceDAO.class);

    /** Suffix used in backup graph uris. */
    private static final String BACKUP_GRAPH_SUFFIX = "_backup";

    /** Suffix used in temporary graph uris. */
    private static final String TEMP_GRAPH_SUFFIX = "_tempharvest";
    /** */
    private static final String GET_SOURCES_SQL = "SELECT<pagingParams> * FROM HARVEST_SOURCE WHERE delete_requested is null ";
    /** */
    private static final String SEARCH_SOURCES_SQL =
            "SELECT<pagingParams> * FROM HARVEST_SOURCE WHERE URL like (?) AND delete_requested is null ";

    /** */
    private static final String GET_HARVEST_SOURCES_FAILED_SQL =
            "SELECT<pagingParams> * FROM HARVEST_SOURCE JOIN HARVEST H ON H.HARVEST_ID = LAST_HARVEST_ID "
                    + "WHERE (H.HTTP_CODE <> 401 OR H.HTTP_CODE IS NULL) AND LAST_HARVEST_FAILED = 'Y' "
                    + "AND delete_requested is null";
    /** */
    private static final String SEARCH_HARVEST_SOURCES_FAILED_SQL =
            "SELECT<pagingParams> * FROM HARVEST_SOURCE JOIN HARVEST H ON H.HARVEST_ID = LAST_HARVEST_ID "
                    + "WHERE (H.HTTP_CODE <> 401 OR H.HTTP_CODE IS NULL) "
                    + "AND LAST_HARVEST_FAILED = 'Y' AND URL LIKE(?) AND delete_requested is null";
    /** */
    private static final String GET_HARVEST_SOURCES_UNAVAIL_SQL = "SELECT * FROM HARVEST_SOURCE WHERE COUNT_UNAVAIL >= "
            + HarvestSourceDTO.COUNT_UNAVAIL_THRESHOLD + " AND delete_requested is null";
    /** */
    private static final String SEARCH_HARVEST_SOURCES_UNAVAIL_SQL =
            "SELECT<pagingParams> * FROM HARVEST_SOURCE WHERE URL LIKE (?) AND COUNT_UNAVAIL >= "
                    + HarvestSourceDTO.COUNT_UNAVAIL_THRESHOLD + " AND delete_requested is null";

    /** */
    private static final String GET_REMOTE_ENDPOINTS_SQL = "SELECT * FROM HARVEST_SOURCE WHERE IS_SPARQL_ENDPOINT='Y'"
            + " AND delete_requested is null";
    /** */
    private static final String SEARCH_REMOTE_ENDPOINTS_SQL =
            "SELECT<pagingParams> * FROM HARVEST_SOURCE WHERE IS_SPARQL_ENDPOINT='Y' AND URL LIKE (?) AND delete_requested is null";

    /** */
    private static final String GET_PRIORITY_SOURCES_SQL = "SELECT<pagingParams> * FROM HARVEST_SOURCE "
            + "WHERE PRIORITY_SOURCE = 'Y' AND delete_requested is null ";
    /** */
    private static final String SEARCH_PRIORITY_SOURCES_SQL =
            "SELECT<pagingParams> * FROM HARVEST_SOURCE WHERE PRIORITY_SOURCE = 'Y' and URL like(?) AND delete_requested is null ";
    /** */
    public static final String RENAME_GRAPH_SQL =
            "UPDATE DB.DBA.RDF_QUAD TABLE OPTION (index RDF_QUAD_GS) SET g = iri_to_id ('%new_graph%') "
                    + "WHERE g = iri_to_id ('%old_graph%',0)";

    /** */
    private static final String GRAPH_SYNC_SPARUL1 = "" + "DELETE FROM GRAPH <%perm_graph%> {\n" + "    ?s ?p ?o\n" + "}\n"
            + "WHERE {\n" + "  GRAPH <%perm_graph%> {?s ?p ?o}\n"
            + "  FILTER (!bif:exists((select(1) where {graph <%temp_graph%> {?s ?p ?o}})))\n" + "}";

    /** */
    private static final String GRAPH_SYNC_SPARUL2 = "" + "INSERT INTO GRAPH <%perm_graph%> {\n" + "    ?s ?p ?o\n" + "}\n"
            + "WHERE {\n" + "  GRAPH <%temp_graph%> {?s ?p ?o}\n"
            + "  FILTER (!bif:exists((select (1) where {graph <%perm_graph%> {?s ?p ?o}})))\n" + "}";

    /** */
    private static final int BATCH_CHUNK_SIZE = 5000;

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#getHarvestSources(java.lang.String, eionet.cr.util.pagination.PagingRequest,
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
     * @see eionet.cr.dao.HarvestSourceDAO#getHarvestSourcesFailed(java.lang.String, eionet.cr.util.pagination.PagingRequest,
     * eionet.cr.util.SortingRequest)
     */
    @Override
    public Pair<Integer, List<HarvestSourceDTO>> getHarvestSourcesFailed(String searchString, PagingRequest pagingRequest,
            SortingRequest sortingRequest) throws DAOException {

        return getSources(StringUtils.isBlank(searchString) ? GET_HARVEST_SOURCES_FAILED_SQL : SEARCH_HARVEST_SOURCES_FAILED_SQL,
                searchString, pagingRequest, sortingRequest);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pair<Integer, List<HarvestSourceDTO>> getHarvestSourcesUnauthorized(String searchString, PagingRequest pagingRequest,
            SortingRequest sortingRequest) throws DAOException {

        List<Object> params = new ArrayList<Object>();
        StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        if (pagingRequest != null) {
            query.append("TOP " + pagingRequest.getOffset() + ", " + pagingRequest.getItemsPerPage());
        }
        query.append(" HS.* ");
        query.append("FROM HARVEST_SOURCE HS ");
        query.append("JOIN HARVEST H ON H.HARVEST_ID = HS.LAST_HARVEST_ID ");
        query.append("WHERE H.HTTP_CODE = 401 ");
        if (StringUtils.isNotEmpty(searchString)) {
            query.append("AND HS.URL LIKE(?) ");
            params.add(searchString);
        }
        query.append("AND HS.delete_requested is null ");

        if (sortingRequest != null && sortingRequest.getSortingColumnName() != null) {
            query.append("ORDER BY " + sortingRequest.getSortingColumnName() + " " + sortingRequest.getSortOrder().toSQL());
        } else {
            query.append("ORDER BY HS.URL");
        }

        List<HarvestSourceDTO> list = executeSQL(query.toString(), params, new HarvestSourceDTOReader());

        int rowCount = 0;
        if (list != null && !list.isEmpty()) {
            StringBuffer countQuery = new StringBuffer();
            countQuery.append("select count(*) ");
            countQuery.append("FROM HARVEST_SOURCE HS ");
            countQuery.append("JOIN HARVEST H ON H.HARVEST_ID = HS.LAST_HARVEST_ID ");
            countQuery.append("WHERE H.HTTP_CODE = 401 ");
            if (StringUtils.isNotEmpty(searchString)) {
                countQuery.append("AND HS.URL LIKE(?) ");
            }
            countQuery.append("AND HS.delete_requested is null ");

            rowCount =
                    Integer.parseInt(executeUniqueResultSQL(countQuery.toString(), params, new SingleObjectReader<Object>())
                            .toString());
        }

        return new Pair<Integer, List<HarvestSourceDTO>>(rowCount, list);
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#getHarvestSourcesUnavailable(java.lang.String, eionet.cr.util.pagination.PagingRequest,
     * eionet.cr.util.SortingRequest)
     */
    @Override
    public Pair<Integer, List<HarvestSourceDTO>> getHarvestSourcesUnavailable(String searchString, PagingRequest pagingRequest,
            SortingRequest sortingRequest) throws DAOException {

        return getSources(
                StringUtils.isBlank(searchString) ? GET_HARVEST_SOURCES_UNAVAIL_SQL : SEARCH_HARVEST_SOURCES_UNAVAIL_SQL,
                searchString, pagingRequest, sortingRequest);
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#getRemoteEndpoints(java.lang.String, eionet.cr.util.pagination.PagingRequest,
     * eionet.cr.util.SortingRequest)
     */
    @Override
    public Pair<Integer, List<HarvestSourceDTO>> getRemoteEndpoints(String searchString, PagingRequest pagingRequest,
            SortingRequest sortingRequest) throws DAOException {

        return getSources(StringUtils.isBlank(searchString) ? GET_REMOTE_ENDPOINTS_SQL : SEARCH_REMOTE_ENDPOINTS_SQL,
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

        return getSources(StringUtils.isBlank(searchString) ? GET_PRIORITY_SOURCES_SQL : SEARCH_PRIORITY_SOURCES_SQL,
                searchString, pagingRequest, sortingRequest);
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#getInferenceSources(java.lang.String, eionet.cr.util.PagingRequest,
     * eionet.cr.util.SortingRequest)
     *
     * @Deprecated Inferencing is removed from CR
     */
    @Override
    @Deprecated
    public Pair<Integer, List<HarvestSourceDTO>> getInferenceSources(String searchString, PagingRequest pagingRequest,
            SortingRequest sortingRequest, String sourceUris) throws DAOException {

        if (StringUtils.isBlank(sourceUris)) {
            return new Pair<Integer, List<HarvestSourceDTO>>(0, new ArrayList<HarvestSourceDTO>());
        }

        String query =
                "SELECT<pagingParams> * FROM HARVEST_SOURCE "
                        + "WHERE URL IN (<sources>) AND delete_requested is null ";
        if (!StringUtils.isBlank(searchString)) {
            query =
                    "SELECT<pagingParams> * FROM HARVEST_SOURCE WHERE URL like (?) AND URL IN (<sources>) AND "
                            + " delete_requested is null ";
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
    private static final String ADD_SOURCE_SQL =
            "insert soft HARVEST_SOURCE (URL, URL_HASH,EMAILS, TIME_CREATED, INTERVAL_MINUTES, PRIORITY_SOURCE, SOURCE_OWNER, "
                    + "MEDIA_TYPE, IS_SPARQL_ENDPOINT, COUNT_UNAVAIL, IS_ONLINE_CSV_TSV, CSV_TSV_URL) VALUES (?,?,?,NOW(),?,?,?,?,?,0,?,?)";

    private static final String UPDATE_BULK_SOURCE_LAST_HARVEST =
            "update HARVEST_SOURCE set LAST_HARVEST=stringdate('2000-01-01') where URL_HASH=?";
    private static final String INSERT_BULK_SOURCE =
            "insert soft HARVEST_SOURCE (URL,URL_HASH,TIME_CREATED,INTERVAL_MINUTES) VALUES (?,?,NOW(),?)";

    /**
     * Adds all sources found by a Sparql query
     *
     * @param queryResult
     * @return inserted sources identity values list
     * @throws DAOException
     */
    @Override
    public void addBulkSourcesFromSparql(QueryResult queryResult) throws DAOException {

        if (queryResult == null) {
            return;
        }

        ArrayList<Map<String, Object>> columns = queryResult.getCols();
        if (CollectionUtils.isEmpty(columns)) {
            return;
        }

        String firstColumn = (String) columns.get(0).get("property");
        if (StringUtils.isBlank(firstColumn)) {
            return;
        }

        Connection conn = null;
        try {
            // execute the insert statement
            conn = getSQLConnection();

            PreparedStatement insertAndUpdate = null;
            PreparedStatement updateLastHarvest = null;

            insertAndUpdate = conn.prepareStatement(INSERT_BULK_SOURCE);
            updateLastHarvest = conn.prepareStatement(UPDATE_BULK_SOURCE_LAST_HARVEST);

            int counter = 0;
            int batchDivideCounter = 0;

            for (Map<String, ResultValue> row : queryResult.getRows()) {

                String resultValue = row.get(firstColumn).getValue();
                String sourceUrl = eionet.cr.util.URLUtil.escapeIRI(resultValue);
                sourceUrl = URLUtil.normalizeHarvestSourceUrl(sourceUrl, false);

                if (URLUtil.isURL(sourceUrl)) {
                    insertAndUpdate.setString(1, sourceUrl);
                    insertAndUpdate.setLong(2, Long.valueOf(Hashes.spoHash(sourceUrl)));
                    insertAndUpdate.setInt(3, eionet.cr.config.GeneralConfig.getDefaultHarvestIntervalMinutes());
                    insertAndUpdate.addBatch();

                    updateLastHarvest.setLong(1, Long.valueOf(Hashes.spoHash(sourceUrl)));
                    updateLastHarvest.addBatch();

                    counter++;
                    batchDivideCounter++;
                }

                // If the batch is becoming too large, execute, clear and add new rows in a separate cycle.
                if (batchDivideCounter == BATCH_CHUNK_SIZE) {
                    insertAndUpdate.executeBatch();
                    updateLastHarvest.executeBatch();
                    insertAndUpdate.clearBatch();
                    updateLastHarvest.clearBatch();
                    batchDivideCounter = 0;

                    LOGGER.info("Added/updated a batch of " + BATCH_CHUNK_SIZE + " bulk sources from Sparql endpoint query.");
                }

            }

            insertAndUpdate.executeBatch();
            updateLastHarvest.executeBatch();

            LOGGER.info("Added/updated a total of " + counter + " bulk sources from Sparql endpoint query.");

        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#addSource(HarvestSourceDTO source)
     */
    @Override
    public Integer addSource(HarvestSourceDTO source) throws DAOException {

        Connection conn = null;
        try {
            // execute the insert statement
            conn = getSQLConnection();

            int o = addSource(conn, source);

            return new Integer(o);
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }

    @Override
    public Integer addSource(Connection conn, HarvestSourceDTO source) throws DAOException {

        if (source == null) {
            throw new IllegalArgumentException("Harvest source DTO must not be null!");
        }

        String sourceUrl = source.getUrl();
        if (StringUtils.isBlank(sourceUrl)) {
            throw new IllegalArgumentException("Source URL must not be blank!");
        }

        // Normalize URL before save.
        String url = URLUtil.normalizeHarvestSourceUrl(sourceUrl, false);
        long urlHash = Hashes.spoHash(url);

        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(ADD_SOURCE_SQL);

            ps.setString(1, url);
            ps.setLong(2, Long.valueOf(urlHash));
            ps.setString(3, source.getEmails());
            ps.setInt(4, Integer.valueOf(source.getIntervalMinutes()));
            ps.setString(5, YesNoBoolean.format(source.isPrioritySource()));
            if (StringUtils.isEmpty(source.getOwner())) {
                ps.setString(6, "harvester");
            } else {
                ps.setString(6, source.getOwner());
            }
            ps.setString(7, source.getMediaType());
            ps.setString(8, YesNoBoolean.format(source.isSparqlEndpoint()));
            ps.setString(9, YesNoBoolean.format(source.isOnlineCsvTsv()));
            ps.setString(10, source.getCsvTsvUrl());

            ps.executeUpdate();
            ps = conn.prepareStatement("select identity_value()");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new CRException("No auto-generated keys returned!");
            }
        } catch (Exception e) {
            throw new DAOException(e.toString(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    throw new DAOException(e.toString(), e);
                }
            }
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

    @Override
    public void addSourceIgnoreDuplicate(Connection conn, HarvestSourceDTO source) throws DAOException {
        addSource(conn, source);
    }

    /** */
    private static final String DELETE_HARVEST_SOURCES = "delete from HARVEST_SOURCE where URL_HASH=?";
    /** */
    private static final String DELETE_FROM_URGENT_HARVEST_QUEUE = "delete from URGENT_HARVEST_QUEUE where URL=?";
    /** */
    private static final String DELETE_FROM_RULESET = "DB.DBA.rdfs_rule_set (?, ?, 1)";

    /** delete post harvest scripts of the source. */
    private static final String DELETE_HARVEST_SCRIPTS = "DELETE FROM post_harvest_script WHERE target_source_url = ?";

    /**
     * Removes HARVEST_SOURCE records matching the given URLs + all related records in other tables.
     *
     * @param conn
     *            The SQL connection to use.
     * @param sourceUrls
     *            The URLs in question.
     * @throws SQLException
     *             Any sort of SQL exception.
     */
    private void removeHarvestSources(Connection conn, Collection<String> sourceUrls) throws SQLException {

        // We need to clear all references in:
        // - harvest messages table (cascade deleted)
        // - harvests table (cascade deleted)
        // - harvest sources table
        // - urgent harvest queue
        // - inference rule-set
        // - post-harvest scripts

        PreparedStatement sourcesDeleteStatement = null;
        PreparedStatement urgentQueueDeleteStatement = null;
        PreparedStatement rulesetDeleteStatement = null;
        PreparedStatement harvestScriptsDeleteStatement = null;

        try {
            sourcesDeleteStatement = conn.prepareStatement(DELETE_HARVEST_SOURCES);
            urgentQueueDeleteStatement = conn.prepareStatement(DELETE_FROM_URGENT_HARVEST_QUEUE);
            harvestScriptsDeleteStatement = conn.prepareStatement(DELETE_HARVEST_SCRIPTS);

            if (GeneralConfig.isUseInferencing()) {
                rulesetDeleteStatement = conn.prepareStatement(DELETE_FROM_RULESET);
            }

            for (String sourceUrl : sourceUrls) {

                long urlHash = Hashes.spoHash(sourceUrl);

                sourcesDeleteStatement.setLong(1, urlHash);
                urgentQueueDeleteStatement.setString(1, sourceUrl);
                harvestScriptsDeleteStatement.setString(1, sourceUrl);

                if (GeneralConfig.isUseInferencing()) {
                    rulesetDeleteStatement.setString(1, GeneralConfig.getRequiredProperty(GeneralConfig.VIRTUOSO_CR_RULESET_NAME));
                    rulesetDeleteStatement.setString(2, sourceUrl);
                }

                harvestScriptsDeleteStatement.addBatch();
                sourcesDeleteStatement.addBatch();
                urgentQueueDeleteStatement.addBatch();
                if (GeneralConfig.isUseInferencing()) {
                    rulesetDeleteStatement.addBatch();
                }
            }

            LOGGER.debug("Deleting requested sources from harvest scripts table.");
            harvestScriptsDeleteStatement.executeBatch();
            LOGGER.debug("Deleting requested sources from harvest source table.");
            sourcesDeleteStatement.executeBatch();
            LOGGER.debug("Deleting requested sources from urgent harvest queue.");
            urgentQueueDeleteStatement.executeBatch();
            if (GeneralConfig.isUseInferencing()) {
                LOGGER.debug("Deleting requested sources from inference ruleset.");
                rulesetDeleteStatement.executeBatch();
            }
        } finally {
            SQLUtil.close(sourcesDeleteStatement);
            SQLUtil.close(urgentQueueDeleteStatement);
            SQLUtil.close(rulesetDeleteStatement);
            SQLUtil.close(harvestScriptsDeleteStatement);
        }
    }

    /**
     *
     * @param conn
     * @param sourceUrls
     * @param harvesterContextOnly
     * @throws SQLException
     */
    private void removeResources(Connection conn, Collection<String> sourceUrls, boolean harvesterContextOnly)
            throws SQLException {
        removeResources(conn, sourceUrls, null, harvesterContextOnly);
    }


    /**
     * Helper method for clearing all triples about those URLs.
     *
     * @param conn SQL connection to operate with.
     * @param sourceUrls The URLs in question.
     * @param harvesterContextOnly
     * @throws SQLException
     */
    private void removeResources(Connection conn, Collection<String> sourceUrls, Set<String> exceptPredicates, boolean harvesterContextOnly)
            throws SQLException {

        String sparqlDeleteResourceFromAllGraphs =
                "SPARQL DELETE {GRAPH ?g {?s ?p ?o}} WHERE {GRAPH ?g {?s ?p ?o filter (?s = <RESOURCE_URI>@exceptPredicates@)}}";

        String sparqlDeleteResourceFromSpecificGraph =
                "SPARQL DELETE FROM <GRAPH_URI> {?s ?p ?o} WHERE {GRAPH <GRAPH_URI> {?s ?p ?o filter (?s = <RESOURCE_URI>@exceptPredicates@)}}";

        if (exceptPredicates != null && !exceptPredicates.isEmpty()) {
            StringBuilder sb = new StringBuilder(" AND ?p NOT IN (");
            int i = 0;
            for (String exceptPredicate : exceptPredicates) {
                if (i++ > 0) {
                    sb.append(", ");
                }
                sb.append(String.format("<%s>", exceptPredicate));
            }
            sb.append(")");

            sparqlDeleteResourceFromAllGraphs = sparqlDeleteResourceFromAllGraphs.replace("@exceptPredicates@", sb.toString());
            sparqlDeleteResourceFromSpecificGraph = sparqlDeleteResourceFromSpecificGraph.replace("@exceptPredicates@", sb.toString());
        } else {
            sparqlDeleteResourceFromAllGraphs = sparqlDeleteResourceFromAllGraphs.replace("@exceptPredicates@", "");
            sparqlDeleteResourceFromSpecificGraph = sparqlDeleteResourceFromSpecificGraph.replace("@exceptPredicates@", "");
        }

        Statement resourceStmt = null;
        try {
            resourceStmt = conn.createStatement();

            for (String sourceUrl : sourceUrls) {

                sourceUrl = URLUtil.escapeIRI(sourceUrl);

                String resourceSparql;
                if (harvesterContextOnly) {
                    resourceSparql = sparqlDeleteResourceFromSpecificGraph.replace("GRAPH_URI", GeneralConfig.HARVESTER_URI).replace("RESOURCE_URI", sourceUrl);
                    LOGGER.debug("Deleting this resource from harvester graph");
                } else {
                    LOGGER.debug("Deleting resource from all graphs");
                    resourceSparql = sparqlDeleteResourceFromAllGraphs.replace("RESOURCE_URI", sourceUrl);
                }


                resourceStmt.addBatch(resourceSparql);
            }

            resourceStmt.executeBatch();
        } finally {
            SQLUtil.close(resourceStmt);
        }
    }

    /**
     *
     * @param conn
     * @param sourceUrls
     * @throws SQLException
     */
    private void removeGraphs(Connection conn, Collection<String> sourceUrls) throws SQLException {

        String sparqlClearGraph = "SPARQL CLEAR GRAPH <GRAPH_URI>";

        Statement graphStmt = null;
        try {
            graphStmt = conn.createStatement();

            for (String sourceUrl : sourceUrls) {

                sourceUrl = URLUtil.escapeIRI(sourceUrl);
                String graphSparql = sparqlClearGraph.replace("GRAPH_URI", sourceUrl);
                graphStmt.addBatch(graphSparql);
            }

            LOGGER.debug("Clearing graphs: " + sourceUrls);

            graphStmt.executeBatch();
        } finally {
            SQLUtil.close(graphStmt);
        }
    }

    /** */
    private static final String EDIT_SOURCE_SQL = "update HARVEST_SOURCE set URL=?, URL_HASH=?, EMAILS=?, INTERVAL_MINUTES=?,"
            + " PRIORITY_SOURCE=?, SOURCE_OWNER=?, MEDIA_TYPE=?, IS_SPARQL_ENDPOINT=? where HARVEST_SOURCE_ID=?";

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
        values.add(YesNoBoolean.format(source.isSparqlEndpoint()));
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
    private static final String GET_SOURCES_BY_ID_SQL = "select * from HARVEST_SOURCE where HARVEST_SOURCE_ID=?";

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#getHarvestSourceById(java.lang.Integer)
     */
    @Override
    public HarvestSourceDTO getHarvestSourceById(Integer harvestSourceID) throws DAOException {

        List<Object> values = new ArrayList<Object>();
        values.add(harvestSourceID);
        List<HarvestSourceDTO> list = executeSQL(GET_SOURCES_BY_ID_SQL, values, new HarvestSourceDTOReader());
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }

    /** */
    private static final String GET_SOURCES_BY_URL_SQL = "select "
            + "url, harvest_source_id, "
            + "url_hash, emails, time_created, statements, count_unavail,"
            + "cast(\"last_harvest\" as varchar) as last_harvest,interval_minutes,source, last_modified,"
            + "gen_time,last_harvest_failed,priority_source,source_owner,permanent_error,media_type,last_harvest_id,is_sparql_endpoint,delete_requested,delete_flag, is_online_csv_tsv, csv_tsv_url "
            + " from HARVEST_SOURCE where URL_HASH=?";

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#getHarvestSourceByUrl(java.lang.String)
     */
    @Override
    public HarvestSourceDTO getHarvestSourceByUrl(String url) throws DAOException {

        List<Object> values = new ArrayList<Object>();
        values.add(Long.valueOf(Hashes.spoHash(url)));
        List<HarvestSourceDTO> list = executeSQL(GET_SOURCES_BY_URL_SQL, values, new HarvestSourceDTOReader());
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }

    /** */
    private static final String GET_NEXT_SCHEDULED_SOURCES_SQL =
            "select top <limit> *, cast(abs(datediff('second', now(), coalesce(LAST_HARVEST, dateadd('second', -1*INTERVAL_MINUTES*60, TIME_CREATED)))) as float) / cast(INTERVAL_MINUTES*60 as float) as urgency " +
            "from HARVEST_SOURCE where COUNT_UNAVAIL < 5 and INTERVAL_MINUTES > 0 " +
            "and <seconds_since_last_harvest> >= <harvest_interval_seconds> " +
            "and ( IS_ONLINE_CSV_TSV IS NULL or IS_ONLINE_CSV_TSV = 'N' ) " +
            "ORDER BY urgency DESC";

    /** */
    private static final String SECONDS_SINCE_LAST_HARVEST_EXPR = "cast("
            + "abs(datediff('second', now(), coalesce(LAST_HARVEST, dateadd('second', -1*INTERVAL_MINUTES*60, TIME_CREATED)))) "
            + "as float)";

    /** */
    private static final String HARVEST_INTERVAL_SECONDS_EXPR = "cast(INTERVAL_MINUTES*60 as float)";

    private static final String GET_NEXT_SCHEDULED_ONLINE_SOURCES_SQL =
            "select top <limit> *, cast(abs(datediff('second', now(), coalesce(LAST_HARVEST, dateadd('second', -1*INTERVAL_MINUTES*60, TIME_CREATED)))) as float) / cast(INTERVAL_MINUTES*60 as float) as urgency " +
            "from HARVEST_SOURCE where IS_ONLINE_CSV_TSV = 'Y' " +
            "and INTERVAL_MINUTES > 0 " +
            "and <seconds_since_last_harvest> >= <harvest_interval_seconds> " +
            "ORDER BY urgency DESC";

    /*
     * (non-Javadoc)
     *
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

    /**
     *
     * @param limit
     *            - max number of sources to return.
     * @return
     * @throws DAOException
     */
    @Override
    public List<HarvestSourceDTO> getNextScheduledOnlineCsvTsv(int limit) throws DAOException {

        if (limit < 1) {
            throw new IllegalArgumentException("Limit must be >=1");
        }

        String query = GET_NEXT_SCHEDULED_ONLINE_SOURCES_SQL.replace("<limit>", String.valueOf(limit));
        query = query.replace("<seconds_since_last_harvest>", SECONDS_SINCE_LAST_HARVEST_EXPR);
        query = query.replace("<harvest_interval_seconds>", HARVEST_INTERVAL_SECONDS_EXPR);
        return executeSQL(query, Collections.EMPTY_LIST, new HarvestSourceDTOReader());
    }

    /** */
    private static final String INCREASE_UNAVAIL_COUNT =
            "update HARVEST_SOURCE set COUNT_UNAVAIL=(COUNT_UNAVAIL+1) where URL_HASH=?";

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#increaseUnavailableCount(java.lang.String)
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
    private static final String GET_MOST_URGENT_HARVEST_SOURCES =
            "select top <limit> * from HARVEST_SOURCE where COUNT_UNAVAIL < 5"
                    + " and INTERVAL_MINUTES > 0 order by (<seconds_since_last_harvest> / <harvest_interval_seconds>) desc";

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#getMostUrgentHarvestSources(int)
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
                    + " LAST_HARVEST_FAILED=?, PRIORITY_SOURCE=?, SOURCE_OWNER=?, PERMANENT_ERROR=?, LAST_HARVEST_ID=?, LAST_MODIFIED=? "
                    + "where URL_HASH=?";

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#updateSourceHarvestFinished(eionet.cr.dto.HarvestSourceDTO)
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
        values.add(sourceDTO.getLastHarvestId());
        values.add(sourceDTO.getLastModified());
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

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#removeHarvestSources(java.util.Collection)
     */
    @Override
    public void removeHarvestSources(Collection<String> sourceUrls) throws DAOException {
        removeHarvestSources(sourceUrls, true);
    }

    @Override
    public void removeHarvestSources(Collection<String> sourceUrls, boolean harvesterContextOnly) throws DAOException {
        removeHarvestSources(sourceUrls, harvesterContextOnly, true);
    }

    @Override
    public void removeHarvestSources(
            Collection<String> sourceUrls, boolean harvesterContextOnly, boolean clearGraphs) throws DAOException {
        removeHarvestSources(sourceUrls, null, harvesterContextOnly, true);
    }


    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#removeHarvestSources(java.util.Collection, boolean)
     */
    @Override
    public void removeHarvestSources(Collection<String> sourceUrls,
                                     Set<String> exceptPredicates, boolean harvesterContextOnly, boolean clearGraphs) throws DAOException {

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

            removeResources(sqlConn, sourceUrls, exceptPredicates, harvesterContextOnly);
            removeHarvestSources(sqlConn, sourceUrls);

            if (clearGraphs) {
                removeGraphs(sqlConn, sourceUrls);
            }
        } catch (RepositoryException e) {
            throw new DAOException("Repository exception when deleting sources", e);
        } catch (SQLException e) {
            throw new DAOException("SQLException when deleting sources", e);
        } finally {
            SesameUtil.close(repoConn);
            SQLUtil.close(sqlConn);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.virtuoso.VirtuosoBaseDAO#clearGraph(java.lang.String)
     */
    @Override
    public void clearGraph(String graphUri) throws DAOException {

        if (StringUtils.isBlank(graphUri)) {
            return;
        }

        Connection sqlConn = null;
        try {
            sqlConn = getSQLConnection();
            SQLUtil.executeUpdate("sparql clear graph <" + graphUri + ">", sqlConn);
        } catch (SQLException e) {
            throw new DAOException("Exception when clearing graph: e", e);
        } finally {
            SQLUtil.close(sqlConn);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#getSourcesInInferenceRules()
     *
     * @Deprecated Inferencing is removed from CR
     */
    @Override
    @Deprecated
    public String getSourcesInInferenceRules() throws DAOException {

        Connection conn = null;
        ResultSet rs = null;
        String ret = "";
        PreparedStatement stmt = null;
        if (GeneralConfig.isUseInferencing()) {
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
                SQLUtil.close(rs);
                SQLUtil.close(stmt);
                SQLUtil.close(conn);
            }
        }
        return ret;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#isSourceInInferenceRule()
     *
     * @Deprecated Inferencing is removed from CR
     */
    @Override
    @Deprecated
    public boolean isSourceInInferenceRule(String url) throws DAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        boolean ret = false;
        if (GeneralConfig.isUseInferencing()) {
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
                SQLUtil.close(rs);
                SQLUtil.close(stmt);
                SQLUtil.close(conn);
            }
        }
        return ret;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#addSourceIntoInferenceRule()
     *
     * @Deprecated Inferencing is removed from CR
     */
    @Override
    @Deprecated
    public boolean addSourceIntoInferenceRule(String url) throws DAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        boolean ret = false;

        if (GeneralConfig.isUseInferencing()) {
            try {
                conn = SesameUtil.getSQLConnection();
                stmt = conn.prepareStatement("DB.DBA.rdfs_rule_set (?, ?)");
                stmt.setString(1, GeneralConfig.getRequiredProperty(GeneralConfig.VIRTUOSO_CR_RULESET_NAME));
                stmt.setString(2, url);
                ret = stmt.execute();

            } catch (Exception e) {
                throw new DAOException(e.getMessage(), e);
            } finally {
                SQLUtil.close(stmt);
                SQLUtil.close(conn);
            }
        } else {
            ret = true;
        }
        return ret;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#removeSourceFromInferenceRule()
     *
     * @Deprecated Inference is removed from CR
     */
    @Deprecated
    @Override
    public boolean removeSourceFromInferenceRule(String url) throws DAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        boolean ret = false;

        if (GeneralConfig.isUseInferencing()) {
            try {
                conn = SesameUtil.getSQLConnection();
                stmt = conn.prepareStatement("DB.DBA.rdfs_rule_set (?, ?, 1)");
                stmt.setString(1, GeneralConfig.getRequiredProperty(GeneralConfig.VIRTUOSO_CR_RULESET_NAME));
                stmt.setString(2, url);
                ret = stmt.execute();
            } catch (Exception e) {
                throw new DAOException(e.getMessage(), e);
            } finally {
                SQLUtil.close(stmt);
                SQLUtil.close(conn);
            }
        } else {
            ret = true;
        }

        return ret;
    }

    /**
     * See {@link HarvestSourceDAO#loadContentNaive(InputStream, RDFFormat, String, boolean)}.
     *
     * @param inputStream
     * @param rdfFormat
     * @param graphUrl
     * @param clearPreviousGraphContent
     * @return
     * @throws IOException
     * @throws OpenRDFException
     *
     * {@link Deprecated}
     */
    @Override
    public int loadContentNaive(
            InputStream inputStream, RDFFormat rdfFormat, String graphUrl, boolean clearPreviousGraphContent)
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

    /*
     * (non-Javadoc)
     *
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
     * @see eionet.cr.dao.HarvestSourceDAO#insertUpdateSourceMetadata(java.lang.String, java.lang.String, eionet.cr.dto.ObjectDTO[])
     */
    @Override
    public void insertUpdateSourceMetadata(String subject, String predicate, ObjectDTO... object) throws DAOException,
            RepositoryException, IOException {
        RepositoryConnection conn = null;
        try {
            conn = SesameUtil.getRepositoryConnection();

            insertUpdateSourceMetadata(conn, subject, predicate, object);
        } finally {
            SesameUtil.close(conn);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#insertUpdateSourceMetadata(org.openrdf.repository.RepositoryConnection, java.lang.String,
     * java.lang.String, eionet.cr.dto.ObjectDTO[])
     */
    @Override
    public void insertUpdateSourceMetadata(RepositoryConnection conn, String subject, String predicate, ObjectDTO... object)
            throws RepositoryException {

        URI harvesterContext = conn.getValueFactory().createURI(GeneralConfig.HARVESTER_URI);
        URI sub = conn.getValueFactory().createURI(subject);
        URI pred = conn.getValueFactory().createURI(predicate);

        conn.remove(sub, pred, null, harvesterContext);
        for (ObjectDTO obj : object) {
            if (obj.isLiteral()) {
                Literal literalObject = conn.getValueFactory().createLiteral(obj.toString(), obj.getDatatype());
                conn.add(sub, pred, literalObject, harvesterContext);
            } else {
                URI resourceObject = conn.getValueFactory().createURI(obj.toString());
                conn.add(sub, pred, resourceObject, harvesterContext);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#deleteSubjectTriplesInSource(java.lang.String, java.lang.String)
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

    /*
     * (non-Javadoc)
     *
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
    private static final String NEW_SOURCES_SPARQL = "PREFIX cr: "
            + "<http://cr.eionet.europa.eu/ontologies/contreg.rdf#> SELECT ?s FROM ?sourceUrl FROM ?deploymentHost WHERE "
            + "{ ?s a cr:File . OPTIONAL { ?s cr:lastRefreshed ?refreshed } FILTER( !BOUND(?refreshed)) }";

    /*
     * (non-Javadoc)
     *
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
            reader.setBlankNodeUriPrefix(VirtuosoBaseDAO.N3_BNODE_PREFIX);
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
     * { @inheritDoc }.
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateHarvestedStatementsTriple(String sourceUri) throws DAOException {
        String sourceCountQuery = "SELECT COUNT(*) FROM ?sourceUri WHERE {?s ?p ?o}";
        Bindings bindings = new Bindings();
        bindings.setURI("sourceUri", sourceUri);

        String result = executeUniqueResultSPARQL(sourceCountQuery, bindings, new SingleObjectReader<String>());
        int statementsInt = Integer.parseInt(result);

        RepositoryConnection repoConn = null;
        try {
            repoConn = SesameUtil.getRepositoryConnection();
            ValueFactory valueFactory = repoConn.getValueFactory();

            URI subjectResource = valueFactory.createURI(sourceUri);
            URI harvestedStatements = valueFactory.createURI(Predicates.CR_HARVESTED_STATEMENTS);
            URI harvesterContext = valueFactory.createURI(GeneralConfig.HARVESTER_URI);
            Value statements = valueFactory.createLiteral(statementsInt);

            repoConn.remove(subjectResource, harvestedStatements, null, harvesterContext);

            repoConn.add(subjectResource, harvestedStatements, statements, harvesterContext);

        } catch (RepositoryException e) {
            throw new DAOException(e.toString(), e);
        } finally {
            SesameUtil.close(repoConn);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#loadContent(java.util.Map, java.lang.String)
     */
    @Override
    public int loadContent(Map<File, ContentLoader> filesAndLoaders, String graphUri) throws DAOException {

        LOGGER.debug(BaseHarvest.loggerMsg("Starting content loading ...", graphUri));

        // Prepare connections (repository and SQL).

        RepositoryConnection repoConn = null;
        Connection sqlConn = null;
        boolean noExceptions = false;
        try {
            repoConn = SesameUtil.getRepositoryConnection();
            sqlConn = SesameUtil.getSQLConnection();
            noExceptions = true;
        } catch (RepositoryException e) {
            throw new DAOException("Creating repository connection failed", e);
        } catch (SQLException e) {
            throw new DAOException("Creating SQL connection failed", e);
        } finally {
            if (!noExceptions) {
                SesameUtil.close(repoConn);
                SQLUtil.close(sqlConn);
            }
        }

        int currentTriplesCount = getNoOfHarvestedStatements(sqlConn, graphUri);
        int xoringThresholdTriplesCount = GeneralConfig.getIntProperty(
                GeneralConfig.HARVESTER_SKIP_XORING_NOOFTRIPLES_THRESHOLD, 1000000000);
        long fileSize = getFileSizesSum(filesAndLoaders.keySet());
        long fileSizeThreshold = GeneralConfig.getLongProperty(
                GeneralConfig.HARVESTER_SKIP_XORING_FILESIZE_BYTES_THRESHOLD, 1000000000000L);
        boolean isDirectLoading = currentTriplesCount > xoringThresholdTriplesCount || fileSize > fileSizeThreshold;

        LOGGER.debug(BaseHarvest.loggerMsg(String.format(
                "Current triples count = %d, file size = %d, direct loading flag = %s",
                currentTriplesCount, fileSize, String.valueOf(isDirectLoading).toUpperCase()), graphUri));

        // Prepare URI objects of the original graph, backup graph and temporary graph.

        URI graphResource = repoConn.getValueFactory().createURI(graphUri);
        String tempGraphUri = graphUri + TEMP_GRAPH_SUFFIX;
        URI tempGraphResource = repoConn.getValueFactory().createURI(tempGraphUri);

        // Start loading steps.

        int triplesLoaded = 0;
        boolean wasOrigEmpty = false;
        try {
            // Ensure auto-commit.
            forceLogEnable(3, sqlConn, LOGGER);

            // Clear potential temporary graph leftover from previous harvest.
            clearGraph(sqlConn, tempGraphUri, "Clearing leftovers of previous TEMP graph", false);

            // Load the content into the temporary graph, but be sure to use the "original" graph URI
            // as the base URI for resolving any relative identifiers in the content.
            try {

                if (isDirectLoading) {
                    clearGraph(sqlConn, graphUri, "Clearing ORIGINAL graph", false);
                } else {
                    // We need to know if the "original" graph is empty.
                    wasOrigEmpty = isGraphEmpty(sqlConn, graphResource);
                }

                // Prepare base URI for resolving relative URIs and also the target graph where the triples will be loaded into.
                // The latter is either "original" or temporary graph, depending on whether original is empty or not.
                String baseUri = graphUri;
                String targetGraphUri = isDirectLoading || wasOrigEmpty ? graphUri : tempGraphUri;
                LOGGER.debug(BaseHarvest.loggerMsg(String.format("Going to load triples into %s graph",
                        isDirectLoading || wasOrigEmpty ? "ORIGINAL" : "TEMP"), graphUri));

                // Ensure auto-commit, as Virtuoso tends to forget it at long harvests.
                forceLogEnable(3, sqlConn, LOGGER);

                for (Entry<File, ContentLoader> entry : filesAndLoaders.entrySet()) {

                    File file = entry.getKey();
                    ContentLoader loader = entry.getValue();

                    if (loader instanceof RDFFormatLoader) {
                        RDFFormat rdfFormat = ((RDFFormatLoader) loader).getRdfFormat();
                        loadRdfFile(file, rdfFormat, sqlConn, baseUri, targetGraphUri);
                    } else {
                        InputStream inputStream = null;
                        try {
                            inputStream = new FileInputStream(file);

                            // Essential to set auto-commit to false, cause' otherwise lazy-loading will cause
                            // "Too many open statements".
                            // Read more about lazy-loading in the JavaDocs of virtuoso.sesame2.driver.VirtuosoRepository
                            // and eionet.cr.util.sesame.SesameConnectionProvider.java.createRepository().
                            repoConn.setAutoCommit(false);
                            triplesLoaded += loader.load(inputStream, repoConn, sqlConn, baseUri, targetGraphUri);
                            repoConn.commit();
                            repoConn.setAutoCommit(true);
                        } finally {
                            IOUtils.closeQuietly(inputStream);
                        }
                    }
                }

                if (!isDirectLoading && !wasOrigEmpty) {

                    // Log the number of triples loaded into TEMP graph.
                    boolean isTempEmpty = isGraphEmpty(sqlConn, tempGraphResource);
                    LOGGER.debug(BaseHarvest.loggerMsg(
                            String.format("TEMP graph %sempty", isTempEmpty ? "" : "not "), tempGraphUri));

                    if (!isTempEmpty) {
                        // XOR the temporary and original graphs.
                        LOGGER.debug("XOR-ing <" + tempGraphUri + " with <" + graphUri + ">");
                        synchronizeGraphs(sqlConn, graphResource, tempGraphResource);
                    } else {
                        clearGraph(sqlConn, graphUri, "Clearing ORIGINAL graph since TEMP graph is empty", true);
                    }
                }

            } catch (Exception e) {

                // The repository connection rollback is ignored if the Virtuoso connection URL has log_enable=2 or log_enable=3.
                SesameUtil.rollback(repoConn);

                // Clean-up attempt
                if (!isDirectLoading && wasOrigEmpty) {
                    clearGraph(sqlConn, graphUri, "Clearing ORIGINAL graph after failed content loading", true);
                }

                throw new DAOException("Failed content loading of " + graphUri, e);

            } finally {
                if (!isDirectLoading && !wasOrigEmpty) {
                    forceLogEnable(3, sqlConn, LOGGER);
                    clearGraph(sqlConn, tempGraphUri, "Clearing TEMP graph", true);
                }
            }

            // Get the total number of triples in the loaded graph
            triplesLoaded = getGraphTriplesCount(sqlConn, graphResource);

        } finally {
            // Ensure connections will be closed regardless of success or exceptions.
            SQLUtil.close(sqlConn);
            SesameUtil.close(repoConn);
        }

        // Return the number of triples loaded into the graph.
        return triplesLoaded;
    }

    /**
     * Gets the {@link Predicates#CR_HARVESTED_STATEMENTS} value for given subject.
     * Defaults to -1, if not found.
     *
     * @param sqlConn
     * @param subjectUri
     * @return
     */
    private int getNoOfHarvestedStatements(Connection sqlConn, String subjectUri) throws DAOException {

        String sql = String.format("SPARQL SELECT ?o FROM <%s> WHERE {<%s> <%s> ?o} ORDER BY DESC(?o) LIMIT 1",
                GeneralConfig.HARVESTER_URI, subjectUri, Predicates.CR_HARVESTED_STATEMENTS);
        try {
            Object o = SQLUtil.executeSingleReturnValueQuery(sql, sqlConn);
            return o == null ? -1 : NumberUtils.toInt(o.toString(), -1);
        } catch (Exception e) {
            throw new DAOException("Failed to get number of harvested statements for " + subjectUri, e);
        }
    }

    /**
     *
     * @param sqlConn
     * @param graphResource
     * @return
     * @throws DAOException
     */
    private int getGraphTriplesCount(Connection sqlConn, URI graphResource) throws DAOException {

        try {
            String sql = "sparql select count(*) from <GRAPH_URI> where {?s ?p ?o}";
            sql = sql.replace("GRAPH_URI", graphResource.stringValue());
            Object o = SQLUtil.executeSingleReturnValueQuery(sql, sqlConn);
            return Integer.parseInt(o.toString());
        } catch (Exception e) {
            throw new DAOException("Failed to get triples count for <" + graphResource.stringValue() + ">", e);
        }
    }

    /**
     *
     * @param sqlConn
     * @param graphResource
     * @return
     * @throws SQLException
     */
    private boolean isGraphEmpty(Connection sqlConn, URI graphResource) throws SQLException {

        String qry = String.format("sparql select 1 from <%s> where {?s ?p ?o} limit 1", graphResource);
        Object o = SQLUtil.executeSingleReturnValueQuery(qry, sqlConn);
        boolean result = o == null || !o.toString().equals("1");
        return result;
    }

    /**
     *
     * @param files
     * @return
     */
    private boolean fileSizeExceedsXoringThreshold(Collection<File> files) {

        boolean result = false;

        long fileSizeThreshold = GeneralConfig.getLongProperty(
                GeneralConfig.HARVESTER_SKIP_XORING_FILESIZE_BYTES_THRESHOLD, 1000000000000L);

        for (File file : files) {
            long fileSize = file.length();
            if (fileSize > fileSizeThreshold) {
                result = true;
                break;
            }
        }

        return result;
    }

    /**
     *
     * @param files
     * @return
     */
    private long getFileSizesSum(Collection<File> files) {

        long result = 0L;

        for (File file : files) {
            result = result + file.length();
        }

        return result;
    }

    /**
     * Replace graph URI with new one.
     *
     * @param sqlConn
     *            Connection.
     * @param oldGraph
     *            Existing graph URI.
     * @param newGraph
     *            The new URI of the graph.
     * @param message
     *            Log message written into log file.
     * @throws DAOException
     *             Database error.
     */
    private void renameGraph(Connection sqlConn, URI oldGraph, URI newGraph, String message) throws DAOException {

        String logMessage = BaseHarvest.loggerMsg(message, newGraph.stringValue());
        try {
            LOGGER.debug(logMessage);

            // We're not using prepared statement here, because its imply does not with graph rename query for some reason.
            String sql =
                    RENAME_GRAPH_SQL.replace("%old_graph%", oldGraph.stringValue()).replaceFirst("%new_graph%",
                            newGraph.stringValue());
            Statement stmt = null;
            try {
                long startTime = System.currentTimeMillis();

                stmt = sqlConn.createStatement();
                stmt.execute(sql);

                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Renaming graph took " + Util.durationSince(startTime) + "; sql=" + sql);
                }
            } finally {
                SQLUtil.close(stmt);
            }
        } catch (Exception e) {
            throw new DAOException("Failed to rename graph. " + logMessage, e);
        }
    }

    /**
     *
     * @param sqlConn
     * @param permGraph
     * @param tempGraph
     * @throws DAOException
     */
    private void synchronizeGraphs(Connection sqlConn, URI permGraph, URI tempGraph) throws DAOException {

        String tempGraphStr = tempGraph.stringValue();
        String permGraphStr = permGraph.stringValue();

        Statement stmt = null;
        try {
            String sparul1 = GRAPH_SYNC_SPARUL1.replace("%perm_graph%", permGraphStr);
            sparul1 = sparul1.replace("%temp_graph%", tempGraphStr);
            stmt = sqlConn.createStatement();
            LOGGER.debug(BaseHarvest.loggerMsg("Executing 1st XOR query", permGraph.stringValue()));
            stmt.execute("log_enable(2,1)");
            stmt.execute("SPARQL " + sparul1);
            SQLUtil.close(stmt);

            String sparul2 = GRAPH_SYNC_SPARUL2.replace("%perm_graph%", permGraphStr);
            sparul2 = sparul2.replace("%temp_graph%", tempGraphStr);
            stmt = sqlConn.createStatement();
            LOGGER.debug(BaseHarvest.loggerMsg("Executing 2nd XOR query", permGraph.stringValue()));
            stmt.execute("log_enable(2,1)");
            stmt.execute("SPARQL " + sparul2);

        } catch (Exception e) {
            throw new DAOException("Failed to XOR <" + tempGraphStr + "> with <" + permGraphStr + ">", e);
        } finally {
            SQLUtil.close(stmt);
        }
    }

    /**
     * Deletes the given graph from repository by executing SPARQL "clear graph" statement.
     *
     * @param sqlConn
     *            SQL Connection to be used when deleting the graph.
     * @param graphUri
     *            Graph Uri to be deleted.
     * @param message
     *            Log message written into log file.
     * @param failSafely
     *            if true, then the Exception will not be thrown and it is only logged on warning level.
     * @return true if the graph was successfully deleted from repository.
     * @throws DAOException
     *             if clear graph failed and the method don't have to fail safely.
     */
    private boolean clearGraph(Connection sqlConn, String graphUri, String message, boolean failSafely) throws DAOException {

        boolean graphCleared = false;
        String logMessage = BaseHarvest.loggerMsg(message, graphUri);
        String graphEmptyCheckerQuery = String.format("sparql select 1 from <%s> where {?s ?p ?o} limit 1", graphUri);

        try {
            Object rslt = SQLUtil.executeSingleReturnValueQuery(graphEmptyCheckerQuery, sqlConn);
            boolean isGraphEmpty = rslt == null || !rslt.toString().equals("1");
            if (isGraphEmpty) {
                graphCleared = true;
            } else {
                LOGGER.debug(logMessage);
                SQLUtil.executeUpdate("sparql clear graph <" + graphUri + ">", sqlConn);
                graphCleared = true;
            }
        } catch (Exception e) {
            if (failSafely) {
                LOGGER.warn("Failed to clear graph. " + logMessage);
            } else {
                throw new DAOException("Failed to clear graph. " + logMessage, e);
            }
        }

        return graphCleared;
    }

    /**
     * Load given RDF file, using the fast DB.DBA.RDF_LOAD_RDFXML(file_open(f), graph, graph) and DB.DBA.TTLP(file_open(f), graph,
     * graph) functions. The file's exact syntax is determined by the given {@link RDFFormat}.
     *
     * @param file
     *            File to load.
     * @param rdfFormat
     *            The file's exact RDF format.
     * @param conn
     *            The SQL connection to use.
     * @param baseUri
     *            The base URI to for resolving relative URLs in the file.
     * @param contextUri
     *            The target graph where the the triples must be loaded into.
     * @throws SQLException
     *             In case database access error happens.
     */
    private void loadRdfFile(File file, RDFFormat rdfFormat, Connection conn, String baseUri, String contextUri)
            throws SQLException {

        String filePath = file.getAbsolutePath();
        if (filePath.startsWith("C:\\") || filePath.startsWith("C:/")) {
            filePath = filePath.substring(2).replace('\\', '/');
        }

        long fileSize = file.length();
        int fileSizeThreshold = GeneralConfig.getIntProperty(
                GeneralConfig.TRANSACTIONAL_LOADING_FILE_SIZE_THRESHOLD_BYTES, 500000000);
        int logMode = fileSize > fileSizeThreshold ? 0 : 1;

        int noOfThreads = GeneralConfig.getIntProperty(GeneralConfig.RDF_LOADER_THREADS, 1);
        String sql = String.format("DB.DBA.RDF_LOAD_RDFXML_MT(file_open('%s'), '%s', '%s', %d, %d, %d)",
                filePath, baseUri, contextUri, logMode, noOfThreads, logMode);

        if (!rdfFormat.equals(RDFFormat.RDFXML)) {

            if (rdfFormat.equals(RDFFormat.TRIG)) {
                // For TriG format we must raise the flag 256 (see http://docs.openlinksw.com/virtuoso/fn_ttlp.html).
                sql = "DB.DBA.TTLP(file_open('" + filePath + "'), '" +baseUri+ "', '"+contextUri+"', 256)";
            } else if (rdfFormat.equals(RDFFormat.NQUADS)) {
                // For N-Quads format we must raise the flag 512 (see http://docs.openlinksw.com/virtuoso/fn_ttlp.html).
                sql = "DB.DBA.TTLP(file_open('" + filePath + "'), '" +baseUri+ "', '"+contextUri+"', 512)";
            } else {
                // No flags for other cases.
                sql = "DB.DBA.TTLP(file_open('" + filePath + "'), '" +baseUri+ "', '"+contextUri+"', 0)";
            }
        }

        LOGGER.debug(BaseHarvest.loggerMsg("Executing file loading command: " + sql, ""));

        Statement s = null;
        try {
            s = conn.createStatement();
            long start = System.currentTimeMillis();
            s.execute(sql);

            long durationMinutes = (System.currentTimeMillis() - start) / 1000L / 60L;
            int durationThreshold = GeneralConfig.getIntProperty(
                    GeneralConfig.CHECKPOINT_LOADING_DURATION_THRESHOLD_MINUTES, 15);
            if (durationMinutes >= durationThreshold) {
                LOGGER.debug(BaseHarvest.loggerMsg(
                        "Making checkpoint, because loading lasted " + durationMinutes + " min", ""));
                s.execute("checkpoint");
            }

            s.close();
        } finally {
            SQLUtil.close(s);
        }
    }

    @Override
    public List<UrlAuthenticationDTO> getUrlAuthentications() throws DAOException {
        String query = "SELECT authurl_id, url_namestart, url_username, url_password FROM AUTHURL ORDER BY url_namestart";
        List<Object> inParams = new LinkedList<Object>();
        List<UrlAuthenticationDTO> list = executeSQL(query, inParams, new UrlAuthenticationDTOReader());
        return list;
    }

    @Override
    public UrlAuthenticationDTO getUrlAuthentication(int id) throws DAOException {

        String query = "SELECT authurl_id, url_namestart, url_username, url_password FROM AUTHURL WHERE authurl_id = ?";

        List<Object> inParams = new LinkedList<Object>();
        inParams.add(id);
        List<UrlAuthenticationDTO> list = executeSQL(query, inParams, new UrlAuthenticationDTOReader());

        if (list != null && list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    @Override
    public UrlAuthenticationDTO getUrlAuthentication(String fullUrl) throws DAOException {

        String queryExact = "SELECT authurl_id, url_namestart, url_username, url_password FROM authurl WHERE ? LIKE url_namestart";

        String queryBeginning =
                "SELECT authurl_id, url_namestart, url_username, url_password FROM authurl WHERE ? LIKE CONCAT(url_namestart, '%')";

        List<Object> inParams = new LinkedList<Object>();
        inParams.add(fullUrl.toLowerCase());
        List<UrlAuthenticationDTO> list = executeSQL(queryExact, inParams, new UrlAuthenticationDTOReader());

        if (list == null || list.size() == 0) {
            list = executeSQL(queryBeginning, inParams, new UrlAuthenticationDTOReader());
        }

        if (list != null && list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    /**
     * Insert a record into the the table of authentication data for harvest source urls.
     */
    private static final String ADD_AUTHURL = "INSERT INTO AUTHURL (url_namestart, url_username, url_password) VALUES (?,?,?)";
    private static final String EDIT_AUTHURL =
            "UPDATE AUTHURL set url_namestart=?, url_username=?, url_password=? WHERE authurl_id=?";

    @Override
    public int saveUrlAuthentication(UrlAuthenticationDTO urlAuthentication) throws DAOException {

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            if (urlAuthentication.getId() == 0) {
                ps = conn.prepareStatement(ADD_AUTHURL);
            } else {
                ps = conn.prepareStatement(EDIT_AUTHURL);
                ps.setInt(4, urlAuthentication.getId());
            }

            ps.setString(1, urlAuthentication.getUrlBeginning().toLowerCase());
            ps.setString(2, urlAuthentication.getUsername());
            ps.setString(3, urlAuthentication.getPassword());

            ps.executeUpdate();
            ps = conn.prepareStatement("select identity_value()");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new CRException("No auto-generated keys returned!");
            }
        } catch (Exception e) {
            throw new DAOException(e.toString(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    throw new DAOException(e.toString(), e);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#deleteUrlAuthentication(int)
     */
    @Override
    public void deleteUrlAuthentication(int id) throws DAOException {
        String query = "DELETE FROM AUTHURL WHERE authurl_id = ?";
        List<Object> inParams = new LinkedList<Object>();
        inParams.add(id);
        executeSQL(query, inParams, new UrlAuthenticationDTOReader());
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#getDistinctPredicates(java.lang.String, java.lang.String)
     */
    @Override
    public List<String> getDistinctPredicates(String graphUri, String typeUri) throws DAOException {

        String sparqlTemplate =
                "select distinct ?p from <@GRAPH_URI@> where {?s ?p ?o. ?s a <@TYPE_URI@>"
                        + " filter (?p!=<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>)}";

        RepositoryConnection repoConn = null;
        try {
            repoConn = SesameUtil.getRepositoryConnection();

            ValueFactory vf = repoConn.getValueFactory();
            String sparql = StringUtils.replaceOnce(sparqlTemplate, "@GRAPH_URI@", vf.createURI(graphUri).stringValue());
            sparql = StringUtils.replaceOnce(sparql, "@TYPE_URI@", vf.createURI(typeUri).stringValue());

            List<String> list = executeSPARQL(sparql, new SingleObjectReader<String>());
            return list;
        } catch (RepositoryException e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SesameUtil.close(repoConn);
        }
    }

    /**
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#getSourceAllDistinctPredicates(java.lang.String)
     */
    @Override
    public List<String> getSourceAllDistinctPredicates(String sourceUri) throws DAOException {
        String sparqlTemplate = "SELECT distinct ?p from <@SOURCE_URI@> where {?s ?p ?o} order by ?p";

        RepositoryConnection repoConn = null;
        try {
            repoConn = SesameUtil.getRepositoryConnection();

            ValueFactory vf = repoConn.getValueFactory();
            String sparql = StringUtils.replaceOnce(sparqlTemplate, "@SOURCE_URI@", vf.createURI(sourceUri).stringValue());

            List<String> list = executeSPARQL(sparql, new SingleObjectReader<String>());
            return list;
        } catch (RepositoryException e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SesameUtil.close(repoConn);
        }
    }

    /**
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#getTypeAllDistinctPredicates(java.lang.String)
     */
    @Override
    public List<String> getTypeAllDistinctPredicates(String typeUri) throws DAOException {
        String sparqlTemplate = "SELECT distinct ?p where {?s a <@TYPE_URI@> . ?s ?p ?o } order by ?p";

        RepositoryConnection repoConn = null;
        try {
            repoConn = SesameUtil.getRepositoryConnection();

            ValueFactory vf = repoConn.getValueFactory();
            String sparql = StringUtils.replaceOnce(sparqlTemplate, "@TYPE_URI@", vf.createURI(typeUri).stringValue());

            List<String> list = executeSPARQL(sparql, new SingleObjectReader<String>());
            return list;
        } catch (RepositoryException e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SesameUtil.close(repoConn);
        }
    }

}
