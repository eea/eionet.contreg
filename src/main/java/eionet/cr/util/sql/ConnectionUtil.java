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
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eionet.cr.common.CRRuntimeException;
import eionet.cr.config.GeneralConfig;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class ConnectionUtil {
	
	/** */
	private static Log logger = LogFactory.getLog(ConnectionUtil.class);

	/** */
	private static DataSource dataSource = null;
	private static boolean returnSimpleConnection = false;
	
	/**
	 * 
	 */
	private static void initDataSource(){
		
		String dataSourceName = GeneralConfig.getRequiredProperty(GeneralConfig.DATASOURCE_NAME);
		try{
			Context initContext = new InitialContext();
			Context context = (Context) initContext.lookup("java:comp/env");
			ConnectionUtil.dataSource = (javax.sql.DataSource)context.lookup(dataSourceName);
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
		if(ConnectionUtil.returnSimpleConnection)
			return getSimpleConnection();
		else
			return getJNDIConnection();
	}

	/**
	 * 
	 * @return
	 * @throws SQLException 
	 */
	protected static synchronized Connection getJNDIConnection() throws SQLException{
		
		if (dataSource==null)
			initDataSource();
		return dataSource.getConnection();
	}
	
	/**
	 * 
	 * @return
	 * @throws SQLException 
	 * @throws SQLException
	 */
	protected static Connection getSimpleConnection() throws SQLException{
		
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
	 * @param conn
	 */
	public static void closeConnection(Connection conn) {
		try {
			if (conn!=null && !conn.isClosed())
				conn.close();
		}
		catch (SQLException e) {
			logger.error("Failed to close connection", e);
		}
	}

	/**
	 * 
	 * @return
	 */
	public static boolean isReturnSimpleConnection() {
		return returnSimpleConnection;
	}

	/**
	 * 
	 * @param testConnection
	 */
	public static void setReturnSimpleConnection(boolean b) {
		ConnectionUtil.returnSimpleConnection = b;
	}

}
