package eionet.cr.util.sesame;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import virtuoso.sesame2.driver.VirtuosoRepository;

import eionet.cr.common.CRRuntimeException;
import eionet.cr.config.GeneralConfig;

/**
 *
 * @author jaanus
 *
 */
public class SesameConnectionProvider {

    /** */
    private static Repository repository = null;

    /** */
    private static Object repositoryLock = new Object();

    /**
     *
     */
    private static void initRepository(){

        String url = GeneralConfig.getRequiredProperty(GeneralConfig.VIRTUOSO_DB_URL);
        String usr = GeneralConfig.getRequiredProperty(GeneralConfig.VIRTUOSO_DB_USR);
        String pwd = GeneralConfig.getRequiredProperty(GeneralConfig.VIRTUOSO_DB_PWD);

        try {
            repository = new VirtuosoRepository(url, usr, pwd);
            repository.initialize();
        }
        catch (RepositoryException e) {
            throw new CRRuntimeException("Failed to initialize repository", e);
        }
    }

    /**
     *
     * @return RepositoryConnection
     * @throws RepositoryException
     */
    public static RepositoryConnection getRepositoryConnection() throws RepositoryException{

        if (repository==null){
            synchronized (repositoryLock) {

                // double-checked locking pattern
                // (http://www.ibm.com/developerworks/java/library/j-dcl.html)
                if (repository==null){
                    initRepository();
                }
            }
        }

        return repository.getConnection();
    }
    
    /**
    *
    * @return Connection
    * @throws SQLException
    */
    public static Connection getSimpleConnection() throws SQLException {

       String drv = GeneralConfig.getRequiredProperty(GeneralConfig.VIRTUOSO_DB_DRV);
       String url = GeneralConfig.getRequiredProperty(GeneralConfig.VIRTUOSO_DB_URL);
       String usr = GeneralConfig.getRequiredProperty(GeneralConfig.VIRTUOSO_DB_USR);
       String pwd = GeneralConfig.getRequiredProperty(GeneralConfig.VIRTUOSO_DB_PWD);

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
       }
       catch (ClassNotFoundException e) {
           throw new CRRuntimeException("Failed to get connection, driver class not found: " + drv, e);
       }
   }
}
