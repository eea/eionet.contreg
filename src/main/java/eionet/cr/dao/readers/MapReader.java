package eionet.cr.dao.readers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.BNode;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

/**
 * Generic Reader iterates through resultset rows and stores the query result as Map object, where keys are bindingNames (aliases in
 * query).
 *
 * @author Enriko Käsper
 *
 */
public class MapReader extends ResultSetMixedReader<Map<String, String>> {

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.util.sql.SQLResultSetReader#readRow(java.sql.ResultSet)
     */
    @Override
    public void readRow(ResultSet rs) throws SQLException, ResultSetReaderException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.util.sesame.SPARQLResultSetReader#readRow(org.openrdf.query.BindingSet)
     */
    @Override
    public void readRow(BindingSet bindingSet) throws ResultSetReaderException {

        Map<String, String> resultMap = new HashMap<String, String>();

        for (String bindingName : bindingNames) {
            Value value = bindingSet.getValue(bindingName);

            String strValue = value.stringValue();
            if (value instanceof BNode && blankNodeUriPrefix != null) {
                if (!strValue.startsWith(blankNodeUriPrefix)) {
                    strValue = blankNodeUriPrefix + strValue;
                }
            }
            resultMap.put(bindingName, strValue);
        }
        resultList.add(resultMap);
    }
}
