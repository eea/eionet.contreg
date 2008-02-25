package eionet.cr.dao.mysql;

import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestScheduleDAO;
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
	private HarvestScheduleDAO harvestScheduleDAO = new MySQLHarvestScheduleDAO();
	

	 
	@Override
	public HarvestSourceDAO getHarvestSourceDAO() {
		return new MySQLHarvestSourceDAO();
	}
	
	@Override
	public HarvestScheduleDAO getHarvestScheduleDAO() {
		return new MySQLHarvestScheduleDAO();
	}
}
