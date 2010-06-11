package eionet.cr.util.sql;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import eionet.cr.util.export.ExportException;

public interface ResultSetReader {

	public void setResultSetMetaData(ResultSetMetaData rsMd);

	public void readRow(ResultSet rs) throws ExportException, SQLException;
}
