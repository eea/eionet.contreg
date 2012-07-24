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

import eionet.cr.dto.HarvestDTO;
import eionet.cr.util.sql.SQLResultSetBaseReader;

/**
 *
 * @author altnyris
 *
 */
public class HarvestDTOReader extends SQLResultSetBaseReader<HarvestDTO> {

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.util.sql.ResultSetBaseReader#readRow(java.sql.ResultSet)
     */
    @Override
    public void readRow(ResultSet rs) throws SQLException, ResultSetReaderException {

        HarvestDTO harvestDTO = new HarvestDTO();

        harvestDTO.setHarvestId(new Integer(rs.getInt("HARVEST_ID")));
        harvestDTO.setHarvestSourceId(new Integer(rs.getInt("HARVEST_SOURCE_ID")));

        harvestDTO.setHarvestType(rs.getString("TYPE"));
        harvestDTO.setUser(rs.getString("USER"));
        harvestDTO.setStatus(rs.getString("STATUS"));

        harvestDTO.setDatetimeStarted(rs.getTimestamp("STARTED"));
        harvestDTO.setDatetimeFinished(rs.getTimestamp("FINISHED"));

        harvestDTO.setEncodingSchemes(new Integer(rs.getInt("ENC_SCHEMES")));
        harvestDTO.setTotalStatements(new Integer(rs.getInt("TOT_STATEMENTS")));
        harvestDTO.setLitObjStatements(new Integer(rs.getInt("LIT_STATEMENTS")));

        harvestDTO.setResponseCode(new Integer(rs.getInt("HTTP_CODE")));

        resultList.add(harvestDTO);
    }
}
