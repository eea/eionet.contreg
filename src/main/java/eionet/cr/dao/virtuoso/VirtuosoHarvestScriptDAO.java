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

import eionet.cr.dao.DAOException;
import eionet.cr.dao.HarvestScriptDAO;
import eionet.cr.dao.readers.HarvestScriptDTOReader;
import eionet.cr.dao.readers.HarvestScriptTestResultsReader;
import eionet.cr.dao.util.HarvestScriptSet;
import eionet.cr.dto.HarvestScriptDTO;
import eionet.cr.dto.HarvestScriptDTO.Phase;
import eionet.cr.dto.HarvestScriptDTO.TargetType;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.enums.HarvestScriptType;
import eionet.cr.util.Bindings;
import eionet.cr.util.Pair;
import eionet.cr.util.YesNoBoolean;
import eionet.cr.util.sesame.SesameConnectionProvider;
import eionet.cr.util.sesame.SesameUtil;
import eionet.cr.util.sql.PairReader;
import eionet.cr.util.sql.SQLUtil;
import eionet.cr.util.sql.SingleObjectReader;
import eionet.cr.web.action.admin.harvestscripts.HarvestScriptParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.openrdf.repository.RepositoryConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Jaanus Heinlaid
 */
public class VirtuosoHarvestScriptDAO extends VirtuosoBaseDAO implements HarvestScriptDAO {

    /** */
    private static final String LIST_SQL = "select * from POST_HARVEST_SCRIPT where "
            + "coalesce(TARGET_SOURCE_URL,'')=? and coalesce(TARGET_TYPE_URL,'')=? and PHASE=coalesce(?,PHASE) "
            + "order by POSITION_NUMBER asc";
    private static final String LIST_ACTIVE_SQL = "select * from POST_HARVEST_SCRIPT where "
            + "coalesce(TARGET_SOURCE_URL,'')=? and coalesce(TARGET_TYPE_URL,'')=? and PHASE=coalesce(?,PHASE) and TYPE=coalesce(?,TYPE) and ACTIVE='Y' "
            + "order by POSITION_NUMBER asc";
    private static final String LIST_ACTIVE_FOR_TYPES_SQL = "select * from POST_HARVEST_SCRIPT where "
            + "TARGET_SOURCE_URL is null and TARGET_TYPE_URL in (@types@) and PHASE=coalesce(?,PHASE) and TYPE=coalesce(?,TYPE) and ACTIVE='Y' "
            + "order by TARGET_TYPE_URL asc, POSITION_NUMBER asc";
    /** */
    private static final String SAVE_SQL = "update POST_HARVEST_SCRIPT "
            + "set TITLE=?, SCRIPT=?, ACTIVE=?, RUN_ONCE=?, PHASE=?, TYPE=?, EXTERNAL_SERVICE_ID=?, EXTERNAL_SERVICE_PARAMS=?, LAST_MODIFIED=NOW() where POST_HARVEST_SCRIPT_ID=?";
    /** */
    private static final String DELETE_SQL = "delete from POST_HARVEST_SCRIPT where POST_HARVEST_SCRIPT_ID=?";
    /** */
    private static final String GET_LAST_POSITION_SQL = "select max(POSITION_NUMBER) as MAX_POS from POST_HARVEST_SCRIPT where "
            + "coalesce(TARGET_SOURCE_URL,'')=? and coalesce(TARGET_TYPE_URL,'')=?";
    /** */
    private static final String INSERT_SQL = "insert into POST_HARVEST_SCRIPT "
            + "(TARGET_SOURCE_URL,TARGET_TYPE_URL,TITLE,SCRIPT,POSITION_NUMBER,ACTIVE,RUN_ONCE,PHASE,TYPE, EXTERNAL_SERVICE_ID, EXTERNAL_SERVICE_PARAMS, LAST_MODIFIED) values "
            + "(?,?,?,?,?,?,?,?,?,?,?,NOW())";
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

    /** */
    private static final String EXISTS_SQL = "select count(*) from POST_HARVEST_SCRIPT where "
            + "coalesce(TARGET_SOURCE_URL,'')=? and coalesce(TARGET_TYPE_URL,'')=? and TITLE=?";

    /** */
    private static final String SEARCH_SQL =
            "select * from POST_HARVEST_SCRIPT where strcasestr(TITLE,?) >= 0 or strcasestr(cast (SCRIPT as varchar),?) >= 0 order by TITLE";

    /**
     * @see eionet.cr.dao.HarvestScriptDAO#list(eionet.cr.dto.HarvestScriptDTO.TargetType, java.lang.String, Phase)
     */
    @Override
    public List<HarvestScriptDTO> list(TargetType targetType, String targetUrl, Phase phase)
            throws DAOException {

        String sourceUrl = targetType != null && targetType.equals(TargetType.SOURCE) ? targetUrl : null;
        String typeUrl = targetType != null && targetType.equals(TargetType.TYPE) ? targetUrl : null;

        ArrayList<Object> values = new ArrayList<Object>();
        values.add(sourceUrl == null ? "" : sourceUrl);
        values.add(typeUrl == null ? "" : typeUrl);
        values.add(phase == null ? null : phase.name());

        return executeSQL(LIST_SQL, values, new HarvestScriptDTOReader());
    }

    /**
     * @see eionet.cr.dao.HarvestScriptDAO#listActive(eionet.cr.dto.HarvestScriptDTO.TargetType, java.lang.String, Phase, HarvestScriptType)
     */
    @Override
    public List<HarvestScriptDTO> listActive(TargetType targetType, String targetUrl, Phase phase, 
            HarvestScriptType scriptType) throws DAOException {

        String sourceUrl = targetType != null && targetType.equals(TargetType.SOURCE) ? targetUrl : null;
        String typeUrl = targetType != null && targetType.equals(TargetType.TYPE) ? targetUrl : null;

        ArrayList<Object> values = new ArrayList<Object>();
        values.add(sourceUrl == null ? "" : sourceUrl);
        values.add(typeUrl == null ? "" : typeUrl);
        values.add(phase == null ? null : phase.name());
        values.add(scriptType == null ? null : scriptType.name());
        
        return executeSQL(LIST_ACTIVE_SQL, values, new HarvestScriptDTOReader());
    }

    /**
     * @see eionet.cr.dao.HarvestScriptDAO#listActiveForTypes(java.util.List, Phase)
     */
    @Override
    public List<HarvestScriptDTO> listActiveForTypes(List<String> types, Phase phase, 
            HarvestScriptType scriptType) throws DAOException {

        if (types == null || types.isEmpty()) {
            throw new IllegalArgumentException("Types must not be null or empty!");
        }

        StringBuilder questionMarks = new StringBuilder();
        for (int i = 0; i < types.size(); i++) {
            questionMarks.append(questionMarks.length() == 0 ? "" : ",").append("?");
        }

        String sql = LIST_ACTIVE_FOR_TYPES_SQL.replace("@types@", questionMarks);

        ArrayList<Object> values = new ArrayList<Object>();
        values.addAll(types);
        values.add(phase == null ? null : phase.name());
        values.add(scriptType == null ? null : scriptType.name());

        return executeSQL(sql, values, new HarvestScriptDTOReader());
    }

    /**
     * @see eionet.cr.dao.HarvestScriptDAO#fetch(int)
     */
    @Override
    public HarvestScriptDTO fetch(int id) throws DAOException {

        ArrayList<Object> values = new ArrayList<Object>();
        values.add(Integer.valueOf(id));
        return executeUniqueResultSQL(FETCH_SQL, values, new HarvestScriptDTOReader());
    }

    /**
     * @see eionet.cr.dao.HarvestScriptDAO#insert(eionet.cr.dto.HarvestScriptDTO.TargetType, java.lang.String, java.lang.String,
     *      java.lang.String, boolean, boolean, Phase,HarvestScriptType, java.lang.Integer, java.lang.String)
     */
    @Override
    public int insert(TargetType targetType, String targetUrl, String title, String script, boolean active, boolean runOnce,
            Phase phase, HarvestScriptType type, Integer serviceId, String externalServiceParams) throws DAOException {

        String sourceUrl = targetType != null && targetType.equals(TargetType.SOURCE) ? targetUrl : null;
        String typeUrl = targetType != null && targetType.equals(TargetType.TYPE) ? targetUrl : null;

        if (phase == null) {
            phase = HarvestScriptDTO.DEFAULT_PHASE;
        }

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
            values.add(YesNoBoolean.format(runOnce));
            values.add(phase.name());
            values.add(type.name());
            values.add(serviceId);
            values.add(externalServiceParams);

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
     * @see eionet.cr.dao.HarvestScriptDAO#save(int, String, String, boolean, boolean, Phase,HarvestScriptType, java.lang.Integer, java.lang.String)
     */
    @Override
    public void save(int id, String title, String script, boolean active, boolean runOnce, Phase phase, HarvestScriptType type, 
            Integer serviceId, String externalServiceParams) throws DAOException {

        if (phase == null) {
            phase = HarvestScriptDTO.DEFAULT_PHASE;
        }

        ArrayList<Object> values = new ArrayList<Object>();
        values.add(title);
        values.add(script);
        values.add(YesNoBoolean.format(active));
        values.add(YesNoBoolean.format(runOnce));
        values.add(phase.name());
        values.add(type.name());
        values.add(serviceId);
        values.add(externalServiceParams);
        
        //id
        values.add(Integer.valueOf(id));

        executeSQL(SAVE_SQL, values);
    }

    /**
     * @see eionet.cr.dao.HarvestScriptDAO#delete(List<Integer>)
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
     * @see eionet.cr.dao.HarvestScriptDAO#activateDeactivate(java.util.List)
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
     * @see eionet.cr.dao.HarvestScriptDAO#listTargets(TargetType)
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
     * @see eionet.cr.dao.HarvestScriptDAO#move(eionet.cr.dto.HarvestScriptDTO.TargetType, java.lang.String, java.util.Set, int)
     */
    @Override
    public void move(TargetType targetType, String targetUrl, Set<Integer> ids, int direction) throws DAOException {

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
        values.add(null);

        Connection conn = null;
        try {
            conn = getSQLConnection();
            conn.setAutoCommit(false);

            HarvestScriptDTOReader reader = new HarvestScriptDTOReader();
            SQLUtil.executeQuery(LIST_SQL, values, reader, conn);
            List<HarvestScriptDTO> scripts = reader.getResultList();

            // helper object for handling min, max positions and real count of scripts
            HarvestScriptSet scriptSet = new HarvestScriptSet(scripts);

            // If even one script is already at position 1 then moving up is not considered possible.
            // And conversely, if even one script is already at the last position, then moving down
            // is not considered possible either.
            boolean isMovingPossible = true;
            List<Integer> selectedPositions = new ArrayList<Integer>();
            for (HarvestScriptDTO script : scripts) {

                // we do this check only for scripts that have been selected
                if (ids.contains(script.getId())) {
                    int position = script.getPosition();
                    if ((direction < 0 && position == scriptSet.getMinPosition())
                            || (direction > 0 && position == scriptSet.getMaxPosition())) {
                        isMovingPossible = false;
                    } else {
                        selectedPositions.add(position);
                    }
                }
            }

            if (isMovingPossible) {

                if (direction < 0) {
                    for (Integer selectedPosition : selectedPositions) {
                        HarvestScriptDTO scriptToMove = scriptSet.getScriptByPosition(selectedPosition);
                        int i = scripts.indexOf(scriptToMove);

                        scripts.set(i, scripts.get(i - 1));
                        scripts.set(i - 1, scriptToMove);
                    }
                } else {
                    for (int j = selectedPositions.size() - 1; j >= 0; j--) {
                        HarvestScriptDTO scriptToMove = scriptSet.getScriptByPosition(selectedPositions.get(j));
                        int i = scripts.indexOf(scriptToMove);

                        scripts.set(i, scripts.get(i + 1));
                        scripts.set(i + 1, scriptToMove);
                    }
                }
            }

            values = new ArrayList<Object>();
            values.add(sourceUrl == null ? "" : sourceUrl);
            values.add(typeUrl == null ? "" : typeUrl);
            values.add(0, Integer.valueOf(scriptSet.getMaxPosition()));
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

    /**
     * @see eionet.cr.dao.HarvestScriptDAO#exists(eionet.cr.dto.HarvestScriptDTO.TargetType, java.lang.String, java.lang.String)
     */
    @Override
    public boolean exists(TargetType targetType, String targetUrl, String title) throws DAOException {

        if (StringUtils.isBlank(title)) {
            throw new IllegalArgumentException("Title must not be blank!");
        }

        String sourceUrl = targetType != null && targetType.equals(TargetType.SOURCE) ? targetUrl : null;
        String typeUrl = targetType != null && targetType.equals(TargetType.TYPE) ? targetUrl : null;

        ArrayList<Object> values = new ArrayList<Object>();
        values.add(sourceUrl == null ? "" : sourceUrl);
        values.add(typeUrl == null ? "" : typeUrl);
        values.add(title);

        Object o = executeUniqueResultSQL(EXISTS_SQL, values, new SingleObjectReader<Object>());
        return o == null ? false : Integer.parseInt(o.toString()) > 0;
    }

    /**
     * @throws DAOException
     * @see eionet.cr.dao.HarvestScriptDAO#test(java.lang.String)
     */
    @Override
    public List<Map<String, ObjectDTO>> test(String query) throws DAOException {

        if (StringUtils.isBlank(query)) {
            return new ArrayList<Map<String, ObjectDTO>>();
        }

        RepositoryConnection conn = null;
        try {
            conn = SesameConnectionProvider.getReadOnlyRepositoryConnection();
            HarvestScriptTestResultsReader reader = new HarvestScriptTestResultsReader();
            SesameUtil.executeQuery(query, reader, conn);
            return reader.getResultList();
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SesameUtil.close(conn);
        }
    }

    /**
     * @see eionet.cr.dao.HarvestScriptDAO#test(java.lang.String, eionet.cr.dto.HarvestScriptDTO.TargetType, java.lang.String,
     *      java.lang.String)
     */
    @Override
    public List<Map<String, ObjectDTO>>
            test(String constructQuery, TargetType targetType, String targetUrl, String harvestedSource) throws DAOException {

        if (StringUtils.isBlank(constructQuery)) {
            return new ArrayList<Map<String, ObjectDTO>>();
        }

        String sourceReplacer = harvestedSource;
        if (StringUtils.isBlank(sourceReplacer)) {
            if (targetType != null && targetType.equals(TargetType.SOURCE)) {
                sourceReplacer = targetUrl;
            }
        }
        String typeReplacer = targetType != null && targetType.equals(TargetType.TYPE) ? targetUrl : null;

        RepositoryConnection conn = null;
        try {
            conn = SesameConnectionProvider.getReadOnlyRepositoryConnection();

            Bindings bindings = new Bindings();
            if (!StringUtils.isBlank(sourceReplacer)) {
                bindings.setURI(HarvestScriptParser.HARVESTED_SOURCE_VARIABLE, sourceReplacer);
            }
            if (!StringUtils.isBlank(typeReplacer)) {
                bindings.setURI(HarvestScriptParser.ASSOCIATED_TYPE_VARIABLE, typeReplacer);
            }

            HarvestScriptTestResultsReader reader = new HarvestScriptTestResultsReader();
            SesameUtil.executeQuery(constructQuery, bindings, reader, conn);
            return reader.getResultList();
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SesameUtil.close(conn);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isScriptsModified(Date lastHarvestDate, String harvestSource) throws DAOException {

        String sql =
                "SELECT count(*) FROM post_harvest_script WHERE " + "(target_source_url = ? OR target_source_url IS NULL) AND "
                        + "last_modified > ? AND active = 'Y' AND target_type_url IS  NULL";

        ArrayList<Object> values = new ArrayList<Object>();
        values.add(harvestSource);
        values.add(lastHarvestDate);

        Object result = executeUniqueResultSQL(sql, values, new SingleObjectReader<Object>());
        return result != null && NumberUtils.toInt(result.toString()) > 0;
    }

    @Override
    public List<HarvestScriptDTO> getScriptsByIds(List<Integer> ids) throws DAOException {
        ArrayList<HarvestScriptDTO> result = new ArrayList<HarvestScriptDTO>();

        for (int id : ids) {
            result.add(fetch(id));
        }

        return result.isEmpty() ? null : result;

    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.PostHarvestScriptDAO#addScripts(eionet.cr.dto.HarvestScriptDTO.TargetType, java.lang.String,
     * java.util.List)
     */
    @Override
    public void addScripts(TargetType targetType, String targetUrl, List<HarvestScriptDTO> scripts) throws DAOException {
        for (HarvestScriptDTO script : scripts) {
            insert(targetType, targetUrl, script.getTitle(), script.getScript(), script.isActive(), script.isRunOnce(),
                    script.getPhase(), script.getType(), script.getExternalServiceId(), script.getExternalServiceParams());
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.PostHarvestScriptDAO#search(java.lang.String)
     */
    @Override
    public List<HarvestScriptDTO> search(String searchText) throws DAOException {

        ArrayList<Object> values = new ArrayList<Object>();
        values.add(searchText);
        values.add(searchText);
        return executeSQL(SEARCH_SQL, values, new HarvestScriptDTOReader());
    }
}
