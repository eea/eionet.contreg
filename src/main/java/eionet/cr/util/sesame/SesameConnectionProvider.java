package eionet.cr.util.sesame;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.MessageFormat;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import virtuoso.sesame2.driver.VirtuosoRepository;
import virtuoso.sesame2.driver.VirtuosoRepositoryConnection;
import eionet.cr.common.CRRuntimeException;
import eionet.cr.config.GeneralConfig;
import eionet.cr.util.IsJUnitRuntime;

/**
 *
 * @author jaanus
 *
 */
public class SesameConnectionProvider {

    /** */
    private static Logger LOGGER = Logger.getLogger(SesameConnectionProvider.class);

    /** */
    public static final String READWRITE_DATASOURCE_NAME = "jdbc/readWriteRepo";
    public static final String READONLY_DATASOURCE_NAME = "jdbc/readOnlyRepo";

    /** Repository and data source for read-write connection. */
    private static Repository readWriteRepository;
    private static DataSource readWriteDataSource;

    /** Repository and data source for read-only connection. */
    private static Repository readOnlyRepository;
    private static DataSource readOnlyDataSource;

    /**
     * @return the readWriteRepository
     */
    private static synchronized Repository getReadWriteRepository() {

        if (readWriteRepository == null) {

            String urlProperty = IsJUnitRuntime.VALUE ? GeneralConfig.VIRTUOSO_UNITTEST_DB_URL : GeneralConfig.VIRTUOSO_DB_URL;
            String usrProperty = IsJUnitRuntime.VALUE ? GeneralConfig.VIRTUOSO_UNITTEST_DB_USR : GeneralConfig.VIRTUOSO_DB_USR;
            String pwdProperty = IsJUnitRuntime.VALUE ? GeneralConfig.VIRTUOSO_UNITTEST_DB_PWD : GeneralConfig.VIRTUOSO_DB_PWD;

            readWriteRepository =
                createRepository(GeneralConfig.getRequiredProperty(urlProperty),
                        GeneralConfig.getRequiredProperty(usrProperty), GeneralConfig.getRequiredProperty(pwdProperty));
        }
        return readWriteRepository;
    }

    /**
     * @return the readOnlyRepository
     */
    private static synchronized Repository getReadOnlyRepository() {

        if (readOnlyRepository == null) {
            readOnlyRepository =
                createRepository(GeneralConfig.getRequiredProperty(GeneralConfig.VIRTUOSO_DB_URL),
                        GeneralConfig.getRequiredProperty(GeneralConfig.VIRTUOSO_DB_ROUSR),
                        GeneralConfig.getRequiredProperty(GeneralConfig.VIRTUOSO_DB_ROPWD));
        }
        return readOnlyRepository;
    }

    /**
     *
     * @param url
     * @param usr
     * @param pwd
     * @return
     */
    private static Repository createRepository(String url, String usr, String pwd) {

        Repository repository = new VirtuosoRepository(url, usr, pwd);
        try {
            repository.initialize();
        } catch (RepositoryException e) {
            throw new CRRuntimeException(MessageFormat.format("Failed to initialize repository {0} with user {1}", url, usr), e);
        }

        return repository;
    }

    /**
     * @return the readWriteDataSource
     */
    private static synchronized DataSource getReadWriteDataSource() {

        if (readWriteDataSource == null) {
            readWriteDataSource = lookupDataSource(READWRITE_DATASOURCE_NAME);
        }
        return readWriteDataSource;
    }

    /**
     * @return the readOnlyDataSource
     */
    private static synchronized DataSource getReadOnlyDataSource() {

        if (readOnlyDataSource == null) {
            readOnlyDataSource = lookupDataSource(READONLY_DATASOURCE_NAME);
        }
        return readOnlyDataSource;
    }

    /**
     *
     * @param dataSourceName
     * @return
     */
    private static DataSource lookupDataSource(String dataSourceName) {

        try {
            Context initContext = new InitialContext();
            Context context = (Context) initContext.lookup("java:comp/env");
            return (javax.sql.DataSource) context.lookup(dataSourceName);
        } catch (NamingException e) {
            return null;
        }
    }

    /**
     * Returns read-write connection to the repository.
     *
     * @return RepositoryConnection
     * @throws RepositoryException
     */
    public static RepositoryConnection getRepositoryConnection() throws RepositoryException {

        DataSource dataSource = getReadWriteDataSource();
        if (dataSource == null) {

            LOGGER.debug(MessageFormat.format("Found no data source with name {0}, going to create a direct connection",
                    READWRITE_DATASOURCE_NAME));
            return getReadWriteRepository().getConnection();
        } else {
            try {
                return new VirtuosoRepositoryConnection((VirtuosoRepository)getReadWriteRepository(), dataSource.getConnection());
            } catch (SQLException e) {
                throw new RepositoryException("Could not create repository connection from the given SQL connection", e);
            }
        }
    }

    /**
     * Returns read-only connection to the repository.
     *
     * @return RepositoryConnection connection
     * @throws RepositoryException
     */
    public static RepositoryConnection getReadOnlyRepositoryConnection() throws RepositoryException {

        DataSource dataSource = getReadOnlyDataSource();
        if (dataSource == null) {

            LOGGER.debug(MessageFormat.format("Found no data source with name {0}, going to create a direct connection",
                    READONLY_DATASOURCE_NAME));
            return getReadOnlyRepository().getConnection();
        } else {
            try {
                return new VirtuosoRepositoryConnection((VirtuosoRepository) getReadOnlyRepository(), dataSource.getConnection());
            } catch (SQLException e) {
                throw new RepositoryException("Could not create repository connection from the given SQL connection", e);
            }
        }
    }

    /**
     * Returns a {@link java.sql.Connection} to the underlying repository. Uses a {@link javax.sql.DataSource} with name
     * {@link #READWRITE_DATASOURCE_NAME} if such can be found. Otherwise creates a direct connection using the "classical" way
     * through {@link java.sql.DriverManager}.
     *
     * @return Connection
     * @throws SQLException
     */
    public static Connection getSQLConnection() throws SQLException {

        // first try to create connection through data source
        DataSource dataSource = getReadWriteDataSource();
        if (dataSource != null) {
            return dataSource.getConnection();
        } else {
            LOGGER.debug(MessageFormat.format(
                    "Found no data source with name {0}, going to create a connection through DriverManager",
                    READWRITE_DATASOURCE_NAME));
        }

        // no data source was found above, so create the connection through DriverManager

        String urlProperty = IsJUnitRuntime.VALUE ? GeneralConfig.VIRTUOSO_UNITTEST_DB_URL : GeneralConfig.VIRTUOSO_DB_URL;
        String usrProperty = IsJUnitRuntime.VALUE ? GeneralConfig.VIRTUOSO_UNITTEST_DB_USR : GeneralConfig.VIRTUOSO_DB_USR;
        String pwdProperty = IsJUnitRuntime.VALUE ? GeneralConfig.VIRTUOSO_UNITTEST_DB_PWD : GeneralConfig.VIRTUOSO_DB_PWD;

        String drv = GeneralConfig.getRequiredProperty(GeneralConfig.VIRTUOSO_DB_DRV);
        String url = GeneralConfig.getRequiredProperty(urlProperty);
        String usr = GeneralConfig.getRequiredProperty(usrProperty);
        String pwd = GeneralConfig.getRequiredProperty(pwdProperty);

        if (drv == null || drv.trim().length() == 0) {
            throw new SQLException("Failed to get connection, missing property: " + GeneralConfig.VIRTUOSO_DB_DRV);
        }

        if (url == null || url.trim().length() == 0) {
            throw new SQLException("Failed to get connection, missing property: " + GeneralConfig.VIRTUOSO_DB_URL);
        }

        if (usr == null || usr.trim().length() == 0) {
            throw new SQLException("Failed to get connection, missing property: " + GeneralConfig.VIRTUOSO_DB_USR);
        }

        if (pwd == null || pwd.trim().length() == 0) {
            throw new SQLException("Failed to get connection, missing property: " + GeneralConfig.VIRTUOSO_DB_PWD);
        }

        try {
            Class.forName(drv);
            return DriverManager.getConnection(url, usr, pwd);
        } catch (ClassNotFoundException e) {
            throw new CRRuntimeException("Failed to get connection, driver class not found: " + drv, e);
        }
    }
}
