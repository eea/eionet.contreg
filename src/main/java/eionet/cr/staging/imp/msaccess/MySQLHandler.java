package eionet.cr.staging.imp.msaccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Table;

import eionet.cr.staging.imp.ImportException;
import eionet.cr.staging.util.MySQLUtil;
import eionet.cr.util.sql.SQLUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class MySQLHandler.
 *
 * @author jaanus
 */
public class MySQLHandler implements MSAccessImportHandlerIF {

    /** */
    private static final Logger LOGGER = Logger.getLogger(MySQLHandler.class);

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

    /**
     * Instantiates a new my sql handler.
     *
     * @param conn the conn
     */
    public MySQLHandler(Connection conn) {
        this.conn = conn;
    }

    /*
     * (non-Javadoc)
     *
     * @see das.conv.msaccess.ConversionHandlerIF#newTable(com.healthmarketscience .jackcess.Table)
     */
    @Override
    public void newTable(Table table) throws ImportException {

        String sql = MySQLUtil.createTableStatement(table);
        try {
            LOGGER.debug(sql);
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
                currRowInsertStmt = conn.prepareStatement(MySQLUtil.parameterizedInsertStatement(tableName, currColumnsOrder));
                prevTableName = tableName;
            }

            int i = 0;
            for (String column : currColumnsOrder) {
                currRowInsertStmt.setObject(++i, row.get(column));
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
}
