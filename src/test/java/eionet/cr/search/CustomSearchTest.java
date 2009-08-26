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
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.cr.search;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.junit.Test;

import eionet.cr.util.Hashes;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class CustomSearchTest extends TestCase{

	@Test
	public void testSubjectSelectSQL(){
		
		Map<String,String> criteria = new LinkedHashMap<String,String>();
		criteria.put("coverage", "2001");
		criteria.put("description", "EEA Roug");
		criteria.put("subject", "\"Marine water\"");
		criteria.put("locality", "http://www.localities.com/14");
		
		CustomSearch customSearch = new CustomSearch(criteria);
		customSearch.setPageNumber(12);
		customSearch.setSorting("sortPredicate", (String)null);

		List inParameters = new ArrayList();
		String subjectSelectSQL = customSearch.getSubjectSelectSQL(inParameters);
		
		String s = "select SPO1.SUBJECT as SUBJECT_HASH from SPO as SPO1 left join SPO as ORDERING on " +
				"(SPO1.SUBJECT=ORDERING.SUBJECT and ORDERING.PREDICATE=?), SPO as SPO2, SPO as SPO3, SPO as SPO4 " +
				"where SPO1.PREDICATE=? and SPO1.OBJECT_HASH=? and SPO1.SUBJECT=SPO2.SUBJECT and " +
				"SPO2.PREDICATE=? and SPO2.OBJECT_HASH=? and SPO2.SUBJECT=SPO3.SUBJECT and SPO3.PREDICATE=? " +
				"and SPO3.OBJECT_HASH=? and SPO3.SUBJECT=SPO4.SUBJECT and SPO4.PREDICATE=? and SPO4.OBJECT_HASH=? " +
				"order by ORDERING.OBJECT asc";
		
		assertEquals(s, subjectSelectSQL);
		
		ArrayList inParamsExpected = new ArrayList();
		inParamsExpected.add(Long.valueOf(Hashes.spoHash("sortPredicate")));
		inParamsExpected.add(Long.valueOf(Hashes.spoHash("coverage")));
		inParamsExpected.add(Long.valueOf(Hashes.spoHash("2001")));
		inParamsExpected.add(Long.valueOf(Hashes.spoHash("description")));
		inParamsExpected.add(Long.valueOf(Hashes.spoHash("EEA Roug")));
		inParamsExpected.add(Long.valueOf(Hashes.spoHash("subject")));
		// removed quotes from "Marine water", since CustomSearch removes them when normalizing predicate value
		inParamsExpected.add(Long.valueOf(Hashes.spoHash("Marine water")));
		inParamsExpected.add(Long.valueOf(Hashes.spoHash("locality")));
		inParamsExpected.add(Long.valueOf(Hashes.spoHash("http://www.localities.com/14")));

		assertEquals(inParamsExpected.toString(), inParameters.toString());
	}
}
