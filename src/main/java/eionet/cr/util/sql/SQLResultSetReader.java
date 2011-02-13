package eionet.cr.util.sql;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import eionet.cr.dao.readers.ResultSetReader;
import eionet.cr.dao.readers.ResultSetReaderException;

/**
 * 
 * @author jaanus
 *
 */
public interface SQLResultSetReader<T> extends ResultSetReader<T>{

	/**
	 * 
	 * @param resultSetMetaData
	 */
	public void startResultSet(ResultSetMetaData resultSetMetaData);

	/**
	 * 
	 * @param rs
	 * @throws SQLException
	 * @throws ResultSetReaderException TODO
	 */
	public void readRow(ResultSet rs) throws SQLException, ResultSetReaderException;
}
