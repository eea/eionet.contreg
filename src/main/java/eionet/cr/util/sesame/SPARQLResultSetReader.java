package eionet.cr.util.sesame;

import java.util.List;

import org.openrdf.query.BindingSet;

import eionet.cr.dao.readers.ResultSetReader;
import eionet.cr.dao.readers.ResultSetReaderException;

/**
 * 
 * @author jaanus
 * 
 */
public interface SPARQLResultSetReader<T> extends ResultSetReader<T> {

    /**
     * 
     * @param bindingNames
     */
    void startResultSet(List<String> bindingNames);

    /**
     * 
     * @param bindingSet
     */
    void readRow(BindingSet bindingSet) throws ResultSetReaderException;
}
