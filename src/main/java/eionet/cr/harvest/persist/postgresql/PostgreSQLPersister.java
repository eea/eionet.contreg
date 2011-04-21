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
package eionet.cr.harvest.persist.postgresql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.jsp.jstl.sql.SQLExecutionTag;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eionet.cr.common.Predicates;
import eionet.cr.dto.UnfinishedHarvestDTO;
import eionet.cr.harvest.CurrentHarvests;
import eionet.cr.harvest.Harvest;
import eionet.cr.harvest.RDFHandler;
import eionet.cr.harvest.persist.IHarvestPersister;
import eionet.cr.harvest.persist.PersisterConfig;
import eionet.cr.harvest.persist.PersisterException;
import eionet.cr.harvest.persist.mysql.MySQLDerivationEngine;
import eionet.cr.harvest.scheduled.HarvestingJob;
import eionet.cr.harvest.util.HarvestLog;
import eionet.cr.util.Hashes;
import eionet.cr.util.Util;
import eionet.cr.util.YesNoBoolean;
import eionet.cr.util.sql.DbConnectionProvider;
import eionet.cr.util.sql.SQLUtil;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class PostgreSQLPersister implements IHarvestPersister {

    /** */
    private String spoTempTableName = "SPO_TEMP";
    private String resourceTempTableName = "RESOURCE_TEMP";
    private static final int TRIPLE_PROGRESS_INTERVAL = 50000;
    private static final int BULK_INSERT_SIZE = 50000;

    /** */
    private Log logger;

    //fields initialized through PersisterConfig object
    private long sourceUrlHash;
    private long genTime;
    private String instantHarvestUser;
    private String sourceUrl;

    private Connection connection;

    /** */
    private PreparedStatement preparedStatementForTriples;
    private PreparedStatement preparedStatementForResources;
    private PreparedStatement preparedStatementForSourceMetadata;
    private int tripleCounter;
    private PersisterConfig config;

    /** */
    private int storedTriplesCount;

    /** */
    private boolean isAddingSourceMetadata;

    /**
     *
     */
    public PostgreSQLPersister() {
    }

    /**
     *
     * @param config
     */
    public PostgreSQLPersister(PersisterConfig config) {

        this.config = config;
        sourceUrl = config.getSourceUrl();
        genTime = config.getGenTime();
        sourceUrlHash = config.getSourceUrlHash();
        instantHarvestUser = config.getInstantHarvestUser();

        logger = new HarvestLog(config.getSourceUrl(), config.getGenTime(), LogFactory.getLog(this.getClass()));
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.harvest.persist.IHarvestPersister#addTriple(long, boolean, long, java.lang.String, long, java.lang.String, boolean, boolean, long)
     */
    // TODO this method has got way too many arguments, the whole approach needs refactoring
    public void addTriple(long subjectHash, boolean anonSubject, long predicateHash,
            String object, long objectHash, String objectLang, boolean litObject, boolean anonObject, long objSourceObject) throws PersisterException {

        if (isAddingSourceMetadata) {

            addSourceMetadataTriple(subjectHash, anonSubject, predicateHash,
                    object, objectHash, objectLang, litObject, anonObject, objSourceObject);
            return;
        }

        try {
            preparedStatementForTriples.setLong(1, subjectHash);
            preparedStatementForTriples.setLong(2, predicateHash);
            preparedStatementForTriples.setString(3, object);
            preparedStatementForTriples.setLong(4, objectHash);
            preparedStatementForTriples.setObject(5, Util.toDouble(object));
            preparedStatementForTriples.setString(6, YesNoBoolean.format(anonSubject));
            preparedStatementForTriples.setString(7, YesNoBoolean.format(anonObject));
            preparedStatementForTriples.setString(8, YesNoBoolean.format(litObject));
            preparedStatementForTriples.setString(9, objectLang == null ? "" : objectLang);
            preparedStatementForTriples.setLong(10, objSourceObject == 0 ? 0 : sourceUrlHash);
            preparedStatementForTriples.setLong(11, objSourceObject == 0 ? 0 : genTime);
            preparedStatementForTriples.setLong(12, objSourceObject);

            preparedStatementForTriples.addBatch();
            tripleCounter++;

            // if at BULK_INSERT_SIZE, execute the batch
            if (tripleCounter % BULK_INSERT_SIZE == 0) {
                executeBatch();
            }
        } catch (SQLException e) {
            throw new PersisterException(e.getMessage(), e);
        }
    }

    /**
     *
     * @param subjectHash
     * @param anonSubject
     * @param predicateHash
     * @param object
     * @param objectHash
     * @param objectLang
     * @param litObject
     * @param anonObject
     * @param objSourceObject
     * @throws PersisterException
     */
    private void addSourceMetadataTriple(long subjectHash, boolean anonSubject, long predicateHash,
            String object, long objectHash, String objectLang, boolean litObject, boolean anonObject, long objSourceObject) throws PersisterException {

        try {
            preparedStatementForSourceMetadata.setLong(1, subjectHash);
            preparedStatementForSourceMetadata.setLong(2, predicateHash);
            preparedStatementForSourceMetadata.setString(3, object);
            preparedStatementForSourceMetadata.setLong(4, objectHash);
            preparedStatementForSourceMetadata.setObject(5, Util.toDouble(object));
            preparedStatementForSourceMetadata.setString(6, YesNoBoolean.format(anonSubject));
            preparedStatementForSourceMetadata.setString(7, YesNoBoolean.format(anonObject));
            preparedStatementForSourceMetadata.setString(8, YesNoBoolean.format(litObject));
            preparedStatementForSourceMetadata.setString(9, objectLang == null ? "" : objectLang);
            preparedStatementForSourceMetadata.setLong(10, objSourceObject == 0 ? 0 : sourceUrlHash);
            preparedStatementForSourceMetadata.setLong(11, objSourceObject == 0 ? 0 : genTime);
            preparedStatementForSourceMetadata.setLong(12, objSourceObject);

            preparedStatementForSourceMetadata.addBatch();
            tripleCounter++;

            // if at BULK_INSERT_SIZE, execute the batch
            if (tripleCounter % BULK_INSERT_SIZE == 0) {
                executeBatch();
            }
        } catch (SQLException e) {
            throw new PersisterException(e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.harvest.persist.IHarvestPersister#addResource(java.lang.String, long)
     */
    public void addResource(String uri, long uriHash) throws PersisterException {

        try {
            preparedStatementForResources.setString(1, uri);
            preparedStatementForResources.setLong(2, uriHash);
            preparedStatementForResources.addBatch();
        } catch (SQLException e) {
            throw new PersisterException(e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.harvest.persist.IHarvestPersister#commit()
     */
    public void commit() throws PersisterException {

        // if no DB updates were made at all
        if (connection == null) {
            return;
        }

        try {

            // derive inferred triples
            PostgreSQLDerivationEngine derivEngine = new PostgreSQLDerivationEngine(
                    sourceUrl, sourceUrlHash, genTime, connection);
            if (config.isDeriveInferredTriples()) {

                derivEngine.deriveParentClasses();
                derivEngine.deriveParentProperties();
                derivEngine.deriveLabels();
            }

            // extract new harvest sources
            derivEngine.extractNewHarvestSources();

            // lower the "unfinished-harvest" flag
            deleteUnfinishedHarvestFlag();

            // commit all changes made during this harvest
            connection.commit();
        } catch (SQLException e) {
            throw new PersisterException(e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.harvest.persist.IHarvestPersister#rollback()
     */
    public void rollback() throws PersisterException {

        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                throw new PersisterException(e.getMessage(), e);
            }
        }
    }

    /**
     *
     * @throws PersisterException
     */
    public void executeBatch() throws PersisterException {

        try {
            int[] tripleCounts = preparedStatementForTriples.executeBatch();
            preparedStatementForTriples.clearParameters();
            System.gc();

            int[] sourceMetadataCounts = preparedStatementForSourceMetadata.executeBatch();
            preparedStatementForSourceMetadata.clearParameters();
            System.gc();

            preparedStatementForResources.executeBatch();
            preparedStatementForResources.clearParameters();
            System.gc();

            int len = tripleCounts.length;
            for (int i = 0; i < len; i++) {
                storedTriplesCount = storedTriplesCount + tripleCounts[i];
            }
            len = sourceMetadataCounts.length;
            for (int i = 0; i < len; i++) {
                storedTriplesCount = storedTriplesCount + sourceMetadataCounts[i];
            }

            if (tripleCounter % TRIPLE_PROGRESS_INTERVAL == 0) {
                logger.debug("Progress: " + String.valueOf(tripleCounter) + " triples processed");
            }
        } catch(SQLException e) {
            throw new PersisterException(e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.harvest.persist.IHarvestPersister#endOfFile()
     */
    public void endOfFile() throws PersisterException {

        // if there are any un-executed records left in the batch, execute them
        if (tripleCounter % BULK_INSERT_SIZE != 0) {
            executeBatch();
        }

        logger.debug("End of file, total of " + tripleCounter + " triples found in source");
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.harvest.persist.IHarvestPersister#openResources()
     */
    public void openResources() throws PersisterException {
        try {
            connection = DbConnectionProvider.getConnection();
            connection.setAutoCommit(false);

            // create unfinished harvest flag for the current harvest
            raiseUnfinishedHarvestFlag();

            // prepare statements
            prepareStatementForTriples();
            prepareStatementForSourceMetadata();
            prepareStatementForResources();

            // store the hash of the source itself
            addResource(sourceUrl, sourceUrlHash);

            // let the debugger know that we have got our first triple
            logger.debug("Got first triple");

            //
            if (config.isClearPreviousContent()) {

                logger.debug("Deleting SPO rows of previous harvests");

                StringBuffer buf = new StringBuffer("delete from SPO where SOURCE=");
                buf.append(sourceUrlHash);
                SQLUtil.executeUpdate(buf.toString(), getConnection());

                buf = new StringBuffer("delete from SPO where OBJ_DERIV_SOURCE=");
                buf.append(sourceUrlHash);
                SQLUtil.executeUpdate(buf.toString(), getConnection());

                // delete also source metadata that was previously auto-generated by harvester
                buf = new StringBuffer("delete from SPO where SUBJECT=");
                buf.append(sourceUrlHash).append(" and SOURCE=").
                append(Hashes.spoHash(Harvest.HARVESTER_URI));

                SQLUtil.executeUpdate(buf.toString(), getConnection());
            }
        } catch (SQLException fatal) {
            throw new PersisterException(fatal.getMessage(), fatal);
        }
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.harvest.persist.IHarvestPersister#closeResources()
     */
    public void closeResources() {

        SQLUtil.close(preparedStatementForTriples);
        SQLUtil.close(preparedStatementForSourceMetadata);
        SQLUtil.close(preparedStatementForResources);
        SQLUtil.close(connection);
    }

    /**
     *
     * @throws SQLException
     */
    private void prepareStatementForTriples() throws SQLException {

        StringBuffer buf = new StringBuffer().
        append("insert into SPO (SUBJECT,PREDICATE,OBJECT,OBJECT_HASH,OBJECT_DOUBLE").
        append(",ANON_SUBJ,ANON_OBJ,LIT_OBJ,OBJ_LANG").
        append(",OBJ_DERIV_SOURCE,OBJ_DERIV_SOURCE_GEN_TIME,OBJ_SOURCE_OBJECT,SOURCE,GEN_TIME)").
        append(" values (?,?,?,?,?,cast(? as ynboolean),cast(? as ynboolean),cast(? as ynboolean),?,?,?,?,").
        append(sourceUrlHash).append(",").append(genTime).append(")");

        preparedStatementForTriples = getConnection().prepareStatement(buf.toString());
    }

    /**
     *
     * @throws SQLException
     */
    private void prepareStatementForSourceMetadata() throws SQLException {

        StringBuffer buf = new StringBuffer().
        append("insert into SPO (SUBJECT,PREDICATE,OBJECT,OBJECT_HASH,OBJECT_DOUBLE").
        append(",ANON_SUBJ,ANON_OBJ,LIT_OBJ,OBJ_LANG").
        append(",OBJ_DERIV_SOURCE,OBJ_DERIV_SOURCE_GEN_TIME,OBJ_SOURCE_OBJECT,SOURCE,GEN_TIME)").
        append(" values (?,?,?,?,?,cast(? as ynboolean),cast(? as ynboolean),cast(? as ynboolean),?,?,?,?,").
        append(Hashes.spoHash(Harvest.HARVESTER_URI)).append(",").append(genTime).append(")");

        preparedStatementForSourceMetadata = getConnection().prepareStatement(buf.toString());
    }

    /**
     *
     * @throws SQLException
     */
    private void prepareStatementForResources() throws SQLException {

        StringBuffer buf = new StringBuffer().
        append("insert into RESOURCE (URI,URI_HASH,FIRSTSEEN_SOURCE,FIRSTSEEN_TIME,LASTMODIFIED_TIME)").
        append(" values (?,?,").append(sourceUrlHash).append(",").append(genTime).
        append(",").append(genTime).append(")");

        preparedStatementForResources = getConnection().prepareStatement(buf.toString());
    }

    /**
     *
     * @throws SQLException
     */
    private void raiseUnfinishedHarvestFlag() throws SQLException {

        StringBuffer buf = new StringBuffer();
        buf.append("insert into UNFINISHED_HARVEST (SOURCE, GEN_TIME) values (").
        append(sourceUrlHash).append(", ").append(genTime).append(")");

        SQLUtil.executeUpdate(buf.toString(), getConnection());
    }

    /**
     *
     * @throws SQLException
     */
    private void deleteUnfinishedHarvestFlag() throws SQLException {

        StringBuffer buf = new StringBuffer();
        buf.append("delete from UNFINISHED_HARVEST where SOURCE=").append(sourceUrlHash).append(" and GEN_TIME=").append(genTime);

        SQLUtil.executeUpdate(buf.toString(), connection);
    }

    /**
     *
     * @throws SQLException
     */
    private static void deleteUnfinishedHarvestFlag(long sourceUrlHash, long genTime, Connection conn) throws SQLException {

        StringBuffer buf = new StringBuffer();
        buf.append("delete from UNFINISHED_HARVEST where SOURCE=").append(sourceUrlHash).append(" and GEN_TIME=").append(genTime);

        SQLUtil.executeUpdate(buf.toString(), conn);
    }

    /**
     * @return
     */
    private Connection getConnection() {
        return connection;
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.harvest.persist.IHarvestPersister#rollbackUnfinishedHarvests()
     */
    public void rollbackUnfinishedHarvests() throws PersisterException {

        ResultSet rs = null;
        Statement stmt = null;
        Connection conn = null;
        ArrayList<UnfinishedHarvestDTO> list = new ArrayList<UnfinishedHarvestDTO>();
        try {
            conn = DbConnectionProvider.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("select * from UNFINISHED_HARVEST");
            while (rs != null && rs.next()) {
                list.add(UnfinishedHarvestDTO.create(rs.getLong("SOURCE"), rs.getLong("GEN_TIME")));
            }

            if (!list.isEmpty()) {

                LogFactory.getLog(RDFHandler.class).debug("Deleting leftovers from unfinished harvests");

                for (UnfinishedHarvestDTO unfinishedHarvestDTO:list) {

                    // if the source is not actually being currently harvested, only then roll it back
                    if (!CurrentHarvests.contains(unfinishedHarvestDTO.getSource())) {
                        rollbackUnfinishedHarvest(unfinishedHarvestDTO.getSource(),
                                unfinishedHarvestDTO.getGenTime(), conn);
                    }
                }
            }
        } catch (SQLException fatal) {
            throw new PersisterException(fatal.getMessage(), fatal);
        } finally {
            SQLUtil.close(rs);
            SQLUtil.close(stmt);
            SQLUtil.close(conn);
        }
    }

    /**
     *
     * @param sourceUrlHash
     * @param genTime
     * @param conn
     * @throws SQLException
     */
    private static void rollbackUnfinishedHarvest(
            long sourceUrlHash, long genTime, Connection conn) throws SQLException {

        try {
            // start transaction
            conn.setAutoCommit(false);

            // delete rows of given harvest from SPO
            StringBuffer buf = new StringBuffer("delete from SPO where (SOURCE=");
            buf.append(sourceUrlHash).append(" and GEN_TIME=").append(genTime).
            append(") or (OBJ_DERIV_SOURCE=").append(sourceUrlHash).
            append(" and OBJ_DERIV_SOURCE_GEN_TIME=").append(genTime).append(")");
            SQLUtil.executeUpdate(buf.toString(), conn);

            // delete rows that represent the source metadata that harvester auto-generated
            buf = new StringBuffer("delete from SPO where SUBJECT=").append(sourceUrlHash).
            append(" and SOURCE=").append(Hashes.spoHash(Harvest.HARVESTER_URI)).
            append(" and GEN_TIME=").append(genTime);
            SQLUtil.executeUpdate(buf.toString(), conn);

            // delete rows of given harvest from RESOURCE
            buf = new StringBuffer("delete from RESOURCE where FIRSTSEEN_SOURCE=");
            buf.append(sourceUrlHash).append(" and FIRSTSEEN_TIME=").append(genTime);
            SQLUtil.executeUpdate(buf.toString(), conn);

            // delete new sources extracted from the given harvest
            buf = new StringBuffer("delete from HARVEST_SOURCE where SOURCE=").append(sourceUrlHash).
            append(" and GEN_TIME=").append(genTime);

            //
            deleteUnfinishedHarvestFlag(sourceUrlHash, genTime, conn);

            conn.commit();
        } catch (SQLException e) {
            try {conn.rollback();}catch (SQLException ee) {}
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.harvest.persist.IHarvestPersister#getStoredTriplesCount()
     */
    public int getStoredTriplesCount() {
        return storedTriplesCount;
    }

    /** */
    private static String sql_insertLastRefreshed =
        "insert into SPO (SUBJECT,PREDICATE,OBJECT,OBJECT_HASH,LIT_OBJ,SOURCE,GEN_TIME)" +
            " values (?,?,?,?,cast(? as ynboolean),?,?)";
    /*
     * (non-Javadoc)
     * @see eionet.cr.harvest.persist.IHarvestPersister#updateLastRefreshed()
     */
    public void updateLastRefreshed(long subjectHash, DateFormat dateFormat) throws SQLException {

        long time = System.currentTimeMillis();
        String dateString = dateFormat.format(new Date(time));

        long predicateHash = Hashes.spoHash(Predicates.CR_LAST_REFRESHED);

        StringBuffer deleteSQL = new StringBuffer("delete from SPO where SUBJECT=").
        append(subjectHash).append(" and PREDICATE=").append(predicateHash);

        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DbConnectionProvider.getConnection();
            conn.setAutoCommit(false);

            pstmt = conn.prepareStatement(sql_insertLastRefreshed);
            pstmt.setLong(1, subjectHash);
            pstmt.setLong(2, predicateHash);
            pstmt.setString(3, dateString);
            pstmt.setLong(4, Hashes.spoHash(dateString));
            pstmt.setString(5, YesNoBoolean.format(true));
            pstmt.setLong(6, subjectHash);
            pstmt.setLong(7, time);

            SQLUtil.executeUpdate(deleteSQL.toString(), conn);
            pstmt.execute();

            conn.commit();
        } catch (SQLException e) {
            SQLUtil.rollback(conn);
            throw e;
        } finally {
            SQLUtil.close(pstmt);
            SQLUtil.close(conn);
        }
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.harvest.persist.IHarvestPersister#setAddingSourceMetadata(boolean)
     */
    public void setAddingSourceMetadata(boolean flag) {

        isAddingSourceMetadata = flag;
    }
}
