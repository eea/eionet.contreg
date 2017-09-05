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
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Enriko Käsper
 */

package eionet.cr.harvest.scheduled;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.UrgentHarvestQueueDAO;
import eionet.cr.dto.UrgentHarvestQueueItemDTO;
import eionet.cr.test.helpers.CRDatabaseTestCase;
import eionet.cr.web.security.CRUser;
import org.junit.Ignore;

/**
 * Test urgent harvests.
 *
 * @author Enriko Käsper
 * @author Jaanus Heinlaid
 */
public class UrgentHarvestIT extends CRDatabaseTestCase {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.test.helpers.CRDatabaseTestCase#getXMLDataSetFiles()
     */
    @Override
    protected List<String> getXMLDataSetFiles() {
        return Arrays.asList("emptydb.xml");
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.test.helpers.CRDatabaseTestCase#forceClearTriplesOnSetup()
     */
    @Override
    protected boolean forceClearTriplesOnSetup() {
        return true;
    }

    /**
     * Test that no double ping harvest can be added to the urgent harvest queue.
     */
    @Test
    public void testNoDoublePing() throws Exception {

        String url = "http://url.under.test/";
        String pingUserName = CRUser.PING_HARVEST.getUserName();
        UrgentHarvestQueue.addPullHarvest(url, pingUserName);
        UrgentHarvestQueue.addPullHarvest(url, pingUserName);
        UrgentHarvestQueue.addPullHarvest(url, "heinlja");

        List<UrgentHarvestQueueItemDTO> queue = DAOFactory.get().getDao(UrgentHarvestQueueDAO.class).getUrgentHarvestQueue();
        assertNotNull("Urgent harevst queue should not be null", queue);
        assertEquals("URL with ping user should not twice in queue", 2, queue.size());
    }

    /**
     * Test UNICODE urls in urgent harvest queue.
     *
     * @throws Exception
     */
    @Test
    public void testUrgentHarvestUnicodeUrls() throws Exception {

        String url = "http://www.google.com/öö";

        UrgentHarvestQueueDAO dao = DAOFactory.get().getDao(UrgentHarvestQueueDAO.class);
        dao.removeUrl(url);
        assertFalse("Didn't expect this URL in harvest queue: " + url, UrgentHarvestQueue.isInQueue(url));

        UrgentHarvestQueue.addPullHarvest(url, "enriko");
        assertTrue("Expected this URL in harvest queue: " + url, UrgentHarvestQueue.isInQueue(url));

        UrgentHarvestQueueItemDTO dto = UrgentHarvestQueue.poll();
        assertNotNull("Expected non-null poll result", dto);
        assertTrue("Expected poll-result to have this URL: " + url, url.equals(dto.getUrl()));

        Thread.sleep(1000);
        assertFalse("Didn't expect this URL in harvest queue any more: " + url, UrgentHarvestQueue.isInQueue(url));
    }
}
