package eionet.cr.dao.mysql;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.HarvestMessageDAO;
import eionet.cr.dto.HarvestMessageDTO;
import eionet.cr.dto.readers.HarvestMessageDTOReader;
import eionet.cr.util.sql.ConnectionUtil;
import eionet.cr.util.sql.SQLUtil;

/**
 * 
 * @author heinljab
 *
 */
public class MySQLHarvestMessageDAO extends MySQLBaseDAO implements HarvestMessageDAO {

	/** */
	private static final String q_HarvestMessageByHarvestID = "select * from HARVEST_MESSAGE where HARVEST_ID=?";
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HarvestMessageDAO#findHarvestMessagesByHarvestID(java.lang.String)
	 */
	public List<HarvestMessageDTO> findHarvestMessagesByHarvestID(int harvestID) throws DAOException {
		
		List<Object> values = new ArrayList<Object>();
		values.add(new Integer(harvestID));
				
		Connection conn = null;
		HarvestMessageDTOReader rsReader = new HarvestMessageDTOReader();
		try{
			conn = getConnection();
			SQLUtil.executeQuery(q_HarvestMessageByHarvestID, values, rsReader, conn);
			List<HarvestMessageDTO>  list = rsReader.getResultList();
			return list;
		}
		catch (Exception e){
			throw new DAOException(e.getMessage(), e);
		}
		finally{
			closeConnection(conn);
		}
	}
	
	/** */
	private static final String q_HarvestMessageByMessageID = "select * from HARVEST_MESSAGE where HARVEST_MESSAGE_ID=?";
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HarvestMessageDAO#findHarvestMessageByMessageID(java.lang.String)
	 */
	public HarvestMessageDTO findHarvestMessageByMessageID(int messageID) throws DAOException {
		
		List<Object> values = new ArrayList<Object>();
		values.add(new Integer(messageID));
				
		Connection conn = null;
		HarvestMessageDTOReader rsReader = new HarvestMessageDTOReader();
		try{
			conn = getConnection();
			SQLUtil.executeQuery(q_HarvestMessageByMessageID, values, rsReader, conn);
			List<HarvestMessageDTO>  list = rsReader.getResultList();
			return list!=null && list.size()>0 ? list.get(0) : null;
		}
		catch (Exception e){
			throw new DAOException(e.getMessage(), e);
		}
		finally{
			closeConnection(conn);
		}
	}

	private static final String q_insertHarvestMessage =
		"insert into HARVEST_MESSAGE (HARVEST_ID, TYPE, MESSAGE, STACK_TRACE) values (?, ?, ?, ?)";
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HarvestMessageDAO#insertHarvestMessage(eionet.cr.dto.HarvestMessageDTO)
	 */
	public Integer insertHarvestMessage(HarvestMessageDTO harvestMessageDTO) throws DAOException {
		
		Integer harvestMessageID = null;
		
		if (harvestMessageDTO==null)
			return null;
		
		List<Object> values = new ArrayList<Object>();
		values.add(harvestMessageDTO.getHarvestId());
		values.add(harvestMessageDTO.getType());
		values.add(harvestMessageDTO.getMessage());
		values.add(harvestMessageDTO.getStackTrace());
		
		Connection conn = null;
		try{
			conn = getConnection();
			SQLUtil.executeUpdate(q_insertHarvestMessage, values, conn);
			harvestMessageID = getLastInsertID(conn);
			
			return harvestMessageID;
		}
		catch (Exception e){
			throw new DAOException(e.getMessage(), e);
		}
		finally{
			closeConnection(conn);
		}
	}
	
	/** */
	private static final String deleteHarvestMessageSQL = "delete from HARVEST_MESSAGE where HARVEST_MESSAGE_ID=?";
	
	/*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestMessageDao#deleteMessage()
     */
    public void deleteMessage(Integer messageId) throws DAOException {
    	    	
    	List<Object> values = new ArrayList<Object>();
		values.add(messageId);
		
		Connection conn = null;
		try{
			conn = getConnection();
			SQLUtil.executeUpdate(deleteHarvestMessageSQL, values, conn);
		}
		catch (Exception e){
			throw new DAOException(e.getMessage(), e);
		}
		finally{
			ConnectionUtil.closeConnection(conn);
		}
    }


}
