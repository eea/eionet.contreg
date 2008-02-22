package eionet.cr.dao.mysql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.util.sql.ConnectionUtil;
import eionet.cr.util.sql.ParameterizedSQL;
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
	private static final String getSourcesSQL = "select * from HARVEST_SOURCE where HARVEST_SOURCE_ID=?";
	
	/*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.ISourceDao#getSources()
     */
    public List<Map<String,SQLValue>> getSources() throws DAOException {
    	List<Object> values = new ArrayList<Object>();
		values.add(Integer.valueOf("1"));
		
		Connection conn = null;
		try{
			conn = getConnection();
			return SQLUtil.executeQuery(getSourcesSQL, values, conn);
		}
		catch (Exception e){
			throw new DAOException(e.getMessage(), e);
		}
		finally{
			closeConnection(conn);
		}
    }
    
    /** */
	private static final String addSourceSQL = "insert into HARVEST_SOURCE (IDENTIFIER,PULL_URL,TYPE,EMAILS,DATE_CREATED,CREATOR) VALUES (?,?,?,?,NOW(),?)";
	
	/*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.ISourceDao#getSources()
     */
    public void addSource(HarvestSourceDTO source, String user) throws DAOException {
    	    	
    	List<Object> values = new ArrayList<Object>();
		values.add(source.getIdentifier());
		values.add(source.getPullUrl());
		values.add(source.getType());
		values.add(source.getEmails());
		values.add(user);
		
		Connection conn = null;
		try{
			conn = getConnection();
			SQLUtil.executeUpdate(addSourceSQL, values, conn);
		}
		catch (Exception e){
			throw new DAOException(e.getMessage(), e);
		}
		finally{
			ConnectionUtil.closeConnection(conn);
		}
    }
}
