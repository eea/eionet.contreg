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

package eionet.cr.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.PostHarvestScriptDTO;
import eionet.cr.dto.PostHarvestScriptDTO.TargetType;
import eionet.cr.util.Pair;

/**
 *
 * @author Jaanus Heinlaid
 */
public interface PostHarvestScriptDAO extends DAO {

    /**
     *
     * @param targetType
     * @param targetUrl
     * @return
     * @throws DAOException
     */
    List<PostHarvestScriptDTO> list(PostHarvestScriptDTO.TargetType targetType, String targetUrl) throws DAOException;

    /**
     *
     * @param targetType
     * @param targetUrl
     * @return
     * @throws DAOException
     */
    List<PostHarvestScriptDTO> listActive(PostHarvestScriptDTO.TargetType targetType, String targetUrl) throws DAOException;

    /**
     *
     * @param types
     * @return
     * @throws DAOException
     */
    List<PostHarvestScriptDTO> listActiveForTypes(List<String> types) throws DAOException;

    /**
     *
     * @param id
     * @return
     * @throws DAOException
     */
    PostHarvestScriptDTO fetch(int id) throws DAOException;

    /**
     *
     * @param targetType
     * @param targetUrl
     * @param title
     * @param script
     * @param active
     * @param runOnce TODO
     * @return
     * @throws DAOException
     */
    int insert(TargetType targetType, String targetUrl, String title, String script, boolean active, boolean runOnce)
            throws DAOException;

    /**
     *
     * @param id
     * @param title
     * @param script
     * @param active
     * @param runOnce TODO
     * @throws DAOException
     */
    void save(int id, String title, String script, boolean active, boolean runOnce) throws DAOException;

    /**
     *
     * @param ids
     * @throws DAOException
     */
    void delete(List<Integer> ids) throws DAOException;

    /**
     *
     * @param ids
     * @throws DAOException
     */
    void activateDeactivate(List<Integer> ids) throws DAOException;

    /**
     *
     * @param targetType
     * @return
     * @throws DAOException
     */
    List<Pair<String, Integer>> listTargets(TargetType targetType) throws DAOException;

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
     * @param targetType
     * @param targetUrl
     * @param ids
     * @param direction
     * @throws DAOException
     */
    void move(TargetType targetType, String targetUrl, Set<Integer> selectedIds, int direction) throws DAOException;

    /**
     *
     * @param targetType
     * @param targetUrl
     * @param title
     * @return
     * @throws DAOException
     */
    boolean exists(TargetType targetType, String targetUrl, String title) throws DAOException;

    /**
     *
     * @param query
     * @return
     */
    List<Map<String, ObjectDTO>> test(String query) throws DAOException;

    /**
     *
     * @param constructQuery
     * @param targetType
     * @param targetUrl
     * @param harvestedSource
     * @return
     * @throws DAOException
     */
    List<Map<String, ObjectDTO>> test(String constructQuery, TargetType targetType, String targetUrl, String harvestedSource)
            throws DAOException;

    /**
     * Checks if there are any post-harvest scripts, that are modified later than last harvest of given harvest source
     * was done. It checks all the all-source scripts and source-specific scripts that are bound to the given harvest
     * source. The type-specific scripts are ignored as we don't only know rdf:type from the previous harvest.
     *
     * @param lastHarvestDate date of the last harvest of the harvest source
     * @param harvestSource harvest source
     * @return true, if scripts have been modified since the last harvest
     * @throws DAOException
     */
    boolean isScriptsModified(Date lastHarvestDate, String harvestSource) throws DAOException;

    /**
     * Returns scripts for given IDs.
     * @param ids IDs array
     * @return Array of PostHarvestScriptDTO objets
     * @throws DAOException if query fails
     */
    List<PostHarvestScriptDTO> getScriptsByIds(List<Integer> ids) throws DAOException;

    /**
     * Adds scripts to the end of the given source or type.
     * @param targetType type shows if scripts are added to source or RDF type
     * @param targetUrl source or RDF type uri
     * @param scripts set of scripts
     * @throws DAOException if insert fails
     */
    void addScripts(TargetType targetType, String targetUrl, List<PostHarvestScriptDTO> scripts)
            throws DAOException;

    /**
     * Returns a list of scripts matching the given search text either in title or script content.
     * @param searchText Search text.
     * @return Matching scripts.
     * @throws DAOException in case any database error happens.
     */
    List<PostHarvestScriptDTO> search(String searchText) throws DAOException;
}
