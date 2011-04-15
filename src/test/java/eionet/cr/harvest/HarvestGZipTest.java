/**
 *
 */
package eionet.cr.harvest;

import java.net.URL;

import org.dbunit.dataset.IDataSet;
import org.junit.Test;

import eionet.cr.test.helpers.CRDatabaseTestCase;

/**
 * @author kaptejaa
 *
 */
public class HarvestGZipTest extends CRDatabaseTestCase {
    /*
     * (non-Javadoc)
     * @see eionet.cr.test.helpers.CRDatabaseTestCase#getDataSet()
     */
    protected IDataSet getDataSet() throws Exception {
        return getXmlDataSet("emptydb.xml");
    }

    @Test
    public void testGzipHarvest(){

        try{
            URL url = getClass().getClassLoader().getResource("test-rdf.xml.gz");
            Harvest harvest = new PullHarvest(url.toString(), null);
            harvest.execute();

            assertEquals((int)49, harvest.getStoredTriplesCount());
        }
        catch (Throwable e) {
            e.printStackTrace();
            fail("Was not expecting this exception: " + e.toString());
        }
    }

}
