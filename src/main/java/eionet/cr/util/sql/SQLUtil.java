package eionet.cr.util.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author heinljab
 *
 */
public class SQLUtil {
	
	/**
	 * 
	 * @param sql
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static Object executeSingleReturnValueQuery(String sql, Connection conn) throws SQLException{
		
		ResultSet rs = null;
		Statement stmt = null;
		try{
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			return (rs!=null && rs.next()) ? rs.getObject(1) : null;
		}
		finally{
			SQLUtil.close(rs);
			SQLUtil.close(stmt);
		}
	}

	/**
	 * 
	 * @param parameterizedSQL
	 * @param valueMap
	 * @throws SQLException 
	 */
	public static List<Map<String,SQLValue>> executeQuery(String parameterizedSQL, List<?> values, Connection conn) throws SQLException{
		
		SQLValueReader sqlValueReader = new SQLValueReader();
		executeQuery(parameterizedSQL, values, sqlValueReader, conn);
		return sqlValueReader.getResultList();
	}
	
	/**
	 * 
	 * @param parameterizedSQL
	 * @param values
	 * @param rsReader
	 * @param conn
	 * @throws SQLException
	 */
	public static void executeQuery(String parameterizedSQL, List<?> values, ResultSetBaseReader rsReader, Connection conn)
																											throws SQLException{
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		try{
			pstmt = prepareStatement(parameterizedSQL, values, conn);
			rs = pstmt.executeQuery();
			if (rs!=null){
				ResultSetMetaData rsMd = rs.getMetaData();
				if (rsMd!=null && rsMd.getColumnCount()>0){
					rsReader.setResultSetMetaData(rsMd);
					while (rs.next())
						rsReader.readRow(rs);
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

	}

	/**
	 * 
	 * @param sql
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static List<Map<String,SQLValue>> executeQuery(String sql, Connection conn) throws SQLException{
		
		SQLValueReader sqlValueReader = new SQLValueReader();
		executeQuery(sql, sqlValueReader, conn);
		return sqlValueReader.getResultList();
	}

	/**
	 * 
	 * @param sql
	 * @param rsReader
	 * @param conn
	 * @throws SQLException
	 */
	public static void executeQuery(String sql, ResultSetBaseReader rsReader, Connection conn) throws SQLException{
		
		ResultSet rs = null;
		Statement stmt = null;
		try{
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			if (rs!=null){
				ResultSetMetaData rsMd = rs.getMetaData();
				if (rsMd!=null && rsMd.getColumnCount()>0){
					rsReader.setResultSetMetaData(rsMd);
					while (rs.next())
						rsReader.readRow(rs);
				}
			}
		}
		finally{
			try{
				if (rs!=null) rs.close();
				if (stmt!=null) stmt.close();
			}
			catch (SQLException e){}
		}
	}
	
	/**
	 * 
	 * @param parameterizedSQL
	 * @param valueMap
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static int executeUpdate(String parameterizedSQL, List<?> values, Connection conn) throws SQLException{
		
		PreparedStatement pstmt = null;
		try{
			pstmt = prepareStatement(parameterizedSQL, values, conn);
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
	 * @param sql
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static int executeUpdate(String sql, Connection conn) throws SQLException{
		
		Statement stmt = null;
		try{
			stmt = conn.createStatement();
			return stmt.executeUpdate(sql);
		}
		finally{
			SQLUtil.close(stmt);
		}
	}
	
	/**
	 * 
	 * @param parameterizedSQL
	 * @param valueMap
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static PreparedStatement prepareStatement(String parameterizedSQL, List<?> values, Connection conn) throws SQLException{
		
		PreparedStatement pstmt= conn.prepareStatement(parameterizedSQL);
		for (int i=0; values!=null && i<values.size(); i++){
			pstmt.setObject(i+1, values.get(i));
		}
		return pstmt;
	}
	
	/**
	 * 
	 * @param conn
	 */
	public static void close(Connection conn){
		if (conn!=null){
			try{
				conn.close();
			}
			catch (SQLException e){}
		}
	}

	/**
	 * 
	 * @param stmt
	 */
	public static void close(Statement stmt){
		if (stmt!=null){
			try{
				stmt.close();
			}
			catch (SQLException e){}
		}
	}
	
	/**
	 * 
	 * @param rs
	 */
	public static void close(ResultSet rs){
		if (rs!=null){
			try{
				rs.close();
			}
			catch (SQLException e){}
		}
	}
}
