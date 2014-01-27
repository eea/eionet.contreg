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

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mortbay.jetty.Server;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.test.helpers.CRDatabaseTestCase;
import eionet.cr.test.helpers.JettyUtil;

/**
 *
 * @author roug
 *
 */
public class PullHarvestTest extends CRDatabaseTestCase {

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.test.helpers.CRDatabaseTestCase#getXMLDataSetFiles()
     */
    @Override
    protected List<String> getXMLDataSetFiles() {
        return Arrays.asList("emptydb.xml");
    }

    /**
     * @throws Exception
     *
     */
    @Test
    public void testSimpleRdf() throws Exception {

        Server server = null;
        try {
            server = JettyUtil.startResourceServerMock(8999, "/testResources", "simple-rdf.xml");
            String url = "http://localhost:8999/testResources/simple-rdf.xml";

            HarvestSourceDTO source = new HarvestSourceDTO();
            source.setUrl(url);
            source.setIntervalMinutes(5);
            DAOFactory.get().getDao(HarvestSourceDAO.class).addSource(source);

            PullHarvest harvest = new PullHarvest(url);
            harvest.execute();
            assertTrue(harvest.isSourceAvailable());
            assertEquals(12, harvest.getStoredTriplesCount());
        } finally {
            JettyUtil.close(server);
        }
    }

    /**
     * @throws DAOException
     * @throws HarvestException
     *
     */
    @Test
    public void testHarvestNonExistingURL() throws DAOException, HarvestException {

        String url = "http://www.jaanusheinlaid.tw";
        HarvestSourceDTO source = new HarvestSourceDTO();
        source.setUrl(url);
        source.setIntervalMinutes(5);
        DAOFactory.get().getDao(HarvestSourceDAO.class).addSource(source);

        PullHarvest harvest = new PullHarvest(url);
        harvest.execute();
        assertFalse(harvest.isSourceAvailable());
    }

    /**
     * @throws Exception
     *
     */
    @Test
    public void testEncodingRdf() throws Exception {

        Server server = null;
        try {
            server = JettyUtil.startResourceServerMock(8999, "/testResources", "encoding-scheme-rdf.xml");
            String url = "http://localhost:8999/testResources/encoding-scheme-rdf.xml";

            HarvestSourceDTO source = new HarvestSourceDTO();
            source.setUrl(url);
            source.setIntervalMinutes(5);
            DAOFactory.get().getDao(HarvestSourceDAO.class).addSource(source);

            Harvest harvest = new PullHarvest(url);
            harvest.execute();

            assertEquals(3, harvest.getStoredTriplesCount());
        } finally {
            JettyUtil.close(server);
        }

    }

    /**
     * @throws Exception
     *
     */
    @Test
    public void testInlineRdf() throws Exception {

        Server server = null;
        try {
            server = JettyUtil.startResourceServerMock(8999, "/testResources", "inline-rdf.xml");
            String url = "http://localhost:8999/testResources/inline-rdf.xml";

            HarvestSourceDTO source = new HarvestSourceDTO();
            source.setUrl(url);
            source.setIntervalMinutes(5);
            DAOFactory.get().getDao(HarvestSourceDAO.class).addSource(source);

            Harvest harvest = new PullHarvest(url.toString());
            harvest.execute();

            assertEquals(6, harvest.getStoredTriplesCount());
        } finally {
            JettyUtil.close(server);
        }
    }
}
