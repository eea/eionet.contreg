/*
 * The contents of this file are subject to the Mozilla Public
 *
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
 * Agency. Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency. All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti*/
package eionet.cr.dao;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import eionet.cr.ApplicationTestContext;
import org.junit.Test;

import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.test.helpers.CRDatabaseTestCase;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationTestContext.class })
public class ScheduledHarvestSourcesIT extends CRDatabaseTestCase {

    @Autowired
    private HarvestSourceDAO harvestSourceDAO;

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
     *
     * @throws Exception
     */
    @Test
    public void testScheduledSources() throws Exception {

        HarvestSourceDTO source = new HarvestSourceDTO();
        source.setUrl("http://rod.eionet.europa.eu/testObligations");
        source.setIntervalMinutes(5);
        source.setPrioritySource(false);
        source.setEmails("bob@europe.eu");
        harvestSourceDAO.addSource(source);

        // finish harvest
        source.setStatements(100);
        source.setLastHarvestFailed(false);
        source.setLastHarvest(new Date());
        source.setOwner("system");
        harvestSourceDAO.updateSourceHarvestFinished(source);

        source.setUrl("http://rod.eionet.europa.eu/testObligations2");
        source.setIntervalMinutes(10);
        harvestSourceDAO.addSource(source);

        List<HarvestSourceDTO> dtos = harvestSourceDAO.getNextScheduledSources(10);
        assertNotNull(dtos);
        // The first harvest_source has Harvest date field filled with now() and it will be available again in 5 minutes
        // The second harvest_soruce is not yet harvested and this is returned by the method.
        assertEquals(1, dtos.size());
    }
}
