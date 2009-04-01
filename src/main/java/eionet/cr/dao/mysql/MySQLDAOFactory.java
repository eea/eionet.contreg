package eionet.cr.dao.mysql;

import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestDAO;
import eionet.cr.dao.HarvestMessageDAO;
import eionet.cr.dao.UrgentHarvestQueueDAO;
import eionet.cr.dao.HarvestSourceDAO;

/**
 * 
 * @author heinljab, altnyris
 *
 */
public class MySQLDAOFactory extends DAOFactory {
	

	/**
	 * 
	 */
	public MySQLDAOFactory(){
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.DAOFactory#getHarvestSourceDAO()
	 */
	public HarvestSourceDAO getHarvestSourceDAO() {
		return new MySQLHarvestSourceDAO();
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.DAOFactory#getHarvestDAO()
	 */
	public HarvestDAO getHarvestDAO() {
		return new MySQLHarvestDAO();	}
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.DAOFactory#getHarvestMessageDAO()
	 */
	public HarvestMessageDAO getHarvestMessageDAO() {
		return new MySQLHarvestMessageDAO();
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.DAOFactory#getHarvestQueueDAO()
	 */
	public UrgentHarvestQueueDAO getUrgentHarvestQueueDAO() {
		return new MySQLUrgentHarvestQueueDAO();
	}
}
