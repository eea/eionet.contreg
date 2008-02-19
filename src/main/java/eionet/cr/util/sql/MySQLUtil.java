package eionet.cr.util.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * @author heinljab
 *
 */
public class MySQLUtil {

	/**
	 * 
	 * @param tableName
	 * @param valueMap
	 * @return
	 * @throws SQLException
	 */
	public static int insertRow(String tableName, HashMap<String,Object> valueMap, Connection conn) throws SQLException{
		
		if (tableName==null || tableName.trim().length()==0 || valueMap==null || valueMap.size()==0)
			return -1;
		
		StringBuffer sqlStringBuffer = new StringBuffer("insert into ");
		sqlStringBuffer.append(tableName).append(" (");
		
		int i;
		StringBuffer questionMarksBuffer = new StringBuffer();
		Iterator<String> colNamesIter = valueMap.keySet().iterator();
		for (i=0; colNamesIter.hasNext(); i++){
			String colName = colNamesIter.next();
			if (i>0){
				sqlStringBuffer.append(", ");
				questionMarksBuffer.append(", ");
			}
			sqlStringBuffer.append(colName);
			questionMarksBuffer.append("?");
		}
		
		sqlStringBuffer.append(") values (").append(questionMarksBuffer.toString()).append(")");
		
		return SQLUtil.executeUpdate(sqlStringBuffer.toString(), new ArrayList<Object>(valueMap.values()), conn);
	}

	/**
	 * 
	 * @param tableName
	 * @param valueMap
	 * @param criteriaMap
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static int updateRow(String tableName, HashMap<String,Object> valueMap, HashMap<String,Object> criteriaMap, Connection conn)
																												throws SQLException{

		if (tableName==null || tableName.trim().length()==0 ||
				valueMap==null || valueMap.size()==0 || criteriaMap==null || criteriaMap.size()==0)
			return -1;
		
		StringBuffer sqlStringBuffer = new StringBuffer("update ");
		sqlStringBuffer.append(tableName).append(" set ");

		int i;
		Iterator<String> colNamesIter = valueMap.keySet().iterator();
		for (i=0; colNamesIter.hasNext(); i++){			
			if (i>0)
				sqlStringBuffer.append(", ");
			String colName = colNamesIter.next();
			sqlStringBuffer.append(colName).append("=?");
		}

		sqlStringBuffer.append(" where ");
		Iterator<String> criteriaIter = criteriaMap.keySet().iterator();
		for (i=0; colNamesIter.hasNext(); i++){
			if (i>0)
				sqlStringBuffer.append(" and ");
			String critName = criteriaIter.next();
			sqlStringBuffer.append(critName).append("=?");
		}
		
		List<Object> values = new ArrayList<Object>(valueMap.values());
		values.addAll(criteriaMap.values());
		
		return SQLUtil.executeUpdate(sqlStringBuffer.toString(), values, conn);
	}
	
	/**
	 * 
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static Integer getLastInsertID(Connection conn) throws SQLException{
		
		ResultSet rs = null;
		Statement stmt = null;
		try{
			stmt = conn.createStatement();
			rs = stmt.executeQuery("select last_insert_id()");
			return (rs!=null && rs.next()) ? new Integer(rs.getInt(1)) : null;
		}
		finally{
			try{
				if (rs!=null) rs.close();
				if (stmt!=null) stmt.close();
			}
			catch (SQLException e){}
		}
	}
}
