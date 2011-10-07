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

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;

import eionet.cr.common.CRRuntimeException;
import eionet.cr.dto.PostHarvestScriptDTO;
import eionet.cr.dto.PostHarvestScriptDTO.Type;
import eionet.cr.util.sql.SQLResultSetBaseReader;

/**
 *
 * @author Jaanus Heinlaid
 */
public class PostHarvestScriptDTOReader extends SQLResultSetBaseReader<PostHarvestScriptDTO>{

    /** */
    private PostHarvestScriptDTO dto;

    /** */
    private Type targetType;

    /**
     *
     * @param targetType
     */
    public PostHarvestScriptDTOReader(Type targetType){
        this.targetType = targetType;
    }

    /**
     * @see eionet.cr.util.sql.SQLResultSetReader#readRow(java.sql.ResultSet)
     */
    @Override
    public void readRow(ResultSet rs) throws SQLException, ResultSetReaderException {

        String uri = rs.getString("URI");
        if (StringUtils.isBlank(uri)){
            throw new CRRuntimeException("URI cannot be blank here!");
        }

        if (dto==null || !dto.getUri().equals(uri)){

            dto = PostHarvestScriptDTO.create(targetType, uri);
            resultList.add(dto);
        }

        dto.addQuery(rs.getString("QUERY"));
    }
}
