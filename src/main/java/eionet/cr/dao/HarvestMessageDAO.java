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
	 * @return
	 * @throws DAOException
	 */
	public HarvestMessageDTO findHarvestMessageByMessageID(int messageID) throws DAOException;
	
	/**
	 * 
	 * @param harvestMessageDTO
	 * @throws DAOException
	 */
	public Integer insertHarvestMessage(HarvestMessageDTO harvestMessageDTO) throws DAOException;
	
	/**
	 * 
	 * @param messageId
	 * @throws DAOException
	 */
	public void deleteMessage(Integer messageId) throws DAOException;
}
