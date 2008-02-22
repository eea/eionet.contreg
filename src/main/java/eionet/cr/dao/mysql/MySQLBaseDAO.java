package eionet.cr.dao.mysql;

import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.util.sql.ConnectionUtil;
import eionet.cr.util.sql.DataSourceException;
import eionet.cr.util.sql.MySQLUtil;

/**
 * 
 * @author heinljab, altnyris
 *
 */
public abstract class MySQLBaseDAO {
	
	/**
	 * 
	 * @return
	 */
	protected Connection getConnection() throws DAOException{
		try{
			return ConnectionUtil.getJNDIConnection();
		}
		catch (DataSourceException e){
			throw new DAOException(e.toString(), e);
		}

	}
	
	/**
	 * 
	 * @param conn
	 */
	protected void closeConnection(Connection conn){
		ConnectionUtil.closeConnection(conn);
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
