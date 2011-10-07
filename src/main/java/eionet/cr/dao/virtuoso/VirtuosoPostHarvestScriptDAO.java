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

package eionet.cr.dao.virtuoso;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.PostHarvestScriptDAO;
import eionet.cr.dao.readers.PostHarvestScriptDTOReader;
import eionet.cr.dto.PostHarvestScriptDTO;
import eionet.cr.dto.PostHarvestScriptDTO.Type;
import eionet.cr.dto.SourcePostHarvestScriptDTO;
import eionet.cr.util.sql.SQLUtil;

/**
 *
 * @author Jaanus Heinlaid
 */
public class VirtuosoPostHarvestScriptDAO extends VirtuosoBaseDAO implements PostHarvestScriptDAO {

    /** */
    private static final String SAVE_SOURCE_SCRIPT_SQL =
        "insert into POST_HARVEST_SCRIPT (HARVEST_SOURCE_URL, QUERY, POSITION_INDEX) values (?, ?, ?)";
    private static final String SAVE_TYPE_SCRIPT_SQL =
        "insert into POST_HARVEST_SCRIPT (RDF_TYPE_URI, QUERY, POSITION_INDEX) values (?, ?, ?)";
    /**
     * @see eionet.cr.dao.PostHarvestScriptDAO#save(eionet.cr.dto.PostHarvestScriptDTO)
     */
    @Override
    public void save(PostHarvestScriptDTO dto) throws DAOException {

        if (dto == null) {
            throw new IllegalArgumentException("The dto must not be null");
        }

        List<String> queries = dto.getQueries();
        if (queries.isEmpty()) {
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = getSQLConnection();
            conn.setAutoCommit(false);

            List<String> values = new ArrayList<String>();
            values.add(dto.getUri());
            if (dto instanceof SourcePostHarvestScriptDTO){
                SQLUtil.executeUpdate(CLEAR_SOURCE_SCRIPT_SQL, values, conn);
                pstmt = conn.prepareStatement(SAVE_SOURCE_SCRIPT_SQL);
            }
            else{
                SQLUtil.executeUpdate(CLEAR_TYPE_SCRIPT_SQL, values, conn);
                pstmt = conn.prepareStatement(SAVE_TYPE_SCRIPT_SQL);
            }

            int i = 1;
            for (String query : queries) {

                pstmt.setString(1, dto.getUri());
                pstmt.setString(2, query);
                pstmt.setInt(3, i++);
                pstmt.addBatch();
            }

            pstmt.executeBatch();
            conn.commit();

        } catch (Throwable t) {
            SQLUtil.rollback(conn);
            throw new DAOException(t.getMessage(), t);
        } finally {
            SQLUtil.close(pstmt);
            SQLUtil.close(conn);
        }
    }

    /** */
    private static final String CLEAR_SOURCE_SCRIPT_SQL =
        "delete from POST_HARVEST_SCRIPT where HARVEST_SOURCE_URL=?";
    private static final String CLEAR_TYPE_SCRIPT_SQL =
        "delete from POST_HARVEST_SCRIPT where RDF_TYPE_URI=?";
    /**
     * @see eionet.cr.dao.PostHarvestScriptDAO#delete(eionet.cr.dto.PostHarvestScriptDTO)
     */
    @Override
    public void delete(PostHarvestScriptDTO dto) throws DAOException {

        if (dto == null) {
            throw new IllegalArgumentException("The dto must not be null");
        }

        List<String> values = new ArrayList<String>();
        values.add(dto.getUri());
        if (dto instanceof SourcePostHarvestScriptDTO){
            executeSQL(CLEAR_SOURCE_SCRIPT_SQL, values);
        }
        else{
            executeSQL(CLEAR_TYPE_SCRIPT_SQL, values);
        }
    }

    /** */
    private static final String GET_SOURCE_SCRIPT_SQL =
        "select HARVEST_SOURCE_URL as URI, QUERY from POST_HARVEST_SCRIPT where HARVEST_SOURCE_URL=? order by POSITION_INDEX";
    private static final String GET_TYPE_SCRIPT_SQL =
        "select RDF_TYPE_URI as URI, QUERY from POST_HARVEST_SCRIPT where RDF_TYPE_URI=? order by POSITION_INDEX";

    /**
     * @see eionet.cr.dao.PostHarvestScriptDAO#get(eionet.cr.dto.PostHarvestScriptDTO.Type, java.lang.String)
     */
    @Override
    public PostHarvestScriptDTO get(Type targetType, String uri) throws DAOException {

        if (targetType == null || StringUtils.isBlank(uri)) {
            throw new IllegalArgumentException("Target type and URI must not be blank!");
        }

        List<String> values = new ArrayList<String>();
        values.add(uri);
        String sql = targetType.equals(Type.HARVEST_SOURCE) ? GET_SOURCE_SCRIPT_SQL : GET_TYPE_SCRIPT_SQL;
        List<PostHarvestScriptDTO> resultList = executeSQL(sql, values, new PostHarvestScriptDTOReader(targetType));
        return resultList.isEmpty() ? null : resultList.get(0);
    }
}
