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

import eionet.cr.dto.HarvestBaseDTO;
import eionet.cr.dto.HarvestDTO;
import eionet.cr.util.sql.SQLResultSetBaseReader;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class HarvestWithMessageTypesReader extends SQLResultSetBaseReader<HarvestDTO> {

    /** */
    private HarvestDTO currHarvest;

    /** */
    private int maxDistinctHarvests;

    /**
     *
     * @param maxDistinctHarvests
     */
    public HarvestWithMessageTypesReader(int maxDistinctHarvests) {
        this.maxDistinctHarvests = maxDistinctHarvests;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.util.sql.ResultSetBaseReader#readRow(java.sql.ResultSet)
     */
    @Override
    public void readRow(ResultSet rs) throws SQLException, ResultSetReaderException {

        Integer harvestId = rs.getInt("HARVEST_ID");
        if (currHarvest == null || currHarvest.getHarvestId().equals(harvestId) == false) { // if first row or new harvest

            // create new harvest object and add it to the list, but only if the list has not reached it's max allowed size yet
            if (resultList.size() >= maxDistinctHarvests) {
                return;
            }

            currHarvest = new HarvestDTO();
            resultList.add(currHarvest);

            currHarvest.setHarvestId(harvestId);
            currHarvest.setHarvestSourceId(new Integer(rs.getInt("SOURCE_ID")));

            currHarvest.setHarvestType(rs.getString("HARVEST_TYPE"));
            currHarvest.setUser(rs.getString("HARVEST_USER"));
            currHarvest.setStatus(rs.getString("STATUS"));

            currHarvest.setDatetimeStarted(rs.getTimestamp("STARTED"));
            currHarvest.setDatetimeFinished(rs.getTimestamp("FINISHED"));

            currHarvest.setEncodingSchemes(new Integer(rs.getInt("ENC_SCHEMES")));
            currHarvest.setTotalStatements(new Integer(rs.getInt("TOT_STATEMENTS")));
            currHarvest.setLitObjStatements(new Integer(rs.getInt("LIT_STATEMENTS")));
            currHarvest.setResponseCode(new  Integer(rs.getInt("HTTP_CODE")));
        }

        HarvestBaseDTO.addMessageType(currHarvest, rs.getString("MESSAGE_TYPE"));
    }
}
