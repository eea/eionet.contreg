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
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.cr.dao;

import java.util.Arrays;
import java.util.List;
import eionet.cr.ApplicationTestContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.test.helpers.CRDatabaseTestCase;
import eionet.cr.util.Pair;
import eionet.cr.util.pagination.PagingRequest;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import org.junit.Ignore;

/**
 * JUnit test tests HarvestSourceDAO functionality.
 *
 * @author altnyris
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationTestContext.class })
public class HarvestSourceDAOIT extends CRDatabaseTestCase {

    @Autowired
    private HarvestSourceDAO harvestSourceDao;

    @Before
    public void setup() throws Exception {
        super.setUp();
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.test.helpers.CRDatabaseTestCase#getXMLDataSetFiles()
     */
    @Override
    protected List<String> getXMLDataSetFiles() {
        return Arrays.asList("sources-harvests-messages.xml");
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testAddSource() throws Exception {


        HarvestSourceDTO source = new HarvestSourceDTO();
        source.setUrl("http://rod.eionet.europa.eu/testObligations");
        source.setIntervalMinutes(0);
        source.setPrioritySource(false);
        source.setEmails("bob@europe.eu");

        Integer harvestSourceID = harvestSourceDao.addSource(source);
        assertNotNull(harvestSourceID);

        HarvestSourceDTO harvestSource = harvestSourceDao.getHarvestSourceById(harvestSourceID);
        assertEquals("bob@europe.eu", harvestSource.getEmails());
        assertEquals("http://rod.eionet.europa.eu/testObligations", harvestSource.getUrl());
        assertEquals("bob@europe.eu", harvestSource.getEmails());
    }

    @Test
    public void testGetHarvestSourceByUrl() throws Exception {

        HarvestSourceDTO dto = harvestSourceDao.getHarvestSourceByUrl("http://www.eionet.europa.eu/seris/rdf");
        assertNotNull(dto);
    }

    @Test
    public void testGetHarvestSources() throws Exception {

        Pair<Integer, List<HarvestSourceDTO>> result = harvestSourceDao.getHarvestSources("", PagingRequest.create(1, 100), null);
        assertNotNull(result);
        assertNotNull(result.getRight());
        assertEquals(8, result.getRight().size());
    }

    @Test
    public void testEditSource() throws Exception {

        // get the source by URL
        HarvestSourceDTO harvestSource = harvestSourceDao.getHarvestSourceByUrl("http://www.eionet.europa.eu/seris/rdf");
        assertNotNull(harvestSource);

        // change the URL of the source
        harvestSource.setUrl("http://www.eionet.europa.eu/seris/rdf-dummy");
        harvestSourceDao.editSource(harvestSource);

        // get the source by previous URL again- now it must be null
        assertNull(harvestSourceDao.getHarvestSourceByUrl("http://www.eionet.europa.eu/seris/rdf"));

        // get the source by new URL, it must not be null
        assertNotNull(harvestSourceDao.getHarvestSourceByUrl("http://www.eionet.europa.eu/seris/rdf-dummy"));
    }

    /**
     * tests unauthorized sources query.
     *
     * @throws Exception if fails
     */
    @Test
    public void testGetUnauthorizedSources() throws Exception {

        Pair<Integer, List<HarvestSourceDTO>> dto = harvestSourceDao.getHarvestSourcesUnauthorized("", null, null);

        assertNotNull(dto);
        assertEquals(2, (int) dto.getLeft());

    }

    /**
     * tests unauthorized query filter.
     *
     * @throws Exception if fails
     */
    @Test
    public void testGetUnauthorizedSourcesFilter() throws Exception {

        Pair<Integer, List<HarvestSourceDTO>> dto = harvestSourceDao.getHarvestSourcesUnauthorized("%countries%", null, null);

        assertNotNull(dto);
        assertEquals(1, (int) dto.getLeft());

        HarvestSourceDTO source = dto.getRight().get(0);
        assertEquals("http://rod.eionet.europa.eu/countries", source.getUrl());

    }

    @Test
    public void testGetNextScheduledOnlineCsvTsv() throws Exception {

        List<HarvestSourceDTO> sources = harvestSourceDao.getNextScheduledOnlineCsvTsv(1);
        assertEquals(1, sources.size());
        assertEquals(9, sources.get(0).getSourceId().intValue());
    }

    @Test
    public void testGetNextScheduleOnlineCsvTsvException() throws Exception {

        try {
            harvestSourceDao.getNextScheduledOnlineCsvTsv(0);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("Limit must be >=1"));
        }
    }

}
