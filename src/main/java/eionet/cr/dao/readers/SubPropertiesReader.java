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
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.cr.dao.readers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

import eionet.cr.dao.util.SubProperties;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
@SuppressWarnings("rawtypes")
public class SubPropertiesReader extends ResultSetMixedReader {

    /** */
    private SubProperties subProperties;

    /**
     *
     * @param subProperties
     */
    public SubPropertiesReader(SubProperties subProperties) {

        if (subProperties == null)
            throw new IllegalArgumentException();

        this.subProperties = subProperties;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.util.sql.ResultSetBaseReader#readRow(java.sql.ResultSet)
     */
    public void readRow(ResultSet rs) throws SQLException, ResultSetReaderException {
        subProperties.add(rs.getString("PREDICATE"), rs.getString("SUB_PROPERTY"));
    }

    /**
     * Reads row from SPARQL bindingset and creates an elemnt in Subproperties hash.
     *
     * @param bindingSet SPARQL query result
     */
    @Override
    public void readRow(final BindingSet bindingSet) {
        if (bindingSet != null && bindingSet.size() > 0) {
            Value subProp = bindingSet.getValue("p");
            Value prop = bindingSet.getValue("s");
            subProperties.add(prop.stringValue(), subProp.stringValue());
        }
    }
}
