package eionet.cr.dao.readers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openrdf.model.BNode;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

import eionet.cr.util.Pair;
import eionet.cr.util.Util;

/**
 * Reader for recent files shown in the front page.
 *
 * @author kaido
 */
public class RecentFilesReader extends ResultSetMixedReader<Pair<String, String>> {

    /*
     * (non-Javadoc)
     * @see eionet.cr.util.sesame.SPARQLResultSetReader#readRow(org.openrdf.query.BindingSet)
     */
    @Override
    public void readRow(final BindingSet bindingSet) throws ResultSetReaderException {
        Value subjectValue = bindingSet.getValue("s");

        // expecting the URI of the matching subject to be in column "s"
        String subjectUri = subjectValue.stringValue();
        String label = subjectUri;
        if (subjectValue instanceof BNode && blankNodeUriPrefix != null) {
            if (!subjectUri.startsWith(blankNodeUriPrefix)) {
                subjectUri = blankNodeUriPrefix + subjectUri;
            }
        }

        // expecting the column "l" to contain the label
        Value labelValue = bindingSet.getValue("l");
        if (labelValue != null && !Util.isNullOrEmpty(labelValue.stringValue())) {
            label = labelValue.stringValue();
        }
        resultList.add(new Pair<String, String>(subjectUri, label));

    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.util.sql.SQLResultSetReader#readRow(java.sql.ResultSet)
     */
    @Deprecated
    @Override
    public void readRow(final ResultSet rs) throws SQLException, ResultSetReaderException {
        throw new UnsupportedOperationException("Method not supported");
    }
}
