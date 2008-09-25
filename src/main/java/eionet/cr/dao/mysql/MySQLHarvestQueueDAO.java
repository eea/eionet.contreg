package eionet.cr.dao.mysql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.HarvestQueueDAO;
import eionet.cr.dto.HarvestQueueItemDTO;
import eionet.cr.dto.readers.HarvestQueueItemDTOReader;
import eionet.cr.util.sql.SQLUtil;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class MySQLHarvestQueueDAO extends MySQLBaseDAO implements HarvestQueueDAO{

	/** */
	private static final String getHarvestQueueByPriority = "select * from HARVEST_QUEUE where PRIORITY = ? order by TIMESTAMP asc";

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HarvestQueueDAO#getNormalHarvestQueue()
	 */
	public List<HarvestQueueItemDTO> getNormalHarvestQueue() throws DAOException {
		
		List<Object> values = new ArrayList<Object>();
    	values.add(HarvestQueueItemDTO.PRIORITY_NORMAL);
				
		Connection conn = null;
		HarvestQueueItemDTOReader rsReader = new HarvestQueueItemDTOReader();
		try{
			conn = getConnection();
			SQLUtil.executeQuery(getHarvestQueueByPriority, values, rsReader, conn);
			return rsReader.getResultList();
		}
		catch (Exception e){
			throw new DAOException(e.getMessage(), e);
		}
		finally{
			try{
				if (conn!=null) conn.close();
			}
			catch (SQLException e){}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HarvestQueueDAO#getUrgentHarvestQueue()
	 */
	public List<HarvestQueueItemDTO> getUrgentHarvestQueue() throws DAOException{
		
		List<Object> values = new ArrayList<Object>();
    	values.add(HarvestQueueItemDTO.PRIORITY_URGENT);
				
		Connection conn = null;
		HarvestQueueItemDTOReader rsReader = new HarvestQueueItemDTOReader();
		try{
			conn = getConnection();
			SQLUtil.executeQuery(getHarvestQueueByPriority, values, rsReader, conn);
			return rsReader.getResultList();
		}
		catch (Exception e){
			throw new DAOException(e.getMessage(), e);
		}
		finally{
			try{
				if (conn!=null) conn.close();
			}
			catch (SQLException e){}
		}
	}
}
