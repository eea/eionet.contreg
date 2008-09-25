package eionet.cr.dao.mysql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dto.HarvestDTO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.readers.HarvestDTOReader;
import eionet.cr.dto.readers.HarvestSourceDTOReader;
import eionet.cr.util.Util;
import eionet.cr.util.YesNoBoolean;
import eionet.cr.util.sql.ConnectionUtil;
import eionet.cr.util.sql.SQLUtil;
import eionet.cr.util.sql.SQLValue;

/**
 * @author altnyris
 *
 */
public class MySQLHarvestSourceDAO extends MySQLBaseDAO implements HarvestSourceDAO {
	
	/**
	 * 
	 */
	public MySQLHarvestSourceDAO() {
	}
	
	/** */
	private static final String getSourcesSQL = "select * from HARVEST_SOURCE order by TYPE desc, URL";
		
	/*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestSourceDao#getHarvestSources()
     */
    public List<HarvestSourceDTO> getHarvestSources() throws DAOException {
    	List<Object> values = new ArrayList<Object>();
				
		Connection conn = null;
		HarvestSourceDTOReader rsReader = new HarvestSourceDTOReader();
		try{
			conn = getConnection();
			SQLUtil.executeQuery(getSourcesSQL, values, rsReader, conn);
			List<HarvestSourceDTO>  list = rsReader.getResultList();
			return list;
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
    private static final String getSourcesByTypeSQL = "select * from HARVEST_SOURCE where TYPE=? order by TYPE desc, URL";
    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.HarvestSourceDAO#getHarvestSourcesByType()
     */
    public List<HarvestSourceDTO> getHarvestSourcesByType(String type) throws DAOException {
    	
    	List<Object> values = new ArrayList<Object>();
    	values.add(type);
				
		Connection conn = null;
		HarvestSourceDTOReader rsReader = new HarvestSourceDTOReader();
		try{
			conn = getConnection();
			SQLUtil.executeQuery(getSourcesByTypeSQL, values, rsReader, conn);
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
	private static final String getSourcesByIdSQL = "select * from HARVEST_SOURCE where HARVEST_SOURCE_ID=?";
	
	/*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestSourceDao#getHarvestSourceById()
     */
    public HarvestSourceDTO getHarvestSourceById(Integer harvestSourceID) throws DAOException {
    	List<Object> values = new ArrayList<Object>();
    	values.add(harvestSourceID);
				
		Connection conn = null;
		HarvestSourceDTO source = null;
		HarvestSourceDTOReader rsReader = new HarvestSourceDTOReader();
		try{
			conn = getConnection();
			SQLUtil.executeQuery(getSourcesByIdSQL, values, rsReader, conn);
			List<HarvestSourceDTO>  list = rsReader.getResultList();
			return (list!=null && !list.isEmpty()) ? list.get(0) : null;
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
	private static final String getSourcesByUrlSQL = "select * from HARVEST_SOURCE where URL=?";

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.HarvestSourceDAO#getHarvestSourceByUrl(java.lang.String)
     */
	public HarvestSourceDTO getHarvestSourceByUrl(String url) throws DAOException {
		
    	List<Object> values = new ArrayList<Object>();
    	values.add(url);
				
		Connection conn = null;
		HarvestSourceDTO source = null;
		HarvestSourceDTOReader rsReader = new HarvestSourceDTOReader();
		try{
			conn = getConnection();
			SQLUtil.executeQuery(getSourcesByUrlSQL, values, rsReader, conn);
			List<HarvestSourceDTO>  list = rsReader.getResultList();
			return (list!=null && !list.isEmpty()) ? list.get(0) : null;
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
	private static final String addSourceSQL = "insert into HARVEST_SOURCE (NAME,URL,TYPE,EMAILS,DATE_CREATED,CREATOR,SCHEDULE_CRON) VALUES (?,?,?,?,NOW(),?,?)";
	private static final String addSourceIgnoreSQL = "insert into HARVEST_SOURCE (NAME,URL,TYPE,EMAILS,DATE_CREATED,CREATOR,SCHEDULE_CRON) VALUES (?,?,?,?,NOW(),?,?) on duplicate key update HARVEST_SOURCE_ID=LAST_INSERT_ID(HARVEST_SOURCE_ID)";

	/*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestSourceDao#addSource()
     */
    public Integer addSource(HarvestSourceDTO source, String user) throws DAOException {
    	return addSource(addSourceSQL, source, user);
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.HarvestSourceDAO#addSourceIgnoreDuplicate(eionet.cr.dto.HarvestSourceDTO, java.lang.String)
     */
	public Integer addSourceIgnoreDuplicate(HarvestSourceDTO source, String user) throws DAOException {
		return addSource(addSourceIgnoreSQL, source, user);
	}

	/**
	 * 
	 * @param sql
	 * @param source
	 * @param user
	 * @return
	 * @throws DAOException 
	 */
	private Integer addSource(String sql, HarvestSourceDTO source, String user) throws DAOException{
		
		Integer harvestSourceID = null;
    	
    	List<Object> values = new ArrayList<Object>();
		values.add(source.getName());
		values.add(source.getUrl());
		values.add(source.getType());
		values.add(source.getEmails());
		values.add(user);
		values.add(source.getScheduleCron());
		
		Connection conn = null;
		try{
			conn = getConnection();
			SQLUtil.executeUpdate(sql, values, conn);
			harvestSourceID = getLastInsertID(conn);
			return harvestSourceID;
		}
		catch (Exception e){
			throw new DAOException(e.getMessage(), e);
		}
		finally{
			ConnectionUtil.closeConnection(conn);
		}
	}

    /** */
	private static final String editSourceSQL = "update HARVEST_SOURCE set NAME=?,URL=?,TYPE=?,EMAILS=?,SCHEDULE_CRON=? where HARVEST_SOURCE_ID=?";
	
	/*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestSourceDao#editSource()
     */
    public void editSource(HarvestSourceDTO source) throws DAOException {
    	    	
    	List<Object> values = new ArrayList<Object>();
		values.add(source.getName());
		values.add(source.getUrl());
		values.add(source.getType());
		values.add(source.getEmails());
		values.add(source.getScheduleCron());
		values.add(source.getSourceId());
		
		Connection conn = null;
		try{
			conn = getConnection();
			SQLUtil.executeUpdate(editSourceSQL, values, conn);
		}
		catch (Exception e){
			throw new DAOException(e.getMessage(), e);
		}
		finally{
			ConnectionUtil.closeConnection(conn);
		}
    }
    
    /** */
	private static final String deleteSourceSQL = "delete from HARVEST_SOURCE where HARVEST_SOURCE_ID=?";
	
	/*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestSourceDao#deleteSource()
     */
    public void deleteSource(HarvestSourceDTO source) throws DAOException {
    	    	
    	List<Object> values = new ArrayList<Object>();
		values.add(source.getSourceId());
		
		Connection conn = null;
		try{
			conn = getConnection();
			SQLUtil.executeUpdate(deleteSourceSQL, values, conn);
		}
		catch (Exception e){
			throw new DAOException(e.getMessage(), e);
		}
		finally{
			ConnectionUtil.closeConnection(conn);
		}
    }

    /** */
    private static final String updateHarvestFinishedSQL = "update HARVEST_SOURCE set STATEMENTS=?, RESOURCES=? where HARVEST_SOURCE_ID=?";
    private static final String updateHarvestFinishedSQL_avail = "update HARVEST_SOURCE set STATEMENTS=?, RESOURCES=?, COUNT_UNAVAIL=if(?,0,(COUNT_UNAVAIL+1)) where HARVEST_SOURCE_ID=?";

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.HarvestSourceDAO#updateHarvestFinished(int, Integer, Integer)
     */
	public void updateHarvestFinished(int sourceId, Integer numStatements, Integer numResources, Boolean sourceAvailable) throws DAOException {
		
		List<Object> values = new ArrayList<Object>();
		values.add(numStatements);
		values.add(numResources);
		if (sourceAvailable!=null)
			values.add(sourceAvailable.booleanValue()==true ? new Integer(1) : new Integer(0));
		values.add(new Integer(sourceId));		
		
		Connection conn = null;
		try{
			conn = getConnection();
			SQLUtil.executeUpdate(sourceAvailable!=null ? updateHarvestFinishedSQL_avail : updateHarvestFinishedSQL, values, conn);
		}
		catch (Exception e){
			throw new DAOException(e.getMessage(), e);
		}
		finally{
			ConnectionUtil.closeConnection(conn);
		}
	}
	
	/** */
	private static final String getDistinctSchedulesSQL = "select distinct SCHEDULE_CRON from HARVEST_SOURCE";

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HarvestSourceDAO#getDistinctSchedules()
	 */
	public List<String> getDistinctSchedules() throws DAOException {
		
		List<String> result = new ArrayList<String>();
		
		Connection conn = null;
		HarvestSourceDTOReader rsReader = new HarvestSourceDTOReader();
		try{
			conn = getConnection();
			List<Map<String,SQLValue>> list = SQLUtil.executeQuery(getDistinctSchedulesSQL, conn);
			if (list!=null){
				for (int i=0; i<list.size(); i++){
					Map<String,SQLValue> map = list.get(i);
					if (map!=null && !map.isEmpty()){
						SQLValue sqlValue = map.get("SCHEDULE_CRON");
						if (sqlValue!=null){
							String strValue = sqlValue.getString();
							if (!Util.isNullOrEmpty(strValue))
								result.add(strValue);
						}
					}
				}
			}
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
		
		return result;
	}

	/** */
	private static final String getSourcesByScheduleSQL = "select * from HARVEST_SOURCE where SCHEDULE_CRON=? order by HARVEST_SOURCE_ID asc";

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HarvestSourceDAO#getHarvestSourcesBySchedule(java.lang.String)
	 */
	public List<HarvestSourceDTO> getHarvestSourcesBySchedule(String schedule) throws DAOException {

    	List<Object> values = new ArrayList<Object>();
    	values.add(schedule);
				
		Connection conn = null;
		HarvestSourceDTOReader rsReader = new HarvestSourceDTOReader();
		try{
			conn = getConnection();
			SQLUtil.executeQuery(getSourcesByScheduleSQL, values, rsReader, conn);
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
