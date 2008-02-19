package eionet.cr.util.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;

/**
 * 
 * @author heinljab
 *
 */
public class ConnectionUtil {
	
	/** */
	private static Logger logger = Logger.getLogger(ConnectionUtil.class);

	/** */
	private static DataSource dataSource = null;
	
	/**
	 * 
	 * @throws NamingException 
	 * @throws DAOException
	 */
	private static void initDataSource() throws NamingException{
		Context initContext = new InitialContext();
		Context context = (Context) initContext.lookup("java:comp/env");
		dataSource = (javax.sql.DataSource)context.lookup("jdbc/cr");
	}

	/**
	 * 
	 * @return
	 * @throws SQLException 
	 * @throws NamingException 
	 */
	public static synchronized Connection getJNDIConnection() throws SQLException{
		
		if (dataSource==null){
			try{
				initDataSource();
			}
			catch (NamingException e){
				throw new SQLException("Failed to get connection through JNDI: " + e.toString(), e);
			}
		}
		
		return dataSource.getConnection();
	}
	
	/**
	 * 
	 * @return
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
			throw new SQLException("Failed to get connection, driver class not found: " + drv, e);
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
}
