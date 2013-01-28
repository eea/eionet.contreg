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
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        jaanus
 */

package eionet.cr.dao.virtuoso;

import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import eionet.cr.common.CRException;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.StagingDatabaseDAO;
import eionet.cr.dao.readers.ResultSetReaderException;
import eionet.cr.dao.readers.StagingDatabaseDTOReader;
import eionet.cr.dto.StagingDatabaseDTO;
import eionet.cr.staging.imp.ImportStatus;
import eionet.cr.util.sql.SQLUtil;

/**
 * Virtuoso-specific implementation of {@link StagingDatabaseDAO}.
 *
 * @author jaanus
 */
public class VirtuosoStagingDatabaseDAO extends VirtuosoBaseDAO implements StagingDatabaseDAO {

    /** */
    private static final Logger LOGGER = Logger.getLogger(VirtuosoStagingDatabaseDAO.class);

    /** */
    private static final String ADD_NEW_DB_SQL =
            "insert into STAGING_DB (NAME,CREATOR,CREATED,DESCRIPTION,IMPORT_STATUS,IMPORT_LOG) values (?,?,?,?,?,?)";

    /** */
    private static final String GET_DATABASE_BY_ID_SQL = "select * from STAGING_DB where DATABASE_ID=?";

    /** */
    private static final String GET_DATABASE_BY_NAME_SQL = "select * from STAGING_DB where NAME=?";

    /** */
    private static final String UPDATE_IMPORT_STATUS_SQL = "update STAGING_DB set IMPORT_STATUS=? where DATABASE_ID=?";

    /** */
    private static final String ADD_IMPORT_LOG_MESSAGE_SQL =
            "update STAGING_DB set IMPORT_LOG=concat(IMPORT_LOG, ?) where DATABASE_ID=?";

    /** */
    private static final String LIST_ALL_SQL = "select * from STAGING_DB order by NAME asc";

    /** */
    private static final String DELETE_DB_SQL = "delete from STAGING_DB where NAME in (?)";

    /** */
    private static final String COUNT_EXISTING_DBS_SQL = "select count(*) from STAGING_DB where NAME in (?)";

    /** */
    private static final String GET_IMPORT_LOG_SQL = "select IMPORT_LOG from STAGING_DB where DATABASE_ID=?";

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.StagingDatabaseDAO#createDatabase()
     */
    @Override
    public void createDatabase(String databaseName) throws DAOException {

        // We do nothing here, because in Virtuoso there's actually no such thing as "create a database". Instead, tables in
        // Virtuoso's RDBMS are sectioned into so-called qualifiers. A qualifier/database becomes "created" the moment a first
        // table is created with that qualifier.
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.StagingDatabaseDAO#createRecord(eionet.cr.dto.StagingDatabaseDTO, java.lang.String)
     */
    @Override
    public int createRecord(StagingDatabaseDTO databaseDTO, String userName) throws DAOException {

        ArrayList<Object> params = new ArrayList<Object>();
        params.add(databaseDTO.getName());
        params.add(userName);
        params.add(new Date());
        params.add(databaseDTO.getDescription());
        params.add(ImportStatus.NOT_STARTED.name());
        params.add("");

        Connection conn = null;
        try {
            conn = getSQLConnection();
            return SQLUtil.executeUpdateReturnAutoID(ADD_NEW_DB_SQL, params, conn);
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        } catch (CRException e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.StagingDatabaseDAO#getDatabaseById(int)
     */
    @Override
    public StagingDatabaseDTO getDatabaseById(int id) throws DAOException {

        ArrayList<Object> params = new ArrayList<Object>();
        params.add(id);
        List<StagingDatabaseDTO> list = executeSQL(GET_DATABASE_BY_ID_SQL, params, new StagingDatabaseDTOReader());
        return list == null || list.isEmpty() ? null : list.iterator().next();
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.StagingDatabaseDAO#getDatabaseByName(java.lang.String)
     */
    @Override
    public StagingDatabaseDTO getDatabaseByName(String name) throws DAOException {

        ArrayList<Object> params = new ArrayList<Object>();
        params.add(name);
        List<StagingDatabaseDTO> list = executeSQL(GET_DATABASE_BY_NAME_SQL, params, new StagingDatabaseDTOReader());
        return list == null || list.isEmpty() ? null : list.iterator().next();
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.StagingDatabaseDAO#updateImportStatus(int, eionet.cr.staging.ImportStatus)
     */
    @Override
    public void updateImportStatus(int databaseId, ImportStatus importStatus) throws DAOException {

        ArrayList<Object> params = new ArrayList<Object>();
        params.add(importStatus.name());
        params.add(databaseId);

        Connection conn = null;
        try {
            conn = getSQLConnection();
            SQLUtil.executeUpdate(UPDATE_IMPORT_STATUS_SQL, params, conn);
        } catch (SQLException e) {
            throw new DAOException("Updating database import status failed with " + e.getClass().getSimpleName(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.StagingDatabaseDAO#addImportLogMessage(java.lang.String, eionet.cr.staging.ImportLogLevel)
     */
    @Override
    public void addImportLogMessage(int databaseId, String message) throws DAOException {

        ArrayList<Object> params = new ArrayList<Object>();
        params.add(message);
        params.add(databaseId);

        Connection conn = null;
        try {
            conn = getSQLConnection();
            SQLUtil.executeUpdate(ADD_IMPORT_LOG_MESSAGE_SQL, params, conn);
        } catch (SQLException e) {
            throw new DAOException("Adding database import log message failed with " + e.getClass().getSimpleName(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.StagingDatabaseDAO#listAll()
     */
    @Override
    public List<StagingDatabaseDTO> listAll() throws DAOException {

        return executeSQL(LIST_ALL_SQL, null, new StagingDatabaseDTOReader());
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.StagingDatabaseDAO#delete(java.util.List)
     */
    @Override
    public void delete(List<String> dbNames) throws DAOException {

        if (dbNames == null || dbNames.isEmpty()) {
            return;
        }

        Connection conn = null;
        try {
            conn = getSQLConnection();
            conn.setAutoCommit(false);

            // First, ensure the given databases are present in staging databases table (to avoid deleting other databases).
            String questionMarks = StringUtils.join(Collections.nCopies(dbNames.size(), "?"), ',');
            String sql = COUNT_EXISTING_DBS_SQL.replace("?", questionMarks);
            int count = NumberUtils.toInt(SQLUtil.executeSingleReturnValueQuery(sql, dbNames, conn).toString());
            if (dbNames.size() > count) {
                throw new DAOException("At least one of the given databases is unknown");
            }

            // Second, delete the databases themselves.
            deleteDatabases(dbNames, conn);

            // Third, delete rows from the staging databases table.
            sql = DELETE_DB_SQL.replace("?", questionMarks);
            SQLUtil.executeUpdate(sql, dbNames, conn);

            conn.commit();
        } catch (SQLException e) {
            SQLUtil.rollback(conn);
            throw new DAOException(e.getMessage(), e);
        } catch (ResultSetReaderException e) {
            SQLUtil.rollback(conn);
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }

    /**
     * Deletes the given databases from Virtuoso.
     *
     * @param dbNames
     * @param conn
     * @throws SQLException
     * @throws ResultSetReaderException
     */
    private void deleteDatabases(List<String> dbNames, Connection conn) throws ResultSetReaderException, SQLException {

        HashMap<String, HashSet<String>> dbTables = new HashMap<String, HashSet<String>>();
        String userName = conn.getMetaData().getUserName();

        // First, get the tables present in each of the given databases.

        ResultSet rs = null;
        try {
            for (String dbName : dbNames) {

                HashSet<String> tables = new HashSet<String>();
                rs = conn.getMetaData().getTables(dbName, userName, null, null);
                while (rs.next()) {
                    tables.add(rs.getString("TABLE_NAME"));
                }
                if (!tables.isEmpty()) {
                    dbTables.put(dbName, tables);
                }
            }
        } finally {
            SQLUtil.close(rs);
        }

        // Second, loop through the above-found tables, delete each.
        for (Entry<String, HashSet<String>> entry : dbTables.entrySet()) {

            String dbName = entry.getKey();
            HashSet<String> tables = entry.getValue();
            for (String tableName : tables) {
                SQLUtil.executeUpdate("drop table " + dbName + "." + userName + ".\"" + tableName + "\"", conn);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.StagingDatabaseDAO#exists(java.lang.String)
     */
    @Override
    public boolean exists(String dbName) throws DAOException {

        ResultSet rs = null;
        Connection conn = null;
        try {
            conn = getSQLConnection();
            DatabaseMetaData metaData = conn.getMetaData();
            rs = metaData.getTables(dbName, metaData.getUserName(), null, null);
            return rs != null && rs.next();
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(rs);
            SQLUtil.close(conn);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.StagingDatabaseDAO#getImportLog(int)
     */
    @Override
    public String getImportLog(int databaseId) throws DAOException {

        ArrayList<Object> params = new ArrayList<Object>();
        params.add(Integer.valueOf(databaseId));

        ResultSet rs = null;
        Statement stmt = null;
        Connection conn = null;
        try {
            conn = getSQLConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(GET_IMPORT_LOG_SQL.replace("?", String.valueOf(databaseId)));
            if (rs.next()) {
                Blob blob = rs.getBlob(1);
                if (blob != null) {
                    try {
                        return blob.length() == 0 ? "" : IOUtils.toString(blob.getBinaryStream());
                    } catch (IOException e) {
                        LOGGER.warn("Could not retreive import log of database #" + databaseId + ": " + e);
                        return null;
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }

        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.StagingDatabaseDAO#prepareStatement(java.lang.String)
     */
    @Override
    public List<String> prepareStatement(String sql) throws DAOException {

        if (StringUtils.isBlank(sql)){
            throw new IllegalArgumentException("The given SQL statement must not be blank!");
        }

        ArrayList<String> result = new ArrayList<String>();

        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            pstmt = SQLUtil.prepareStatement(sql, null, conn);
            ResultSetMetaData metaData = pstmt.getMetaData();
            int colCount = metaData.getColumnCount();
            for (int i = 1; i <= colCount; i++) {
                String colName = metaData.getColumnName(i);
                result.add(colName);
            }
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(pstmt);
            SQLUtil.close(conn);
        }

        return result;
    }
}
