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

package eionet.cr.dao.virtuoso;

import java.util.List;
import java.util.Map;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.UserHomeDAO;
import eionet.cr.dao.readers.UserHomesReader;
import eionet.cr.dao.virtuoso.helpers.VirtuosoUserFolderSearchHelper;
import eionet.cr.dto.UserFolderDTO;
import eionet.cr.util.Pair;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.Util;
import eionet.cr.util.pagination.PagingRequest;

/**
 * User home folder methods in Virtuoso.
 *
 * @author Enriko Käsper
 */
public class VirtuosoUserHomeDAO extends VirtuosoBaseDAO implements UserHomeDAO {


    @Override
    public Pair<Integer, List<UserFolderDTO>> getFolderContents(String parentFolder, Map<String, String> filters,
            PagingRequest pagingRequest, SortingRequest sortingRequest, List<String> selectedPredicates) throws DAOException {
        // create query helper
        VirtuosoUserFolderSearchHelper helper = new VirtuosoUserFolderSearchHelper(parentFolder, pagingRequest, sortingRequest);

        // let the helper create the query and fill IN parameters
        String query = helper.getQuery(null);

        long startTime = System.currentTimeMillis();
        logger.trace("Search folder contents, executing subject finder query: " + query);

        // execute the query, with the IN parameters
        //List<String> subjectUris = executeSPARQL(query, helper.getQueryBindings(), new SingleObjectReader<String>());
        UserHomesReader reader = new UserHomesReader();

        List<UserFolderDTO> userFolders = executeSPARQL(query, helper.getQueryBindings(), reader);
        logger.debug("Search folder contents, find subjects query time " + Util.durationSince(startTime));

        int totalRowCount = 0;
        //List<SubjectDTO> resultList = new ArrayList<SubjectDTO>();

        // if paging required, get the total number of found subjects too
        if (pagingRequest != null) {
            totalRowCount = getExactRowCount(helper);
        }

        // return new Pair<Integer,List<SubjectDTO>>(0, new LinkedList<SubjectDTO>());
        logger.debug("Search folder contents, total query time " + Util.durationSince(startTime));

        // the result Pair contains total number of subjects and the requested sub-list
        return new Pair<Integer, List<UserFolderDTO>>(totalRowCount, userFolders);
    }

}
