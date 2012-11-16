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
import java.util.List;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

import eionet.cr.dao.readers.ResultSetMixedReader;
import eionet.cr.dao.readers.ResultSetReaderException;
import eionet.cr.util.Pair;

/**
 * @author Aleksandr Ivanov <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public class PairReader<T, T1> extends ResultSetMixedReader<Pair<T, T1>> {

    /**
     * Field name for left column in query.
     */
    public static final String LEFTCOL = "LCOL";
    /**
     * Field name for right column in query.
     */
    public static final String RIGHTCOL = "RCOL";

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.readers.ResultSetMixedReader#getResultList()
     */
    @Override
    public List<Pair<T, T1>> getResultList() {
        return resultList;
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.util.sql.SQLResultSetReader#readRow(java.sql.ResultSet)
     */
    @Override
    @SuppressWarnings("unchecked")
    public void readRow(ResultSet rs) throws SQLException, ResultSetReaderException {

        T left = (T) rs.getObject(LEFTCOL);
        T1 right = (T1) rs.getObject(RIGHTCOL);
        resultList.add(new Pair<T, T1>(left, right));
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.util.sesame.SPARQLResultSetReader#readRow(org.openrdf.query.BindingSet)
     */
    @Override
    @SuppressWarnings("unchecked")
    public void readRow(BindingSet bindingSet) {
        if (bindingSet != null && bindingSet.size() > 0) {
            Value lcol = bindingSet.getValue(LEFTCOL);
            Value rcol = bindingSet.getValue(RIGHTCOL);
            String strLcol = "", strRCol = "";
            if (lcol != null) {
                strLcol = lcol.stringValue();
            }
            if (rcol != null) {
                strRCol = rcol.stringValue();
            }

            resultList.add(new Pair<T, T1>((T) strLcol, (T1) strRCol));
        }

    }

}
