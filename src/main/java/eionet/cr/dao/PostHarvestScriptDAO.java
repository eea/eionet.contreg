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

import eionet.cr.dto.PostHarvestScriptDTO;

/**
 *
 * @author Jaanus Heinlaid
 */
public interface PostHarvestScriptDAO extends DAO{

    /**
     *
     * @param targetType
     * @param uri
     * @return TODO
     * @throws DAOException
     */
    PostHarvestScriptDTO get(PostHarvestScriptDTO.Type targetType, String uri) throws DAOException;

    /**
     *
     * @param dto
     * @throws DAOException
     */
    void save(PostHarvestScriptDTO dto) throws DAOException;

    /**
     *
     * @param dto
     * @throws DAOException
     */
    void delete(PostHarvestScriptDTO dto) throws DAOException;
}
