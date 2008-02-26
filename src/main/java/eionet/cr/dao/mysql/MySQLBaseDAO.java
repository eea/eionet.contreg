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
	protected Connection getConnection() throws DataSourceException{
		return ConnectionUtil.getJNDIConnection();
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
	 * @throws SQLException 
	 * @throws DAOException
	 */
	protected Integer getLastInsertID(Connection conn) throws SQLException{
		return MySQLUtil.getLastInsertID(conn);
	}
}
