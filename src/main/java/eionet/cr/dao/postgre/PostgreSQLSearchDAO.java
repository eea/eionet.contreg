/*
* The contents of this file are subject to the Mozilla Public
* 
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
* Agency. Portions created by Tieto Eesti are Copyright
* (C) European Environment Agency. All Rights Reserved.
* 
* Contributor(s):
* Jaanus Heinlaid, Tieto Eesti*/
package eionet.cr.dao.postgre;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.time.DurationFormatUtils;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.SearchDAO;
import eionet.cr.dao.postgre.helpers.FilteredSearchHelper;
import eionet.cr.dao.postgre.helpers.FreeTextSearchHelper;
import eionet.cr.dao.postgre.helpers.ReferencesSearchHelper;
import eionet.cr.dao.postgre.helpers.SpatialSearchHelper;
import eionet.cr.dao.readers.FreeTextSearchDataReader;
import eionet.cr.dao.readers.SubjectDataReader;
import eionet.cr.dao.util.BBOX;
import eionet.cr.dao.util.SearchExpression;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.Pair;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.Util;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.util.sql.PairReader;
import eionet.cr.util.sql.PostgreSQLFullTextQuery;
import eionet.cr.util.sql.SingleObjectReader;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class PostgreSQLSearchDAO extends PostgreSQLBaseDAO implements SearchDAO{

	private static final int EXACT_ROW_COUNT_LIMIT = 500;
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.SearchDAO#searchByFreeText(eionet.cr.search.util.SearchExpression, eionet.cr.util.PagingRequest, eionet.cr.util.SortingRequest)
	 */
	public Pair<Integer, List<SubjectDTO>> searchByFreeText(
			SearchExpression expression,
			PagingRequest pagingRequest,
			SortingRequest sortingRequest) throws DAOException{

		// if search expression is null or empty, return empty result
		if (expression==null || expression.isEmpty()){
			return new Pair<Integer, List<SubjectDTO>>(0, new LinkedList<SubjectDTO>());
		}

		// parse search expression for PostgreSQL
		PostgreSQLFullTextQuery pgQuery = null;
		try{
			pgQuery = PostgreSQLFullTextQuery.parse(expression);
			logger.trace("Free-text search string parsed for PostgreSQL: " + pgQuery);
		}
		catch (ParseException pe){
			throw new DAOException("Error parsing the search text", pe);
		}

		// if search expression is empty after being parsed for PostgreSQL, return empty result
		if (pgQuery.getParsedQuery().length()==0){
			return new Pair<Integer, List<SubjectDTO>>(0, new LinkedList<SubjectDTO>());
		}

		// create query helper
		FreeTextSearchHelper helper = new FreeTextSearchHelper(
				expression, pgQuery, pagingRequest, sortingRequest);

		// create the list of IN parameters of the query
		ArrayList<Object> inParams = new ArrayList<Object>();
		
		// let the helper create the query and fill IN parameters
		String query = helper.getQuery(inParams);
		
		long startTime = System.currentTimeMillis();
		logger.trace("Free-text search, executing subject finder query: " + query);

		// execute the query, with the IN parameters
		List<Pair<Long,Long>> list = executeQuery(query, inParams, new PairReader<Long,Long>());

		// if result list not empty, do the necessary processing and get total row count
		Integer totalRowCount = Integer.valueOf(0);
		Map<Long,SubjectDTO> temp = new LinkedHashMap<Long,SubjectDTO>();
		if (list!=null && !list.isEmpty()) {

			// get the hashes of harvest sources where the search hits came from (i.e. hit-sources)
			Map<Long,Long> hitSources = new HashMap<Long,Long>();
			for (Pair<Long,Long> subjectPair : list) {
				temp.put(subjectPair.getLeft(), null);
				hitSources.put(subjectPair.getLeft(),subjectPair.getRight());
			}
			
			// get the data of all found subjects, provide hit-sources to the reader
			SubjectDataReader reader = new FreeTextSearchDataReader(temp, hitSources);
			
			logger.trace("Free-text search, getting the data of the found subjects");
			
			getSubjectsData(reader);
			
			// get total number of found subjects, unless no paging required
			if (pagingRequest!=null){
				
				inParams = new ArrayList<Object>();
				query = helper.getCountQuery(inParams);
				
				logger.trace("Search by filters, executing rowcount query: " + query);
				
				totalRowCount = Integer.valueOf(executeQueryUniqueResult(query,
					inParams, new SingleObjectReader<Long>()).toString());

			}
		}
		logger.debug("Free-text search, total query time " + Util.durationSince(startTime));

		// the result Pair contains total number of subjects and the requested sub-list
		return new Pair<Integer, List<SubjectDTO>>(
				totalRowCount, new LinkedList<SubjectDTO>(temp.values()));
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.SearchDAO#filteredSearch(java.util.Map, java.util.Set, eionet.cr.util.PagingRequest, eionet.cr.util.SortingRequest)
	 */
	public Pair<Integer, List<SubjectDTO>> searchByFilters(
			Map<String, String> filters, Set<String> literalPredicates,
			PagingRequest pagingRequest, SortingRequest sortingRequest) throws DAOException {

		// create query helper
		FilteredSearchHelper helper = new FilteredSearchHelper(filters, literalPredicates,
				pagingRequest, sortingRequest);
		
		// create the list of IN parameters of the query
		ArrayList<Object> inParams = new ArrayList<Object>();
		
		// let the helper create the query and fill IN parameters
		String query = helper.getQuery(inParams);

		long startTime = System.currentTimeMillis();
		logger.trace("Search by filters, executing subject finder query: " + query);
		
		// execute the query, with the IN parameters
		List<Long> list = executeQuery(query, inParams, new SingleObjectReader<Long>());

		int totalRowCount = 0;
		List<SubjectDTO> subjects = new ArrayList<SubjectDTO>();
		
		// if result list not null and not empty, then get the subjects data and total rowcount
		if(list!= null && !list.isEmpty()){

			// create the subjects map that needs to be fed into the subjects data reader
			Map<Long,SubjectDTO> subjectsMap = new LinkedHashMap<Long, SubjectDTO>();
			for (Long hash : list){
				subjectsMap.put(hash, null);
			}

			logger.trace("Search by filters, getting the data of the found subjects");

			// get the data of all found subjects
			subjects = getSubjectsData(subjectsMap);

		}
		// if paging required, get the total number of found subjects too
		if (pagingRequest!=null){
			
			totalRowCount = new Integer(getEstimatedRowCount(helper));
		}

		//return new Pair<Integer,List<SubjectDTO>>(0, new LinkedList<SubjectDTO>());
		logger.debug("Search by filters, total query time " + Util.durationSince(startTime));

		// the result Pair contains total number of subjects and the requested sub-list
		return new Pair<Integer,List<SubjectDTO>>(totalRowCount, subjects);
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.SearchDAO#searchReferences(java.lang.Long, eionet.cr.util.pagination.PagingRequest, eionet.cr.util.SortingRequest)
	 */
	public Pair<Integer, List<SubjectDTO>> searchReferences(Long subjectHash,
			PagingRequest pagingRequest, SortingRequest sortingRequest) throws DAOException {
		
		// create query helper
		ReferencesSearchHelper helper = new ReferencesSearchHelper(
				subjectHash, pagingRequest, sortingRequest);
		
		// create the list of IN parameters of the query
		ArrayList<Object> inParams = new ArrayList<Object>();
		
		// let the helper create the query and fill IN parameters
		String query = helper.getQuery(inParams);
		
		long startTime = System.currentTimeMillis();
		logger.trace("Search references, executing subject finder query: " + query);

		// execute the query, with the IN parameters
		List<Long> list = executeQuery(query, inParams, new SingleObjectReader<Long>());
		
		int totalRowCount = 0;
		List<SubjectDTO> subjects = new ArrayList<SubjectDTO>();
		
		// if result list not null and not empty, then get the subjects data and total rowcount
		if (list!=null && !list.isEmpty()){
			
			// create the subjects map that needs to be fed into the subjects data reader
			Map<Long,SubjectDTO> subjectsMap = new LinkedHashMap<Long, SubjectDTO>();
			for (Long hash : list){
				subjectsMap.put(hash, null);
			}

			logger.trace("Search references, getting the data of the found subjects");
			
			// get the data of all found subjects
			subjects = getSubjectsData(subjectsMap);

			// if paging required, get the total number of found subjects too
			if (pagingRequest!=null){
				
				inParams = new ArrayList<Object>();
				query = helper.getCountQuery(inParams);
				
				logger.trace("Search references, executing rowcount query: " + query);
				
				totalRowCount = Integer.valueOf(executeQueryUniqueResult(query,
						inParams, new SingleObjectReader<Long>()).toString());
			}
		}
		
		logger.debug("Search references, total query time " +
				Util.durationSince(startTime));

		// the result Pair contains total number of subjects and the requested sub-list
		return new Pair<Integer,List<SubjectDTO>>(Integer.valueOf(totalRowCount), subjects);
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.SearchDAO#searchBySpatialBox(eionet.cr.search.util.BBOX, java.lang.String, eionet.cr.util.pagination.PagingRequest, eionet.cr.util.SortingRequest, boolean)
	 */
	public Pair<Integer, List<SubjectDTO>> searchBySpatialBox(BBOX box, String sourceUri, 
			PagingRequest pagingRequest,
			SortingRequest sortingRequest, boolean sortByObjectHash) throws DAOException {

		
		// create query helper
		SpatialSearchHelper helper = new SpatialSearchHelper(box, sourceUri,
				pagingRequest, sortingRequest, sortByObjectHash);
		
		// create the list of IN parameters of the query
		ArrayList<Object> inParams = new ArrayList<Object>();
		
		// let the helper create the query and fill IN parameters
		String query = helper.getQuery(inParams);
		
		long startTime = System.currentTimeMillis();
		logger.trace("Spatial search, executing subject finder query: " + query);
		
		// execute the query, with the IN parameters
		List<Long> list = executeQuery(query, inParams, new SingleObjectReader<Long>());
		
		int totalRowCount = 0;
		List<SubjectDTO> subjects = new ArrayList<SubjectDTO>();
		
		// if result list not null and not empty, then get the subjects data and total rowcount
		if (list!=null && !list.isEmpty()){
		
			// create the subjects map that needs to be fed into the subjects data reader
			Map<Long,SubjectDTO> subjectsMap = new LinkedHashMap<Long, SubjectDTO>();
			for (Long hash : list){
				subjectsMap.put(hash, null);
			}

			logger.trace("Spatial search, getting the data of the found subjects");

			// get the data of all found subjects
			subjects = getSubjectsData(subjectsMap);

			// if paging required, get the total number of found subjects too
			if (pagingRequest!=null){

				inParams = new ArrayList<Object>();
				query = helper.getCountQuery(inParams);
				
				logger.trace("Spatial search, executing rowcount query: " + query);

				totalRowCount = Integer.valueOf(executeQueryUniqueResult(
						helper.getCountQuery(inParams),
						inParams, new SingleObjectReader<Long>()).toString());
			}
		}

		logger.debug("Search references, total query time " + Util.durationSince(startTime));
		
		// the result Pair contains total number of subjects and the requested sub-list
		return new Pair<Integer,List<SubjectDTO>>(Integer.valueOf(totalRowCount), subjects);
	}
	@Override
	public int getExactRowCountLimit() {
		return EXACT_ROW_COUNT_LIMIT;
	}
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args){
		
		long duration = 1001;
		System.out.println(DurationFormatUtils.formatDuration(duration, "mm min ss sec"));
	}
	
	/**
	 * Calculates the estimated number of rows without using COUNT(*). 
	 * If estimatation is lower than EXACT_ROW_COUNT_LIMIT, then find exact number of rows.
	 * @param helper
	 * @return
	 * @throws DAOException
	 */
	private int getEstimatedRowCount(FilteredSearchHelper helper) throws DAOException{
		
		int totalRowCount = 0;		
		long startTime = System.currentTimeMillis();
		
		ArrayList<Object> inParams = new ArrayList<Object>();

		//get the minimum Hash
		String query = helper.getMinMaxHashQuery(inParams);
		Pair minMaxPair = executeQueryUniqueResult(query, inParams,
				new PairReader<Long, Long>());
		long minHash = minMaxPair==null || minMaxPair.getLeft()==null ? 0 : (Long)minMaxPair.getLeft();
		long maxHash = minMaxPair==null || minMaxPair.getRight()==null ? 0 : (Long)minMaxPair.getRight();
		logger.trace("Search by filters, executing min max hash query: " + query);
		logger.trace("Estimated rows count, total query time " + Util.durationSince(startTime));
		
		// calculate number of rows estimation
		startTime = System.currentTimeMillis();
		totalRowCount = Util.calculateHashesCount(minHash, maxHash);
		
		logger.trace("Estimated rows count, calculation time " + Util.durationSince(startTime));		
		logger.trace("Estimated rows count: " + totalRowCount);
		
		if (totalRowCount <= EXACT_ROW_COUNT_LIMIT){
			long startTime2 = System.currentTimeMillis();

			// get exact number of rows
			query = helper.getCountQuery(inParams);
		
			logger.trace("Search by filters, executing rowcount query: " + query);
		
			totalRowCount = Integer.valueOf(executeQueryUniqueResult(query, inParams,
				new SingleObjectReader<Long>()).toString());
			logger.trace("Exact rows count, total query time " + Util.durationSince(startTime2));
			logger.trace("Exact row count: " + totalRowCount);
		}

		return totalRowCount;
	}
}
