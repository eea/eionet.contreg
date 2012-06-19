package eionet.cr.dao.readers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.BNode;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

/**
 *
 * @author jaanus
 *
 * @param <T>
 */
public class FreeTextSearchReader<T> extends ResultSetMixedReader<T> {

    /** */
    private List<String> graphUris = new ArrayList<String>();

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.util.sql.SQLResultSetReader#readRow(java.sql.ResultSet)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void readRow(ResultSet rs) throws SQLException, ResultSetReaderException {

        // expecting the hash of the matching subject URI to be in the 1st column
        Long subjectHash = Long.valueOf(rs.getLong(1));
        resultList.add((T) subjectHash);

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

    }

    /**
     * @return List<String>
     */
    public List<String> getGraphUris() {
        return graphUris;
    }
}
