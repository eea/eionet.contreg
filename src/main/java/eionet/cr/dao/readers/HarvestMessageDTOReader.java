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

import eionet.cr.dto.HarvestMessageDTO;
import eionet.cr.util.sql.SQLResultSetBaseReader;

/**
 *
 * @author heinljab
 *
 */
public class HarvestMessageDTOReader extends SQLResultSetBaseReader<HarvestMessageDTO> {

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.util.sql.ResultSetBaseReader#readRow(java.sql.ResultSet)
     */
    public void readRow(ResultSet rs) throws SQLException, ResultSetReaderException {

        HarvestMessageDTO harvestMessageDTO = new HarvestMessageDTO();
        harvestMessageDTO.setHarvestId(new Integer(rs.getInt("HARVEST_ID")));
        harvestMessageDTO.setType(rs.getString("TYPE"));
        harvestMessageDTO.setMessage(rs.getString("MESSAGE"));
        harvestMessageDTO.setStackTrace(rs.getString("STACK_TRACE"));
        harvestMessageDTO.setHarvestMessageId(new Integer(rs.getInt("HARVEST_MESSAGE_ID")));
        resultList.add(harvestMessageDTO);
    }
}
