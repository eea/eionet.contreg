package eionet.cr.dao.virtuoso;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.SearchDAO;
import eionet.cr.dao.postgre.helpers.FreeTextSearchHelper.FilterType;
import eionet.cr.dao.util.BBOX;
import eionet.cr.dao.util.SearchExpression;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.Pair;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.pagination.PagingRequest;

/**
 * 
 * @author jaanus
 *
 */
public class VirtuosoSearchDAO extends VirtuosoBaseDAO implements SearchDAO{

	/* (non-Javadoc)
	 * @see eionet.cr.dao.SearchDAO#searchByFreeText(eionet.cr.dao.util.SearchExpression, eionet.cr.dao.postgre.helpers.FreeTextSearchHelper.FilterType, eionet.cr.util.pagination.PagingRequest, eionet.cr.util.SortingRequest)
	 */
	@Override
	public Pair<Integer, List<SubjectDTO>> searchByFreeText(
			SearchExpression expression, FilterType filterType,
			PagingRequest pagingRequest,SortingRequest sortingRequest) throws DAOException {
		
		
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.SearchDAO#searchByFilters(java.util.Map, java.util.Set, eionet.cr.util.pagination.PagingRequest, eionet.cr.util.SortingRequest, java.util.List)
	 */
	@Override
	public Pair<Integer, List<SubjectDTO>> searchByFilters(
			Map<String, String> filters, Set<String> literalPredicates,
			PagingRequest pagingRequest, SortingRequest sortingRequest,
			List<String> selectedPredicates) throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.SearchDAO#searchReferences(java.lang.Long, eionet.cr.util.pagination.PagingRequest, eionet.cr.util.SortingRequest)
	 */
	@Override
	public Pair<Integer, List<SubjectDTO>> searchReferences(Long subjectHash,
			PagingRequest pagingRequest, SortingRequest sortingRequest)
			throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.SearchDAO#searchBySpatialBox(eionet.cr.dao.util.BBOX, java.lang.String, eionet.cr.util.pagination.PagingRequest, eionet.cr.util.SortingRequest, boolean)
	 */
	@Override
	public Pair<Integer, List<SubjectDTO>> searchBySpatialBox(BBOX box,
			String sourceUri, PagingRequest pagingRequest,
			SortingRequest sortingRequest, boolean sortByObjectHash)
			throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.SearchDAO#getExactRowCountLimit()
	 */
	@Override
	public int getExactRowCountLimit() {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.SearchDAO#searchByTypeAndFilters(java.util.Map, java.util.Set, eionet.cr.util.pagination.PagingRequest, eionet.cr.util.SortingRequest, java.util.List)
	 */
	@Override
	public Pair<Integer, List<SubjectDTO>> searchByTypeAndFilters(
			Map<String, String> filters, Set<String> literalPredicates,
			PagingRequest pagingRequest, SortingRequest sortingRequest,
			List<String> selectedPredicates) throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.SearchDAO#searchBySource(java.lang.String, eionet.cr.util.pagination.PagingRequest, eionet.cr.util.SortingRequest)
	 */
	@Override
	public Pair<Integer, List<SubjectDTO>> searchBySource(String sourceUrl,
			PagingRequest pagingRequest, SortingRequest sortingRequest)
			throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.SearchDAO#searchDeliveriesForROD(eionet.cr.util.pagination.PagingRequest)
	 */
	@Override
	public Vector<Hashtable<String, Vector<String>>> searchDeliveriesForROD(
			PagingRequest pagingRequest) throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.SearchDAO#searchByTags(java.util.List, eionet.cr.util.pagination.PagingRequest, eionet.cr.util.SortingRequest, java.util.List)
	 */
	@Override
	public Pair<Integer, List<SubjectDTO>> searchByTags(List<String> tags,
			PagingRequest pagingRequest, SortingRequest sortingRequest,
			List<String> selectedPredicates) throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

}
