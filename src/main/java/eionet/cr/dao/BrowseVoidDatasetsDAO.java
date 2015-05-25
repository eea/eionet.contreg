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

import eionet.cr.dao.util.VoidDatasetsResultRow;
import eionet.cr.util.Pair;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.pagination.PagingRequest;

import java.util.List;

/**
 * An interface for the DAO that provides functions for the faceted browsing of VoID datasets.
 *
 * @author jaanus
 */
public interface BrowseVoidDatasetsDAO extends DAO {

    /**
     * Returns a list of VoID datasets matching the given creators (http://purl.org/dc/terms/creator) and subjects
     * (http://purl.org/dc/terms/subject).
     *
     * @param creators The given creators.
     * @param subjects The given subjects.
     * @param titleSubstr Substring of dcterms:title to search by.
     * @param pagingRequest
     * @param sortingRequest
     * @return Pair of total rows number and matching datasets
     * @throws DAOException If database error happens.
     */
    Pair<Integer, List<VoidDatasetsResultRow>> findDatasets(List<String> creators, List<String> subjects, String titleSubstr, PagingRequest pagingRequest, SortingRequest sortingRequest) throws DAOException;

    /**
     * Finds all distinct creators (http://purl.org/dc/terms/creator) of VoID datasets whose subjects
     * (http://purl.org/dc/terms/subject) are in the range of given subjects list. If the latter is null or empty, all distinct
     * creators of all VoID datasets are returned.
     *
     * @param subjects The given subjects
     * @return Matching creators.
     * @throws DAOException If database error happens
     */
    List<String> findCreators(List<String> subjects) throws DAOException;

    /**
     * Finds all distinct subjects (http://purl.org/dc/terms/subject) of VoID datasets whose creators
     * (http://purl.org/dc/terms/creator) are in the range of given creators list. If the latter is null or empty, all distinct
     * subjects of all VoID datasets are returned.
     *
     * @param creators The given creators
     * @return Matching subjects.
     * @throws DAOException If database error happens
     */
    List<String> findSubjects(List<String> creators) throws DAOException;
}
