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
package eionet.cr.util.sql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import eionet.cr.common.CRRuntimeException;
import eionet.cr.config.GeneralConfig;
import eionet.cr.util.Util;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class DbConnectionProvider {

    /** */
    private static Logger logger = Logger.getLogger(DbConnectionProvider.class);

    /** */
    private static final String DATASOURCE_NAME = "jdbc/cr";

    /** */
    private static DataSource dataSource = null;
    private static String connectionUrl = null;
    private static Boolean isJNDIDataSource = null;
    private static Boolean isJUnitRuntime = null;

    /** Lock objects */
    private static Object connectionUrlLock = new Object();
    private static Object isJNDIDataSourceLock = new Object();
    private static Object isJUnitRuntimeLock = new Object();

    /**
     *
     * @return
     * @throws SQLException
     */
    public static Connection getConnection() throws SQLException {

        if (isJNDIDataSource()){
            return dataSource.getConnection();
        }
        else{
            return getSimpleConnection(isJUnitRuntime());
        }
    }

    /**
     *
     * @return
     * @throws SQLException
     */
    private static Connection getSimpleConnection(boolean isUnitTest) throws SQLException{

        // property names depending on whether the code is being run by a unit test
        // (this is just to avoid shooting in the leg by running unit tests
        // accidentally against the real database)
        String drvProperty = isUnitTest ? GeneralConfig.DB_UNITTEST_DRV : GeneralConfig.DB_DRV;
        String urlProperty = isUnitTest ? GeneralConfig.DB_UNITTEST_URL : GeneralConfig.DB_URL;
        String usrProperty = isUnitTest ? GeneralConfig.DB_UNITTEST_USR : GeneralConfig.DB_USR;
        String pwdProperty = isUnitTest ? GeneralConfig.DB_UNITTEST_PWD : GeneralConfig.DB_PWD;

        String drv = GeneralConfig.getProperty(drvProperty);
        if (drv==null || drv.trim().length()==0){
            throw new SQLException("Failed to get connection, missing property: " + drvProperty);
        }

        String url = GeneralConfig.getProperty(urlProperty);
        if (url==null || url.trim().length()==0){
            throw new SQLException("Failed to get connection, missing property: " + urlProperty);
        }

        String usr = GeneralConfig.getProperty(usrProperty);
        if (usr==null || usr.trim().length()==0){
            throw new SQLException("Failed to get connection, missing property: " + usrProperty);
        }

        String pwd = GeneralConfig.getProperty(pwdProperty);
        if (pwd==null || pwd.trim().length()==0){
            throw new SQLException("Failed to get connection, missing property: " + pwdProperty);
        }

        try{
            Class.forName(drv);
            return DriverManager.getConnection(url, usr, pwd);
        }
        catch (ClassNotFoundException e){
            throw new CRRuntimeException("Failed to get connection, driver class not found: " + drv, e);
        }
    }

    /**
     *
     * @return
     */
    public static String getConnectionUrl(){

        if (connectionUrl==null || connectionUrl.trim().length()==0){

            synchronized (connectionUrlLock) {

                // double-checked locking pattern
                // (http://www.ibm.com/developerworks/java/library/j-dcl.html)
                if (connectionUrl==null || connectionUrl.trim().length()==0){

                    Connection conn = null;
                    try{
                        conn = getConnection();
                        DatabaseMetaData dbMetadata = conn.getMetaData();
                        connectionUrl = dbMetadata.getURL();
                    }
                    catch (SQLException sqle){
                        throw new CRRuntimeException("Failed to look up database url!", sqle);
                    }
                    finally{
                        if (conn!=null){
                            try{ conn.close(); } catch (Exception e){}
                        }
                    }
                }
            }
        }

        return connectionUrl;
    }

    /**
     *
     * @return
     */
    private static boolean isJNDIDataSource(){

        if (isJNDIDataSource==null){
            synchronized (isJNDIDataSourceLock) {

                // double-checked locking pattern
                // (http://www.ibm.com/developerworks/java/library/j-dcl.html)
                if (isJNDIDataSource==null){

                    try{
                        Context initContext = new InitialContext();
                        Context context = (Context) initContext.lookup("java:comp/env");
                        dataSource = (javax.sql.DataSource)context.lookup(DATASOURCE_NAME);

                        isJNDIDataSource = Boolean.TRUE;
                        logger.info("Found and initialized JNDI data source named " + DATASOURCE_NAME);
                    }
                    catch (NamingException e){
                        isJNDIDataSource = Boolean.FALSE;
                        logger.info("No JNDI data source named " + DATASOURCE_NAME + " could be found: " + e.toString());
                    }
                }
            }
        }

        return isJNDIDataSource.booleanValue();
    }

    /**
     *
     * @return
     */
    private static boolean isJUnitRuntime(){

        if (isJUnitRuntime==null){

            synchronized (isJUnitRuntimeLock) {

                // double-checked locking pattern
                // (http://www.ibm.com/developerworks/java/library/j-dcl.html)
                if (isJUnitRuntime==null){

                    String stackTrace = Util.getStackTrace(new Throwable());
                    isJUnitRuntime = Boolean.valueOf(stackTrace.indexOf("at junit.framework.TestCase.run") > 0);

                    logger.info("Detected that the code is running in JUnit runtime");
                }
            }
        }

        return isJUnitRuntime.booleanValue();
    }

}
