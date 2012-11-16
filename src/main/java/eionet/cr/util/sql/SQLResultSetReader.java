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
public interface SQLResultSetReader<T> extends ResultSetReader<T> {

    /**
     *
     * @param resultSetMetaData
     */
    void startResultSet(ResultSetMetaData resultSetMetaData);

    /**
     * Reads the current row from the given {@link ResultSet} object. Assumes that {@link ResultSet#next()} has been called, i.e.
     * we are indeed at a "current" row.
     *
     * @param rs The given result-set
     * @throws SQLException If database error occurs.
     * @throws ResultSetReaderException If the result-set reader (if any assigned) throws some error.
     */
    void readRow(ResultSet rs) throws SQLException, ResultSetReaderException;
}
