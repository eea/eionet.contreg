package eionet.cr.dao.readers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.BNode;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

import eionet.cr.util.Util;

/**
 * 
 * @author risto
 * 
 * @param <T>
 */
public class RecentUploadsReader<T> extends ResultSetMixedReader<T> {

    private Map<String, Date> resultMap;

    public RecentUploadsReader() {
        resultMap = new HashMap<String, Date>();
    }

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
    @SuppressWarnings("unchecked")
    @Override
    public void readRow(BindingSet bindingSet) throws ResultSetReaderException {

        Value subjectValue = bindingSet.getValue("s");

        // expecting the URI of the matching subject to be in column "s"
        String subjectUri = subjectValue.stringValue();
        if (subjectValue instanceof BNode && blankNodeUriPrefix != null) {
            if (!subjectUri.startsWith(blankNodeUriPrefix)) {
                subjectUri = blankNodeUriPrefix + subjectUri;
            }
        }
        resultList.add((T) subjectUri);

        // expecting the column "d" to contain the date
        Value dateValue = bindingSet.getValue("d");
        if (dateValue != null) {
            String dateStr = dateValue.stringValue();
            if (dateStr != null && dateStr.length() > 0) {
                resultMap.put(subjectUri, Util.virtuosoStringToDate(dateStr));
            }
        }

    }

    public Map<String, Date> getResultMap() {
        return resultMap;
    }

}
