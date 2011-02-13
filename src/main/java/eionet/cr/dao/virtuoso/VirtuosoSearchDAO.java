package eionet.cr.dao.virtuoso;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import eionet.cr.common.Predicates;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.SearchDAO;
import eionet.cr.dao.postgre.helpers.PostgreFreeTextSearchHelper;
import eionet.cr.dao.helpers.SearchHelper;
import eionet.cr.dao.helpers.FreeTextSearchHelper.FilterType;
import eionet.cr.dao.readers.FreeTextSearchDataReader;
import eionet.cr.dao.readers.SubjectDataReader;
import eionet.cr.dao.util.BBOX;
import eionet.cr.dao.util.SearchExpression;
import eionet.cr.dao.virtuoso.helpers.VirtuosoFreeTextSearchHelper;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.Hashes;
import eionet.cr.util.Pair;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.Util;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.util.sql.PairReader;
import eionet.cr.util.sql.PostgreSQLFullTextQuery;
import eionet.cr.util.sql.SingleObjectReader;

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
		
		
		// if search expression is null or empty, return empty result
		if (expression==null || expression.isEmpty()){
			return new Pair<Integer, List<SubjectDTO>>(0, new LinkedList<SubjectDTO>());
		}

		// create query helper
		// TODO: use SortingRequest instead of null
		VirtuosoFreeTextSearchHelper helper = new VirtuosoFreeTextSearchHelper(
				expression, pagingRequest, null);
		
		// Set Filter
		if (filterType != PostgreFreeTextSearchHelper.FilterType.ANY_OBJECT
			&& filterType != PostgreFreeTextSearchHelper.FilterType.EXACT_MATCH){
				helper.setFilter(filterType);
		}

		// create the list of IN parameters of the query
		ArrayList<Object> inParams = new ArrayList<Object>();
		
		// let the helper create the query and fill IN parameters
		String query = helper.getQuery(inParams);
		
		long startTime = System.currentTimeMillis();
		logger.trace("Free-text search, executing subject finder query: " + query);

		// execute the query, with the IN parameters
		List<String> subjectUris = executeSPARQL(query, new SingleObjectReader<String>());

		logger.debug("Free-text search, find subjects query time " + Util.durationSince(startTime));

		// if result list not empty, do the necessary processing and get total row count
		Integer totalRowCount = Integer.valueOf(0);
		Map<Long,SubjectDTO> temp = new LinkedHashMap<Long,SubjectDTO>();
		if (subjectUris!=null && !subjectUris.isEmpty()) {

			// get the hashes of harvest sources where the search hits came from (i.e. hit-sources)
			Map<Long,Long> hitSources = new HashMap<Long,Long>();
			for (String subjectUri : subjectUris) {
				
				long subjectHash = Hashes.spoHash(subjectUri);
				
				temp.put(Long.valueOf(subjectHash), null);
				hitSources.put(Long.valueOf(subjectHash), Long.valueOf(0));
			}
			
			// get the data of all found subjects, provide hit-sources to the reader
			SubjectDataReader reader = new FreeTextSearchDataReader(temp, hitSources, subjectUris);
			
			// query only needed predicates
			ArrayList<String> predicateUris = new ArrayList<String>();
			predicateUris.add(Predicates.RDF_TYPE);
			predicateUris.add(Predicates.RDFS_LABEL);

			logger.trace("Free-text search, getting the data of the found subjects");

			// get the subjects data
			getSubjectsData(subjectUris, predicateUris, reader);
			
			// get total number of found subjects, unless no paging required
			if (pagingRequest!=null){
				
				logger.trace("Free-text search, getting exact row count");
				totalRowCount = new Integer(getExactRowCount(helper));
			}
		}
		logger.debug("Free-text search, total query time " + Util.durationSince(startTime));

		// the result Pair contains total number of subjects and the requested sub-list
		return new Pair<Integer, List<SubjectDTO>>(
				totalRowCount, new LinkedList<SubjectDTO>(temp.values()));
		
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

	/**
	 * 
	 * @param helper
	 * @return
	 * @throws DAOException
	 */
	private int getExactRowCount(SearchHelper helper) throws DAOException{
		
		String query = helper.getCountQuery(new ArrayList());
		Object resultObject = executeUniqueResultSPARQL(query, new SingleObjectReader<Long>());
		return Integer.valueOf(resultObject.toString());
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.SearchDAO#getExactRowCountLimit()
	 */
	@Override
	public int getExactRowCountLimit() {
		
		return Integer.MAX_VALUE;
	}
}
