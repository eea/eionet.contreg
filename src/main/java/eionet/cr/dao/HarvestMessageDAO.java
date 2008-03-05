package eionet.cr.dao;

import java.util.List;

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
	public List<HarvestMessageDTO> findHarvestMessagesByHarvestID(int harvestID) throws DAOException;
	
	/**
	 * 
	 * @param harvestMessageDTO
	 * @throws DAOException
	 */
	public void insertHarvestMessage(HarvestMessageDTO harvestMessageDTO) throws DAOException;
}
