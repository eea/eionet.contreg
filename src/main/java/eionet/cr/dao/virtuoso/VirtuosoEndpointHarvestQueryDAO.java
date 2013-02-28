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
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        jaanus
 */

package eionet.cr.dao.virtuoso;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Statement;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sparql.SPARQLRepository;

import eionet.cr.common.CRException;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.EndpointHarvestQueryDAO;
import eionet.cr.dao.readers.EndpointHarvestQueryDTOReader;
import eionet.cr.dto.EndpointHarvestQueryDTO;
import eionet.cr.util.Hashes;
import eionet.cr.util.YesNoBoolean;
import eionet.cr.util.sesame.SesameUtil;
import eionet.cr.util.sql.SQLUtil;
import eionet.cr.util.sql.SingleObjectReader;

/**
 * Virtuoso-specific implementation of {@link EndpointHarvestQueryDAO}.
 *
 * @author jaanus
 */
public class VirtuosoEndpointHarvestQueryDAO extends VirtuosoBaseDAO implements EndpointHarvestQueryDAO {

    /**  */
    private static final int TEST_QUERY_LIMIT = 500;

    /** */
    private static final String CREATE_SQL =
            "insert into ENDPOINT_HARVEST_QUERY"
                    + " (TITLE,QUERY,ENDPOINT_URL,ENDPOINT_URL_HASH,POSITION_NUMBER,ACTIVE,LAST_MODIFIED) values"
                    + " (?,?,?,?,(select coalesce(max(POSITION_NUMBER), 0)+1 from ENDPOINT_HARVEST_QUERY where ENDPOINT_URL_HASH=?),?,now())";

    /** */
    private static final String LIST_BY_URL_HASH_SQL = "select * from ENDPOINT_HARVEST_QUERY"
            + " where ENDPOINT_URL_HASH=coalesce(?, ENDPOINT_URL_HASH) order by ENDPOINT_URL, POSITION_NUMBER";

    /** */
    private static final String LIST_BY_URL_HASH_ACTIVE_SQL = "select * from ENDPOINT_HARVEST_QUERY"
            + " where ENDPOINT_URL_HASH=coalesce(?, ENDPOINT_URL_HASH) and ACTIVE=? order by ENDPOINT_URL, POSITION_NUMBER";

    /** */
    private static final String FETCH_BY_ID_SQL = "select * from ENDPOINT_HARVEST_QUERY" + " where ENDPOINT_HARVEST_QUERY_ID=?";

    /** */
    private static final String GET_ENDPOINTS_SQL = "select URL from HARVEST_SOURCE where IS_SPARQL_ENDPOINT='Y' order by URL";

    /** */
    private static final String UPDATE_SQL = "update ENDPOINT_HARVEST_QUERY set TITLE=?, QUERY=?, ACTIVE=?, LAST_MODIFIED=now()"
            + " where ENDPOINT_HARVEST_QUERY_ID=?";

    /** */
    private static final String INCREASE_POSITIONS_SQL = "update ENDPOINT_HARVEST_QUERY set POSITION_NUMBER=POSITION_NUMBER + ? "
            + " where ENDPOINT_URL=?";

    /** */
    private static final String UPDATE_POSITION_SQL =
            "update ENDPOINT_HARVEST_QUERY set POSITION_NUMBER=? where ENDPOINT_HARVEST_QUERY_ID=?";

    /** */
    private static final String EXISTS_SQL = "select count(*) from POST_HARVEST_SCRIPT where "
            + "coalesce(TARGET_SOURCE_URL,'')=? and coalesce(TARGET_TYPE_URL,'')=? and TITLE=?";

    /** */
    private static final String DELETE_SQL = "delete from ENDPOINT_HARVEST_QUERY where ENDPOINT_HARVEST_QUERY_ID=?";

    /** */
    private static final String ACTIVATE_DEACTIVATE_SQL =
            "update ENDPOINT_HARVEST_QUERY set ACTIVE=either(starts_with(ACTIVE,'Y'),'N','Y') where ENDPOINT_HARVEST_QUERY_ID=?";

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.EndpointHarvestQueryDAO#createEndpointHarvestQuery(eionet.cr.dto.EndpointHarvestQueryDTO)
     */
    @Override
    public int create(EndpointHarvestQueryDTO dto) throws DAOException {

        if (dto == null) {
            throw new IllegalArgumentException("The DTO must not be null!");
        }

        long endpointUrlHash = dto.getEndpointUrlHash();
        ArrayList<Object> params = new ArrayList<Object>();
        params.add(dto.getTitle());
        params.add(dto.getQuery());
        params.add(dto.getEndpointUrl());
        params.add(endpointUrlHash);
        params.add(endpointUrlHash);
        params.add(YesNoBoolean.format(dto.isActive()));

        Connection conn = null;
        try {
            conn = getSQLConnection();
            return SQLUtil.executeUpdateReturnAutoID(CREATE_SQL, params, conn);
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        } catch (CRException e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.EndpointHarvestQueryDAO#listByEndpointUrl(java.lang.String)
     */
    @Override
    public List<EndpointHarvestQueryDTO> listByEndpointUrl(String url) throws DAOException {

        ArrayList<Object> values = new ArrayList<Object>();
        values.add(StringUtils.isBlank(url) ? (Long) null : Long.valueOf(Hashes.spoHash(url)));

        return executeSQL(LIST_BY_URL_HASH_SQL, values, new EndpointHarvestQueryDTOReader());
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.EndpointHarvestQueryDAO#listByEndpointUrl(java.lang.String, boolean)
     */
    @Override
    public List<EndpointHarvestQueryDTO> listByEndpointUrl(String url, boolean active) throws DAOException {

        ArrayList<Object> values = new ArrayList<Object>();
        values.add(StringUtils.isBlank(url) ? (Long) null : Long.valueOf(Hashes.spoHash(url)));
        values.add(YesNoBoolean.format(active));

        return executeSQL(LIST_BY_URL_HASH_ACTIVE_SQL, values, new EndpointHarvestQueryDTOReader());
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.EndpointHarvestQueryDAO#fetchById(int)
     */
    @Override
    public EndpointHarvestQueryDTO fetchById(int id) throws DAOException {

        ArrayList<Object> params = new ArrayList<Object>();
        params.add(id);

        List<EndpointHarvestQueryDTO> list = executeSQL(FETCH_BY_ID_SQL, params, new EndpointHarvestQueryDTOReader());
        return list == null || list.isEmpty() ? null : list.iterator().next();
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.EndpointHarvestQueryDAO#getEndpoints()
     */
    @Override
    public List<String> getEndpoints() throws DAOException {

        return executeSQL(GET_ENDPOINTS_SQL, null, new SingleObjectReader<String>());
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.EndpointHarvestQueryDAO#update(eionet.cr.dto.EndpointHarvestQueryDTO)
     */
    @Override
    public void update(EndpointHarvestQueryDTO dto) throws DAOException {

        if (dto == null || dto.getId() <= 0) {
            throw new IllegalArgumentException("The given DTO must not be null, and its id must be > 0");
        }

        ArrayList<Object> params = new ArrayList<Object>();
        params.add(dto.getTitle());
        params.add(dto.getQuery());
        params.add(YesNoBoolean.format(dto.isActive()));
        params.add(dto.getId());

        executeSQL(UPDATE_SQL, params);
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.EndpointHarvestQueryDAO#testConstructQuery(java.lang.String, java.lang.String)
     */
    @Override
    public Collection<Statement> testConstructQuery(String query, String endpointUrl) throws DAOException {

        if (StringUtils.isBlank(query) || StringUtils.isBlank(endpointUrl)) {
            throw new IllegalArgumentException("The query and the endpoint URL must not be blank!");
        }

        query = ensureTestConstructLimit(query, TEST_QUERY_LIMIT);
        ArrayList<Statement> result = new ArrayList<Statement>();
        SPARQLRepository sparqlRepository = new SPARQLRepository(endpointUrl);

        RepositoryConnection repoConn = null;
        GraphQueryResult queryResult = null;
        try {
            repoConn = sparqlRepository.getConnection();
            GraphQuery graphQuery = repoConn.prepareGraphQuery(QueryLanguage.SPARQL, query);
            queryResult = graphQuery.evaluate();
            if (queryResult != null) {
                int counter = 0;
                while (queryResult.hasNext() && counter++ < TEST_QUERY_LIMIT) {
                    result.add(queryResult.next());
                }
            }
        } catch (OpenRDFException e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SesameUtil.close(queryResult);
            SesameUtil.close(repoConn);
        }

        return result;
    }

    /**
     * Ensures that the given test CONSTRUCT query has a limit included, and it does not exceed the given allowed maximum.
     *
     * @param query The given query.
     * @param maxLimit The maximum allowed limit size.
     * @return The query with the reasonable limit ensured.
     */
    private String ensureTestConstructLimit(String query, int maxLimit) {

        String upperCaseQuery = query.toUpperCase();
        String[] tokens = StringUtils.split(upperCaseQuery.trim());
        int len = tokens.length;

        Integer limit = null;
        Integer offset = null;
        boolean limitOnly = false;
        boolean limitPlusOffset = false;
        boolean offsetPlusLimit = false;

        if (len >= 4) {
            if (tokens[len - 4].equals("LIMIT") && NumberUtils.isNumber(tokens[len - 3]) && tokens[len - 2].equals("OFFSET")
                    && NumberUtils.isNumber(tokens[len - 1])) {
                limit = Integer.valueOf(tokens[len - 3]);
                offset = Integer.valueOf(tokens[len - 1]);
                limitPlusOffset = true;
            } else if (tokens[len - 4].equals("OFFSET") && NumberUtils.isNumber(tokens[len - 3])
                    && tokens[len - 2].equals("LIMIT") && NumberUtils.isNumber(tokens[len - 1])) {
                limit = Integer.valueOf(tokens[len - 1]);
                offset = Integer.valueOf(tokens[len - 3]);
                offsetPlusLimit = true;
            } else if (tokens[len - 2].equals("LIMIT") && NumberUtils.isNumber(tokens[len - 1])) {
                limit = Integer.valueOf(tokens[len - 1]);
                limitOnly = true;
            }
        } else if (len >= 2) {
            if (tokens[len - 2].equals("LIMIT") && NumberUtils.isNumber(tokens[len - 1])) {
                limit = Integer.valueOf(tokens[len - 1]);
                limitOnly = true;
            }
        }

        if (limitOnly) {
            int i = upperCaseQuery.lastIndexOf("LIMIT");
            return query.substring(0, i) + " LIMIT " + Math.min(limit.intValue(), maxLimit);
        } else if (offsetPlusLimit) {
            int i = upperCaseQuery.lastIndexOf("OFFSET");
            return query.substring(0, i) + " OFFSET " + offset.intValue() + " LIMIT " + Math.min(limit.intValue(), maxLimit);
        } else if (limitPlusOffset) {
            int i = upperCaseQuery.lastIndexOf("LIMIT");
            return query.substring(0, i) + " LIMIT " + Math.min(limit.intValue(), maxLimit) + " OFFSET " + offset.intValue();
        } else {
            return query + " LIMIT " + maxLimit;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.EndpointHarvestQueryDAO#move(java.util.Set, int)
     */
    @Override
    public void move(String endpointUrl, Set<Integer> ids, int direction) throws DAOException {

        if (StringUtils.isBlank(endpointUrl) || ids == null || ids.isEmpty()) {
            return;
        }

        if (direction == 0) {
            throw new IllegalArgumentException("Direction must not be 0!");
        }

        // Prepare map where we can get queries by position, also find the max and min positions.
        LinkedHashMap<Integer, EndpointHarvestQueryDTO> queriesByPos = getQueriesByPosition(endpointUrl);
        if (queriesByPos.isEmpty()) {
            return;
        }
        Set<Integer> positions = queriesByPos.keySet();
        int maxPos = Collections.max(positions);
        int minPos = Collections.min(positions);

        Connection conn = null;
        try {
            conn = getSQLConnection();
            conn.setAutoCommit(false);

            // If even one query is already at position 1 then moving up is not considered possible.
            // And conversely, if even one query is already at the last position, then moving down
            // is not considered possible either.

            boolean isMovingPossible = true;
            List<Integer> selectedPositions = new ArrayList<Integer>();
            List<EndpointHarvestQueryDTO> queries = new ArrayList<EndpointHarvestQueryDTO>(queriesByPos.values());
            for (EndpointHarvestQueryDTO query : queries) {

                if (ids.contains(query.getId())) {

                    int pos = query.getPosition();
                    if ((direction < 0 && pos == minPos) || (direction > 0 && pos == maxPos)) {
                        isMovingPossible = false;
                    } else {
                        selectedPositions.add(pos);
                    }
                }
            }

            if (isMovingPossible) {

                if (direction < 0) {
                    for (Integer selectedPosition : selectedPositions) {

                        EndpointHarvestQueryDTO queryToMove = queriesByPos.get(selectedPosition);
                        int i = queries.indexOf(queryToMove);
                        queries.set(i, queries.get(i - 1));
                        queries.set(i - 1, queryToMove);
                    }
                } else {
                    for (int j = selectedPositions.size() - 1; j >= 0; j--) {

                        EndpointHarvestQueryDTO queryToMove = queriesByPos.get(selectedPositions.get(j));
                        int i = queries.indexOf(queryToMove);
                        queries.set(i, queries.get(i + 1));
                        queries.set(i + 1, queryToMove);
                    }
                }
            }

            SQLUtil.executeUpdate(INCREASE_POSITIONS_SQL, Arrays.asList(maxPos, endpointUrl), conn);
            for (int i = 0; i < queries.size(); i++) {
                SQLUtil.executeUpdate(UPDATE_POSITION_SQL, Arrays.asList(i + 1, queries.get(i).getId()), conn);
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
     *
     * @param endpointUrl
     * @return
     * @throws DAOException
     */
    private LinkedHashMap<Integer, EndpointHarvestQueryDTO> getQueriesByPosition(String endpointUrl) throws DAOException {

        LinkedHashMap<Integer, EndpointHarvestQueryDTO> map = new LinkedHashMap<Integer, EndpointHarvestQueryDTO>();
        List<EndpointHarvestQueryDTO> queries = listByEndpointUrl(endpointUrl);
        for (EndpointHarvestQueryDTO dto : queries) {
            map.put(dto.getPosition(), dto);
        }

        return map;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.EndpointHarvestQueryDAO#delete(java.util.List)
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

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.EndpointHarvestQueryDAO#activateDeactivate(java.util.List)
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
}
