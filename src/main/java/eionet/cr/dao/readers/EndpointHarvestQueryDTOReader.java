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

import java.sql.ResultSet;
import java.sql.SQLException;

import eionet.cr.dto.EndpointHarvestQueryDTO;
import eionet.cr.util.YesNoBoolean;
import eionet.cr.util.sql.SQLResultSetBaseReader;

/**
 * An SQL result set reader for objects of type {@link EndpointHarvestQueryDTO}.
 *
 * @author jaanus
 */
public class EndpointHarvestQueryDTOReader extends SQLResultSetBaseReader<EndpointHarvestQueryDTO> {

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.util.sql.SQLResultSetReader#readRow(java.sql.ResultSet)
     */
    @Override
    public void readRow(ResultSet rs) throws SQLException, ResultSetReaderException {

        EndpointHarvestQueryDTO dto = new EndpointHarvestQueryDTO();

        dto.setId(rs.getInt("ENDPOINT_HARVEST_QUERY_ID"));
        dto.setTitle(rs.getString("TITLE"));
        dto.setQuery(rs.getString("QUERY"));
        dto.setEndpointUrl(rs.getString("ENDPOINT_URL"));
        dto.setPosition(rs.getInt("POSITION_NUMBER"));
        dto.setActive(YesNoBoolean.parse(rs.getString("ACTIVE")));
        dto.setLastModified(rs.getTimestamp("LAST_MODIFIED"));

        resultList.add(dto);
    }
}
