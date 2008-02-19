package eionet.cr.dao;

import eionet.cr.dto.HarvestMessageDTO;

/**
 * 
 * @author heinljab
 *
 */
public interface HarvestMessageDAO {

	/**
	 * 
	 * @return
	 * @throws DAOException
	 */
	public HarvestMessageDTO findHarvestMessageByHarvestID(int harvestID) throws DAOException;
	
	/**
	 * 
	 * @param harvestMessageDTO
	 * @throws DAOException
	 */
	public void insertHarvestMessage(HarvestMessageDTO harvestMessageDTO) throws DAOException;
}
