package eionet.cr.staging.imp.msaccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Table;

import eionet.cr.staging.imp.ImportException;
import eionet.cr.staging.imp.ImportLoggerImpl;
import eionet.cr.staging.util.VirtuosoUtil;
import eionet.cr.util.sesame.SesameUtil;
import eionet.cr.util.sql.SQLUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class VirtuosoHandler.
 *
 * @author jaanus
 */
public class VirtuosoHandler implements MSAccessImportHandlerIF {

    /** */
    private static final Logger LOGGER = Logger.getLogger(VirtuosoHandler.class);

    /** */
    private static final int INSERT_BATCH_SIZE = 10000;

    /** */
    private Connection conn;

    /** */
    private PreparedStatement currRowInsertStmt;

    /** */
    private List<String> currColumnsOrder;

    /** */
    private String prevTableName;

    /** */
    private int stmtCounter;

    /** */
    private String dbName;

    /** */
    private String dbUser;

    /** */
    private ImportLoggerImpl importLogger;

    /**
     * Instantiates a new virtuoso handler.
     *
     * @param dbName the db name
     * @param importLogger the import logger
     */
    public VirtuosoHandler(String dbName, ImportLoggerImpl importLogger) {

        if (StringUtils.isBlank(dbName)) {
            throw new IllegalArgumentException("Database name must not be blank!");
        }

        if (importLogger == null) {
            throw new IllegalArgumentException("Import logger must not be null!");
        }

        this.dbName = dbName;
        this.importLogger = importLogger;
    }

    /*
     * (non-Javadoc)
     *
     * @see das.conv.msaccess.ConversionHandlerIF#newTable(com.healthmarketscience .jackcess.Table)
     */
    @Override
    public void newTable(Table table) throws ImportException {

        try {
            String sql = VirtuosoUtil.createTableStatement(table, dbName, getDbUser());

            String message = "Creating table:\n" + sql;
            importLogger.info(message);
            LOGGER.debug(message);

            SQLUtil.executeUpdate(sql, conn);
        } catch (SQLException e) {
            throw new ImportException("Failed to create table " + table.getName(), e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see das.conv.msaccess.ConversionHandlerIF#processRow(com.healthmarketscience .jackcess.Table, java.util.Map)
     */
    @Override
    public void processRow(Table table, Map<String, Object> row) throws ImportException {

        String tableName = table.getName();
        try {
            if (prevTableName == null || !tableName.equals(prevTableName)) {
                if (stmtCounter > 0) {
                    currRowInsertStmt.executeBatch();
                }
                SQLUtil.close(currRowInsertStmt);
                currColumnsOrder = new ArrayList<String>(row.keySet());
                stmtCounter = 0;
                currRowInsertStmt =
                        conn.prepareStatement(VirtuosoUtil.parameterizedInsertStatement(tableName, currColumnsOrder, dbName,
                                getDbUser()));
                prevTableName = tableName;
            }

            int i = 0;
            for (String column : currColumnsOrder) {
                Object value = row.get(column);
                if (value == null) {
                    currRowInsertStmt.setNull(++i, Types.OTHER);
                } else {
                    currRowInsertStmt.setObject(++i, value);
                }
            }

            currRowInsertStmt.addBatch();
            stmtCounter++;
            if (stmtCounter % INSERT_BATCH_SIZE == 0) {
                currRowInsertStmt.executeBatch();
            }
        } catch (SQLException e) {
            throw new ImportException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see das.conv.msaccess.ConversionHandlerIF#endOfFile()
     */
    @Override
    public void endOfFile() throws ImportException {

        if (stmtCounter > 0) {
            try {
                currRowInsertStmt.executeBatch();
            } catch (SQLException e) {
                throw new ImportException(e);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see das.conv.msaccess.ConversionHandlerIF#close()
     */
    @Override
    public void close() {
        SQLUtil.close(conn);
    }

    /**
     * Lazy getter for the underlying SQL connection.
     *
     * @return
     * @throws SQLException
     */
    private Connection getConnection() throws SQLException {
        if (conn == null) {
            conn = SesameUtil.getSQLConnection();
        }
        return conn;
    }

    /**
     * Lazy getter for the underlying SQL connection's user.
     *
     * @return
     * @throws SQLException
     */
    private String getDbUser() throws SQLException {

        if (dbUser == null) {
            dbUser = getConnection().getMetaData().getUserName();
        }
        return dbUser;
    }
}
