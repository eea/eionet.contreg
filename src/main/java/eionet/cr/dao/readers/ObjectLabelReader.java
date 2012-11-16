package eionet.cr.dao.readers;

import java.util.Collections;

import org.apache.commons.lang.StringUtils;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

import eionet.cr.util.ObjectLabelPair;
import eionet.cr.util.URIUtil;
import eionet.cr.util.sesame.SPARQLResultSetBaseReader;

/**
 * Reads resultset containing objects and labels from variables "label" and "object" in the BindingSet.
 *
 * @author kaido
 *
 */
public class ObjectLabelReader extends SPARQLResultSetBaseReader<ObjectLabelPair> {

    public ObjectLabelReader(boolean extractLabels) {
        this.extractLabels = extractLabels;
    }

    private boolean extractLabels;

    /*
     * (non-Javadoc)
     * @see eionet.cr.util.sesame.SPARQLResultSetReader#readRow(org.openrdf.query.BindingSet)
     */
    @Override
    public void readRow(BindingSet bindingSet) throws ResultSetReaderException {

        // If label exists use label otherwise take last part of object URI.
        // TODO - take lang into account?
        if (bindingSet != null && bindingSet.size() > 0) {
            Value label = bindingSet.getValue("label");
            Value obj = bindingSet.getValue("object");
            String labelStr = "";
            String objStr = ((obj == null || obj.stringValue() == null) ? "" : obj.stringValue());
            if (label != null && StringUtils.isNotBlank(label.stringValue())) {
                labelStr = label.stringValue();
                // labels must not be extracted from URI or object is not URI:
            } else if (!extractLabels || (obj != null && StringUtils.isNotBlank(objStr) && !URIUtil.isSchemedURI(objStr))) {
                labelStr = objStr;

            } else {
                // last part of URI as label
                labelStr = URIUtil.extractURILabel(objStr, objStr);
            }
            resultList.add(new ObjectLabelPair(objStr, labelStr));
        }
    }

    @Override
    public void endResultSet() {

        Collections.sort(resultList);
    }

}
