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
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        jaanus
 */

package eionet.cr.dao.readers;

import org.openrdf.query.BindingSet;

import eionet.cr.dao.util.VoidDatasetsResultRow;
import eionet.cr.util.sesame.SPARQLResultSetBaseReader;

/**
 * A result set reader for objects of type {@link VoidDatasetsResultRow}.
 *
 * @author jaanus
 */
public class VoidDatasetsReader extends SPARQLResultSetBaseReader<VoidDatasetsResultRow>{

    /*
     * (non-Javadoc)
     * @see eionet.cr.util.sesame.SPARQLResultSetReader#readRow(org.openrdf.query.BindingSet)
     */
    @Override
    public void readRow(BindingSet bindingSet) throws ResultSetReaderException {

        if (bindingSet == null || bindingSet.size()==0){
            return;
        }

        VoidDatasetsResultRow row = new VoidDatasetsResultRow();
        row.setUri(getStringValue(bindingSet, "dataset"));
        row.setLabel(getStringValue(bindingSet, "label"));
        row.setCreator(getStringValue(bindingSet, "creator"));
        row.setSubjects(getStringValue(bindingSet, "subjects"));
        resultList.add(row);
    }
}
