package eionet.cr.liquibase;

import liquibase.change.ChangeFactory;
import liquibase.change.core.RawSQLChange;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.sqlgenerator.SqlGeneratorFactory;

import java.sql.*;
import java.util.Arrays;
import java.util.List;

/**
 * This is a Virtuoso-specific extension of Liquibase's {@link AbstractJdbcDatabase}.
 * The idea is to override some of Liquibase's default implementations for unknown databases,
 * because they don't work out of the box with Virtuoso. These overrides include:
 *
 * - creation/inserts/updates/selects on Liquibase's DATABASECHANGELOGLOCK table
 * - override Liquibase's {@link RawSQLChange} to skip calling nativeSQL() method Virtuoso's JDBC connection.
 *
 * Fully qualified name of this class must be fed into <databaseClass> tag of the <configuration> tag of the Liquibase Maven
 * plugin in pom.xml.
 *
 * @author Jaanus
 */
public class VirtuosoDatabase extends AbstractJdbcDatabase {

    /** The priority indicator to be returned by getPriority() of the extended SQL generators, etc.*/
    public static final int PRIORITY = 9999;

    /** DB product name as expected from Virtuoso's implementation of java.sql.DatabaseMetaData.getDatabaseProductName(). */
    private static final String PRODUCT_NAME = "OpenLink Virtuoso VDBMS";

    /** Words that Virtuoso does not expect in column/table names, unless quoted. */
    private static final List<String> RESERVED_WORDS = Arrays.asList("timestamp", "TIMESTAMP");

    /** The SQL connection obtained from the {@link DatabaseConnection} that comes from {@link AbstractJdbcDatabase}. */
    private Connection sqlConn;

    /** The {@link DatabaseMetaData} obtained from {@link #sqlConn}. */
    private DatabaseMetaData sqlConnMetaData;

    /** The Virtuoso JDBC driver name derived from the class name of {@link #sqlConn}. */
    private String defaultJdbcDiverName;

    /**
     * The constructor that is called by Liquibase when instantiating this Virtuoso-specific implementation.
     */
    public VirtuosoDatabase() {

        super.setCurrentDateTimeFunction("NOW()");
//        super.quotingStartCharacter = "\"";
//        super.quotingEndCharacter = "\"";

        // Register Virtuoso-specific implementations of various SQL generators and Liquibase changes.

        SqlGeneratorFactory sqlGeneratorFactory = SqlGeneratorFactory.getInstance();
        sqlGeneratorFactory.register(new VirtuosoCreateChangeLogLockTableGenerator());
        sqlGeneratorFactory.register(new VirtuosoInitChangeLogLockTableGenerator());
        sqlGeneratorFactory.register(new VirtuosoUnlockChangeLogGenerator());
        sqlGeneratorFactory.register(new VirtuosoLockChangeLogGenerator());
        sqlGeneratorFactory.register(new VirtuosoRawSqlGenerator());

        ChangeFactory.getInstance().register(VirtuosoRawSqlChange.class);
    }

    /*
     * (non-Javadoc)
     *
     * @see liquibase.database.Database#isCorrectDatabaseImplementation(liquibase.database.DatabaseConnection)
     */
    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection databaseConnection) throws DatabaseException {
        return databaseConnection == null ? false : PRODUCT_NAME.equals(databaseConnection.getDatabaseProductName());
    }

    /*
     * (non-Javadoc)
     *
     * @see liquibase.database.Database#getDefaultDriver(java.lang.String)
     */
    @Override
    public String getDefaultDriver(String jdbcConnUrl) {
        return defaultJdbcDiverName;
    }

    /*
     * (non-Javadoc)
     *
     * @see liquibase.database.Database#getShortName()
     */
    @Override
    public String getShortName() {
        return "virtuoso";
    }

    /*
     * (non-Javadoc)
     *
     * @see liquibase.database.Database#getDefaultPort()
     */
    @Override
    public Integer getDefaultPort() {
        return Integer.valueOf(1111);
    }

    /*
     * (non-Javadoc)
     *
     * @see liquibase.database.Database#supportsInitiallyDeferrableColumns()
     */
    @Override
    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see liquibase.database.Database#supportsTablespaces()
     */
    @Override
    public boolean supportsTablespaces() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see liquibase.servicelocator.PrioritizedService#getPriority()
     */
    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    /*
     * (non-Javadoc)
     *
     * @see liquibase.database.AbstractJdbcDatabase#getDefaultDatabaseProductName()
     */
    @Override
    protected String getDefaultDatabaseProductName() {
        return "Virtuoso";
    }

    /*
     * (non-Javadoc)
     *
     * @see liquibase.database.AbstractJdbcDatabase#getConnectionSchemaName()
     */
    @Override
    protected String getConnectionSchemaName() {
        try {
            return sqlConnMetaData == null ? "" : sqlConnMetaData.getUserName();
        } catch (SQLException e) {
            return "";
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see liquibase.database.AbstractJdbcDatabase#supportsSequences()
     */
    @Override
    public boolean supportsSequences() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see liquibase.database.AbstractJdbcDatabase#isCaseSensitive()
     */
    @Override
    public boolean isCaseSensitive() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see liquibase.database.AbstractJdbcDatabase#isReservedWord(java.lang.String)
     */
    @Override
    public boolean isReservedWord(String str) {

        return RESERVED_WORDS.contains(str);
    }

    /*
     * (non-Javadoc)
     *
     * @see liquibase.database.AbstractJdbcDatabase#setConnection(liquibase.database.DatabaseConnection)
     */
    @Override
    public void setConnection(DatabaseConnection conn) {

        super.setConnection(conn);

        if (conn instanceof JdbcConnection) {

            try {
                doAfterSetConnection((JdbcConnection) conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Do some post-processing after {@link #setConnection(DatabaseConnection)} has been called.
     *
     * @param jdbcConnection The {@link JdbcConnection} acquired from {@link #setConnection(DatabaseConnection)}.
     *
     * @throws SQLException When there's a problem extracting various information from the given JDBC connection.
     */
    private void doAfterSetConnection(JdbcConnection jdbcConnection) throws SQLException {

        sqlConn = jdbcConnection.getUnderlyingConnection();
        if (sqlConn == null) {
            return;
        }

        sqlConnMetaData = sqlConn.getMetaData();

        String sqlConnClassName = sqlConn.getClass().getName();
        if (sqlConnClassName.endsWith("VirtuosoConnection")) {
            int i = sqlConnClassName.lastIndexOf(".");
            if (i > 0) {
                defaultJdbcDiverName = sqlConnClassName.substring(0, i) + ".Driver";
            }
        }

        // log_enable(1,1) enforces false as the default auto-commit mode on the underlying Virtuoso JDBC connection.
        // If the database connection URL already contains log_enable=2 then without calling log_enable(1,1) right here the
        // Liquibase fails to execute ALTER TABLE ... ADD FOREIGN KEY ... statements. Don't know why.
        tryExecuteSql("log_enable(1,1)", sqlConn);
    }

    /**
     * Tries to execute the given SQL statement on the given connection.
     * Swallows exceptions and is null-safe.
     *
     * @param sql The SQL statement to execute.
     * @param conn The connection to execute on.
     */
    private void tryExecuteSql(String sql, Connection conn) {

        if (sql == null && conn == null) {
            return;
        }

        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            stmt.execute(sql);
        } catch (Exception e) {
            // Ignore deliberately.
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e2) {
                    // Ignore deliberately.
                }
            }
        }
    }


    /**
     * Checks if index exists for a given table.
     * @param tableName
     * @param indexName
     * @return
     */
    boolean indexExists(String tableName, String indexName) {

        try {
            ResultSet rs =  sqlConnMetaData.getIndexInfo("", getConnectionSchemaName(), tableName, false, true);

            if (indexExistsInResultSet(rs, indexName)) {
                return true;
            }
            
            //there is a bug in virt-jdbc driver metadata:
            //according to interface API *all* indexes should be returned if uniqueIndex=false
            //inVirtuoso implementation the flag is put as a paramtere
            //that is the reason getIndexInfo() is queried twice
            rs =  sqlConnMetaData.getIndexInfo("", getConnectionSchemaName(), tableName, true, true);

            if (indexExistsInResultSet(rs, indexName)) {
                return true;
            }

        } catch (Exception e) {
            //ignore silently
            e.printStackTrace(System.err);
        }

        return false;
        
    }

    /**
     * Loop resultset and check if index metadata is present.
     * @param rs resultset
     * @param indexName index name
     * @return
     * @throws SQLException if looping fails.
     */
    private boolean indexExistsInResultSet(ResultSet rs, String indexName) throws SQLException{
        while (rs.next()) {
            String name = rs.getString("INDEX_NAME");
            if (name.equalsIgnoreCase(indexName)) {
                return true;
            }
        }
        return false;        
    }
    
    
}
