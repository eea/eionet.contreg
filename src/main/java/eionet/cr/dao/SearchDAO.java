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
import java.util.Vector;

import eionet.cr.dao.helpers.FreeTextSearchHelper;
import eionet.cr.dao.util.BBOX;
import eionet.cr.dao.util.SearchExpression;
import eionet.cr.dto.DeliveryDTO;
import eionet.cr.dto.SearchResultDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.Pair;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.web.util.CustomPaginatedList;

/**
 * Interface to define search related dao methods.
 *
 * @author Aleksandr Ivanov <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public interface SearchDAO extends DAO {

    /**
     * @param expression - search expression to find
     * @param filterType
     * @param exactMatch
     * @param pagingRequest - page request
     * @param sortingRequest - sorting request to set
     * @return SearchResultDTO<SubjectDTO>
     * @throws DAOException
     */
    SearchResultDTO<SubjectDTO> searchByFreeText(SearchExpression expression, FreeTextSearchHelper.FilterType filterType,
            boolean exactMatch, PagingRequest pagingRequest, SortingRequest sortingRequest) throws DAOException;

    /**
     * @param filters - search filters.
     * @param checkFiltersRange - set of literal predicates
     * @param pagingRequest - page request
     * @param sortingRequest - sorting request
     * @param selectPredicates - predicates filter
     * @param useInference if query uses inferencing. If inferencing is not needed it is reasoneable to switch it off
     * @return SearchResultDTO<SubjectDTO>
     * @throws DAOException
     */
    SearchResultDTO<SubjectDTO> searchByFilters(Map<String, String> filters, boolean checkFiltersRange,
            PagingRequest pagingRequest, SortingRequest sortingRequest, List<String> selectPredicates, boolean useInference)
            throws DAOException;

    /**
     *
     * @param obligation
     * @param locality
     * @param year
     * @param sortCol
     * @param pagingRequest
     * @param sortingRequest
     * @return CustomPaginatedList<DeliveryDTO>
     * @throws DAOException
     */
    CustomPaginatedList<DeliveryDTO> searchDeliveries(String obligation, String locality, String year, String sortCol,
            PagingRequest pagingRequest, SortingRequest sortingRequest) throws DAOException;

    /**
     *
     * @param subjectHash
     * @param pagingRequest
     * @param sortingRequest
     * @return Pair<Integer, List<SubjectDTO>>
     * @throws DAOException
     */
    Pair<Integer, List<SubjectDTO>> searchReferences(Long subjectHash, PagingRequest pagingRequest, SortingRequest sortingRequest)
            throws DAOException;

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
    Pair<Integer, List<SubjectDTO>> searchBySpatialBox(BBOX box, String sourceUri, PagingRequest pagingRequest,
            SortingRequest sortingRequest, boolean sortByObjectHash) throws DAOException;

    /**
     *
     * @return int
     */
    int getExactRowCountLimit();

    /**
     *
     * @param filters
     * @param checkFiltersRange
     * @param pagingRequest
     * @param sortingRequest
     * @param selectPredicates
     * @return SearchResultDTO<SubjectDTO>
     * @throws DAOException
     */
    SearchResultDTO<SubjectDTO> searchByTypeAndFilters(Map<String, String> filters, boolean checkFiltersRange,
            PagingRequest pagingRequest, SortingRequest sortingRequest, List<String> selectPredicates) throws DAOException;

    /**
     *
     * @param sourceUrl
     * @param pagingRequest
     * @param sortingRequest
     * @return Pair<Integer, List<SubjectDTO>>
     * @throws DAOException
     */
    Pair<Integer, List<SubjectDTO>> searchBySource(String sourceUrl, PagingRequest pagingRequest, SortingRequest sortingRequest)
            throws DAOException;

    /**
     *
     * @param pagingRequest
     * @return Vector<Hashtable<String,Vector<String>>>
     * @throws DAOException
     */
    Vector<Hashtable<String, Vector<String>>> searchDeliveriesForROD(PagingRequest pagingRequest) throws DAOException;

    /**
     *
     * @param tags
     * @param pagingRequest
     * @param sortingRequest
     * @param selectedPredicates
     * @return search result
     * @throws DAOException
     */
    SearchResultDTO<SubjectDTO> searchByTags(List<String> tags, PagingRequest pagingRequest, SortingRequest sortingRequest,
            List<String> selectedPredicates) throws DAOException;

    /**
     *
     * @param subjectUri
     * @param pagingRequest
     * @param sortingRequest
     * @return Pair<Integer, List<SubjectDTO>>
     * @throws DAOException
     */
    Pair<Integer, List<SubjectDTO>>
            searchReferences(String subjectUri, PagingRequest pagingRequest, SortingRequest sortingRequest) throws DAOException;

    /**
     * Returns distinct values of all types used in triples.
     *
     * @return ordered list of object values
     * @throws DAOException if query fails.
     */
    List<SubjectDTO> getTypes() throws DAOException;
}
