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

import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.util.YesNoBoolean;
import eionet.cr.util.sql.SQLResultSetBaseReader;

/**
 *
 * @author altnyris
 *
 */
public class HarvestSourceDTOReader extends SQLResultSetBaseReader<HarvestSourceDTO> {

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.util.sql.ResultSetBaseReader#readRow(java.sql.ResultSet)
     */
    @Override
    public void readRow(ResultSet rs) throws SQLException, ResultSetReaderException {

        HarvestSourceDTO harvestSourceDTO = new HarvestSourceDTO();
        harvestSourceDTO.setSourceId(new Integer(rs.getInt("HARVEST_SOURCE_ID")));
        harvestSourceDTO.setUrl(rs.getString("URL"));
        harvestSourceDTO.setUrlHash(Long.valueOf(rs.getLong("URL_HASH")));
        harvestSourceDTO.setEmails(rs.getString("EMAILS"));
        harvestSourceDTO.setTimeCreated(rs.getTimestamp("TIME_CREATED"));
        harvestSourceDTO.setStatements(new Integer(rs.getInt("STATEMENTS")));
        harvestSourceDTO.setCountUnavail(new Integer(rs.getInt("COUNT_UNAVAIL")));
        harvestSourceDTO.setLastHarvest(rs.getTimestamp("LAST_HARVEST"));
        harvestSourceDTO.setIntervalMinutes(rs.getInt("INTERVAL_MINUTES"));
        harvestSourceDTO.setLastHarvestFailed(YesNoBoolean.parse(rs.getString("LAST_HARVEST_FAILED")));
        harvestSourceDTO.setPrioritySource(YesNoBoolean.parse(rs.getString("PRIORITY_SOURCE")));
        harvestSourceDTO.setOwner(rs.getString("SOURCE_OWNER"));
        harvestSourceDTO.setPermanentError(YesNoBoolean.parse(rs.getString("PERMANENT_ERROR")));
        harvestSourceDTO.setMediaType(rs.getString("MEDIA_TYPE"));
        harvestSourceDTO.setSparqlEndpoint(YesNoBoolean.parse(rs.getString("IS_SPARQL_ENDPOINT")));

        resultList.add(harvestSourceDTO);
    }
}
