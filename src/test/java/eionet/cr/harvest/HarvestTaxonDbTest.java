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

/**
 * 
 * @author roug
 * 
 */
public class HarvestTaxonDbTest extends DatabaseTestCase {

	protected IDatabaseConnection getConnection() throws Exception {
		ConnectionUtil.setReturnSimpleConnection(true);
		return new DatabaseConnection(ConnectionUtil.getConnection());
	}

	private InputStream getFileAsStream(String filename) {
		return this.getClass().getClassLoader().getResourceAsStream(filename);
	}

	protected IDataSet getDataSet() throws Exception {
		return new FlatXmlDataSet(getFileAsStream("emptydb.xml"));
	}

	private void compareDatasets(String testData, boolean dumpIt) throws Exception {

		// Fetch database data after executing your code
		QueryDataSet queryDataSet = new QueryDataSet(getConnection());
		queryDataSet
				.addTable(
						"SPO",
						"SELECT SUBJECT, PREDICATE, OBJECT, OBJECT_HASH, ANON_SUBJ, ANON_OBJ, LIT_OBJ, OBJ_LANG, OBJ_DERIV_SOURCE, OBJ_DERIV_SOURCE_GEN_TIME, OBJ_SOURCE_OBJECT FROM SPO ORDER BY SUBJECT, PREDICATE, OBJECT");
		ITable actSPOTable = queryDataSet.getTable("SPO");

		queryDataSet
				.addTable(
						"RESOURCE",
						"SELECT URI,URI_HASH FROM RESOURCE WHERE URI NOT LIKE 'file:%' ORDER BY URI, URI_HASH");
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
			Assertion.assertEquals(actSPOTable, expSpoTable);

			// Assert actual Resource table matches expected table
			Assertion.assertEquals(actResTable, expResTable);
		}
	}

	@Test
	public void testTaxonUnderRdf() {

		try {
			URL o = getClass().getClassLoader().getResource(
					"taxon-under-rdf.xml");
//			URL o = new URL("http://svn.eionet.europa.eu/repositories/Reportnet/cr2/trunk/src/test/resources/taxon-under-rdf.xml");
			Harvest harvest = new PullHarvest(o.toString(), null);
			harvest.execute();
			assertEquals((int) 2, harvest.getDistinctSubjectsCount());
			assertEquals((int) 18, harvest.getStoredTriplesCount());
			compareDatasets("taxon-db.xml", false);
		} catch (Throwable e) {
			e.printStackTrace();
			fail("Was not expecting this exception: " + e.toString());
		}

	}

	@Test
	public void testTaxonOverRdf() {

		try {
			URL o = getClass().getClassLoader().getResource(
					"taxon-over-rdf.xml");
//			URL o = new URL("http://svn.eionet.europa.eu/repositories/Reportnet/cr2/trunk/src/test/resources/taxon-over-rdf.xml");
			Harvest harvest = new PullHarvest(o.toString(), null);
			harvest.execute();
			assertEquals((int) 2, harvest.getDistinctSubjectsCount());
			assertEquals((int) 18, harvest.getStoredTriplesCount());
			compareDatasets("taxon-db.xml", false);
		} catch (Throwable e) {
			e.printStackTrace();
			fail("Was not expecting this exception: " + e.toString());
		}

	}

}
