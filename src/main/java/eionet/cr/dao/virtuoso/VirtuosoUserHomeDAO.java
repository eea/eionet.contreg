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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import eionet.cr.common.Namespace;
import eionet.cr.common.Predicates;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.UserHomeDAO;
import eionet.cr.dao.readers.SubjectDataReader;
import eionet.cr.dao.virtuoso.helpers.VirtuosoUserFolderSearchHelper;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.dto.UserFolderDTO;
import eionet.cr.util.Bindings;
import eionet.cr.util.Pair;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.Util;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.util.sesame.SPARQLQueryUtil;
import eionet.cr.util.sql.SingleObjectReader;
import eionet.cr.web.security.CRUser;

/**
 * User home folder methods in Virtuoso.
 *
 * @author Enriko Käsper
 */
public class VirtuosoUserHomeDAO extends VirtuosoBaseDAO implements UserHomeDAO {

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.UserHomeDAO#getFolderContents(java.lang.String)
     */
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
        List<String> subjectUris = executeSPARQL(query, helper.getQueryBindings(), new SingleObjectReader<String>());

        logger.debug("Search folder contents, find subjects query time " + Util.durationSince(startTime));

        int totalRowCount = 0;
        List<SubjectDTO> resultList = new ArrayList<SubjectDTO>();

        // if result list not null and not empty, then get the subjects data and total rowcount
        if (subjectUris != null && !subjectUris.isEmpty()) {

            // only these predicates will be queried for
            String[] neededPredicates =
            {Predicates.RDFS_LABEL, Predicates.RDF_TYPE, Predicates.CR_HAS_FILE, Predicates.CR_HAS_FOLDER};

            if (selectedPredicates != null && selectedPredicates.size() > 0) {
                neededPredicates = selectedPredicates.toArray(neededPredicates);
            }
            // get the data of all found subjects
            logger.trace("Search folder contents, getting the data of the found subjects");
            resultList = getSubjectsData(subjectUris, neededPredicates, new SubjectDataReader(subjectUris));
        }
        // if paging required, get the total number of found subjects too
        if (pagingRequest != null) {
            totalRowCount = getExactRowCount(helper);
        }

        // return new Pair<Integer,List<SubjectDTO>>(0, new LinkedList<SubjectDTO>());
        logger.debug("Search folder contents, total query time " + Util.durationSince(startTime));

        List<UserFolderDTO> returnFolders = new ArrayList<UserFolderDTO>();
        for (SubjectDTO resultItem : resultList) {
            UserFolderDTO folderItem = new UserFolderDTO();
            folderItem.setUrl(resultItem.getUri());
            folderItem.setLabel(resultItem.getLabel());
            folderItem.setParentFolderUrl(parentFolder);
            folderItem.setSubFiles(resultItem.getObjectValues(Predicates.CR_HAS_FILE));
            folderItem.setSubFolders(resultItem.getObjectValues(Predicates.CR_HAS_FOLDER));
            returnFolders.add(folderItem);
        }

        // the result Pair contains total number of subjects and the requested sub-list
        return new Pair<Integer, List<UserFolderDTO>>(totalRowCount, returnFolders);
    }

    /**
     * SPARQL for checking if user folder is registered.
     */
    private static final String USER_FOLDER_REGISTERED_SPARQL = SPARQLQueryUtil.getPrefixes(Namespace.CR).toString()
        + " SELECT ?folder WHERE {?rootHomeUri <" + Predicates.CR_HAS_FOLDER + "> ?folder . "
        + "FILTER (?folder = ?userHomeUri) . ?folder a cr:UserFolder}";

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.UserHomeDAO#isUserFolderRegisteredInCrHomeContext(eionet.cr.web.security.CRUser)
     */
    @Override
    public boolean isUserFolderRegisteredInCrHomeContext(CRUser user) throws DAOException {

        Bindings bindings = new Bindings();
        bindings.setURI("rootHomeUri", CRUser.rootHomeUri());
        bindings.setURI("userHomeUri", user.getHomeUri());

        Object resultObject = executeUniqueResultSPARQL(USER_FOLDER_REGISTERED_SPARQL, bindings, new SingleObjectReader<String>());
        return resultObject != null && resultObject.toString().equals(user.getHomeUri());
    }
}
