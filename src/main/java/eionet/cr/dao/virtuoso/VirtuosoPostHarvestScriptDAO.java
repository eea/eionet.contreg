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
import java.util.Set;

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
    private static final String LIST_ACTIVE_SQL = "select * from POST_HARVEST_SCRIPT where "
            + "coalesce(TARGET_SOURCE_URL,'')=? and coalesce(TARGET_TYPE_URL,'')=? where ACTIVE='Y' order by POSITION_NUMBER asc";
    private static final String LIST_ACTIVE_FOR_TYPES_SQL = "select * from POST_HARVEST_SCRIPT where "
            + "TARGET_SOURCE_URL is null and TARGET_TYPE_URL in (@types@) and ACTIVE='Y' "
            + "order by TARGET_TYPE_URL asc, POSITION_NUMBER asc";
    /** */
    private static final String SAVE_SQL = "update POST_HARVEST_SCRIPT "
            + "set TITLE=?, SCRIPT=?, ACTIVE=? where POST_HARVEST_SCRIPT_ID=?";
    /** */
    private static final String DELETE_SQL = "delete from POST_HARVEST_SCRIPT where POST_HARVEST_SCRIPT_ID=?";
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
    /** */
    private static final String ACTIVATE_DEACTIVATE_SQL =
            "update POST_HARVEST_SCRIPT set ACTIVE=either(starts_with(ACTIVE,'Y'),'N','Y') where POST_HARVEST_SCRIPT_ID=?";
    /** */
    private static final String GET_POSITIONS_SQL = "select cast(POST_HARVEST_SCRIPT_ID as varchar) as LCOL, "
            + "cast(POSITION_NUMBER as varchar) as RCOL from POST_HARVEST_SCRIPT "
            + "where coalesce(TARGET_SOURCE_URL,'')=? and coalesce(TARGET_TYPE_URL,'')=? order by RCOL asc";
    /** */
    private static final String UPDATE_POSITION_SQL =
            "update POST_HARVEST_SCRIPT set POSITION_NUMBER=? where POST_HARVEST_SCRIPT_ID=?";
    /** */
    private static final String INCREASE_POSITIONS_SQL = "update POST_HARVEST_SCRIPT set POSITION_NUMBER=POSITION_NUMBER+? "
            + "where coalesce(TARGET_SOURCE_URL,'')=? and coalesce(TARGET_TYPE_URL,'')=?";

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

    /**
     * @see eionet.cr.dao.PostHarvestScriptDAO#listActive(eionet.cr.dto.PostHarvestScriptDTO.TargetType, java.lang.String)
     */
    @Override
    public List<PostHarvestScriptDTO> listActive(TargetType targetType, String targetUrl) throws DAOException {

        String sourceUrl = targetType != null && targetType.equals(TargetType.SOURCE) ? targetUrl : null;
        String typeUrl = targetType != null && targetType.equals(TargetType.TYPE) ? targetUrl : null;

        ArrayList<Object> values = new ArrayList<Object>();
        values.add(sourceUrl == null ? "" : sourceUrl);
        values.add(typeUrl == null ? "" : typeUrl);

        return executeSQL(LIST_ACTIVE_SQL, values, new PostHarvestScriptDTOReader());
    }

    /**
     * @see eionet.cr.dao.PostHarvestScriptDAO#listActiveForTypes(java.util.List)
     */
    @Override
    public List<PostHarvestScriptDTO> listActiveForTypes(List<String> types) throws DAOException {

        if (types == null || types.isEmpty()) {
            throw new IllegalArgumentException("Types must not be null or empty!");
        }

        StringBuilder questionMarks = new StringBuilder();
        for (int i = 0; i < types.size(); i++) {
            questionMarks.append(questionMarks.length() == 0 ? "" : ",").append("?");
        }

        return executeSQL(LIST_ACTIVE_FOR_TYPES_SQL, types, new PostHarvestScriptDTOReader());
    }

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

    /**
     * Move the position of the given scripts by 1 step up or down, depending on the given direction. The direction is given as an
     * integer. Any negative integer is considered as moving up (e.g. position 3 becomes 2), any positive integer is considered
     * moving down (e.g. position 3 becomes 4). The absolute value of the direction is ignored, as the moving is always done by one
     * step only (i.e. direction of -3 does not mean that the moving will be done 3 steps up). A direction of 0 is illegal.
     *
     * Scripts are identified by the given target type, target URL and script ids. If the ids are null or empty, the method returns
     * immediately without doing anything. Target type and target url can be null or blank, in which case the method operates on
     * all-source scripts.
     *
     * @see eionet.cr.dao.PostHarvestScriptDAO#move(eionet.cr.dto.PostHarvestScriptDTO.TargetType, java.lang.String, Set, int)
     */
    @Override
    public void move(TargetType targetType, String targetUrl, Set<Integer> ids, int direction) throws DAOException {

        if (ids == null || ids.isEmpty()) {
            return;
        }

        if (direction == 0) {
            throw new IllegalArgumentException("Direction must not be 0!");
        }

        // First get the positions of all scripts matching this target type and url.

        String sourceUrl = targetType != null && targetType.equals(TargetType.SOURCE) ? targetUrl : null;
        String typeUrl = targetType != null && targetType.equals(TargetType.TYPE) ? targetUrl : null;

        ArrayList<Object> values = new ArrayList<Object>();
        values.add(sourceUrl == null ? "" : sourceUrl);
        values.add(typeUrl == null ? "" : typeUrl);

        Connection conn = null;
        try {
            conn = getSQLConnection();
            conn.setAutoCommit(false);
            PairReader<String, String> reader = new PairReader<String, String>();
            SQLUtil.executeQuery(GET_POSITIONS_SQL, values, reader, conn);
            List<Pair<String, String>> positionsById = reader.getResultList();
            if (positionsById == null || positionsById.isEmpty()) {
                return;
            }

            // If even one script is already at position 1 then moving up is not considered possible.
            // And conversely, if even one script is already at the last position, then moving down
            // is not considered possible either.
            for (Pair<String, String> pair : positionsById) {

                int id = Integer.parseInt(pair.getLeft());
                int position = Integer.parseInt(pair.getRight());

                // we do the check only for scripts that we have to move
                if (ids.contains(id)) {
                    if ((direction < 0 && position == 1) || (direction > 0 && position == positionsById.size())) {
                        return;
                    }
                }
            }

            for (int i = 0; i < positionsById.size(); i++) {

                Pair<String, String> pair = positionsById.get(i);
                int id = Integer.parseInt(pair.getLeft());
                int position = Integer.parseInt(pair.getRight());

                if (ids.contains(id)) {

                    Pair<String, String> neighborPair = positionsById.get(direction < 0 ? i - 1 : i + 1);
                    int neighborId = Integer.parseInt(neighborPair.getLeft());
                    int newPosition = direction < 0 ? position - 1 : position + 1;
                    move(id, position, newPosition, neighborId, conn);
                }
            }

            conn.commit();
        } catch (Exception e) {
            SQLUtil.rollback(conn);
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }

    /**
     * @param id
     * @param oldPosition
     * @param newPosition
     * @param neighborId
     * @throws SQLException
     */
    private void move(int id, int oldPosition, int newPosition, int neighborId, Connection conn) throws SQLException {

        // First move the neighbor temporarily to position 0
        // (because in DB we have set constraint that two scripts cannot be in the same position).

        ArrayList<Object> values = new ArrayList<Object>();
        values.add(Integer.valueOf(0));
        values.add(Integer.valueOf(neighborId));
        SQLUtil.executeUpdate(UPDATE_POSITION_SQL, values, conn);

        // Secondly, move the mover-script to the new position.
        values = new ArrayList<Object>();
        values.add(Integer.valueOf(newPosition));
        values.add(Integer.valueOf(id));
        SQLUtil.executeUpdate(UPDATE_POSITION_SQL, values, conn);

        // Finally, move the neighboring script the mover-script's old position.
        values = new ArrayList<Object>();
        values.add(Integer.valueOf(oldPosition));
        values.add(Integer.valueOf(neighborId));
        SQLUtil.executeUpdate(UPDATE_POSITION_SQL, values, conn);
    }

    public void move2(TargetType targetType, String targetUrl, Set<Integer> ids, int direction) throws DAOException {

        if (ids == null || ids.isEmpty()) {
            return;
        }

        if (direction == 0) {
            throw new IllegalArgumentException("Direction must not be 0!");
        }

        String sourceUrl = targetType != null && targetType.equals(TargetType.SOURCE) ? targetUrl : null;
        String typeUrl = targetType != null && targetType.equals(TargetType.TYPE) ? targetUrl : null;

        ArrayList<Object> values = new ArrayList<Object>();
        values.add(sourceUrl == null ? "" : sourceUrl);
        values.add(typeUrl == null ? "" : typeUrl);

        Connection conn = null;
        try {
            conn = getSQLConnection();
            conn.setAutoCommit(false);

            PostHarvestScriptDTOReader reader = new PostHarvestScriptDTOReader();
            SQLUtil.executeQuery(LIST_SQL, values, reader, conn);
            List<PostHarvestScriptDTO> scripts = reader.getResultList();

            // If even one script is already at position 1 then moving up is not considered possible.
            // And conversely, if even one script is already at the last position, then moving down
            // is not considered possible either.
            boolean isMovingPossible = true;
            List<Integer> selectedPositions = new ArrayList<Integer>();
            for (PostHarvestScriptDTO script : scripts) {

                // we do this check only for scripts that have been selected
                if (ids.contains(script.getId())) {
                    int position = script.getPosition();
                    if ((direction < 0 && position == 1) || (direction > 0 && position == scripts.size())) {
                        isMovingPossible = false;
                    } else {
                        selectedPositions.add(position);
                    }
                }
            }

            if (isMovingPossible) {

                if (direction < 0) {
                    for (Integer selectedPosition : selectedPositions) {
                        int i = selectedPosition - 1;
                        PostHarvestScriptDTO scriptToMove = scripts.get(i);
                        scripts.set(i, scripts.get(i - 1));
                        scripts.set(i - 1, scriptToMove);
                    }
                } else {
                    for (int j = selectedPositions.size() - 1; j >= 0; j--) {

                        int i = selectedPositions.get(j) - 1;
                        PostHarvestScriptDTO scriptToMove = scripts.get(i);
                        scripts.set(i, scripts.get(i + 1));
                        scripts.set(i + 1, scriptToMove);
                    }
                }
            }

            values.add(0, Integer.valueOf(scripts.size()));
            SQLUtil.executeUpdate(INCREASE_POSITIONS_SQL, values, conn);

            for (int i = 0; i < scripts.size(); i++) {

                values = new ArrayList<Object>();
                values.add(i + 1);
                values.add(Integer.valueOf(scripts.get(i).getId()));
                SQLUtil.executeUpdate(UPDATE_POSITION_SQL, values, conn);
            }

            conn.commit();
        } catch (Exception e) {
            SQLUtil.rollback(conn);
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }
}
