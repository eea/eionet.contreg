/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Aleksandr Ivanov, Tieto Eesti
 */
package eionet.cr.util.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openrdf.model.BNode;
import org.openrdf.model.Value;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;

import eionet.cr.dao.readers.ResultSetMixedReader;
import eionet.cr.dao.readers.ResultSetReaderException;

/**
 *
 * @author jaanus
 *
 * @param <T>
 */
public class SingleObjectReader<T> extends ResultSetMixedReader<T> {

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.util.sql.SQLResultSetReader#readRow(java.sql.ResultSet)
     */
    @Override
    @SuppressWarnings("unchecked")
    public void readRow(ResultSet rs) throws SQLException, ResultSetReaderException {
        resultList.add((T) rs.getObject(1));
    }

    /**
     * Reads row from the bindingset that contains only one column.
     *
     * @param bindingSet
     *            - Query result bindingset
     */
    @SuppressWarnings("unchecked")
    @Override
    public void readRow(BindingSet bindingSet) {

        if (bindingSet != null && bindingSet.size() > 0) {

            String strValue = null;
            Binding binding = bindingSet.iterator().next();
            if (binding != null) {

                Value bindingValue = binding.getValue();
                strValue = bindingValue.stringValue();
                if (bindingValue instanceof BNode && blankNodeUriPrefix != null) {
                    if (strValue != null && !strValue.startsWith(blankNodeUriPrefix)) {
                        strValue = blankNodeUriPrefix + strValue;
                    }
                }
                if (strValue != null) {
                    // this casting is done because of the generilization in the interface
                    // only Strings can be read from Sesame Bindingset today
                    resultList.add((T) strValue);
                }
            }
        }
    }
}
