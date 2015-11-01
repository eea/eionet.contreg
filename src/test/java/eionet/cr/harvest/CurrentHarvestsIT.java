package eionet.cr.harvest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eionet.cr.test.helpers.CRDatabaseTestCase;

/**
 * Tests for the {@link CurrentHarvests} class.
 *
 * @author Jaanus
 */
public class CurrentHarvestsIT extends CRDatabaseTestCase {

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.test.helpers.CRDatabaseTestCase#getXMLDataSetFiles()
     */
    @Override
    protected List<String> getXMLDataSetFiles() {
        return Arrays.asList("harvests.xml");
    }

    /*
     * (non-Javadoc)
     *
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    @Before
    protected void setUp() throws Exception {

        super.setUp();

        // First clean up the on-demand harvests.
        Map<String, String> onDemandHarvests = CurrentHarvests.getOnDemandHarvests();
        if (onDemandHarvests != null && !onDemandHarvests.isEmpty()) {
            for (String url : onDemandHarvests.keySet()) {
                CurrentHarvests.removeOnDemandHarvest(url);
            }
        }

        // Now ensure there's no current urgent harevst.
        CurrentHarvests.setQueuedHarvest(null);
    }

    /*
     * (non-Javadoc)
     *
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    @After
    protected void tearDown() throws Exception {
        // No tear-down logic yet.
    }

    /**
     * Assert that {@link CurrentHarvests} is all empty.
     */
    private void assertCurrentHarevstsEmpty() {

        Map<String, String> onDemandHarvests = CurrentHarvests.getOnDemandHarvests();
        assertTrue("Expected no on-demand harvests", onDemandHarvests == null || onDemandHarvests.isEmpty());

        Harvest queuedHarvest = CurrentHarvests.getQueuedHarvest();
        assertNull("Expected no urgent harevst", queuedHarvest);
    }

    /**
     * Various tests against {@link CurrentHarvests}.
     *
     * @throws Exception Any possible exception during execution of this method.
     */
    @Test
    public void test() throws Exception {

        // Ensure that setUp() has emptied the current harvests.
        assertCurrentHarevstsEmpty();

        String url1 = "http://url1.ee";
        String url2 = "http://url2.ee";
        String url3 = "http://url3.ee";
        String url4 = "http://url4.ee";

        CurrentHarvests.setQueuedHarvest(new PullHarvest(url1));
        CurrentHarvests.addOnDemandHarvest(url2, "heinlja");
        CurrentHarvests.addOnDemandHarvest(url3, "heinlja");
        CurrentHarvests.addOnDemandHarvest(url4, "heinlja");

        assertTrue("Expected current harvests to contain " + url1, CurrentHarvests.contains(url1));
        assertTrue("Expected current harvests to contain " + url2, CurrentHarvests.contains(url2));
        assertTrue("Expected current harvests to contain " + url3, CurrentHarvests.contains(url3));
        assertTrue("Expected current harvests to contain " + url4, CurrentHarvests.contains(url4));

        CurrentHarvests.addOnDemandHarvest(url1, "heinlja");
        assertTrue("Expected current harvests to contain " + url1, CurrentHarvests.contains(url1));

        Harvest queuedHarvest = CurrentHarvests.getQueuedHarvest();
        assertNotNull("Expected queued harvest not to be null", queuedHarvest);
        assertEquals("Expected queued harvest URL to be " + url1, url1, queuedHarvest.getContextUrl());

        Map<String, String> onDemandHarvests = CurrentHarvests.getOnDemandHarvests();
        assertNotNull("Expected on-demand harvests not to be null", onDemandHarvests);
        assertEquals("Expected " + 4 + " on-demand harvests", 4, onDemandHarvests.size());
        assertTrue("Expected on-demand harvests to contain " + url1, onDemandHarvests.containsKey(url1));
        assertTrue("Expected on-demand harvests to contain " + url2, onDemandHarvests.containsKey(url2));
        assertTrue("Expected on-demand harvests to contain " + url3, onDemandHarvests.containsKey(url3));
        assertTrue("Expected on-demand harvests to contain " + url4, onDemandHarvests.containsKey(url4));

        CurrentHarvests.removeOnDemandHarvest(url1);
        onDemandHarvests = CurrentHarvests.getOnDemandHarvests();
        assertFalse("Expected on-demand harvests NOT to contain " + url1, onDemandHarvests.containsKey(url1));
        assertTrue("Expected current harvests to contain " + url1, CurrentHarvests.contains(url1));

        CurrentHarvests.setQueuedHarvest(null);
        assertFalse("Expected current harvests NOT to contain " + url1, CurrentHarvests.contains(url1));

        queuedHarvest = CurrentHarvests.getQueuedHarvest();
        assertNull("Expected queued harvest to be null", null);
    }
}
