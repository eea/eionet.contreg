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
package eionet.cr.dao.mysql;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFParseException;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.readers.HarvestSourceDTOReader;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.harvest.Harvest;
import eionet.cr.util.Hashes;
import eionet.cr.util.Pair;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.Util;
import eionet.cr.util.YesNoBoolean;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.util.sql.SQLUtil;
import eionet.cr.util.sql.SingleObjectReader;

/**
 * @author altnyris
 * 
 */
public class MySQLHarvestSourceDAO extends MySQLBaseDAO implements
        HarvestSourceDAO {

    MySQLHarvestSourceDAO() {
        // reducing visibility
    }

    /** */
    private static final String getSourcesSQL = "SELECT SQL_CALC_FOUND_ROWS * FROM HARVEST_SOURCE WHERE URL NOT IN (SELECT URL FROM REMOVE_SOURCE_QUEUE) ";
    private static final String searchSourcesSQL = "SELECT SQL_CALC_FOUND_ROWS * FROM HARVEST_SOURCE WHERE URL like (?) AND URL NOT IN (SELECT URL FROM REMOVE_SOURCE_QUEUE) ";
    private static final String getHarvestTrackedFiles = "SELECT SQL_CALC_FOUND_ROWS * FROM HARVEST_SOURCE WHERE PRIORITY_SOURCE = 'Y' AND URL NOT IN (SELECT URL FROM REMOVE_SOURCE_QUEUE)  ";
    private static final String searchHarvestTrackedFiles = "SELECT SQL_CALC_FOUND_ROWS * FROM HARVEST_SOURCE WHERE PRIORITY_SOURCE = 'Y' and URL like(?) AND URL NOT IN (SELECT URL FROM REMOVE_SOURCE_QUEUE) ";
    private static final String getHarvestSourcesUnavailableSQL = "SELECT SQL_CALC_FOUND_ROWS * FROM HARVEST_SOURCE WHERE COUNT_UNAVAIL > "
            + HarvestSourceDTO.COUNT_UNAVAIL_THRESHOLD
            + " AND URL NOT IN (SELECT URL FROM REMOVE_SOURCE_QUEUE)";
    private static final String searchHarvestSourcesUnavailableSQL = "SELECT SQL_CALC_FOUND_ROWS * FROM HARVEST_SOURCE WHERE URL LIKE (?) AND COUNT_UNAVAIL > "
            + HarvestSourceDTO.COUNT_UNAVAIL_THRESHOLD
            + " AND URL NOT IN (SELECT URL FROM REMOVE_SOURCE_QUEUE)";
    private static final String getHarvestSourcesFailedSQL = "SELECT SQL_CALC_FOUND_ROWS * FROM HARVEST_SOURCE WHERE LAST_HARVEST_FAILED = 'Y' AND URL NOT IN (SELECT URL FROM REMOVE_SOURCE_QUEUE)";
    private static final String searchHarvestSourcesFailedSQL = "SELECT SQL_CALC_FOUND_ROWS * FROM HARVEST_SOURCE WHERE LAST_HARVEST_FAILED = 'Y' AND URL LIKE(?) AND URL NOT IN (SELECT URL FROM REMOVE_SOURCE_QUEUE)";

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestSourceDAO#getHarvestSources(java.lang.String,
     * eionet.cr.util.PagingRequest, eionet.cr.util.SortingRequest)
     */
    public Pair<Integer, List<HarvestSourceDTO>> getHarvestSources(
            String searchString, PagingRequest pagingRequest,
            SortingRequest sortingRequest) throws DAOException {

        if (pagingRequest == null) {
            throw new IllegalArgumentException(
                    "Pagination request cannot be null");
        }

        return getSources(StringUtils.isBlank(searchString) ? getSourcesSQL
                : searchSourcesSQL, searchString, pagingRequest, sortingRequest);
    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestSourceDAO#getPrioritySources(java.lang.String ,
     * eionet.cr.util.PagingRequest, eionet.cr.util.SortingRequest)
     */
    public Pair<Integer, List<HarvestSourceDTO>> getPrioritySources(
            String searchString, PagingRequest pagingRequest,
            SortingRequest sortingRequest) throws DAOException {
        return getSources(
                StringUtils.isBlank(searchString) ? getHarvestTrackedFiles
                        : searchHarvestTrackedFiles, searchString,
                pagingRequest, sortingRequest);
    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestSourceDAO#getHarvestSourcesUnavailable()
     */
    public Pair<Integer, List<HarvestSourceDTO>> getHarvestSourcesUnavailable(
            String searchString, PagingRequest pagingRequest,
            SortingRequest sortingRequest) throws DAOException {
        return getSources(
                StringUtils.isBlank(searchString) ? getHarvestSourcesUnavailableSQL
                        : searchHarvestSourcesUnavailableSQL, searchString,
                pagingRequest, sortingRequest);

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * eionet.cr.dao.HarvestSourceDAO#getHarvestSourcesFailed(java.lang.String,
     * eionet.cr.util.PagingRequest, eionet.cr.util.SortingRequest)
     */
    public Pair<Integer, List<HarvestSourceDTO>> getHarvestSourcesFailed(
            String searchString, PagingRequest pagingRequest,
            SortingRequest sortingRequest) throws DAOException {
        return getSources(
                StringUtils.isBlank(searchString) ? getHarvestSourcesFailedSQL
                        : searchHarvestSourcesFailedSQL, searchString,
                pagingRequest, sortingRequest);
    }

    private Pair<Integer, List<HarvestSourceDTO>> getSources(String sql,
            String searchString, PagingRequest pagingRequest,
            SortingRequest sortingRequest) throws DAOException {
        List<Object> searchParams = new LinkedList<Object>();
        if (!StringUtils.isBlank(searchString)) {
            searchParams.add(searchString);
        }
        if (sortingRequest != null
                && sortingRequest.getSortingColumnName() != null) {
            sql += " ORDER BY " + sortingRequest.getSortingColumnName() + " "
                    + sortingRequest.getSortOrder().toSQL();
        } else {
            // in case no sorting request is present, use default one.
            sql += " ORDER BY URL ";
        }

        if (pagingRequest != null) {
            sql += " LIMIT ?, ? ";
            searchParams.add(pagingRequest.getOffset());
            searchParams.add(pagingRequest.getItemsPerPage());
        }

        return executeQueryWithRowCount(sql, searchParams,
                new HarvestSourceDTOReader());
    }

    /** */
    private static final String getSourcesByIdSQL = "select * from HARVEST_SOURCE where HARVEST_SOURCE_ID=?";

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestSourceDao#getHarvestSourceById()
     */
    public HarvestSourceDTO getHarvestSourceById(Integer harvestSourceID)
            throws DAOException {
        List<Object> values = new ArrayList<Object>();
        values.add(harvestSourceID);
        List<HarvestSourceDTO> list = executeQuery(getSourcesByIdSQL, values,
                new HarvestSourceDTOReader());
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }

    /** */
    private static final String getSourcesByUrlSQL = "select * from HARVEST_SOURCE where URL=?";

    /*
     * (non-Javadoc)
     * 
     * @see
     * eionet.cr.dao.HarvestSourceDAO#getHarvestSourceByUrl(java.lang.String)
     */
    public HarvestSourceDTO getHarvestSourceByUrl(String url)
            throws DAOException {
        List<Object> values = new ArrayList<Object>();
        values.add(url);
        List<HarvestSourceDTO> list = executeQuery(getSourcesByUrlSQL, values,
                new HarvestSourceDTOReader());
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }

    /** */
    private static final String URGENCY_SOURCES_COUNT = "select count(*) from HARVEST_SOURCE where"
            + " INTERVAL_MINUTES>0 AND (extract(epoch from now()-(coalesce(LAST_HARVEST,(TIME_CREATED -"
            + " INTERVAL_MINUTES * interval '1 minute')))) / (INTERVAL_MINUTES*60)) > 1.0";

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestSourceDAO#getUrgencySourcesCount()
     */
    public Long getUrgencySourcesCount() throws DAOException {

        Connection conn = null;
        try {
            Object o = SQLUtil.executeSingleReturnValueQuery(
                    URGENCY_SOURCES_COUNT, conn);
            return o == null ? new Long(0) : Long.valueOf(o.toString());
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }

    /** */
    private static final String addSourceSQL = "insert into HARVEST_SOURCE (URL,URL_HASH,EMAILS,TIME_CREATED,INTERVAL_MINUTES) VALUES (?,?,?,NOW(),?)";
    private static final String addSourceIgnoreSQL = "insert ignore into HARVEST_SOURCE (URL,URL_HASH,EMAILS,TIME_CREATED,INTERVAL_MINUTES) VALUES (?,?,?,NOW(),?)";

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestSourceDAO#addSource(HarvestSourceDTO source)
     */
    public Integer addSource(HarvestSourceDTO source) throws DAOException {
        return addSource(addSourceSQL, source);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * eionet.cr.dao.HarvestSourceDAO#addSourceIgnoreDuplicate(java.lang.String,
     * int, boolean, java.lang.String, boolean schema, boolean priority, String
     * owner)
     */
    public void addSourceIgnoreDuplicate(HarvestSourceDTO source)
            throws DAOException {
        addSource(addSourceIgnoreSQL, source);
    }

    /**
     * 
     * @param source
     * @return Integer
     * @throws DAOException
     */
    private Integer addSource(String sql, HarvestSourceDTO source)
            throws DAOException {

        if (StringUtils.isBlank(source.getUrl())) {
            throw new IllegalArgumentException("url must not be blank");
        }

        // harvest sources where URL has fragment part, are not allowed
        String url = StringUtils.substringBefore(source.getUrl(), "#");

        List<Object> values = new ArrayList<Object>();
        values.add(url);
        values.add(Hashes.spoHash(url));
        values.add(source.getEmails());
        values.add(source.getIntervalMinutes());

        Connection conn = null;
        try {
            conn = getConnection();
            SQLUtil.executeUpdate(sql, values, conn);
            return getLastInsertID(conn);
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }

    /** */
    private static final String editSourceSQL = "update HARVEST_SOURCE set URL=?, EMAILS=?,INTERVAL_MINUTES=? where HARVEST_SOURCE_ID=?";

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestSourceDao#editSource()
     */
    public void editSource(HarvestSourceDTO source) throws DAOException {

        List<Object> values = new ArrayList<Object>();
        values.add(source.getUrl());
        values.add(source.getEmails());
        values.add(source.getIntervalMinutes());
        values.add(source.getSourceId());

        Connection conn = null;
        try {
            conn = getConnection();
            SQLUtil.executeUpdate(editSourceSQL, values, conn);
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }

    /**
     * @see eionet.cr.dao.HarvestSourceDAO#queueSourcesForDeletion(java.util.List)
     *      {@inheritDoc}
     */
    public void queueSourcesForDeletion(List<String> urls) throws DAOException {
        if (urls == null || urls.isEmpty()) {
            return;
        }
        StringBuffer sql = new StringBuffer(
                "INSERT INTO REMOVE_SOURCE_QUEUE (URL) VALUES ");
        List<Object> params = new LinkedList<Object>();
        int i = 0;
        for (String url : urls) {
            sql.append("(?)");
            if (++i < urls.size()) {
                sql.append(',');
            }
            params.add(url);
        }
        execute(sql.toString(), params);
    }

    /**
     * @see eionet.cr.dao.HarvestSourceDAO#getScheduledForDeletion()
     *      {@inheritDoc}
     */
    public List<String> getScheduledForDeletion() throws DAOException {
        return executeQuery("select URL from REMOVE_SOURCE_QUEUE", null,
                new SingleObjectReader<String>());
    }

    /**
     * @see eionet.cr.dao.HarvestSourceDAO#deleteSourceByUrl(java.lang.String)
     *      {@inheritDoc}
     */
    public void deleteSourceByUrl(String url) throws DAOException {

        /* execute deletion queries */
        // we'll need those wrappers later.
        List<Object> urlHashesList = new LinkedList<Object>();
        urlHashesList.add(Hashes.spoHash(url));
        List<Object> urlList = new LinkedList<Object>();
        urlList.add(url);

        Connection conn = null;
        try {
            conn = getConnection();

            /*
             * get harvest source ids, delete harvests and harvest messages by
             * them
             */
            List<Long> sourceIds = executeQuery(
                    "select HARVEST_SOURCE_ID from HARVEST_SOURCE where URL = ?",
                    urlHashesList, new SingleObjectReader<Long>());
            String harvestSourceIdsCSV = Util.toCSV(sourceIds);

            if (!StringUtils.isBlank(harvestSourceIdsCSV)) {

                List<Long> harvestIds = executeQuery(
                        "select HARVEST_ID from HARVEST where HARVEST_SOURCE_ID in ("
                                + harvestSourceIdsCSV + ")", null,
                        new SingleObjectReader<Long>());

                String harvestIdsCSV = Util.toCSV(harvestIds);
                if (harvestIdsCSV.trim().length() > 0) {
                    SQLUtil.executeUpdate(
                            "delete from HARVEST_MESSAGE where HARVEST_ID in ("
                                    + harvestIdsCSV + ")", conn);
                }
                SQLUtil.executeUpdate(
                        "delete from HARVEST where HARVEST_SOURCE_ID in ("
                                + harvestSourceIdsCSV + ")", conn);
            }

            /* delete various stuff by harvest source urls or url hashes */
            SQLUtil.executeUpdate("delete from HARVEST_SOURCE where URL = ?",
                    urlList, conn);
            SQLUtil.executeUpdate(
                    "delete from HARVEST_SOURCE where SOURCE = ?",
                    urlHashesList, conn);
            SQLUtil.executeUpdate("delete from SPO where SOURCE = ?",
                    urlHashesList, conn);
            SQLUtil.executeUpdate("delete from SPO where OBJ_DERIV_SOURCE = ?",
                    urlHashesList, conn);
            SQLUtil.executeUpdate(
                    "delete from UNFINISHED_HARVEST where SOURCE = ?",
                    urlHashesList, conn);
            SQLUtil.executeUpdate(
                    "delete from URGENT_HARVEST_QUEUE where URL = ?", urlList,
                    conn);
            SQLUtil.executeUpdate(
                    "delete from REMOVE_SOURCE_QUEUE where URL = ?", urlList,
                    conn);

            // special case: delete source metadata auto-generated by harvester
            ArrayList list = new ArrayList(urlHashesList);
            list.add(Long.valueOf(Hashes.spoHash(Harvest.HARVESTER_URI)));
            SQLUtil.executeUpdate(
                    "delete from SPO where SUBJECT=? and SOURCE=?", list, conn);
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }

    /** */
    private static final String updateHarvestFinishedSQL = "update HARVEST_SOURCE set STATEMENTS=?, RESOURCES=?, LAST_HARVEST_FAILED=? where HARVEST_SOURCE_ID=?";
    private static final String updateHarvestFinishedSQL_avail = "update HARVEST_SOURCE set STATEMENTS=?, COUNT_UNAVAIL=if(?,0,(COUNT_UNAVAIL+1)), LAST_HARVEST_FAILED=?  where HARVEST_SOURCE_ID=?";

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestSourceDAO#updateHarvestFinished(int, Integer,
     * Integer)
     */
    public void updateHarvestFinished(int sourceId, Integer numStatements,
            Boolean sourceAvailable, boolean failed)
            throws DAOException {

        List<Object> values = new ArrayList<Object>();
        values.add(numStatements);
        if (sourceAvailable != null)
            values.add(sourceAvailable.booleanValue() == true ? new Integer(1)
                    : new Integer(0));
        values.add(YesNoBoolean.format(failed));
        values.add(new Integer(sourceId));

        Connection conn = null;
        try {
            conn = getConnection();
            SQLUtil.executeUpdate(
                    sourceAvailable != null ? updateHarvestFinishedSQL_avail
                            : updateHarvestFinishedSQL, values, conn);
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }

    /** */
    private static final String updateHarvestStartedSQL = "update HARVEST_SOURCE set LAST_HARVEST=NOW() where HARVEST_SOURCE_ID=?";

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestSourceDAO#updateHarvestStarted(int)
     */
    public void updateHarvestStarted(int sourceId) throws DAOException {

        List<Object> values = new ArrayList<Object>();
        values.add(new Integer(sourceId));

        Connection conn = null;
        try {
            conn = getConnection();
            SQLUtil.executeUpdate(updateHarvestStartedSQL, values, conn);
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }

    private static final String getNextScheduledSourcesSQL =

    "select * from HARVEST_SOURCE where INTERVAL_MINUTES>0"
            + " and timestampdiff(SECOND,ifnull(LAST_HARVEST,timestampadd(MINUTE,-1*INTERVAL_MINUTES,TIME_CREATED)),NOW()) >= (INTERVAL_MINUTES*60)"
            + " order by timestampdiff(SECOND,ifnull(LAST_HARVEST,timestampadd(MINUTE,-1*INTERVAL_MINUTES,TIME_CREATED)),NOW()) / (INTERVAL_MINUTES*60)"
            + " desc limit ?";

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestSourceDAO#getNextScheduledSources(int)
     */
    public List<HarvestSourceDTO> getNextScheduledSources(int limit)
            throws DAOException {

        List<Object> values = new ArrayList<Object>();
        values.add(new Integer(limit));
        return executeQuery(getNextScheduledSourcesSQL, values,
                new HarvestSourceDTOReader());
    }

    /**
     * @throws DAOException
     * @see eionet.cr.dao.HarvestSourceDAO#deleteTriplesOfMissingSources()
     *      {@inheritDoc}
     */
    public void deleteTriplesOfMissingSources() throws DAOException {

        Connection conn = null;
        try {
            conn = getConnection();

            String sql = "delete from SPO where SOURCE not in (select URL_HASH from HARVEST_SOURCE)";
            SQLUtil.executeUpdate(sql, conn);
            sql = "delete from SPO where OBJ_DERIV_SOURCE not in (select URL_HASH from HARVEST_SOURCE)";
            SQLUtil.executeUpdate(sql, conn);
        } catch (SQLException e) {
            throw new DAOException(e.toString(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }

    /**
     * @throws DAOException
     * @see eionet.cr.dao.HarvestSourceDAO#deleteHarvestHistory(int)
     *      {@inheritDoc}
     */
    public void deleteHarvestHistory(int neededToRemain) throws DAOException {
        // fetch the last auto_incremented id;
        Long id = executeQueryUniqueResult(
                "select max(HARVEST_ID) from HARVEST", null,
                new SingleObjectReader<Long>());
        // delete everything, except last needed_to_remain records
        List<Object> params = new LinkedList<Object>();
        params.add(id - neededToRemain);
        execute("delete from HARVEST where HARVEST_ID <= ?", params);
        execute("delete from HARVEST_MESSAGE where HARVEST_ID not in (select HARVEST_ID from HARVEST)",
                null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestSourceDAO#getUrgencyScore(int)
     */
    @Override
    public double getUrgencyScore(int harvestSourceId) throws DAOException {

        throw new UnsupportedOperationException("Method not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestSourceDAO#getSourcesInInferenceRule()
     */
    @Override
    public String getSourcesInInferenceRules() throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestSourceDAO#getInferenceSources()
     */
    @Override
    public Pair<Integer, List<HarvestSourceDTO>> getInferenceSources(
            String searchString, PagingRequest pagingRequest,
            SortingRequest sortingRequest, String sourceUris)
            throws DAOException {
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
    public boolean removeSourceFromInferenceRule(String url)
            throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestSourceDAO#addSourceToRepository(File, String)
     */
    @Override
    public int addSourceToRepository(File file, String sourceUrlString)
            throws DAOException, RepositoryException, RDFParseException,
            IOException {
        throw new UnsupportedOperationException("Method not implemented");
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestSourceDAO#addSourceMetadata(SubjectDTO)
     */
    @Override
    public void addSourceMetadata(SubjectDTO sourceMetadata)
            throws DAOException, RDFParseException, RepositoryException, IOException {
        throw new UnsupportedOperationException("Method not implemented");
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestSourceDAO#getNewSources(String)
     */
    @Override
    public List<String> getNewSources(String sourceUrl)
            throws DAOException, RDFParseException, RepositoryException,
            IOException {
        throw new UnsupportedOperationException("Method not implemented");
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestSourceDAO#getSourceMetadata(String, String)
     */
    @Override
    public String getSourceMetadata(String subject, String predicate) throws DAOException, RepositoryException {
        throw new UnsupportedOperationException("Method not implemented");
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestSourceDAO#insertUpdateSourceMetadata(String, String, String)
     */
    @Override
    public void insertUpdateSourceMetadata(String subject, String predicate, String value) throws DAOException, RepositoryException {
        throw new UnsupportedOperationException("Method not implemented");
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * eionet.cr.dao.HarvestSourceDAO#editRedirectedSource(eionet.cr.dto.HarvestSourceDTO)
     */
    public void editRedirectedSource(HarvestSourceDTO source) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");
    }
}
