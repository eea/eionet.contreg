package eionet.cr.dao.mysql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.UrgentHarvestQueueDAO;
import eionet.cr.dto.UrgentHarvestQueueItemDTO;
import eionet.cr.dto.readers.HarvestQueueItemDTOReader;
import eionet.cr.harvest.scheduled.UrgentHarvestQueue;
import eionet.cr.util.sql.ConnectionUtil;
import eionet.cr.util.sql.SQLUtil;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class MySQLUrgentHarvestQueueDAO extends MySQLBaseDAO implements UrgentHarvestQueueDAO{

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HarvestQueueDAO#addQueueItem(eionet.cr.dto.HarvestQueueItemDTO)
	 */
	public void addPullHarvests(List<UrgentHarvestQueueItemDTO> queueItems) throws DAOException {
		
		String valueStr = "(?,NOW())";
		List<Object> values = new ArrayList<Object>();
		StringBuffer buf = new StringBuffer("insert ignore into URGENT_HARVEST_QUEUE (URL,TIMESTAMP) VALUES ");
		for (int i=0; i<queueItems.size(); i++){
			UrgentHarvestQueueItemDTO dto = queueItems.get(i);
			buf.append(i>0 ? "," : "").append(valueStr);
			values.add(dto.getUrl());
		}
		
		Connection conn = null;
		try{
			conn = getConnection();
			SQLUtil.executeUpdate(buf.toString(), values, conn);
		}
		catch (Exception e){
			throw new DAOException(e.getMessage(), e);
		}
		finally{
			ConnectionUtil.closeConnection(conn);
		}
	}

	/** */
	private static final String addPushHarvestSQL = "insert ignore into URGENT_HARVEST_QUEUE (URL,TIMESTAMP,PUSHED_CONTENT) VALUES (?,NOW(),?)";
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HarvestQueueDAO#addPushHarvest(eionet.cr.dto.HarvestQueueItemDTO)
	 */
	public void addPushHarvest(UrgentHarvestQueueItemDTO queueItem) throws DAOException {

		List<Object> values = new ArrayList<Object>();
		values.add(queueItem.getUrl());
		values.add(queueItem.getPushedContent());
		
		Connection conn = null;
		try{
			conn = getConnection();
			SQLUtil.executeUpdate(addPushHarvestSQL, values, conn);
		}
		catch (Exception e){
			throw new DAOException(e.getMessage(), e);
		}
		finally{
			ConnectionUtil.closeConnection(conn);
		}
	}

	/** */
	private static final String getUrgentHarvestQueueSQL = "select * from URGENT_HARVEST_QUEUE order by TIMESTAMP asc";
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HarvestQueueDAO#getUrgentHarvestQueue()
	 */
	public List<UrgentHarvestQueueItemDTO> getUrgentHarvestQueue() throws DAOException{
		
		List<Object> values = new ArrayList<Object>();
				
		Connection conn = null;
		HarvestQueueItemDTOReader rsReader = new HarvestQueueItemDTOReader();
		try{
			conn = getConnection();
			SQLUtil.executeQuery(getUrgentHarvestQueueSQL, values, rsReader, conn);
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
	 * @see eionet.cr.dao.UrgentHarvestQueueDAO#poll()
	 */
	public UrgentHarvestQueueItemDTO poll() throws DAOException {
		
		Connection conn = null;
		try{
			conn = getConnection();
			UrgentHarvestQueueItemDTO queueItem = peek(conn);
			if (queueItem!=null)
				deleteQueueItem(queueItem, conn);
			
			return queueItem;
		}
		catch (SQLException e){
			throw new DAOException(e.getMessage(), e);
		}
		finally{
			SQLUtil.close(conn);
		}
	}

	/** */
	private static final String peekSQL = "select * from URGENT_HARVEST_QUEUE order by TIMESTAMP asc limit 1";
	/**
	 * 
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	private static UrgentHarvestQueueItemDTO peek(Connection conn) throws SQLException{
		
		List<Object> values = new ArrayList<Object>();
				
		HarvestQueueItemDTOReader rsReader = new HarvestQueueItemDTOReader();
		SQLUtil.executeQuery(peekSQL, values, rsReader, conn);
		List<UrgentHarvestQueueItemDTO> list = rsReader.getResultList();
		
		return (list!=null && !list.isEmpty()) ? list.get(0) : null;
	}

	/** */
	private static final String deleteQueueItemSQL = "delete from URGENT_HARVEST_QUEUE where URL=? and TIMESTAMP=?";
	/**
	 * 
	 * @param queueItem
	 * @throws SQLException 
	 */
	private static void deleteQueueItem(UrgentHarvestQueueItemDTO queueItem, Connection conn) throws SQLException{
		
		List<Object> values = new ArrayList<Object>();
		values.add(queueItem.getUrl());
		values.add(queueItem.getTimeAdded());
		
		SQLUtil.executeUpdate(deleteQueueItemSQL, values, conn);
	}
}
