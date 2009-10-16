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
package eionet.cr.util;

import java.util.ArrayList;

import org.junit.Test;

import junit.framework.TestCase;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class UtilTest extends TestCase{

	// TODO some tests to be added, a method-less TestCase causes an error in JUnit
	
	@Test
	public void test_toCSV() throws Exception {
		
		try{
			assertEquals("", Util.toCSV(null));
		}
		catch (NullPointerException e){
			fail("Did not expect " + e.getClass().getSimpleName());
		}
		assertEquals("", Util.toCSV(new ArrayList<String>()));
	}

	@Test
	public void test_normalizeHTTPAcceptedLanguage() throws Exception {
		
		try{			
			assertEquals(null, Util.normalizeHTTPAcceptedLanguage(null));
		}
		catch (NullPointerException e){
			fail("Did not expect " + e.getClass().getSimpleName());
		}
		assertEquals("", Util.normalizeHTTPAcceptedLanguage(""));
		assertEquals("", Util.normalizeHTTPAcceptedLanguage(" "));
	}

}
