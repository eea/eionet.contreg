package eionet.cr.dao.mysql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dto.HarvestDTO;
import eionet.cr.dto.HarvestScheduleDTO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.readers.HarvestDTOReader;
import eionet.cr.dto.readers.HarvestSourceDTOReader;
import eionet.cr.util.YesNoBoolean;
import eionet.cr.util.sql.ConnectionUtil;
import eionet.cr.util.sql.SQLUtil;

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
			if(list!=null && list.size()>0){
				source = list.get(0);
				
				HarvestScheduleDTO schedule = DAOFactory.getDAOFactory().getHarvestScheduleDAO().getHarvestScheduleBySourceId(harvestSourceID);
				if(schedule!=null){
					source.setHarvestSchedule(schedule);
				}
				return source;
			}
			return null;
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
	private static final String addSourceSQL = "insert into HARVEST_SOURCE (NAME,URL,TYPE,EMAILS,DATE_CREATED,CREATOR) VALUES (?,?,?,?,NOW(),?)";
	private static final String addSourceIgnoreSQL = "insert ignore into HARVEST_SOURCE (NAME,URL,TYPE,EMAILS,DATE_CREATED,CREATOR) VALUES (?,?,?,?,NOW(),?)";

	/*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestSourceDao#addSource()
     */
    public Integer addSource(HarvestSourceDTO source, String user) throws DAOException {
    	return this.addSource(addSourceSQL, source, user);
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.HarvestSourceDAO#addSourceIgnoreDuplicate(eionet.cr.dto.HarvestSourceDTO, java.lang.String)
     */
	public Integer addSourceIgnoreDuplicate(HarvestSourceDTO source, String user) throws DAOException {
		return this.addSource(addSourceIgnoreSQL, source, user);
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
		
		Connection conn = null;
		try{
			conn = getConnection();
			SQLUtil.executeUpdate(sql, values, conn);
			harvestSourceID = getLastInsertID(conn);
			
			// if this was an "insert ignore" statement and there was a duplicate entry indeed, getLastInsertID() returns 0 
			if(harvestSourceID!=null && harvestSourceID.intValue()>0 && source.getHarvestSchedule() != null){
				source.getHarvestSchedule().setHarvestSourceId(harvestSourceID);
				DAOFactory.getDAOFactory().getHarvestScheduleDAO().addSchedule(source.getHarvestSchedule());
			}
			
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
	private static final String editSourceSQL = "update HARVEST_SOURCE set NAME=?,URL=?,TYPE=?,EMAILS=? where HARVEST_SOURCE_ID=?";
	
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
		values.add(source.getSourceId());
		
		Connection conn = null;
		try{
			conn = getConnection();
			SQLUtil.executeUpdate(editSourceSQL, values, conn);
			
			if(source.getHarvestSchedule() != null){
				DAOFactory.getDAOFactory().getHarvestScheduleDAO().editSchedule(source.getHarvestSchedule());
			}
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
			
			DAOFactory.getDAOFactory().getHarvestScheduleDAO().deleteSchedule(source.getSourceId());
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

}
