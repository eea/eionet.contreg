package eionet.cr.util.sql;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * 
 * @author heinljab
 *
 */
public abstract class ResultSetBaseReader{
	
	/** */
	protected ResultSetMetaData rsMd = null;

	/**
	 * 
	 * @param rsMd
	 */
	public void setResultSetMetaData(ResultSetMetaData rsMd){
		this.rsMd = rsMd;
	}
	
	/**
	 * 
	 * @param rs
	 * @throws SQLException 
	 */
	public abstract void readRow(ResultSet rs) throws SQLException;
}
