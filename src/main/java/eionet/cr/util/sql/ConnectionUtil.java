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
	public static synchronized Connection getJNDIConnection() throws SQLException{
		
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
	public static Connection getSimpleConnection() throws SQLException{
		
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
