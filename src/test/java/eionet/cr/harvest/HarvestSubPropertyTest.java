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
import java.io.InputStream;
import java.net.URL;

import org.dbunit.Assertion;
import org.dbunit.DatabaseTestCase;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.junit.Test;

import eionet.cr.util.sql.ConnectionUtil;

public class HarvestSubPropertyTest extends DatabaseTestCase {
	protected IDatabaseConnection getConnection() throws Exception {
		ConnectionUtil.setReturnSimpleConnection(true);
		return new DatabaseConnection(ConnectionUtil.getConnection());
	}

	/**
	 * 
	 * @param filename
	 * @return
	 */
	private InputStream getFileAsStream(String filename) {
		return this.getClass().getClassLoader().getResourceAsStream(filename);
	}

	/*
	 * (non-Javadoc)
	 * @see org.dbunit.DatabaseTestCase#getDataSet()
	 */
	protected IDataSet getDataSet() throws Exception {
		return new FlatXmlDataSet(getFileAsStream("emptydb.xml"));
	}

	/*
	 * 
	 */
	private void compareDatasets(String testData, boolean dumpIt) throws Exception {

		// Fetch database data after executing your code
		QueryDataSet queryDataSet = new QueryDataSet(getConnection());
		queryDataSet
				.addTable(
						"SPO",
						"SELECT SUBJECT, PREDICATE, OBJECT, OBJECT_HASH, ANON_SUBJ, ANON_OBJ, LIT_OBJ, OBJ_DERIV_SOURCE, OBJ_SOURCE_OBJECT FROM SPO ORDER BY SUBJECT, PREDICATE, OBJECT");
		ITable actSPOTable = queryDataSet.getTable("SPO");

		queryDataSet
				.addTable(
						"RESOURCE",
						"SELECT URI_HASH, URI FROM RESOURCE ORDER BY URI_HASH");
		ITable actResTable = queryDataSet.getTable("RESOURCE");
		if (dumpIt) {
			FlatXmlDataSet.write(queryDataSet, new FileOutputStream(testData));
		} else {

			// Load expected data from an XML dataset
			IDataSet expectedDataSet = new FlatXmlDataSet(
					getFileAsStream(testData));
			ITable expSpoTable = expectedDataSet.getTable("SPO");
			ITable expResTable = expectedDataSet.getTable("RESOURCE");

			// Assert actual SPO table matches expected table
			Assertion.assertEquals(expSpoTable, actSPOTable);

			// Assert actual Resource table matches expected table
			Assertion.assertEquals(expResTable, actResTable);
		}
	}

	@Test
	public void testSimpleSubProperty() {

		try {
			URL o = new URL("http://svn.eionet.europa.eu/repositories/Reportnet/cr2/trunk/src/test/resources/subproperty-rdf.xml");
//			URL o = getClass().getClassLoader().getResource("subproperty-rdf.xml");
			Harvest harvest = new PullHarvest(o.toString(), null);
			harvest.execute();
			compareDatasets("subproperty-db.xml", false);
		} catch (Throwable e) {
			e.printStackTrace();
			fail("Was not expecting this exception: " + e.toString());
		}

	}

	@Test
	public void testTripleSubProperty() {

		try {
			URL o = new URL("http://svn.eionet.europa.eu/repositories/Reportnet/cr2/trunk/src/test/resources/subproperty2-rdf.xml");
//			URL o = getClass().getClassLoader().getResource("subproperty2-rdf.xml");
			Harvest harvest = new PullHarvest(o.toString(), null);
			harvest.execute();
			compareDatasets("subproperty2-db.xml", false);
		} catch (Throwable e) {
			e.printStackTrace();
			fail("Was not expecting this exception: " + e.toString());
		}

	}


}