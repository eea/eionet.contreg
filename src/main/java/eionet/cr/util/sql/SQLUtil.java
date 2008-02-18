package eionet.cr.util.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * 
 * @author heinljab
 *
 */
public class SQLUtil {

	/**
	 * 
	 * @param parametrizedSQL
	 * @param valueMap
	 * @throws SQLException 
	 */
	public static List<Map<String,SQLValue>> executeQuery(ParameterizedSQL parametrizedSQL, Map<String,Object> valueMap, Connection conn)
																														throws SQLException{		
		List<Map<String,SQLValue>> result = new ArrayList<Map<String,SQLValue>>();
		
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		try{
			pstmt = prepareStatement(parametrizedSQL, valueMap, conn);
			rs = pstmt.executeQuery();
			if (rs!=null){
				ResultSetMetaData rsMetadata = rs.getMetaData();
				int colCount = rsMetadata.getColumnCount();
				if (colCount>0){
					while (rs.next()){
						Map<String,SQLValue> rowMap = new HashMap<String,SQLValue>();
						for (int i=1; i<=colCount; i++){
							String colName = rsMetadata.getColumnName(i);
							int colSQLType = rsMetadata.getColumnType(i);
							rowMap.put(colName, new SQLValue(rs.getObject(i), colSQLType));
						}
						if (rowMap.size()>0)
							result.add(rowMap);
					}
				}
			}
		}
		finally{
			try{
				if (rs!=null) rs.close();
				if (pstmt!=null) pstmt.close();
			}
			catch (SQLException e){}
		}
		
		return result.size()==0 ? null : result;
	}

	/**
	 * 
	 * @param parametrizedSQL
	 * @param valueMap
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static int executeUpdate(ParameterizedSQL parametrizedSQL, Map<String,Object> valueMap, Connection conn) throws SQLException{
		
		PreparedStatement pstmt = null;
		try{
			pstmt = prepareStatement(parametrizedSQL, valueMap, conn);
			return pstmt.executeUpdate();
		}
		finally{
			try{
				if (pstmt!=null) pstmt.close();
			}
			catch (SQLException e){}
		}
	}
	
	/**
	 * 
	 * @param parametrizedSQL
	 * @param valueMap
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	private static PreparedStatement prepareStatement(ParameterizedSQL parametrizedSQL, Map<String,Object> valueMap, Connection conn)
																													throws SQLException{
		String[] sqlParamNames = parametrizedSQL.getParamNames();
		PreparedStatement pstmt= conn.prepareStatement(parametrizedSQL.getSqlString());
		for (int i=0; sqlParamNames!=null && i<sqlParamNames.length; i++){
			pstmt.setObject(i+1, valueMap.get(sqlParamNames[i]));
		}
		
		return pstmt;
	}
}
