package eionet.cr.harvest;

import java.net.URL;
import java.io.InputStream;
import java.sql.Connection;

import org.dbunit.DatabaseTestCase;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.Assertion;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import eionet.cr.util.sql.ConnectionUtil;

import org.junit.Test;

/**
 * 
 * @author heinljab
 *
 */
public class HarvestTaxonDbTest extends DatabaseTestCase {


        protected IDatabaseConnection getConnection() throws Exception {
                return new DatabaseConnection(ConnectionUtil.getSimpleConnection());
        }
        
        private InputStream getFileAsStream(String filename) {
        	return this.getClass().getClassLoader().getResourceAsStream(filename);
        }
        
        protected IDataSet getDataSet() throws Exception
        {
                return new FlatXmlDataSet(getFileAsStream("emptydb.xml"));
        }

        private void compateDatasets() throws Exception {

                // Fetch database data after executing your code
                IDataSet databaseDataSet = getConnection().createDataSet();
                ITable actualTable = databaseDataSet.getTable("SPO");

                // Load expected data from an XML dataset
                IDataSet expectedDataSet = new FlatXmlDataSet(getFileAsStream("taxon-db.xml"));
                ITable expSpoTable = expectedDataSet.getTable("SPO");
                ITable expResTable = expectedDataSet.getTable("RESOURCE");

                ITable filteredTable = DefaultColumnFilter.includedColumnsTable(actualTable, 
                        expSpoTable.getTableMetaData().getColumns());


                // Assert actual SPO table match expected table
                Assertion.assertEquals(filteredTable, expSpoTable);

                QueryDataSet queryDataSet = new QueryDataSet(getConnection());
                queryDataSet.addTable("RESOURCE", "SELECT URI,URI_HASH FROM RESOURCE WHERE URI NOT LIKE 'file:%' ORDER BY URI, URI_HASH");
                ITable actResTable = queryDataSet.getTable("RESOURCE");
                Assertion.assertEquals(actResTable, expResTable);
        }

	@Test
	public void testTaxonDownRdf() {
				
		try {
			URL o = getClass().getClassLoader().getResource("taxon-rdf.xml");
			Harvest harvest = new PullHarvest(o.toString(), null);
			harvest.execute();
			assertEquals((int)2, harvest.getDistinctSubjectsCount());
			assertEquals((int)18, harvest.getStoredTriplesCount());
		        compateDatasets();	
		}
		catch (Throwable e) {
			e.printStackTrace();
			fail("Was not expecting this exception: " + e.toString());
		}

        }

	@Test
	public void testTaxonUpRdf() {
				
		try {
			URL o = getClass().getClassLoader().getResource("taxon-up-rdf.xml");
			Harvest harvest = new PullHarvest(o.toString(), null);
			harvest.execute();
			assertEquals((int)2, harvest.getDistinctSubjectsCount());
			assertEquals((int)18, harvest.getStoredTriplesCount());
		        compateDatasets();	
		}
		catch (Throwable e) {
			e.printStackTrace();
			fail("Was not expecting this exception: " + e.toString());
		}

        }

}
