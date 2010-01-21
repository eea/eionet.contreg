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
package eionet.cr.search;

import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import eionet.cr.dao.SearchDAO;
import eionet.cr.dao.mysql.MySQLDAOFactory;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.search.util.SearchExpression;
import eionet.cr.search.util.SortOrder;
import eionet.cr.util.PageRequest;
import eionet.cr.util.Pair;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.sql.ConnectionUtil;

/**
 * tests the performance of {@link SimpleSearch} against 
 * new {@link eionet.cr.dao.HelperDAO#performSimpleSearch(eionet.cr.search.util.SearchExpression, int, eionet.cr.util.SortingRequest)}.
 * 
 * @author Aleksandr Ivanov
 * <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public class SimpleSearchPerformanceTest extends TestCase{

	@Test
	public void testPerformance() throws Exception {
		ConnectionUtil.setReturnSimpleConnection(true);
		//just to initiate the connection launch simplesearch.
		SimpleSearch simpleSearch = new SimpleSearch(getSearchExpression());
		simpleSearch.setPageNumber(getPageNumber());
		simpleSearch.setSorting(getSortPredicate(), SortOrder.DESCENDING);
		simpleSearch.execute();
		
		//time to measure things
		long time = System.currentTimeMillis();
		simpleSearch = new SimpleSearch(getSearchExpression());
		simpleSearch.setPageNumber(getPageNumber());
		simpleSearch.setSorting(getSortPredicate(), SortOrder.DESCENDING);
		simpleSearch.execute();
		long simpleSearchCompleteTime = System.currentTimeMillis() - time;
		
		time = System.currentTimeMillis();
		Pair<Integer, List<SubjectDTO>> result = MySQLDAOFactory.get().getDao(SearchDAO.class)
				.performSimpleSearch(
						getSearchExpression(),
						new PageRequest(getPageNumber()),
						new SortingRequest(getSortPredicate(), SortOrder.DESCENDING));
		long newSearchCompleteTime = System.currentTimeMillis() - time;
		System.out.println("Simple search : " + simpleSearchCompleteTime);
		System.out.println("HelperDAO search: " + newSearchCompleteTime);
		assertTrue(simpleSearchCompleteTime > newSearchCompleteTime);
	}

	private int getPageNumber() {
		return 10;
	}
	
	/**
	 * @return
	 */
	private String getSortPredicate() {
		return null;
	}

	/**
	 * @return
	 */
	private SearchExpression getSearchExpression() {
		return new SearchExpression("air pollution");
	}
	
}
