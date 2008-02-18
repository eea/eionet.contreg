package eionet.cr.dao;

import eionet.cr.dao.mysql.MySQLDAOFactory;

public abstract class DAOFactory {

	/** */
	public static final int MYSQL = 1;
	
	/**
	 * 
	 * @return
	 */
	public abstract SampleDAO getSampleDAO();
	
	/**
	 * 
	 * @param whichFactory
	 * @return
	 */
	public static DAOFactory getDAOFactory(int whichFactory) {
		
		switch (whichFactory) {
	      case MYSQL: 
	          return new MySQLDAOFactory();
	      default : 
	          return null;
	    }
	}
	
	/**
	 * Convenience method that always returns DAO factory for MySQL.  
	 * @return
	 */
	public static DAOFactory getDAOFactory() {
		return getDAOFactory(MYSQL);
	}
}
