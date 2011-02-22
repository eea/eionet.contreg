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

import eionet.cr.common.CRRuntimeException;
import eionet.cr.config.GeneralConfig;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class DbConnectionProvider {
	
	/** */
	public enum ConnectionType {SIMPLE,JNDI};

	/** */
	private static final String DATASOURCE_NAME = "jdbc/cr";
	
	/** */
	private static DataSource dataSource = null;
	private static ConnectionType connectionType = null;
	private static String connectionUrl = null;
	
	/** Lock objects */
	private static Object dataSourceLock = new Object();
	private static Object connectionTypeLock = new Object();
	private static Object connectionUrlLock = new Object();
	
	/**
	 * 
	 */
	private static void initDataSource(){
		
		try{
			Context initContext = new InitialContext();
			Context context = (Context) initContext.lookup("java:comp/env");
			DbConnectionProvider.dataSource = (javax.sql.DataSource)context.lookup(DATASOURCE_NAME);
		}
		catch (NamingException e){
			throw new CRRuntimeException("Failed to init JDBC resource " + DATASOURCE_NAME, e);
		}
	}
	
	/**
	 * 
	 * @return
	 * @throws SQLException 
	 */
	public static Connection getConnection() throws SQLException {
		
		if (connectionType==null){
			return getJNDIConnection();
		}
		else if (connectionType.equals(ConnectionType.JNDI)){
			return getJNDIConnection();
		}
		else if (connectionType.equals(ConnectionType.SIMPLE)){
			return getSimpleConnection();
		}
		else
			throw new CRRuntimeException("Unknown connection type: " + connectionType);
	}
	
	/**
	 * 
	 * @return
	 * @throws SQLException 
	 */
	private static Connection getJNDIConnection() throws SQLException{
		
		if (dataSource==null){
			synchronized (dataSourceLock) {
				
				// double-checked locking pattern
				// (http://www.ibm.com/developerworks/java/library/j-dcl.html)
				if (dataSource==null){
					initDataSource();
				}
			}			
		}
		
		return dataSource.getConnection();
	}
	
	/**
	 * 
	 * @return
	 * @throws SQLException
	 */
	public static Connection getUnitTestConnection() throws SQLException{
		
		DbConnectionProvider.setConnectionType(DbConnectionProvider.ConnectionType.SIMPLE);
		return getSimpleConnection(true);
	}

	/**
	 * 
	 * @return
	 * @throws SQLException
	 */
	private static Connection getSimpleConnection() throws SQLException{

		// see if this code is being executed at Maven's test time right now
		String mavenPhase = System.getProperty("contreg.maven.phase");
		boolean isUnitTest = mavenPhase!=null && mavenPhase.trim().equals("test");

		return getSimpleConnection(isUnitTest);
	}
	/**
	 * 
	 * @return
	 * @throws SQLException
	 */
	private static Connection getSimpleConnection(boolean isUnitTest) throws SQLException{
		
		// property names depending on whether it's Maven unit test currently
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
	public static ConnectionType getConnectionType() {
		return connectionType;
	}

	/**
	 * 
	 * @param testConnection
	 */
	public static void setConnectionType(ConnectionType connType){
		
		if (connectionType==null){
			
			synchronized (connectionTypeLock) {
				
				// double-checked locking pattern
				// (http://www.ibm.com/developerworks/java/library/j-dcl.html)
				if (connectionType==null){
					connectionType = connType;
				}
			}			
		}
		else{
			//It's ok, do nothing
			//throw new CRRuntimeException("Connection type already set!");
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
}
