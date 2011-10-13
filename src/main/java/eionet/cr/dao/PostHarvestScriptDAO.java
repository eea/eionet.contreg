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

import java.util.List;

import eionet.cr.dto.PostHarvestScriptDTO;
import eionet.cr.dto.PostHarvestScriptDTO.TargetType;
import eionet.cr.util.Pair;

/**
 *
 * @author Jaanus Heinlaid
 */
public interface PostHarvestScriptDAO extends DAO{

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
     * @return
     * @throws DAOException
     */
    int insert(TargetType targetType, String targetUrl, String title, String script, boolean active) throws DAOException;

    /**
     *
     * @param id
     * @param title
     * @param script
     * @param active
     * @throws DAOException
     */
    void save(int id, String title, String script, boolean active) throws DAOException;

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
    List<Pair<String,Integer>> listTargets(TargetType targetType) throws DAOException;
}
