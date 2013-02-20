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

package eionet.cr.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Statement;

import eionet.cr.dto.EndpointHarvestQueryDTO;

/**
 * DAO interface for operations with SPARQL endpoint harvest queries.
 *
 * @author jaanus
 */
public interface EndpointHarvestQueryDAO extends DAO {

    /**
     * Creates a new endpoint harvest query record, returns its ID.
     *
     * @param dto The DTo from which to create.
     * @return The created record's id.
     * @throws DAOException If any sort of database access error happens.
     */
    public int create(EndpointHarvestQueryDTO dto) throws DAOException;

    /**
     * Returns a list of {@link EndpointHarvestQueryDAO} matching to the given endpoint URL. The latter may be null in which case
     * all will be returned.
     * @param url The endpoint url to search by. May be null in which case all will be returned.
     * @return The list of matching objects.
     * @throws DAOException If any sort of database access error happens.
     */
    public List<EndpointHarvestQueryDTO> listByEndpointUrl(String url) throws DAOException;

    /**
     * Same as {@link #listByEndpointUrl(String)}, but additional boolean input enables to control whether the matching queries
     * should be active (true) or not (false).
     *
     * @param url The endpoint url to search by. May be null in which case all will be returned.
     * @param active If true, only active queries will be searched for, otherwise only non-active are searched.
     * @return The list of matching objects.
     * @throws DAOException If any sort of database access error happens.
     */
    public List<EndpointHarvestQueryDTO> listByEndpointUrl(String url, boolean active) throws DAOException;

    /**
     * Fetches a {@link EndpointHarvestQueryDAO} by the given id.
     * @param id The given id.
     * @return The matching {@link EndpointHarvestQueryDAO} or null if no match found.
     * @throws DAOException If any sort of database access error happens.
     */
    public EndpointHarvestQueryDTO fetchById(int id) throws DAOException;

    /**
     * Returns a list of SPARQL endpoint URLs currently listed in HARVEST_SOURCE table.
     * @return The list.
     * @throws DAOException If any sort of database access error happens.
     */
    public List<String> getEndpoints() throws DAOException;

    /**
     * Updates the given endpoint harvest query in the database.
     * @param dto The given endpoint harvest query.
     * @throws DAOException If any sort of database access error happens.
     */
    public void update(EndpointHarvestQueryDTO dto) throws DAOException;

    /**
     * Tests the given CONSTRUCT query at the given remote endpoint URL. Neither of the two must be blank!
     * The query is assumed to be a CONSTRUCT query abd the method assures that a reasonable LIMIT clause is added to the query
     * before sending it!
     *
     * The method returns the statements of the graph that the query returned.
     *
     * This method should really not be in a DAO layer, but let it be here until we have no service layer.
     *
     * @param query The query.
     * @param endpointUrl The endpoint URL.
     * @return The statements.
     * @throws DAOException If any sort of database access error happens.
     */
    public Collection<Statement> testConstructQuery(String query, String endpointUrl) throws DAOException;

    /**
     * Moves the given queries up/down in terms of their position in the queries of the given endpoint.
     * @param endpointUrl Given endpoint URL.
     * @param ids IDs of the queries to move.
     * @param direction The moving direction: -1 == move down, 1 == move up.
     * @throws DAOException If any sort of database access error happens.
     */
    void move(String endpointUrl, Set<Integer> ids, int direction) throws DAOException;

    /**
     * Deletes endpoint harvest queries by the given ids.
     *
     * @param selectedIds Given query ids.
     * @throws DAOException If any sort of database access error happens.
     */
    public void delete(List<Integer> selectedIds) throws DAOException;

    /**
     * Activates/deactivates the given queries (i.e. sets their ACTIVE field in the database accordingly).
     * @param ids Given query ids.
     * @throws DAOException If any sort of database access error happens.
     */
    void activateDeactivate(List<Integer> ids) throws DAOException;
}
