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
import java.util.Map;

import eionet.cr.util.sql.SQLResultSetBaseReader;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 * 
 */
@SuppressWarnings("rawtypes")
public class UriHashesReader extends SQLResultSetBaseReader {

    /** */
    private Map<Long, String> urisByHashes;

    /**
     * 
     * @param urisByHashes
     */
    public UriHashesReader(Map<Long, String> map) {
        this.urisByHashes = map;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.util.sql.ResultSetBaseReader#readRow(java.sql.ResultSet)
     */
    @Override
    public void readRow(ResultSet rs) throws SQLException, ResultSetReaderException {

        urisByHashes.put(Long.valueOf(rs.getLong("URI_HASH")), rs.getString("URI"));
    }
}
