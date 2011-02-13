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
		
		String dataSourceName = GeneralConfig.getRequiredProperty(GeneralConfig.DATASOURCE_NAME);
		try{
			Context initContext = new InitialContext();
			Context context = (Context) initContext.lookup("java:comp/env");
			DbConnectionProvider.dataSource = (javax.sql.DataSource)context.lookup(dataSourceName);
		}
		catch (NamingException e){
			throw new CRRuntimeException("Failed to init JDBC resource " + dataSourceName, e);
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
	 * @throws SQLException
	 */
	private static Connection getSimpleConnection() throws SQLException{
		
		String drv = GeneralConfig.getProperty(GeneralConfig.DB_DRV);
		if (drv==null || drv.trim().length()==0)
			throw new SQLException("Failed to get connection, missing property: " + GeneralConfig.DB_DRV);
		
		String url = GeneralConfig.getProperty(GeneralConfig.DB_URL);
		if (url==null || url.trim().length()==0)
			throw new SQLException("Failed to get connection, missing property: " + GeneralConfig.DB_URL);

		String usr = GeneralConfig.getProperty(GeneralConfig.DB_USER_ID);
		if (usr==null || usr.trim().length()==0)
			throw new SQLException("Failed to get connection, missing property: " + GeneralConfig.DB_USER_ID);

		String pwd = GeneralConfig.getProperty(GeneralConfig.DB_USER_PWD);
		if (pwd==null || pwd.trim().length()==0)
			throw new SQLException("Failed to get connection, missing property: " + GeneralConfig.DB_USER_PWD);

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
