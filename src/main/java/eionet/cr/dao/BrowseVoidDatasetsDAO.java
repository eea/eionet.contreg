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

import eionet.cr.dao.util.VoidDatasetsResultRow;

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
     * @param creators
     * @param subjects
     * @return
     * @throws DAOException
     */
    List<VoidDatasetsResultRow> findDatasets(List<String> creators, List<String> subjects) throws DAOException;

    /**
     * Finds all distinct creators (http://purl.org/dc/terms/creator) of VoID datasets whose subjects
     * (http://purl.org/dc/terms/subject) are in the range of given subjects list. If the latter is null or empty, all distinct
     * creators of all VoID datasets are returned.
     *
     * @param subjects
     * @return
     * @throws DAOException
     */
    List<String> findCreators(List<String> subjects) throws DAOException;

    /**
     * Finds all distinct subjects (http://purl.org/dc/terms/subject) of VoID datasets whose creators
     * (http://purl.org/dc/terms/creator) are in the range of given creators list. If the latter is null or empty, all distinct
     * subjects of all VoID datasets are returned.
     *
     * @param creators
     * @return
     * @throws DAOException
     */
    List<String> findSubjects(List<String> creators) throws DAOException;
}
