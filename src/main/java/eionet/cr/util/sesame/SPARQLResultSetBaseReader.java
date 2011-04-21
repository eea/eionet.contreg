package eionet.cr.util.sesame;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jaanus
 *
 */
public abstract class SPARQLResultSetBaseReader<T> implements SPARQLResultSetReader<T>{

    /** */
    protected List<T> resultList = new ArrayList<T>();

    /** */
    protected List<String> bindingNames;

    /*
     * (non-Javadoc)
     * @see eionet.cr.util.sesame.SPARQLResultSetReader#startResultSet(java.util.List)
     */
    public void startResultSet(List<String> bindingNames) {

        this.bindingNames = bindingNames;
        startResultSet();
    }

    /**
     *
     */
    protected void startResultSet() {
    }

    /* (non-Javadoc)
     * @see eionet.cr.dao.readers.ResultSetReader#endResultSet()
     */
    @Override
    public void endResultSet() {

        // default implementation, which does nothing, implementors can override
    }

    /* (non-Javadoc)
     * @see eionet.cr.dao.readers.ResultSetReader#getResultList()
     */
    @Override
    public List<T> getResultList() {
        return resultList;
    }
}
