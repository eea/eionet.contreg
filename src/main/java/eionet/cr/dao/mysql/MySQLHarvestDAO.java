package eionet.cr.dao.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.HarvestDAO;
import eionet.cr.dto.HarvestBaseDTO;
import eionet.cr.dto.HarvestDTO;
import eionet.cr.dto.readers.HarvestDTOReader;
import eionet.cr.dto.readers.HarvestWithMessageTypesReader;
import eionet.cr.harvest.util.HarvestMessageType;
import eionet.cr.util.sql.ConnectionUtil;
import eionet.cr.util.sql.SQLUtil;

/**
 * 
 * @author heinljab
 *
 */
public class MySQLHarvestDAO extends MySQLBaseDAO implements HarvestDAO {

	/** */
	private static final String insertStartedHarvestSQL = 
		"insert into HARVEST (HARVEST_SOURCE_ID, TYPE, USER, STATUS, STARTED) values (?, ?, ?, ?, now())";
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HarvestDAO#insertHarvest(int, java.lang.String, java.lang.String, java.lang.String)
	 */
	public int insertStartedHarvest(int harvestSourceId, String harvestType, String user, String status) throws DAOException {
		
		List<Object> values = new ArrayList<Object>();
		values.add(new Integer(harvestSourceId));
		values.add(harvestType);
		values.add(user);
		values.add(status);
		
		Connection conn = null;
		try{
			conn = getConnection();
			SQLUtil.executeUpdate(insertStartedHarvestSQL, values, conn);
			return getLastInsertID(conn);
		}
		catch (Exception e){
			throw new DAOException(e.getMessage(), e);
		}
		finally{
			ConnectionUtil.closeConnection(conn);
		}
	}

	/** */
	private static final String updateFinishedHarvestSQL =
		"update HARVEST set STATUS=?, FINISHED=now(), TOT_STATEMENTS=?, LIT_STATEMENTS=?, RES_STATEMENTS=?, TOT_RESOURCES=?, ENC_SCHEMES=? " +
		"where HARVEST_ID=?";
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HarvestDAO#updateFinishedHarvest(int, int, int, int, int, java.lang.String)
	 */
	public void updateFinishedHarvest(int harvestId, String status, int totStatements, int totResources,
			int litStatements, int encSchemes) throws DAOException {
		
		List<Object> values = new ArrayList<Object>();
		values.add(status);
		values.add(new Integer(totStatements));
		values.add(new Integer(litStatements));
		values.add(new Integer(totStatements-litStatements));
		values.add(new Integer(totResources));
		values.add(new Integer(encSchemes));
		values.add(new Integer(harvestId));
		
		Connection conn = null;
		try{
			conn = getConnection();
			SQLUtil.executeUpdate(updateFinishedHarvestSQL, values, conn);
		}
		catch (Exception e){
			throw new DAOException(e.getMessage(), e);
		}
		finally{
			ConnectionUtil.closeConnection(conn);
		}
	}
	
	/** */
	private static final String getHarvestsBySourceIdSQL = 
		"select distinct HARVEST.*, HARVEST_MESSAGE.TYPE from HARVEST left join HARVEST_MESSAGE on HARVEST.HARVEST_ID=HARVEST_MESSAGE.HARVEST_ID "
		+ "where HARVEST.HARVEST_SOURCE_ID=? order by HARVEST.STARTED desc limit ?";
	
	/*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestDAO#getHarvestsBySourceId()
     */
    public List<HarvestDTO> getHarvestsBySourceId(Integer harvestSourceId) throws DAOException {
    	
    	int maxDistinctHarvests = 10;
    	
    	List<Object> values = new ArrayList<Object>();
    	values.add(harvestSourceId);
    	values.add(HarvestMessageType.values().length * maxDistinctHarvests);
				
		Connection conn = null;
		HarvestWithMessageTypesReader rsReader = new HarvestWithMessageTypesReader(maxDistinctHarvests);
		try{
			conn = getConnection();
			SQLUtil.executeQuery(getHarvestsBySourceIdSQL, values, rsReader, conn);
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
	private static final String getHarvestByIdSQL = "select * from HARVEST where HARVEST_ID=?";

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.HarvestDAO#getHarvestById(java.lang.Integer)
     */
	public HarvestDTO getHarvestById(Integer harvestId) throws DAOException {
		
		List<Object> values = new ArrayList<Object>();
    	values.add(harvestId);
				
		Connection conn = null;
		HarvestDTOReader rsReader = new HarvestDTOReader();
		try{
			conn = getConnection();
			SQLUtil.executeQuery(getHarvestByIdSQL, values, rsReader, conn);
			List<HarvestDTO>  list = rsReader.getResultList();
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
}
