/*
 * The contents of this file are subject to the Mozilla Public
 *
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
 * Agency. Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency. All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti*/
package eionet.cr.dao.readers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

import eionet.cr.dto.PairDTO;

/**
 * 
 * @author Risto Alt
 * 
 */
public class PairReader extends ResultSetMixedReader<PairDTO> {

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.util.sql.ResultSetBaseReader#readRow(java.sql.ResultSet)
     */
    public void readRow(ResultSet rs) throws SQLException, ResultSetReaderException {
    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.util.sesame.SPARQLResultSetReader#readRow(org.openrdf.query .BindingSet)
     */
    @Override
    public void readRow(BindingSet bindingSet) {
        if (bindingSet != null && bindingSet.size() > 0) {
            Value name = bindingSet.getValue("name");
            Value val = bindingSet.getValue("value");
            String strName = "", strValue = "";
            if (name != null) {
                strName = name.stringValue();
            }
            if (val != null) {
                strValue = val.stringValue();
            }
            resultList.add(new PairDTO(strName, strValue));
        }
    }
}
