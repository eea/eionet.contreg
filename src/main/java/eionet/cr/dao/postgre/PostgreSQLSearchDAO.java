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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.SearchDAO;
import eionet.cr.dao.postgre.helpers.FreeTextSearchHelper;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.search.util.BBOX;
import eionet.cr.search.util.SearchExpression;
import eionet.cr.search.util.SimpleSearchDataReader;
import eionet.cr.search.util.SortOrder;
import eionet.cr.search.util.SubjectDataReader;
import eionet.cr.util.Hashes;
import eionet.cr.util.Pair;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.util.sql.PairReader;
import eionet.cr.util.sql.SingleObjectReader;
import eionet.cr.web.util.columns.SubjectLastModifiedColumn;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class PostgreSQLSearchDAO extends PostgreSQLBaseDAO implements SearchDAO{

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.SearchDAO#searchByFreeText(eionet.cr.search.util.SearchExpression, eionet.cr.util.PagingRequest, eionet.cr.util.SortingRequest)
	 */
	public Pair<Integer, List<SubjectDTO>> searchByFreeText(
			SearchExpression expression,
			PagingRequest pagingRequest,
			SortingRequest sortingRequest) throws DAOException{

		long startTime = System.currentTimeMillis();

		FreeTextSearchHelper helper = FreeTextSearchHelper.create(expression,
				pagingRequest, sortingRequest);

		ArrayList<Object> inParams = new ArrayList<Object>();
		List<Pair<Long,Long>> list = executeQuery(helper.getQuery(inParams),
				inParams,
				new PairReader<Long,Long>());

		Integer totalRowCount = Integer.valueOf(0);
		Map<Long,SubjectDTO> temp = new LinkedHashMap<Long,SubjectDTO>();
		if (list!=null && !list.isEmpty()) {

			Map<Long,Long> hitSources = new HashMap<Long,Long>();
			for (Pair<Long,Long> subjectPair : list) {
				temp.put(subjectPair.getLeft(), null);
				hitSources.put(subjectPair.getLeft(),subjectPair.getRight());
			}
			SubjectDataReader reader = new SimpleSearchDataReader(temp, hitSources);
			executeQuery(getSubjectsDataQuery(temp.keySet()), null, reader);
			
			if (pagingRequest!=null){
				inParams = new ArrayList<Object>();
				totalRowCount = executeQueryUniqueResult(
					helper.getCountQuery(inParams), inParams, new SingleObjectReader<Integer>());
			}
		}
		logger.debug("Free text search, total query time " + (System.currentTimeMillis()-startTime) + " ms");

		return new Pair<Integer, List<SubjectDTO>>(
				totalRowCount, new LinkedList<SubjectDTO>(temp.values()));
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.SearchDAO#filteredSearch(java.util.Map, java.util.Set, eionet.cr.util.PagingRequest, eionet.cr.util.SortingRequest)
	 */
	public Pair<Integer, List<SubjectDTO>> searchByFilters(
			Map<String, String> filters, Set<String> literalPredicates,
			PagingRequest pagingRequest, SortingRequest sortingRequest)
			throws DAOException {
		
		// TODO method to be implemented
		return null;
	}

	public Pair<Integer, List<SubjectDTO>> searchReferences(Long subjectHash,
			PagingRequest pagingRequest, SortingRequest sortingRequest)
			throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.SearchDAO#searchBySpatialBox(eionet.cr.search.util.BBOX, java.lang.String, eionet.cr.util.pagination.PagingRequest, eionet.cr.util.SortingRequest, boolean)
	 */
	public Pair<Integer, List<SubjectDTO>> searchBySpatialBox(BBOX box, String sourceUri, 
			PagingRequest pagingRequest,
			SortingRequest sortingRequest, boolean sortByObjectHash) throws DAOException {
		
		return null;
	}
}
