package eionet.cr.dao.mysql;

import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.util.sql.MySQLUtil;

/**
 * 
 * @author heinljab, altnyris
 *
 */
public abstract class MySQLBaseDAO {
	
	private static DataSource ds = null;
	
	public static void initDatasource() throws DAOException{
		try{
			Context initContext = new InitialContext();
			Context context = (Context) initContext.lookup("java:comp/env");
			String jndiName = "jdbc/" + new GeneralConfig().getStringProperty(GeneralConfig.JDBC_NAME);
			ds = (javax.sql.DataSource)context.lookup(jndiName);
		}
		catch (Exception e){
			throw new DAOException("Connection pool initialization failed - " + e.toString());
		}
	}

	/**
	 * 
	 * @return
	 */
	public static Connection getConnection() throws DAOException{
		if (ds==null) initDatasource();
		
		try{
			return ds.getConnection();
		}
		catch (Exception e){
			throw new DAOException("Failed to get connection from pool - " + e.toString());
		}

	}
	
	public static void closeConnection(Connection conn) {
		try {
			if ((conn != null) && (!conn.isClosed())) {
				conn.close();
				conn = null;
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @return
	 * @throws DAOException
	 */
	protected Integer getLastInsertID() throws DAOException{
		Connection conn = getConnection();
		try{
			return MySQLUtil.getLastInsertID(conn);
		}
		catch (SQLException e){
			throw new DAOException(e.getMessage(), e);
		}
		finally{
			try{
				if (conn!=null) conn.close();
			}
			catch (SQLException e){}
		}	
	}
}
