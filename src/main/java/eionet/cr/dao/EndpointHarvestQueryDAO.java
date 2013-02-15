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

import java.util.List;

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
}
