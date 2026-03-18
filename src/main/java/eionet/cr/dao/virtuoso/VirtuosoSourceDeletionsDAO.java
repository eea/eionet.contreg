package eionet.cr.dao.virtuoso;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.SourceDeletionsDAO;
import eionet.cr.dao.readers.ResultSetReaderException;
import eionet.cr.util.Hashes;
import eionet.cr.util.Pair;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.util.sesame.SesameUtil;
import eionet.cr.util.sql.PairReader;
import eionet.cr.util.sql.SQLUtil;

/**
 * Virtuoso-specific implementation of {@link SourceDeletionsDAO}.
 *
 * @author Jaanus
 */
public class VirtuosoSourceDeletionsDAO extends VirtuosoBaseDAO implements SourceDeletionsDAO {

    /** Marking will be done in batches, this is the batch size. */
    private static final int MARKING_BATCH_SIZE = 5000;

    /** SQL for marking a harvest source for background deletion. */
    private static final String MARK_FOR_DELETION_SQL =
            "UPDATE harvest_source SET delete_requested=now(), delete_flag=1 where URL_HASH=?";

    /** SQL for "softly" inserting a harvest source. "Softly" means that if source already exists, no errors thrown. */
    private static final String SOFT_INSERT_SOURCE =
            "INSERT SOFT harvest_source (url,url_hash,time_created,interval_minutes) VALUES (?,?,NOW(),"
                    + GeneralConfig.getDefaultHarvestIntervalMinutes() + ")";

    /** The SQL for returning the deletion queue. */
    private static final String GET_DELETION_QUEUE_SQL = "SELECT url AS LCOL, delete_requested AS RCOL FROM harvest_source "
            + "WHERE delete_flag=1 ORDER BY delete_requested, url";

    /** The SQL for returning the deletion queue filtered. */
    private static final String GET_DELETION_QUEUE_FILTERED_SQL =
            "SELECT url AS LCOL, delete_requested AS RCOL FROM harvest_source "
                    + "WHERE delete_flag=1 AND url LIKE (?) ORDER BY delete_requested, url";

    /** The SQL for returning the size of deletion queue. */
    private static final String GET_DELETION_QUEUE_SIZE_SQL = "SELECT count(*) FROM harvest_source WHERE delete_flag=1";

    /** The SQL for returning the size of deletion queue filtered. */
    private static final String GET_DELETION_QUEUE_SIZE_FILTERED_SQL = "SELECT count(*) FROM harvest_source "
            + "WHERE delete_flag=1 AND url LIKE (?)";

    /** SQL for removing a given URL from the deletion queue. */
    private static final String CANCEL_DELETION_SQL =
            "UPDATE harvest_source SET delete_requested=NULL, delete_flag=0 where URL_HASH=?";

    /** SQL for picking the first source from the deletion queue. */
    private static final String PICK_FOR_DELETION_SQL = "SELECT TOP 1 url FROM harvest_source "
            + "WHERE delete_flag=1 ORDER BY delete_requested, url";

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.SourceDeletionsDAO#markForDeletion(java.util.Collection)
     */
    @Override
    public int markForDeletion(Collection<String> sourceUrls) throws DAOException {

        if (CollectionUtils.isEmpty(sourceUrls)) {
            return 0;
        }

        Connection conn = null;
        PreparedStatement pstmtInsert = null;
        PreparedStatement pstmtMark = null;
        try {
            conn = SesameUtil.getSQLConnection();
            conn.setAutoCommit(false);

            // We shall require to ensure the record exists (i.e. by doing soft insert) and that it gets updated, hence 2 steps.
            pstmtInsert = conn.prepareStatement(SOFT_INSERT_SOURCE);
            pstmtMark = conn.prepareStatement(MARK_FOR_DELETION_SQL);

            int batchCounter = 0;
            for (String sourceUrl : sourceUrls) {

                addMarkingBatch(sourceUrl, pstmtInsert, pstmtMark);
                if (++batchCounter % MARKING_BATCH_SIZE == 0) {
                    executeMarkingBatch(pstmtInsert, pstmtMark);
                }
            }

            if (batchCounter % MARKING_BATCH_SIZE != 0) {
                executeMarkingBatch(pstmtInsert, pstmtMark);
            }

            conn.commit();
            return batchCounter;
        } catch (SQLException e) {
            SQLUtil.rollback(conn);
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(pstmtInsert);
            SQLUtil.close(pstmtMark);
            SQLUtil.close(conn);
        }
    }

    /**
     * Adds given source URL to the deletion marking batch.
     *
     * @param sourceUrl The source to mark.
     * @param pstmtInsert Prepared statement for {@link #SOFT_INSERT_SOURCE}.
     * @param pstmtMark Prepared statement for {@link #MARK_FOR_DELETION_SQL}
     *
     * @throws SQLException the SQL exception
     */
    private void addMarkingBatch(String sourceUrl, PreparedStatement pstmtInsert, PreparedStatement pstmtMark) throws SQLException {

        pstmtInsert.setString(1, sourceUrl);
        pstmtInsert.setLong(2, Hashes.spoHash(sourceUrl));
        pstmtInsert.addBatch();

        pstmtMark.setLong(1, Hashes.spoHash(sourceUrl));
        pstmtMark.addBatch();
    }

    /**
     * Executes current deletion marking batch.
     *
     * @param pstmtInsert Prepared statement for {@link #SOFT_INSERT_SOURCE}.
     * @param pstmtMark Prepared statement for {@link #MARK_FOR_DELETION_SQL}
     * @return The count of successfully executed rows.
     * @throws SQLException the SQL exception
     */
    private int executeMarkingBatch(PreparedStatement pstmtInsert, PreparedStatement pstmtMark) throws SQLException {

        int resultCount = 0;
        pstmtInsert.executeBatch();
        int[] updateCounts = pstmtMark.executeBatch();
        for (int i = 0; i < updateCounts.length; i++) {
            resultCount += updateCounts[i];
        }
        return resultCount;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.SourceDeletionsDAO#markForDeletionSparql(java.lang.String)
     */
    @Override
    public int markForDeletionSparql(String sparqlQuery) throws DAOException {

        if (StringUtils.isBlank(sparqlQuery)) {
            return 0;
        }

        RepositoryConnection repoConn = null;
        Connection sqlConn = null;
        PreparedStatement pstmtInsert = null;
        PreparedStatement pstmtMark = null;
        TupleQueryResult tupleQueryResult = null;

        try {
            repoConn = SesameUtil.getRepositoryConnection();
            sqlConn = SesameUtil.getSQLConnection();
            sqlConn.setAutoCommit(false);

            // We shall require to ensure the record exists (i.e. by doing soft insert) and that it gets updated, hence 2 steps.
            pstmtInsert = sqlConn.prepareStatement(SOFT_INSERT_SOURCE);
            pstmtMark = sqlConn.prepareStatement(MARK_FOR_DELETION_SQL);

            int batchCounter = 0;

            TupleQuery tupleQuery = repoConn.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery);
            tupleQueryResult = tupleQuery.evaluate();
            if (tupleQueryResult != null) {
                List<String> bindingNames = tupleQueryResult.getBindingNames();
                if (CollectionUtils.isNotEmpty(bindingNames)) {
                    while (tupleQueryResult.hasNext()) {
                        BindingSet bindingSet = tupleQueryResult.next();
                        if (CollectionUtils.isNotEmpty(bindingSet)) {
                            continue;
                        }
                        Value firstColumnValue = bindingSet.iterator().next().getValue();
                        if (firstColumnValue instanceof URI) {

                            String urlString = firstColumnValue.stringValue();
                            addMarkingBatch(urlString, pstmtInsert, pstmtMark);
                            if (++batchCounter % MARKING_BATCH_SIZE == 0) {
                                executeMarkingBatch(pstmtInsert, pstmtMark);
                            }
                        }
                    }
                }
            }

            if (batchCounter % MARKING_BATCH_SIZE != 0) {
                executeMarkingBatch(pstmtInsert, pstmtMark);
            }

            sqlConn.commit();
            return batchCounter;

        } catch (SQLException e) {
            SQLUtil.rollback(sqlConn);
            throw new DAOException(e.getMessage(), e);
        } catch (OpenRDFException e) {
            SQLUtil.rollback(sqlConn);
            throw new DAOException(e.getMessage(), e);
        } finally {
            SesameUtil.close(tupleQueryResult);
            SQLUtil.close(pstmtInsert);
            SQLUtil.close(pstmtMark);
            SQLUtil.close(sqlConn);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.SourceDeletionsDAO#getDeletionQueue(java.lang.String, eionet.cr.util.pagination.PagingRequest)
     */
    @Override
    public Pair<Integer, List<Pair<String, Date>>> getDeletionQueue(String filterStr, PagingRequest pagingRequest)
            throws DAOException {

        String sql = StringUtils.isBlank(filterStr) ? GET_DELETION_QUEUE_SQL : GET_DELETION_QUEUE_FILTERED_SQL;
        String countSql = StringUtils.isBlank(filterStr) ? GET_DELETION_QUEUE_SIZE_SQL : GET_DELETION_QUEUE_SIZE_FILTERED_SQL;

        if (pagingRequest != null) {
            String pagingParams = "TOP " + pagingRequest.getOffset() + ", " + pagingRequest.getItemsPerPage();
            sql = StringUtils.replaceOnce(sql, "SELECT", "SELECT " + pagingParams);
        }

        List<Object> sqlValues = new ArrayList<Object>();
        if (StringUtils.isNotBlank(filterStr)) {
            sqlValues.add("%" + filterStr + "%");
        }

        Connection conn = null;
        try {
            conn = SesameUtil.getSQLConnection();
            PairReader<String, Date> reader = new PairReader<String, Date>();
            SQLUtil.executeQuery(sql, sqlValues, reader, conn);
            List<Pair<String, Date>> resultList = reader.getResultList();

            int totalMatchCount = resultList.size();
            if (pagingRequest != null) {
                Object count = SQLUtil.executeSingleReturnValueQuery(countSql, sqlValues, conn);
                totalMatchCount = NumberUtils.toInt(count == null ? null : count.toString(), totalMatchCount);
            }

            return new Pair<Integer, List<Pair<String, Date>>>(totalMatchCount, resultList);
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        } catch (ResultSetReaderException e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.SourceDeletionsDAO#cancelDeletion(java.util.Collection)
     */
    @Override
    public int unmarkForDeletion(Collection<String> sourceUrls) throws DAOException {

        if (CollectionUtils.isEmpty(sourceUrls)) {
            return 0;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = SesameUtil.getSQLConnection();
            conn.setAutoCommit(false);

            pstmt = conn.prepareStatement(CANCEL_DELETION_SQL);

            int batchCounter = 0;

            for (String sourceUrl : sourceUrls) {

                pstmt.setLong(1, Hashes.spoHash(sourceUrl));
                pstmt.addBatch();

                if (++batchCounter % MARKING_BATCH_SIZE == 0) {
                    pstmt.executeBatch();
                }
            }

            if (batchCounter % MARKING_BATCH_SIZE != 0) {
                pstmt.executeBatch();
            }

            conn.commit();
            return batchCounter;
        } catch (SQLException e) {
            SQLUtil.rollback(conn);
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(pstmt);
            SQLUtil.close(conn);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.SourceDeletionsDAO#pickForDeletion()
     */
    @Override
    public String pickForDeletion() throws DAOException {

        Connection conn = null;
        try {
            conn = SesameUtil.getSQLConnection();
            Object o = SQLUtil.executeSingleReturnValueQuery(PICK_FOR_DELETION_SQL, conn);
            return o == null ? null : o.toString();
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }
}
