package eionet.cr.harvest;

import java.net.URL;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.sql.Connection;

import org.dbunit.DatabaseTestCase;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.Assertion;
import eionet.cr.util.sql.ConnectionUtil;

import org.junit.Test;

/**
 * 
 * @author heinljab
 *
 */
public class HarvestSimpleDbTest extends DatabaseTestCase {

        protected IDatabaseConnection getConnection() throws Exception {
		ConnectionUtil.setReturnSimpleConnection(true);
                return new DatabaseConnection(ConnectionUtil.getConnection());
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
                QueryDataSet queryDataSet = new QueryDataSet(getConnection());
                queryDataSet.addTable("SPO", "SELECT SUBJECT, PREDICATE, OBJECT, OBJECT_HASH, ANON_SUBJ, ANON_OBJ, LIT_OBJ, OBJ_LANG, OBJ_DERIV_SOURCE, OBJ_SOURCE_OBJECT FROM SPO ORDER BY SUBJECT, PREDICATE, OBJECT");
                ITable actSPOTable = queryDataSet.getTable("SPO");

                queryDataSet.addTable("RESOURCE", "SELECT URI,URI_HASH FROM RESOURCE WHERE URI NOT LIKE 'file:%' ORDER BY URI, URI_HASH");
                ITable actResTable = queryDataSet.getTable("RESOURCE");

//                FlatXmlDataSet.write(queryDataSet, new FileOutputStream("simple-db.xml"));

                // Load expected data from an XML dataset
                IDataSet expectedDataSet = new FlatXmlDataSet(getFileAsStream("simple-db.xml"));
                ITable expSpoTable = expectedDataSet.getTable("SPO");
                ITable expResTable = expectedDataSet.getTable("RESOURCE");


                // Assert actual SPO table matches expected table
                Assertion.assertEquals(actSPOTable, expSpoTable);

                // Assert actual Resource table matches expected table
                Assertion.assertEquals(actResTable, expResTable);
        }


	@Test
	public void testSimpleRdf() {
				
		try {
			URL o = getClass().getClassLoader().getResource("simple-rdf.xml");
			Harvest harvest = new PullHarvest(o.toString(), null);
			harvest.execute();
//			assertEquals((int)2, harvest.getDistinctSubjectsCount());
//			assertEquals((int)18, harvest.getStoredTriplesCount());
		        compateDatasets();	
		}
		catch (Throwable e) {
			e.printStackTrace();
			fail("Was not expecting this exception: " + e.toString());
		}

        }

}
