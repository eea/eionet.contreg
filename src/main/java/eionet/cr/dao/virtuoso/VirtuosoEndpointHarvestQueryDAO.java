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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import eionet.cr.common.CRException;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.EndpointHarvestQueryDAO;
import eionet.cr.dao.readers.EndpointHarvestQueryDTOReader;
import eionet.cr.dto.EndpointHarvestQueryDTO;
import eionet.cr.util.Hashes;
import eionet.cr.util.YesNoBoolean;
import eionet.cr.util.sql.SQLUtil;

/**
 * Virtuoso-specific implementation of {@link EndpointHarvestQueryDAO}.
 *
 * @author jaanus
 */
public class VirtuosoEndpointHarvestQueryDAO extends VirtuosoBaseDAO implements EndpointHarvestQueryDAO {

    /** */
    private static final String CREATE_SQL = "insert into ENDPOINT_HARVEST_QUERY"
            + " (TITLE,QUERY,ENDPOINT_URL,ENDPOINT_URL_HASH,POSITION_NUMBER,ACTIVE) values (?,?,?,?,?,?)";

    /** */
    private static final String LIST_BY_URL_HASH_SQL = "select * from ENDPOINT_HARVEST_QUERY"
            + " where ENDPOINT_URL_HASH=? order by ENDPOINT_URL, POSITION_NUMBER";

    /** */
    private static final String LIST_BY_URL_HASH_ACTIVE_SQL = "select * from ENDPOINT_HARVEST_QUERY"
            + " where ENDPOINT_URL_HASH=? and ACTIVE=? order by ENDPOINT_URL, POSITION_NUMBER";

    /** */
    private static final String FETCH_BY_ID_SQL = "select * from ENDPOINT_HARVEST_QUERY"
            + " where ENDPOINT_HARVEST_QUERY_ID=?";

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

        ArrayList<Object> params = new ArrayList<Object>();
        params.add(dto.getTitle());
        params.add(dto.getQuery());
        params.add(dto.getEndpointUrl());
        params.add(dto.getEndpointUrlHash());
        params.add(dto.getPosition());
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

        if (StringUtils.isBlank(url)) {
            throw new IllegalArgumentException("The endpoint url must not be blank!");
        }

        ArrayList<Object> values = new ArrayList<Object>();
        values.add(Hashes.spoHash(url));

        return executeSQL(LIST_BY_URL_HASH_SQL, values, new EndpointHarvestQueryDTOReader());
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.EndpointHarvestQueryDAO#listByEndpointUrl(java.lang.String, boolean)
     */
    @Override
    public List<EndpointHarvestQueryDTO> listByEndpointUrl(String url, boolean active) throws DAOException {

        if (StringUtils.isBlank(url)) {
            throw new IllegalArgumentException("The endpoint url must not be blank!");
        }

        ArrayList<Object> values = new ArrayList<Object>();
        values.add(Hashes.spoHash(url));
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
}
