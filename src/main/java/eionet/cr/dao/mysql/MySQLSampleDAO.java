package eionet.cr.dao.mysql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.SampleDAO;
import eionet.cr.util.sql.ParameterizedSQL;
import eionet.cr.util.sql.SQLUtil;
import eionet.cr.util.sql.SQLValue;

/**
 * 
 * @author heinljab
 *
 */
public class MySQLSampleDAO extends MySQLBaseDAO implements SampleDAO{

	/**
	 * 
	 */
	public MySQLSampleDAO(){
	}

	/** */
	private static final ParameterizedSQL sampleSQL = new ParameterizedSQL(
			"select * from SAMPLE_TABLE where COL1=? and COL2=?",
			"col1, col2");

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.SampleDAO#executeSampleQuery()
	 */
	public List<Map<String,SQLValue>> executeSampleQuery() throws DAOException{
		
		Map<String,Object> valueMap = new HashMap<String,Object>();
		valueMap.put("col1", "some value");
		valueMap.put("col2", Integer.valueOf("9999"));
		
		Connection conn = null;
		try{
			conn = getConnection();
			return SQLUtil.executeQuery(sampleSQL, valueMap, conn);
		}
		catch (SQLException e){
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
