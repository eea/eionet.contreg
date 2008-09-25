package eionet.cr.dao.mysql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.HarvestQueueDAO;
import eionet.cr.dto.HarvestQueueItemDTO;
import eionet.cr.dto.readers.HarvestQueueItemDTOReader;
import eionet.cr.harvest.scheduled.HarvestQueue;
import eionet.cr.util.sql.ConnectionUtil;
import eionet.cr.util.sql.SQLUtil;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class MySQLHarvestQueueDAO extends MySQLBaseDAO implements HarvestQueueDAO{

	/** */
	private static final String addPullHarvestSQL = "insert into HARVEST_QUEUE (URL,PRIORITY,TIMESTAMP) VALUES (?,?,NOW())";
	private static final String addPushHarvestSQL = "insert into HARVEST_QUEUE (URL,PRIORITY,TIMESTAMP,PUSHED_CONTENT) VALUES (?,?,NOW(),?)";
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HarvestQueueDAO#addQueueItem(eionet.cr.dto.HarvestQueueItemDTO)
	 */
	public void addPullHarvest(HarvestQueueItemDTO queueItem) throws DAOException {
		
		List<Object> values = new ArrayList<Object>();
		values.add(queueItem.getUrl());
		values.add(queueItem.getPriority());
		
		Connection conn = null;
		try{
			conn = getConnection();
			SQLUtil.executeUpdate(addPullHarvestSQL, values, conn);
		}
		catch (Exception e){
			throw new DAOException(e.getMessage(), e);
		}
		finally{
			ConnectionUtil.closeConnection(conn);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HarvestQueueDAO#addPushHarvest(eionet.cr.dto.HarvestQueueItemDTO)
	 */
	public void addPushHarvest(HarvestQueueItemDTO queueItem) throws DAOException {

		List<Object> values = new ArrayList<Object>();
		values.add(queueItem.getUrl());
		values.add(queueItem.getPriority());
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
	private static final String getHarvestQueueByPrioritySQL = "select * from HARVEST_QUEUE where PRIORITY = ? order by TIMESTAMP asc";

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HarvestQueueDAO#getNormalHarvestQueue()
	 */
	public List<HarvestQueueItemDTO> getNormalHarvestQueue() throws DAOException {
		
		List<Object> values = new ArrayList<Object>();
    	values.add(HarvestQueue.PRIORITY_NORMAL);
				
		Connection conn = null;
		HarvestQueueItemDTOReader rsReader = new HarvestQueueItemDTOReader();
		try{
			conn = getConnection();
			SQLUtil.executeQuery(getHarvestQueueByPrioritySQL, values, rsReader, conn);
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
    	values.add(HarvestQueue.PRIORITY_URGENT);
				
		Connection conn = null;
		HarvestQueueItemDTOReader rsReader = new HarvestQueueItemDTOReader();
		try{
			conn = getConnection();
			SQLUtil.executeQuery(getHarvestQueueByPrioritySQL, values, rsReader, conn);
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

	/** */
	private static final String pollByPrioritySQL = "select * from HARVEST_QUEUE where PRIORITY = ? order by TIMESTAMP asc limit 1";
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HarvestQueueDAO#pollNormal()
	 */
	public HarvestQueueItemDTO pollNormal() throws DAOException {
		
		List<Object> values = new ArrayList<Object>();
    	values.add(HarvestQueue.PRIORITY_NORMAL);
				
		Connection conn = null;
		HarvestQueueItemDTOReader rsReader = new HarvestQueueItemDTOReader();
		try{
			conn = getConnection();
			SQLUtil.executeQuery(pollByPrioritySQL, values, rsReader, conn);
			List<HarvestQueueItemDTO> list = rsReader.getResultList();
			
			HarvestQueueItemDTO queueItem = (list!=null && !list.isEmpty()) ? list.get(0) : null;
			if (queueItem!=null)
				deleteQueueItem(queueItem, conn);
			
			return queueItem;
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
	 * @see eionet.cr.dao.HarvestQueueDAO#pollUrgent()
	 */
	public HarvestQueueItemDTO pollUrgent() throws DAOException {
		
		List<Object> values = new ArrayList<Object>();
    	values.add(HarvestQueue.PRIORITY_URGENT);
				
		Connection conn = null;
		HarvestQueueItemDTOReader rsReader = new HarvestQueueItemDTOReader();
		try{
			conn = getConnection();
			SQLUtil.executeQuery(pollByPrioritySQL, values, rsReader, conn);
			List<HarvestQueueItemDTO> list = rsReader.getResultList();
			
			HarvestQueueItemDTO queueItem = (list!=null && !list.isEmpty()) ? list.get(0) : null;
			if (queueItem!=null)
				deleteQueueItem(queueItem, conn);
			
			return queueItem;
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

	/** */
	private static final String deleteQueueItemSQL = "delete from HARVEST_QUEUE where URL=? and PRIORITY=? and TIMESTAMP=?";

	/**
	 * 
	 * @param queueItem
	 * @throws SQLException 
	 */
	private void deleteQueueItem(HarvestQueueItemDTO queueItem, Connection conn) throws SQLException{
		
		List<Object> values = new ArrayList<Object>();
		values.add(queueItem.getUrl());
		values.add(queueItem.getPriority());
		values.add(queueItem.getTimeAdded());
		
		SQLUtil.executeUpdate(deleteQueueItemSQL, values, conn);
	}
}
