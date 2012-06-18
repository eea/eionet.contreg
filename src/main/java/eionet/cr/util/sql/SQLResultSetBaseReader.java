package eionet.cr.util.sql;

import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

public abstract class SQLResultSetBaseReader<T> implements SQLResultSetReader<T> {

    /** */
    protected List<T> resultList = new ArrayList<T>();

    /** */
    protected ResultSetMetaData resultSetMetaData;

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.util.sql.SQLResultSetReader#startResultSet(java.sql.ResultSetMetaData)
     */
    @Override
    public void startResultSet(ResultSetMetaData resultSetMetaData) {
        this.resultSetMetaData = resultSetMetaData;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.readers.ResultSetReader#endResultSet()
     */
    @Override
    public void endResultSet() {

        // default implementation, which does nothing, implementors can override
    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.readers.ResultSetReader#getResultList()
     */
    @Override
    public List<T> getResultList() {
        return resultList;
    }
}
