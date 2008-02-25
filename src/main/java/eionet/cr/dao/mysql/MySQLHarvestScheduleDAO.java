package eionet.cr.dao.mysql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.HarvestScheduleDAO;
import eionet.cr.dto.HarvestScheduleDTO;
import eionet.cr.dto.readers.HarvestScheduleDTOReader;
import eionet.cr.util.sql.ConnectionUtil;
import eionet.cr.util.sql.SQLUtil;

/**
 * @author altnyris
 *
 */
public class MySQLHarvestScheduleDAO extends MySQLBaseDAO implements HarvestScheduleDAO {
	
	public MySQLHarvestScheduleDAO() {
	}
	
	/** */
	private static final String getScheduleByIdSQL = "select * from HARVEST_SCHEDULE where HARVEST_SOURCE_ID=?";
    
	/*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestScheduleDao#getHarvestScheduleBySourceId()
     */
    public HarvestScheduleDTO getHarvestScheduleBySourceId(int harvestSourceID) throws DAOException {
    	List<Object> values = new ArrayList<Object>();
    	values.add(new Integer(harvestSourceID));
				
		Connection conn = null;
		HarvestScheduleDTOReader rsReader = new HarvestScheduleDTOReader();
		try{
			conn = getConnection();
			SQLUtil.executeQuery(getScheduleByIdSQL, values, rsReader, conn);
			List<HarvestScheduleDTO>  list = rsReader.getResultList();
			return list!=null && list.size()>0 ? list.get(0) : null;
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
	private static final String addScheduleSQL = "insert into HARVEST_SCHEDULE (HARVEST_SOURCE_ID,WEEKDAY,HOUR,PERIOD_WEEKS) VALUES (?,?,?,?)";
	
	/*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestScheduleDao#addSchedule()
     */
    public void addSchedule(HarvestScheduleDTO schedule) throws DAOException {
    	    	
    	List<Object> values = new ArrayList<Object>();
		values.add(schedule.getHarvestSourceId());
		values.add(schedule.getWeekday());
		values.add(schedule.getHour());
		values.add(schedule.getPeriod());
		
		Connection conn = null;
		try{
			conn = getConnection();
			SQLUtil.executeUpdate(addScheduleSQL, values, conn);
		}
		catch (Exception e){
			throw new DAOException(e.getMessage(), e);
		}
		finally{
			ConnectionUtil.closeConnection(conn);
		}
    }
    
    /** */
	private static final String editScheduleSQL = "update HARVEST_SCHEDULE set WEEKDAY=?,HOUR=?,PERIOD_WEEKS=? where HARVEST_SOURCE_ID=?";
	
	/*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestScheduleDao#editSchedule()
     */
    public void editSchedule(HarvestScheduleDTO schedule) throws DAOException {
    	    	
    	List<Object> values = new ArrayList<Object>();
		values.add(schedule.getWeekday());
		values.add(schedule.getHour());
		values.add(schedule.getPeriod());
		values.add(schedule.getHarvestSourceId());
		
		Connection conn = null;
		try{
			conn = getConnection();
			SQLUtil.executeUpdate(editScheduleSQL, values, conn);
		}
		catch (Exception e){
			throw new DAOException(e.getMessage(), e);
		}
		finally{
			ConnectionUtil.closeConnection(conn);
		}
    }
}

