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
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Aleksandr Ivanov, Tieto Eesti
 */
package eionet.cr.dao;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import eionet.cr.dao.postgre.helpers.PostgreFreeTextSearchHelper;
import eionet.cr.dao.util.BBOX;
import eionet.cr.dao.util.SearchExpression;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.Pair;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.pagination.PagingRequest;

/**
 * Interface to define search related dao methods.
 *
 * @author Aleksandr Ivanov
 * <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public interface SearchDAO extends DAO {

    /**
     * @param expression - search expression to find
     * @param filterType
     * @param exactMatch
     * @param pagingRequest - page request
     * @param sortingRequest - sorting request to set
     * @return Pair<Integer, List<SubjectDTO>>
     * @throws DAOException
     */
    Pair<Integer, List<SubjectDTO>> searchByFreeText(
                SearchExpression expression,
                PostgreFreeTextSearchHelper.FilterType filterType,
                boolean exactMatch,
                PagingRequest pagingRequest,
                SortingRequest sortingRequest) throws DAOException;

    /**
     * @param filters - search filters.
     * @param literalPredicates - set of literal predicates
     * @param pagingRequest - page request
     * @param sortingRequest - sorting request
     * @param selectedPredicates - predicates filter
     * @return Pair<Integer, List<SubjectDTO>>
     * @throws DAOException
     */
    Pair<Integer, List<SubjectDTO>> searchByFilters(
            Map<String,String> filters,
            Set<String> literalPredicates,
            PagingRequest pagingRequest,
            SortingRequest sortingRequest,
            List<String> selectedPredicates
            ) throws DAOException;

    /**
     *
     * @param subjectHash
     * @param pagingRequest
     * @param sortingRequest
     * @return Pair<Integer, List<SubjectDTO>>
     * @throws DAOException
     */
    Pair<Integer, List<SubjectDTO>> searchReferences(
            Long subjectHash,
            PagingRequest pagingRequest,
            SortingRequest sortingRequest) throws DAOException;

    /**
     *
     * @param box
     * @param sourceUri
     * @param pagingRequest
     * @param sortingRequest
     * @param sortByObjectHash
     * @return Pair<Integer, List<SubjectDTO>>
     * @throws DAOException
     */
    Pair<Integer, List<SubjectDTO>> searchBySpatialBox(
            BBOX box,
            String sourceUri,
            PagingRequest pagingRequest,
            SortingRequest sortingRequest, boolean sortByObjectHash) throws DAOException;

    /**
     *
     * @return int
     */
    int getExactRowCountLimit();

    /**
     *
     * @param filters
     * @param literalPredicates
     * @param pagingRequest
     * @param sortingRequest
     * @param selectedPredicates
     * @return Pair<Integer, List<SubjectDTO>>
     * @throws DAOException
     */
    public Pair<Integer, List<SubjectDTO>> searchByTypeAndFilters(
            Map<String, String> filters, Set<String> literalPredicates,
            PagingRequest pagingRequest, SortingRequest sortingRequest,
            List<String> selectedPredicates) throws DAOException;

    /**
     *
     * @param sourceUrl
     * @param pagingRequest
     * @param sortingRequest
     * @return Pair<Integer, List<SubjectDTO>>
     * @throws DAOException
     */
    public Pair<Integer, List<SubjectDTO>> searchBySource(String sourceUrl,
            PagingRequest pagingRequest, SortingRequest sortingRequest) throws DAOException;

    /**
     *
     * @param pagingRequest
     * @return Vector<Hashtable<String,Vector<String>>>
     * @throws DAOException
     */
    public Vector<Hashtable<String,Vector<String>>> searchDeliveriesForROD(
            PagingRequest pagingRequest)throws DAOException;

    /**
     *
     * @param tags
     * @param pagingRequest
     * @param sortingRequest
     * @param selectedPredicates
     * @return Pair<Integer, List<SubjectDTO>>
     * @throws DAOException
     */
    public Pair<Integer, List<SubjectDTO>> searchByTags(
                List<String> tags,
                PagingRequest pagingRequest,
                SortingRequest sortingRequest,
                List<String> selectedPredicates) throws DAOException;

    /**
     *
     * @param subjectUri
     * @param pagingRequest
     * @param sortingRequest
     * @return Pair<Integer, List<SubjectDTO>>
     * @throws DAOException
     */
    public Pair<Integer, List<SubjectDTO>> searchReferences(String subjectUri,
            PagingRequest pagingRequest, SortingRequest sortingRequest) throws DAOException;
}
