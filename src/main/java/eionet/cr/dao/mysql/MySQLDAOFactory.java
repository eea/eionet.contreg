package eionet.cr.dao.mysql;

import java.sql.Connection;

import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.SampleDAO;

/**
 * 
 * @author heinljab
 *
 */
public class MySQLDAOFactory extends DAOFactory {

	/**
	 * 
	 */
	public MySQLDAOFactory(){
	}
	
	/**
	 * 
	 * @return
	 */
	public static Connection getConnection(){
		// TODO - this must return a real java.sql.Connection object
		return null;
	}
	
	@Override
	public SampleDAO getSampleDAO() {
		return new MySQLSampleDAO();
	}
}
