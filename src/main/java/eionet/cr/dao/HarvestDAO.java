package eionet.cr.dao;

/**
 * 
 * @author heinljab
 *
 */
public interface HarvestDAO {

	/**
	 * 
	 * @param harvestDTO
	 */
	public int insertStartedHarvest(int harvestSourceId, String harvestType, String user, String status) throws DAOException;
	
	/**
	 * 
	 * @param harvestDTO
	 * @throws DAOException
	 */
	public void updateFinishedHarvest(int harvestId, int totStatements,
			int litStatements, int resStatements, int totResources, int encSchemes, String messages) throws DAOException;
}
