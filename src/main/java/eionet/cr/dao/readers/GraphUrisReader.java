package eionet.cr.dao.readers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

/**
 * 
 * @author risto
 */
public class GraphUrisReader<T> extends ResultSetMixedReader<T> {

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.util.sql.SQLResultSetReader#readRow(java.sql.ResultSet)
     */
    @Override
    public void readRow(ResultSet rs) throws SQLException, ResultSetReaderException {
    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.util.sesame.SPARQLResultSetReader#readRow(org.openrdf.query.BindingSet)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void readRow(BindingSet bindingSet) throws ResultSetReaderException {

        Value graphValue = bindingSet.getValue("g");
        if (graphValue != null) {
            // expecting the URI of the matching graph to be in column "g"
            String graphUri = graphValue.stringValue();
            resultList.add((T) graphUri);
        }
    }

}
