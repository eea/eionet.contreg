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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.PostHarvestScriptDAO;
import eionet.cr.dao.readers.PostHarvestScriptDTOReader;
import eionet.cr.dto.PostHarvestScriptDTO;
import eionet.cr.dto.PostHarvestScriptDTO.TargetType;
import eionet.cr.util.Pair;
import eionet.cr.util.YesNoBoolean;
import eionet.cr.util.sql.PairReader;
import eionet.cr.util.sql.SQLUtil;

/**
 *
 * @author Jaanus Heinlaid
 */
public class VirtuosoPostHarvestScriptDAO extends VirtuosoBaseDAO implements PostHarvestScriptDAO {

    /** */
    private static final String LIST_SQL = "select * from POST_HARVEST_SCRIPT where "
        + "coalesce(TARGET_SOURCE_URL,'')=? and coalesce(TARGET_TYPE_URL,'')=? order by POSITION_NUMBER asc";

    /**
     * @see eionet.cr.dao.PostHarvestScriptDAO#list(eionet.cr.dto.PostHarvestScriptDTO.TargetType, java.lang.String)
     */
    @Override
    public List<PostHarvestScriptDTO> list(TargetType targetType, String targetUrl) throws DAOException {

        String sourceUrl = targetType != null && targetType.equals(TargetType.SOURCE) ? targetUrl : null;
        String typeUrl = targetType != null && targetType.equals(TargetType.TYPE) ? targetUrl : null;

        ArrayList<Object> values = new ArrayList<Object>();
        values.add(sourceUrl == null ? "" : sourceUrl);
        values.add(typeUrl == null ? "" : typeUrl);

        return executeSQL(LIST_SQL, values, new PostHarvestScriptDTOReader());
    }

    /** */
    private static final String SAVE_SQL = "update POST_HARVEST_SCRIPT "
        + "set TITLE=?, SCRIPT=?, ACTIVE=? where POST_HARVEST_SCRIPT_ID=?";

    /**
     * @see eionet.cr.dao.PostHarvestScriptDAO#fetch(int)
     */
    @Override
    public PostHarvestScriptDTO fetch(int id) throws DAOException {
    
        ArrayList<Object> values = new ArrayList<Object>();
        values.add(Integer.valueOf(id));
        return executeUniqueResultSQL(FETCH_SQL, values, new PostHarvestScriptDTOReader());
    }

    /**
     * @see eionet.cr.dao.PostHarvestScriptDAO#insert(eionet.cr.dto.PostHarvestScriptDTO.TargetType, java.lang.String,
     *      java.lang.String, java.lang.String, boolean)
     */
    @Override
    public int insert(TargetType targetType, String targetUrl, String title, String script, boolean active) throws DAOException {
    
        String sourceUrl = targetType != null && targetType.equals(TargetType.SOURCE) ? targetUrl : null;
        String typeUrl = targetType != null && targetType.equals(TargetType.TYPE) ? targetUrl : null;
    
        Connection conn = null;
        try {
            conn = getSQLConnection();
            conn.setAutoCommit(false);
    
            ArrayList<Object> values = new ArrayList<Object>();
            values.add(sourceUrl == null ? "" : sourceUrl);
            values.add(typeUrl == null ? "" : typeUrl);
    
            Object o = SQLUtil.executeSingleReturnValueQuery(GET_LAST_POSITION_SQL, values, conn);
            int position = o == null ? 1 : Integer.parseInt(o.toString()) + 1;
    
            values = new ArrayList<Object>();
            values.add(sourceUrl);
            values.add(typeUrl);
            values.add(title);
            values.add(script);
            values.add(Integer.valueOf(position));
            values.add(YesNoBoolean.format(active));
    
            int result = SQLUtil.executeUpdateReturnAutoID(INSERT_SQL, values, conn);
            conn.commit();
            return result;
        } catch (Exception e) {
            SQLUtil.rollback(conn);
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }

    /**
     * @see eionet.cr.dao.PostHarvestScriptDAO#save(int, String, String, boolean)
     */
    @Override
    public void save(int id, String title, String script, boolean active) throws DAOException {

        ArrayList<Object> values = new ArrayList<Object>();
        values.add(title);
        values.add(script);
        values.add(YesNoBoolean.format(active));
        values.add(Integer.valueOf(id));

        executeSQL(SAVE_SQL, values);
    }

    /** */
    private static final String DELETE_SQL = "delete from POST_HARVEST_SCRIPT where POST_HARVEST_SCRIPT_ID=?";

    /**
     * @see eionet.cr.dao.PostHarvestScriptDAO#delete(eionet.cr.dto.PostHarvestScriptDTO)
     */
    @Override
    public void delete(List<Integer> ids) throws DAOException {

        if (ids == null || ids.isEmpty()) {
            return;
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = getSQLConnection();
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement(DELETE_SQL);
            for (Integer id : ids) {
                stmt.setInt(1, id);
                stmt.addBatch();
            }
            stmt.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            SQLUtil.rollback(conn);
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(stmt);
            SQLUtil.close(conn);
        }
    }

    /**
     * @see eionet.cr.dao.PostHarvestScriptDAO#activateDeactivate(java.util.List)
     */
    @Override
    public void activateDeactivate(List<Integer> ids) throws DAOException {
    
        if (ids == null || ids.isEmpty()) {
            return;
        }
    
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = getSQLConnection();
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement(ACTIVATE_DEACTIVATE_SQL);
            for (Integer id : ids) {
                stmt.setInt(1, id);
                stmt.addBatch();
            }
            stmt.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            SQLUtil.rollback(conn);
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(stmt);
            SQLUtil.close(conn);
        }
    }

    /** */
    private static final String GET_LAST_POSITION_SQL = "select max(POSITION_NUMBER) as MAX_POS from POST_HARVEST_SCRIPT where "
        + "coalesce(TARGET_SOURCE_URL,'')=? and coalesce(TARGET_TYPE_URL,'')=?";
    private static final String INSERT_SQL = "insert into POST_HARVEST_SCRIPT "
        + "(TARGET_SOURCE_URL,TARGET_TYPE_URL,TITLE,SCRIPT,POSITION_NUMBER,ACTIVE) values " + "(?,?,?,?,?,?)";

    /** */
    private static final String FETCH_SQL = "select * from POST_HARVEST_SCRIPT where POST_HARVEST_SCRIPT_ID=?";

    /** */
    private static final String LIST_TARGETS_SQL = "select coalesce(TARGET_SOURCE_URL,TARGET_TYPE_URL) as LCOL, count(*) as RCOL "
        + "from POST_HARVEST_SCRIPT " + "where isnull(TARGET_SOURCE_URL)=? and isnull(TARGET_TYPE_URL)=? "
        + "group by LCOL order by LCOL";

    /**
     * @see eionet.cr.dao.PostHarvestScriptDAO#listTargets(TargetType)
     */
    @Override
    public List<Pair<String, Integer>> listTargets(TargetType targetType) throws DAOException {

        boolean sourceUrlMustBeNull = targetType.equals(TargetType.TYPE);
        boolean typeUrlMustBeNull = targetType.equals(TargetType.SOURCE);

        ArrayList<Object> values = new ArrayList<Object>();
        values.add(Integer.valueOf(sourceUrlMustBeNull ? 1 : 0));
        values.add(Integer.valueOf(typeUrlMustBeNull ? 1 : 0));

        return executeSQL(LIST_TARGETS_SQL, values, new PairReader<String, Integer>());
    }

    /** */
    private static final String ACTIVATE_DEACTIVATE_SQL =
        "update POST_HARVEST_SCRIPT set ACTIVE=either(starts_with(ACTIVE,'Y'),'N','Y') where POST_HARVEST_SCRIPT_ID=?";
}
