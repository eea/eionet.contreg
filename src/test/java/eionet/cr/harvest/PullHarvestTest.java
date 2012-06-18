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

import org.dbunit.dataset.IDataSet;
import org.junit.Test;

import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.test.helpers.CRDatabaseTestCase;

/**
 * 
 * @author roug
 * 
 */
public class PullHarvestTest extends CRDatabaseTestCase {

    /** */
    // private static final String[] ignoreCols = {"SOURCE", "GEN_TIME"};

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.test.helpers.CRDatabaseTestCase#getDataSet()
     */
    protected IDataSet getDataSet() throws Exception {
        return getXmlDataSet("emptydb.xml");
    }

    @Test
    public void testSimpleRdf() {

        try {
            String url = "https://svn.eionet.europa.eu/repositories/Reportnet/cr3/trunk/src/test/resources/simple-rdf.xml";
            HarvestSourceDTO source = new HarvestSourceDTO();
            source.setUrl(url);
            source.setIntervalMinutes(5);
            DAOFactory.get().getDao(HarvestSourceDAO.class).addSource(source);

            PullHarvest harvest = new PullHarvest(url);
            harvest.execute();
            assertTrue(harvest.isSourceAvailable());
            assertEquals(12, harvest.getStoredTriplesCount());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Was not expecting this exception: " + e.toString());
        }
    }

    @Test
    public void testHarvestNonExistingURL() {

        try {
            String url = "http://www.jaanusheinlaid.tw";
            HarvestSourceDTO source = new HarvestSourceDTO();
            source.setUrl(url);
            source.setIntervalMinutes(5);
            DAOFactory.get().getDao(HarvestSourceDAO.class).addSource(source);

            PullHarvest harvest = new PullHarvest(url);
            harvest.execute();
            assertFalse(harvest.isSourceAvailable());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Was not expecting this exception: " + e.toString());
        }
    }

    @Test
    public void testEncodingRdf() {

        try {
            String url =
                    "https://svn.eionet.europa.eu/repositories/Reportnet/cr3/trunk/src/test/resources/encoding-scheme-rdf.xml";
            HarvestSourceDTO source = new HarvestSourceDTO();
            source.setUrl(url);
            source.setIntervalMinutes(5);
            DAOFactory.get().getDao(HarvestSourceDAO.class).addSource(source);

            Harvest harvest = new PullHarvest(url);
            harvest.execute();

            assertEquals(3, harvest.getStoredTriplesCount());
        } catch (Throwable e) {
            e.printStackTrace();
            fail("Was not expecting this exception: " + e.toString());
        }

    }

    @Test
    public void testInlineRdf() {

        try {
            String url = "https://svn.eionet.europa.eu/repositories/Reportnet/cr3/trunk/src/test/resources/inline-rdf.xml";
            HarvestSourceDTO source = new HarvestSourceDTO();
            source.setUrl(url);
            source.setIntervalMinutes(5);
            DAOFactory.get().getDao(HarvestSourceDAO.class).addSource(source);

            Harvest harvest = new PullHarvest(url.toString());
            harvest.execute();

            assertEquals(6, harvest.getStoredTriplesCount());
        } catch (Throwable e) {
            e.printStackTrace();
            fail("Was not expecting this exception: " + e.toString());
        }
    }
}
