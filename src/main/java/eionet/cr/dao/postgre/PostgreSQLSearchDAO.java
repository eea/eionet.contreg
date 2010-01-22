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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.SearchDAO;
import eionet.cr.dto.RawTripleDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.search.SearchException;
import eionet.cr.search.util.BBOX;
import eionet.cr.search.util.SearchExpression;
import eionet.cr.util.PagingRequest;
import eionet.cr.util.Pair;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.Util;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class PostgreSQLSearchDAO extends PostgreSQLBaseDAO implements SearchDAO{

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

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.SearchDAO#freetextSearch(eionet.cr.search.util.SearchExpression, eionet.cr.util.PagingRequest, eionet.cr.util.SortingRequest)
	 */
	public Pair<Integer, List<SubjectDTO>> searchByFreeText(
			SearchExpression expression, PagingRequest pagingRequest,
			SortingRequest sortingRequest) throws Exception {

		// TODO method to be implemented
		return null;
	}

	public Pair<Integer, List<SubjectDTO>> searchReferences(Long subjectHash,
			PagingRequest pagingRequest, SortingRequest sortingRequest)
			throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}

	public Pair<Integer, List<SubjectDTO>> searchBySpatialBox(BBOX box,
			String sourceUri, boolean googleEarthMode, PagingRequest pagingRequest, SortingRequest sortingRequest)
			throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}
}
