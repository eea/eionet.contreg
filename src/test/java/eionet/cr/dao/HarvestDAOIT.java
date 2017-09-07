package eionet.cr.dao;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import eionet.cr.ApplicationTestContext;
import org.junit.Test;

import eionet.cr.dto.HarvestDTO;
import eionet.cr.test.helpers.CRDatabaseTestCase;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

/**
 * Tests for the {@link HarvestDAO} implementations.
 *
 * @author Jaanus
 */
@SqlGroup({
        @Sql({"/harvests.sql"}),
        @Sql(scripts = "/harvests-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
})
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationTestContext.class })
public class HarvestDAOIT extends CRDatabaseTestCase {

    @Autowired
    private HarvestDAO harvestDAO;

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.test.helpers.CRDatabaseTestCase#getXMLDataSetFiles()
     */
    @Override
    protected List<String> getXMLDataSetFiles() {
        return Arrays.asList("harvests.xml");
    }

    /**
     * Test {@link HarvestDAO#markAbandonedHarvests()}.
     *
     * @throws Exception Any exception occurred during this test.
     */
    @Test
    public void testMarkAbandonedHarvests() throws Exception {

        List<HarvestDTO> harvests = harvestDAO.getHarvestsBySourceId(1);

        assertNotNull("Harvests shouldn't be null", harvests);
        assertFalse("Was expecting at least one harvest record", harvests.isEmpty());

        HashSet<Integer> actualAbandonedHarvests = collectAbandonedHarvests(harvests);
        assertTrue("Was expecting no abandoned harevsts yet!", actualAbandonedHarvests.isEmpty());

        int updateCount = harvestDAO.markAbandonedHarvests();
        assertEquals("Was expecting that 2 harvests were marked as abandoned!", 2, updateCount);

        HashSet<Integer> expectedAbandonedHarvests = new HashSet<Integer>();
        expectedAbandonedHarvests.add(3);
        expectedAbandonedHarvests.add(4);

        harvests = harvestDAO.getHarvestsBySourceId(1);
        actualAbandonedHarvests = collectAbandonedHarvests(harvests);
        assertEquals("Was expecting different abandoned harvests!", expectedAbandonedHarvests, actualAbandonedHarvests);
    }

    /**
     * Helper method for collecting abandoned harvests from the given list.
     *
     * @param harvests The harvests to check.
     * @return Ids of harvests that were abandoned.
     */
    private HashSet<Integer> collectAbandonedHarvests(List<HarvestDTO> harvests) {

        HashSet<Integer> abandonedHarvests = new HashSet<Integer>();
        for (HarvestDTO harvestDTO : harvests) {
            if (harvestDTO.isAbandoned()) {
                abandonedHarvests.add(harvestDTO.getHarvestId());
            }
        }
        return abandonedHarvests;
    }
}
