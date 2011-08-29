/*
 * The contents of this file are subject to the Mozilla Public
 *
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
 * Agency. Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency. All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti*/
package eionet.cr.dao.postgre;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openrdf.OpenRDFException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.readers.HarvestSourceDTOReader;
import eionet.cr.dao.virtuoso.VirtuosoBaseDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.harvest.statistics.dto.HarvestUrgencyScoreDTO;
import eionet.cr.harvest.statistics.dto.HarvestedUrlCountDTO;
import eionet.cr.util.Hashes;
import eionet.cr.util.Pair;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.Util;
import eionet.cr.util.YesNoBoolean;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.util.sql.SQLUtil;
import eionet.cr.util.sql.SingleObjectReader;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class PostgreSQLHarvestSourceDAO extends VirtuosoBaseDAO implements HarvestSourceDAO {

    private static final String GET_SOURCES_SQL =
        "SELECT * FROM HARVEST_SOURCE WHERE URL NOT IN (SELECT URL FROM REMOVE_SOURCE_QUEUE) ";
    private static final String SEARCH_SOURCES_SQL =
        "SELECT * FROM HARVEST_SOURCE WHERE URL like (?) AND URL NOT IN (SELECT URL FROM REMOVE_SOURCE_QUEUE) ";
    private static final String getHarvestSourcesFailedSQL =
        "SELECT * FROM HARVEST_SOURCE WHERE LAST_HARVEST_FAILED = 'Y' AND URL NOT IN (SELECT URL FROM REMOVE_SOURCE_QUEUE)";
    private static final String searchHarvestSourcesFailedSQL =
        "SELECT * FROM HARVEST_SOURCE WHERE LAST_HARVEST_FAILED = 'Y' AND URL LIKE(?) AND"
        + " URL NOT IN (SELECT URL FROM REMOVE_SOURCE_QUEUE)";
    private static final String getHarvestSourcesUnavailableSQL = "SELECT * FROM HARVEST_SOURCE WHERE COUNT_UNAVAIL > "
        + HarvestSourceDTO.COUNT_UNAVAIL_THRESHOLD + " AND URL NOT IN (SELECT URL FROM REMOVE_SOURCE_QUEUE)";
    private static final String searchHarvestSourcesUnavailableSQL =
        "SELECT * FROM HARVEST_SOURCE WHERE URL LIKE (?) AND COUNT_UNAVAIL > " + HarvestSourceDTO.COUNT_UNAVAIL_THRESHOLD
        + " AND URL NOT IN (SELECT URL FROM REMOVE_SOURCE_QUEUE)";
    private static final String getPrioritySources =
        "SELECT * FROM HARVEST_SOURCE WHERE PRIORITY_SOURCE = 'Y' AND URL NOT IN (SELECT URL FROM REMOVE_SOURCE_QUEUE)  ";
    private static final String searchPrioritySources =
        "SELECT * FROM HARVEST_SOURCE WHERE PRIORITY_SOURCE = 'Y' and URL like(?) AND "
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
            "SELECT * FROM HARVEST_SOURCE WHERE URL IN (<sources>) AND URL NOT IN (SELECT URL FROM REMOVE_SOURCE_QUEUE) ";
        if (!StringUtils.isBlank(searchString)) {
            query =
                "SELECT * FROM HARVEST_SOURCE WHERE URL like (?) AND URL IN (<sources>) AND "
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
        String queryWithoutOrderAndLimit = new String(sql);
        List<Object> inParamsWithoutOrderAndLimit = new LinkedList<Object>(inParams);

        if (sortingRequest != null && sortingRequest.getSortingColumnName() != null) {
            sql += " ORDER BY " + sortingRequest.getSortingColumnName() + " " + sortingRequest.getSortOrder().toSQL();
        } else {
            sql += " ORDER BY URL ";
        }

        if (pagingRequest != null) {
            sql += " LIMIT ? OFFSET ? ";
            inParams.add(pagingRequest.getItemsPerPage());
            inParams.add(pagingRequest.getOffset());
        }

        int rowCount = 0;
        List<HarvestSourceDTO> list = executeSQL(sql, inParams, new HarvestSourceDTOReader());
        if (list != null && !list.isEmpty()) {

            StringBuffer buf = new StringBuffer("select count(*) from (").append(queryWithoutOrderAndLimit).append(") as foo");

            rowCount =
                Integer.parseInt(executeUniqueResultSQL(buf.toString(), inParamsWithoutOrderAndLimit,
                        new SingleObjectReader<Long>()).toString());
        }

        return new Pair<Integer, List<HarvestSourceDTO>>(rowCount, list);
    }

    /**
     * Calculation of number of sources needed to be harvested in PostgreSQL syntax.
     */
    private static final String URGENCY_SOURCES_COUNT = "select count(*) from HARVEST_SOURCE where"
        + " INTERVAL_MINUTES>0 AND (extract(epoch from now()-(coalesce(LAST_HARVEST,(TIME_CREATED -"
        + " INTERVAL_MINUTES * interval '1 minute')))) / (INTERVAL_MINUTES*60)) >= 1.0";

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
     * Insert a record into the the table of harvest sources in PostgreSQL syntax.
     */
    private static final String ADD_SOURCE_SQL = "insert into HARVEST_SOURCE"
        + " (URL,URL_HASH,EMAILS,TIME_CREATED,INTERVAL_MINUTES,PRIORITY_SOURCE,SOURCE_OWNER)"
        + " VALUES (?,?,?,NOW(),?,cast(? as ynboolean),?)";

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
            SQLUtil.executeUpdate(ADD_SOURCE_SQL, values, conn);

            // Get the freshly inserted record's ID.
            // We are not using SQLUtil.executeUpdateReturnAutoID(), because in
            // PostgreSQL one
            // cannot use INSERT RETURNING on a table which has a conditional
            // ON INSERT DO INSTEAD rule. And on HARVEST_SOURCE we have exactly
            // such a rule.
            // There should be no performance problems, as this method is
            // expected to be called
            // by human action only (i.e. adding a new source via web
            // interface).
            Object o =
                SQLUtil.executeSingleReturnValueQuery(
                        "select HARVEST_SOURCE_ID from HARVEST_SOURCE where URL_HASH=" + urlHash, conn);
            return o == null ? null : Integer.valueOf(o.toString());
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

        // JH160210 - in PostgreSQL schema we assume there is a rule created
        // that does nothing if
        // duplicate source added. We lose the ability to notify user if she's
        // trying to add
        // a source that already exists, but for the time being we can live with
        // that.
        addSource(source);
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#deleteHarvestHistory(int)
     */
    @Override
    public void deleteHarvestHistory(int neededToRemain) throws DAOException {

        Connection conn = null;
        try {
            conn = getSQLConnection();
            conn.setAutoCommit(false);

            Object o = SQLUtil.executeSingleReturnValueQuery("select max(HARVEST_ID) from HARVEST", conn);
            Long maxId = o == null || StringUtils.isBlank(o.toString()) ? 0L : Long.valueOf(o.toString());

            if (maxId > neededToRemain) {
                SQLUtil.executeUpdate("delete from HARVEST where HARVEST_ID<=" + (maxId - neededToRemain), conn);
            }

            SQLUtil.executeUpdate("delete from HARVEST_MESSAGE" + " where HARVEST_ID not in (select HARVEST_ID from HARVEST)",
                    conn);

            conn.commit();
        } catch (SQLException e) {
            SQLUtil.rollback(conn);
            throw new DAOException(e.toString(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#deleteTriplesOfMissingSources()
     */
    @Override
    public void deleteTriplesOfMissingSources() throws DAOException {

        Connection conn = null;
        try {
            conn = getSQLConnection();
            conn.setAutoCommit(false);

            String sql = "delete from SPO where SOURCE not in (select URL_HASH from HARVEST_SOURCE)";
            SQLUtil.executeUpdate(sql, conn);
            sql = "delete from SPO where OBJ_DERIV_SOURCE<>0 and OBJ_DERIV_SOURCE not in (select URL_HASH from HARVEST_SOURCE)";
            SQLUtil.executeUpdate(sql, conn);

            conn.commit();
        } catch (SQLException e) {
            SQLUtil.rollback(conn);
            throw new DAOException(e.toString(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#deleteSourceByUrl(java.lang.String)
     */

    @Override
    public void deleteSourceByUrl(String url) throws DAOException {
        // we'll need those wrappers later.

        Connection conn = null;
        try {
            // start transaction
            conn = getSQLConnection();
            conn.setAutoCommit(false);

            deleteSourceByUrl(url, conn);

            conn.commit();
        } catch (Exception e) {
            SQLUtil.rollback(conn);
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
        }

    }

    /*
     * (non-Javadoc) helper "temporary" method to be called from Virtuoso API until PostgreSQL is alive
     *
     * @see eionet.cr.dao.HarvestSourceDAO#deleteSourceByUrl(java.lang.String)
     */
    public void deleteSourceByUrl(String url, Connection conn) throws DAOException {

        ArrayList<Long> hashList = new ArrayList<Long>();
        hashList.add(Hashes.spoHash(url));
        ArrayList<String> urlList = new ArrayList<String>();
        urlList.add(url);

        try {
            // get ID of the harvest source identified by the given URL
            List<Long> sourceIds =
                executeSQL("select HARVEST_SOURCE_ID from HARVEST_SOURCE where URL_HASH = ?", hashList,
                        new SingleObjectReader<Long>());
            String harvestSourceIdsCSV = Util.toCSV(sourceIds);

            // if harvest source ID found, delete harvests and harvest messages
            // by it
            if (!StringUtils.isBlank(harvestSourceIdsCSV)) {

                List<Long> harvestIds =
                    executeSQL("select HARVEST_ID from HARVEST where HARVEST_SOURCE_ID in (" + harvestSourceIdsCSV + ")",
                            null, new SingleObjectReader<Long>());

                String harvestIdsCSV = Util.toCSV(harvestIds);
                if (!StringUtils.isBlank(harvestIdsCSV)) {
                    SQLUtil.executeUpdate("delete from HARVEST_MESSAGE where HARVEST_ID in (" + harvestIdsCSV + ")", conn);
                }
                SQLUtil.executeUpdate("delete from HARVEST where HARVEST_SOURCE_ID in (" + harvestSourceIdsCSV + ")", conn);
            }

            // delete dependencies of this harvest source in other tables
            SQLUtil.executeUpdate("delete from HARVEST_SOURCE where URL_HASH=?", hashList, conn);
            SQLUtil.executeUpdate("delete from HARVEST_SOURCE where SOURCE=?", hashList, conn);
            // SPO replaced by Virtuoso
            // SQLUtil.executeUpdate("delete from SPO where SOURCE=?", hashList,
            // conn);
            // SQLUtil.executeUpdate("delete from SPO where OBJ_DERIV_SOURCE=?",
            // hashList, conn);
            SQLUtil.executeUpdate("delete from UNFINISHED_HARVEST where SOURCE=?", hashList, conn);
            SQLUtil.executeUpdate("delete from URGENT_HARVEST_QUEUE where URL=?", urlList, conn);
            SQLUtil.executeUpdate("delete from REMOVE_SOURCE_QUEUE where URL=?", urlList, conn);

            // special case: delete source metadata auto-generated by harvester
            // Special case not present in Virtuoso
            // ArrayList list = new ArrayList(hashList);
            // list.add(Long.valueOf(Hashes.spoHash(Harvest.HARVESTER_URI)));
            // SQLUtil.executeUpdate("delete from SPO where SUBJECT=? and SOURCE=?",
            // list, conn);

            // end transaction
        } catch (Exception e) {
            throw new DAOException("Error deleting source " + url, e);
        }

    }

    /** */
    private static final String EDIT_SOURCE_SQL = "update HARVEST_SOURCE set URL=?, URL_HASH=?, EMAILS=?, INTERVAL_MINUTES=?,"
        + " PRIORITY_SOURCE=cast(? as ynboolean), SOURCE_OWNER=?, MEDIA_TYPE=? where HARVEST_SOURCE_ID=?";

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
    private static final String editRedirectedSourceSQL = "update HARVEST_SOURCE set LAST_HARVEST=?,"
        + " LAST_HARVEST_FAILED=cast(? as ynboolean) where URL=?";

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
    private static final String GET_NEXT_SCHEDULED_SOURCES_SQL =

        "select * from HARVEST_SOURCE where PERMANENT_ERROR='N' and COUNT_UNAVAIL<5 and INTERVAL_MINUTES>0"
        + " and extract(epoch from now()-(coalesce(LAST_HARVEST,"
        + "(TIME_CREATED - INTERVAL_MINUTES * interval '1 minute')))) >= (INTERVAL_MINUTES*60)"
        + " order by extract(epoch from now()-(coalesce(LAST_HARVEST,"
        + "(TIME_CREATED - INTERVAL_MINUTES * interval '1 minute')))) / (INTERVAL_MINUTES*60)" + " desc limit ?";

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.harvest.scheduled.HarvestingJob#getNextScheduledSources()
     *
     * @see eionet.cr.dao.HarvestSourceDAO#getNextScheduledSources(int)
     */
    @Override
    public List<HarvestSourceDTO> getNextScheduledSources(int limit) throws DAOException {

        List<Object> values = new ArrayList<Object>();
        values.add(new Integer(limit));
        return executeSQL(GET_NEXT_SCHEDULED_SOURCES_SQL, values, new HarvestSourceDTOReader());
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

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#queueSourcesForDeletion(java.util.List)
     */
    @Override
    public void queueSourcesForDeletion(List<String> urls) throws DAOException {

        if (urls == null || urls.isEmpty()) {
            return;
        }
        StringBuffer sql = new StringBuffer("INSERT INTO REMOVE_SOURCE_QUEUE (URL) VALUES ");
        List<Object> params = new LinkedList<Object>();
        int i = 0;
        for (String url : urls) {
            sql.append("(?)");
            if (++i < urls.size()) {
                sql.append(',');
            }
            params.add(url);
        }
        executeSQL(sql.toString(), params);
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#increaseUnavailableCount(int)
     */
    @Override
    public void increaseUnavailableCount(int sourceId) throws DAOException {
        Connection conn = null;
        String query = "update HARVEST_SOURCE set COUNT_UNAVAIL=(COUNT_UNAVAIL+1) where HARVEST_SOURCE_ID=" + sourceId;
        try {
            conn = getSQLConnection();
            SQLUtil.executeUpdate(query, conn);
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#getSourcesInInferenceRule()
     */
    @Override
    public String getSourcesInInferenceRules() throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#isSourceInInferenceRule()
     */
    @Override
    public boolean isSourceInInferenceRule(String url) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#addSourceIntoInferenceRule()
     */
    @Override
    public boolean addSourceIntoInferenceRule(String url) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#removeSourceFromInferenceRule()
     */
    @Override
    public boolean removeSourceFromInferenceRule(String url) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#addSourceToRepository(File, String)
     */
    @Override
    public int loadIntoRepository(File file, RDFFormat rdfFormat, String graphUrl, boolean clearPreviousGraphContent) throws IOException, OpenRDFException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#addSourceMetadata(SubjectDTO)
     */
    @Override
    public void addSourceMetadata(SubjectDTO sourceMetadata) throws DAOException, RDFParseException, RepositoryException,
    IOException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /**
     * @see eionet.cr.dao.HarvestSourceDAO#getNewSources(java.lang.String)
     */
    @Override
    public List<String> getNewSources(String sourceUrl) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#getSourceMetadata(String, String)
     */
    @Override
    public String getHarvestSourceMetadata(String harvestSourceUri, String predicateUri) throws DAOException, RepositoryException,
    IOException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#insertUpdateSourceMetadata(String, String, ObjectDTO)
     */
    @Override
    public void insertUpdateSourceMetadata(String subject, String predicate, ObjectDTO object) throws DAOException,
    RepositoryException, IOException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /**
     *
     */
    @Override
    public void deleteSubjectTriplesInSource(String subjectUri, String sourceUri) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#deleteSourceTriples(String)
     */
    @Override
    public void deleteSourceTriples(String url) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#getLatestHarvestedURLs()
     */
    @Override
    public Pair<Integer, List<HarvestedUrlCountDTO>> getLatestHarvestedURLs(int days) throws DAOException {

        StringBuffer buf =
            new StringBuffer().append(" SELECT DATE(LAST_HARVEST) AS HARVESTDAY, COUNT(HARVEST_SOURCE_ID) AS HARVESTS")
            .append(" FROM HARVEST_SOURCE WHERE LAST_HARVEST IS NOT NULL")
            .append(" AND LAST_HARVEST + INTERVAL '" + days + " days' > current_date")
            .append(" GROUP BY DATE(LAST_HARVEST) ORDER BY HARVESTDAY DESC ;");

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

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#getUrgencyOfComingHarvests()
     */
    @Override
    public Pair<Integer, List<HarvestUrgencyScoreDTO>> getUrgencyOfComingHarvests(int amount) throws DAOException {

        StringBuffer buf =
            new StringBuffer().append("SELECT url, last_harvest, interval_minutes, ")
            .append(" EXTRACT (EPOCH FROM NOW()-(coalesce(last_harvest,")
            .append(" (time_created - interval_minutes * interval '1 minute') ")
            .append(" )))/(interval_minutes * 60) AS urgency ").append(" FROM HARVEST_SOURCE ")
            .append(" WHERE interval_minutes > 0 ").append(" ORDER BY urgency DESC ").append(" LIMIT " + amount + " ");

        List<HarvestUrgencyScoreDTO> result = new ArrayList<HarvestUrgencyScoreDTO>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

            conn = getSQLConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(buf.toString());
            while (rs.next()) {
                HarvestUrgencyScoreDTO resultRow = new HarvestUrgencyScoreDTO();
                resultRow.setUrl(rs.getString("url"));
                try {
                    resultRow.setLastHarvest(sdf.parse(rs.getString("last_harvest") + ""));
                } catch (ParseException ex) {
                    resultRow.setLastHarvest(null);
                    // throw new DAOException(ex.toString(), ex);
                }
                resultRow.setIntervalMinutes(rs.getLong("interval_minutes"));
                resultRow.setUrgency(rs.getDouble("urgency"));
                result.add(resultRow);
            }
        } catch (SQLException e) {
            throw new DAOException(e.toString(), e);
        } finally {
            SQLUtil.close(rs);
            SQLUtil.close(stmt);
            SQLUtil.close(conn);
        }

        return new Pair<Integer, List<HarvestUrgencyScoreDTO>>(result.size(), result);

    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#addSourceToRepository(java.io.InputStream, java.lang.String)
     */
    @Override
    public int loadIntoRepository(InputStream inputStream, RDFFormat rdfFormat, String graphUrl, boolean clearPreviousGraphContent) throws IOException, OpenRDFException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /** */
    private static final String UPDATE_SOURCE_HARVEST_FINISHED_SQL =
        "update HARVEST_SOURCE set EMAILS=?, STATEMENTS=?, COUNT_UNAVAIL=?, LAST_HARVEST=?, INTERVAL_MINUTES=?," +
        " LAST_HARVEST_FAILED=cast(? as ynboolean), PRIORITY_SOURCE=cast(? as ynboolean), SOURCE_OWNER=?," +
        " PERMANENT_ERROR=cast(? as ynboolean) where URL_HASH=?";
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

    /** */
    private static final String DELETE_NONPRIORITY_UNAVAIL_SOURCES = "insert into REMOVE_SOURCE_QUEUE (URL)"
        + " select URL from HARVEST_SOURCE where PRIORITY_SOURCE='N' and COUNT_UNAVAIL>=5";
    /**
     * @see eionet.cr.dao.HarvestSourceDAO#queueNonPriorityUnavailableSourcesForDeletion()
     */
    @Override
    public int queueNonPriorityUnavailableSourcesForDeletion() throws DAOException {

        Connection conn = null;
        try {
            conn = getSQLConnection();
            return SQLUtil.executeUpdate(DELETE_NONPRIORITY_UNAVAIL_SOURCES, conn);
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
        }

    }
}
