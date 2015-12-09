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
 * Agency. Portions created by Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Jaanus Heinlaid
 */

package eionet.cr.dao.readers;

import eionet.cr.dto.HarvestScriptDTO;
import eionet.cr.dto.HarvestScriptDTO.Phase;
import eionet.cr.dto.HarvestScriptDTO.TargetType;
import eionet.cr.dto.enums.HarvestScriptType;
import eionet.cr.util.YesNoBoolean;
import eionet.cr.util.sql.SQLResultSetBaseReader;
import org.apache.commons.lang.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Jaanus Heinlaid
 */
public class HarvestScriptDTOReader extends SQLResultSetBaseReader<HarvestScriptDTO> {

    /*
     * (non-Javadoc)
     * @see eionet.cr.util.sql.SQLResultSetReader#readRow(java.sql.ResultSet)
     */
    @Override
    public void readRow(ResultSet rs) throws SQLException, ResultSetReaderException {

        String targetSourceUrl = rs.getString("TARGET_SOURCE_URL");
        String targetTypeUrl = rs.getString("TARGET_TYPE_URL");

        TargetType targetType = null;
        String targetUrl = null;
        if (!StringUtils.isBlank(targetSourceUrl)) {
            targetType = TargetType.SOURCE;
            targetUrl = targetSourceUrl;
        } else if (!StringUtils.isBlank(targetTypeUrl)) {
            targetType = TargetType.TYPE;
            targetUrl = targetTypeUrl;
        }

        HarvestScriptDTO dto = new HarvestScriptDTO();
        dto.setId(rs.getInt("POST_HARVEST_SCRIPT_ID"));
        dto.setTargetType(targetType);
        dto.setTargetUrl(targetUrl);
        dto.setTitle(rs.getString("TITLE"));
        dto.setScript(rs.getString("SCRIPT"));
        dto.setPosition(rs.getInt("POSITION_NUMBER"));
        dto.setActive(YesNoBoolean.parse(rs.getString("ACTIVE")));
        dto.setRunOnce(YesNoBoolean.parse(rs.getString("RUN_ONCE")));
        dto.setLastModified(rs.getTimestamp("LAST_MODIFIED"));

        try {
            dto.setPhase(Phase.valueOf(rs.getString("PHASE")));
        } catch (Exception e) {
            dto.setPhase(null);
        }
         
        try {
            dto.setType(HarvestScriptType.valueOf(rs.getString("TYPE")));
        } catch (Exception e) {
            //TODO error logging
            dto.setType(null);
        }

        dto.setExternalServiceId(rs.getInt("EXTERNAL_SERVICE_ID"));
        dto.setExternalServiceParams(rs.getString("EXTERNAL_SERVICE_PARAMS"));
        resultList.add(dto);
    }

}
