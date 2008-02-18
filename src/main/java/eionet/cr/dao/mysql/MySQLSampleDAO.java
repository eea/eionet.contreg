package eionet.cr.dao.mysql;

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
	/**
	 * 
	 * @return
	 * @throws DAOException 
	 */
	@Override
	public List<Map<String,SQLValue>> executeSampleQuery() throws DAOException{
		
		Map<String,SQLValue> valueMap = new HashMap<String,SQLValue>();
		valueMap.put("col1", new SQLValue("col1_VALUE", Types.VARCHAR));
		valueMap.put("col2", new SQLValue("col2_VALUE", Types.INTEGER));
		
		try{
			return SQLUtil.executeQuery(sampleSQL, valueMap, getConnection());
		}
		catch (SQLException e){
			throw new DAOException(e.getMessage(), e);
		}
	}
}
