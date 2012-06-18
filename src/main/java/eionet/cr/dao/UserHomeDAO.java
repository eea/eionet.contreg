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
 * The Original Code is cr3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s): Enriko Käsper
 */

package eionet.cr.dao;

import java.util.List;
import java.util.Map;

import eionet.cr.dto.UserFolderDTO;
import eionet.cr.util.Pair;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.pagination.PagingRequest;

/**
 * Interface to define methods related to users home folder.
 * 
 * @author Enriko Käsper
 */
public interface UserHomeDAO extends DAO {

    /**
     * Get the contents of user folder or users root folder. The result contains the list of UserFolderDTO objects.
     * 
     * @param parentFolder
     * @param filters
     * @param pagingRequest
     * @param sortingRequest
     * @param selectedPredicates
     * @return
     * @throws DAOException
     */
    Pair<Integer, List<UserFolderDTO>> getFolderContents(String parentFolder, Map<String, String> filters,
            PagingRequest pagingRequest, SortingRequest sortingRequest, List<String> selectedPredicates) throws DAOException;
}
