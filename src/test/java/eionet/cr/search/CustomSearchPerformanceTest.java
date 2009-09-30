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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import eionet.cr.dao.ISearchDao;
import eionet.cr.dao.mysql.MySQLDAOFactory;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.PageRequest;
import eionet.cr.util.Pair;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.sql.ConnectionUtil;

/**
 * @author Aleksandr Ivanov
 * <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public class CustomSearchPerformanceTest extends TestCase {
	
	

	public void testPerformance() throws Exception {
		Set<String> literalPredicates = new HashSet<String>();
		literalPredicates.add("http://purl.org/dc/elements/1.1/description");
		
		ConnectionUtil.setReturnSimpleConnection(true);
		new SimpleSearchPerformanceTest().testPerformance();
		long startTime = System.currentTimeMillis();
		
		CustomSearch search = new CustomSearch(getCriteria());
		search.setPageNumber(getPageRequest().getPageNumber());
		search.setPageLength(15);
		search.setSorting((String)null, (String)null);
		search.setLiteralsEnabledPredicates((HashSet) literalPredicates);
		search.execute();
		long customSearchTotalTime = System.currentTimeMillis() - startTime;
		Collection<SubjectDTO> list = search.getResultList();
		startTime = System.currentTimeMillis();
		Pair<Integer,List<SubjectDTO>> result = MySQLDAOFactory
				.get().getDao(ISearchDao.class)
						.performCustomSearch(
								getCriteria(),
								literalPredicates,
								getPageRequest(),
								getSorting());
		long newSearchTotalTime = System.currentTimeMillis() - startTime;
		assertEquals((Integer)search.getTotalMatchCount(), result.getId());
		
		System.out.println(customSearchTotalTime);
		System.out.println(newSearchTotalTime);
		assertTrue(newSearchTotalTime < customSearchTotalTime);
		
	}

	/**
	 * @return
	 */
	private PageRequest getPageRequest() {
		return new PageRequest(1);
	}

	/**
	 * @return
	 */
	private SortingRequest getSorting() {
		return null;
	}

	/**
	 * @return
	 */
	private Map<String, String> getCriteria() {
		Map<String, String> resultMap = new HashMap<String, String>();
//		resultMap.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#type","Spatial Thing");
//		resultMap.put("http://purl.org/dc/elements/1.1/description","a");
		resultMap.put("http://purl.org/dc/elements/1.1/publisher","Biodiversity Information Standards (TDWG)");
		return resultMap;
	}
}
