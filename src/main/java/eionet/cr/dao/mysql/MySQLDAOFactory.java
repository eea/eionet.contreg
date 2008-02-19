package eionet.cr.dao.mysql;

import eionet.cr.dao.DAOFactory;
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
	
	private HarvestSourceDAO harvestSourceDAO = new MySQLHarvestSourceDAO();
	

	 
	@Override
	public HarvestSourceDAO getHarvestSourceDAO() {
		return new MySQLHarvestSourceDAO();
	}
}
