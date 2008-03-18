package eionet.cr.dao;

import java.util.List;

import eionet.cr.dto.HarvestDTO;

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
	public void updateFinishedHarvest(int harvestId, String status, int totStatements,
			int litStatements, int totResources, int encSchemes) throws DAOException;
	
	/**
	 * 
	 * @param harvestSourceId
	 */
	public List<HarvestDTO> getHarvestsBySourceId(Integer harvestSourceId) throws DAOException;

	/**
	 * 
	 * @param harvestId
	 * @return
	 * @throws DAOException
	 */
    public HarvestDTO getHarvestById(Integer harvestId) throws DAOException;
}
