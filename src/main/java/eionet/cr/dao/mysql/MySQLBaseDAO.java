package eionet.cr.dao.mysql;

import java.sql.Connection;
import java.sql.SQLException;

import eionet.cr.dao.DAOException;
import eionet.cr.util.sql.MySQLUtil;

/**
 * 
 * @author heinljab
 *
 */
public abstract class MySQLBaseDAO {

	/**
	 * 
	 * @return
	 */
	protected Connection getConnection(){
		return MySQLDAOFactory.getConnection();
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
		}	}
}
