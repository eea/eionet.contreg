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
package eionet.cr.util.sql;

import java.text.ParseException;

import junit.framework.TestCase;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class TestPostgreSQLFullTextQuery extends TestCase{

	/**
	 * 
	 */
	public void test(){
		
		String oper = " " + PostgreSQLFullTextQuery.DEFAULT_BOOLEAN_OPERATOR + " ";
		StringBuffer expected = new StringBuffer().
		append("Car").append(oper).append("blue").append(oper).append("lagoon").
		append(oper).append("this").append(oper).append("wasn't").append(oper).
		append("(bad | good)");

		String s = "Car \"blue lagoon\" if this wasn't (bad | good)";
		try {
			PostgreSQLFullTextQuery query = PostgreSQLFullTextQuery.parse(s);
			assertEquals(expected.toString(), query.getParsedQuery());
			
			assertNotNull(query.getPhrases());
			assertEquals("[blue lagoon]", query.getPhrases().toString());
		}
		catch (ParseException e) {
			fail("Was not expecting this exception: " + e.toString());
			e.printStackTrace();
		}
	}
}
