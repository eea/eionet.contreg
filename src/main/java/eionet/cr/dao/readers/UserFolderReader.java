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
 */
package eionet.cr.dao.readers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

import eionet.cr.util.FolderUtil;

/**
 * 
 * @author altnyris
 * 
 */
public class UserFolderReader extends ResultSetMixedReader<String> {

    /** */
    private List<String> resultList = new ArrayList<String>();

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.util.sql.ResultSetListReader#getResultList()
     */
    public List<String> getResultList() {
        return resultList;
    }

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
            Value folder = bindingSet.getValue("o");

            if (folder != null) {
                String folderUri = folder.stringValue();
                if (!FolderUtil.isUserReservedUri(folderUri)) {
                    resultList.add(folderUri);
                }
            }
        }

    }
}
