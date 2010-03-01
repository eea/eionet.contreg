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
 * Søren Roug, European Environment Agency
 */
package eionet.cr.harvest;

import java.io.FileOutputStream;
import java.net.URL;

import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.junit.Test;

import eionet.cr.test.helpers.CRDatabaseTestCase;

/**
 * 
 * @author roug
 * 
 */
public class HarvestSimpleDbTest extends CRDatabaseTestCase {

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.test.helpers.CRDatabaseTestCase#getDataSet()
	 */
	protected IDataSet getDataSet() throws Exception {
		return getXmlDataSet("emptydb.xml");
	}

	@Test
	public void testSimpleRdf() {

		try {
			URL url = new URL("http://svn.eionet.europa.eu/repositories" +
					"/Reportnet/cr2/trunk/src/test/resources/simple-rdf.xml");
			Harvest harvest = new PullHarvest(url.toString(), null);
			harvest.execute();
			
			compareDatasets("simple-db.xml", false);
		}
		catch (Throwable e) {
			e.printStackTrace();
			fail("Was not expecting this exception: " + e.toString());
		}

	}

	@Test
	public void testEncodingRdf() {

		try {
			URL url = new URL("http://svn.eionet.europa.eu/repositories" +
					"/Reportnet/cr2/trunk/src/test/resources/encoding-scheme-rdf.xml");
			Harvest harvest = new PullHarvest(url.toString(), null);
			harvest.execute();
			
			// Fetch database data after executing your code.
			QueryDataSet queryDataSet = new QueryDataSet(getConnection());
			queryDataSet.addTable("SPO",
					"SELECT SUBJECT, PREDICATE, OBJECT, OBJECT_HASH, ANON_SUBJ, ANON_OBJ,"
					+ " LIT_OBJ, OBJ_LANG, OBJ_DERIV_SOURCE FROM SPO"
					+ " WHERE SUBJECT=-1142222056026225699 AND PREDICATE=6813166255579199724 AND OBJECT='2009-04-09'"
					+ " ORDER BY SUBJECT, PREDICATE, OBJECT, OBJ_DERIV_SOURCE");
			ITable actSPOTable = queryDataSet.getTable("SPO");

			// Load expected data from XML dataset.
			IDataSet expectedDataSet = getXmlDataSet("encoding-scheme-db.xml");
			ITable expSpoTable = expectedDataSet.getTable("SPO");

			// Assert that the actual SPO table matches expected table.
			assertEquals(expSpoTable, actSPOTable);
		}
		catch (Throwable e) {
			e.printStackTrace();
			fail("Was not expecting this exception: " + e.toString());
		}

	}

	@Test
	public void testInlineRdf() {

		try {
			URL url = new URL("http://svn.eionet.europa.eu/repositories" +
					"/Reportnet/cr2/trunk/src/test/resources/inline-rdf.xml");
			Harvest harvest = new PullHarvest(url.toString(), null);
			harvest.execute();
			
			compareDatasets("inline-db.xml", false);
		} catch (Throwable e) {
			e.printStackTrace();
			fail("Was not expecting this exception: " + e.toString());
		}

	}

	/**
	 * 
	 * @param testData
	 * @param dumpIt
	 * @throws Exception
	 */
	private void compareDatasets(String testData, boolean dumpIt) throws Exception {

		// Fetch database data after executing your code.
		QueryDataSet queryDataSet = new QueryDataSet(getConnection());
		queryDataSet.addTable("SPO",
			"SELECT DISTINCT SUBJECT, PREDICATE, OBJECT, OBJECT_HASH, ANON_SUBJ, ANON_OBJ, "
			+ "LIT_OBJ, OBJ_LANG, OBJ_DERIV_SOURCE, OBJ_SOURCE_OBJECT FROM SPO "
                        + "WHERE PREDICATE NOT IN ( 8639511163630871821, 3296918264710147612, -2213704056277764256, 333311624525447614 )"
			+ " ORDER BY SUBJECT, PREDICATE, OBJECT, OBJ_DERIV_SOURCE");
		ITable actSPOTable = queryDataSet.getTable("SPO");

		queryDataSet.addTable("RESOURCE", "SELECT URI,URI_HASH FROM RESOURCE "
				+ "WHERE URI NOT LIKE 'file:%' ORDER BY URI, URI_HASH");
		ITable actResTable = queryDataSet.getTable("RESOURCE");

		if (dumpIt){
			FlatXmlDataSet.write(queryDataSet, new FileOutputStream(testData));
		}
		else{
			// Load the expected data from an XML dataset.
			IDataSet expectedDataSet = getXmlDataSet(testData);
			ITable expSpoTable = expectedDataSet.getTable("SPO");
			ITable expResTable = expectedDataSet.getTable("RESOURCE");

			// Assert that the actual SPO table matches expected table.
			assertEquals(expSpoTable, actSPOTable);

			// Assert that the actual RESOURCE table matches expected table
			assertEquals(expResTable, actResTable);
		}
	}
}
