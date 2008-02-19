package eionet.cr.util.sql;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author heinljab
 *
 */
public class SQLValueReader extends ResultSetBaseReader{
	
	/** */
	private List<Map<String,SQLValue>> result = new ArrayList<Map<String,SQLValue>>();
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.util.sql.ResultSetReader#readRow(java.sql.ResultSet)
	 */
	public void readRow(ResultSet rs) throws SQLException {
		
		int colCount = rsMd.getColumnCount();
		Map<String,SQLValue> rowMap = new HashMap<String,SQLValue>();
		for (int i=1; i<=colCount; i++){
			String colName = rsMd.getColumnName(i);
			int colSQLType = rsMd.getColumnType(i);
			rowMap.put(colName, new SQLValue(rs.getObject(i), colSQLType));
		}
		if (rowMap.size()>0)
			result.add(rowMap);
	}

	/**
	 * 
	 * @return
	 */
	public List<Map<String,SQLValue>> getResultList(){
		return result.size()==0 ? null : result;
	}
}
