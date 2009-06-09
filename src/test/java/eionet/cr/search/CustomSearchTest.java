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
		assertEquals("select sql_calc_found_rows distinct SPO1.OBJECT from SPO as SPO1 left join SPO as ORDERING on "
				+ "(SPO1.SUBJECT=ORDERING.SUBJECT and ORDERING.PREDICATE=?), SPO as SPO2, SPO as SPO3, SPO as SPO4 "
				+ "where SPO1.PREDICATE=? and match (SPO1.OBJECT) against (?) and SPO1.SUBJECT=SPO2.SUBJECT and "
				+ "SPO2.PREDICATE=? and match (SPO2.OBJECT) against (?) and SPO2.SUBJECT=SPO3.SUBJECT and SPO3.PREDICATE=? "
				+ "and SPO3.OBJECT_HASH=? and SPO3.SUBJECT=SPO4.SUBJECT and SPO4.PREDICATE=? and SPO4.OBJECT_HASH=? "
				+ "order by ORDERING.OBJECT asc limit ?,15",
				subjectSelectSQL);
		
		assertEquals("[-6298139117913057870, -7425789506134211845, 2001, 919940972634798377, EEA Roug, -7619963771306832881, "
				+ "4067911937186105398, -5849801858992246572, -2808135257893655278, 165]",
				inParameters.toString());
	}
}
