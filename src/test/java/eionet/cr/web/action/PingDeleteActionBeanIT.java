package eionet.cr.web.action;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.List;

import net.sourceforge.stripes.mock.MockHttpServletResponse;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.mortbay.jetty.Server;

import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestDAO;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.dto.TripleDTO;
import eionet.cr.harvest.BaseHarvest;
import eionet.cr.harvest.PullHarvest;
import eionet.cr.test.helpers.CRDatabaseTestCase;
import eionet.cr.test.helpers.JettyUtil;
import eionet.cr.test.helpers.RdfLoader;
import eionet.cr.web.security.CRUser;

/**
 * Unit tests for the "delete" operation of {@link PingActionBean}.
 *
 * @author Jaanus
 */
public class PingDeleteActionBeanIT extends CRDatabaseTestCase {

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
     * @see eionet.cr.test.helpers.CRDatabaseTestCase#setUp()
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        new RdfLoader().clearAllTriples();
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testDeleteWithoutURI() throws Exception {

        // Set up and execute round-trip.
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, PingActionBean.class);
        trip.setParameter("delete", "");
        trip.execute();
        PingActionBean bean = trip.getActionBean(PingActionBean.class);

        // Assert response code.
        MockHttpServletResponse response = (MockHttpServletResponse) bean.getContext().getResponse();
        assertEquals(200, response.getStatus());

        // Assert returned content type.
        String contentType = bean.getContext().getResponse().getContentType();
        assertEquals("text/xml", contentType);

        // Assert response message.
        String outputStr = response.getOutputString();
        assertNotNull("Expected non-null response message", outputStr);
        assertTrue("Unexpected error code", outputStr.contains("<flerror>" + PingActionBean.ERR_BLANK_URI + "</flerror>"));
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testDeleteWithInvalidURL() throws Exception {

        // Set up and execute round-trip.
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, PingActionBean.class);
        trip.setParameter("uri", "...............");
        trip.setParameter("delete", "");
        trip.execute();
        PingActionBean bean = trip.getActionBean(PingActionBean.class);

        // Assert response code.
        MockHttpServletResponse response = (MockHttpServletResponse) bean.getContext().getResponse();
        assertEquals(200, response.getStatus());

        // Assert returned content type.
        String contentType = bean.getContext().getResponse().getContentType();
        assertEquals("text/xml", contentType);

        // Assert response message.
        String outputStr = response.getOutputString();
        System.out.println(outputStr);
        assertNotNull("Expected non-null response message", outputStr);
        assertTrue("Unexpected message", outputStr.contains("<message>URL not in catalogue of sources, no action taken"));
        assertTrue("Unexpected message", outputStr.contains("<flerror>0</flerror>"));
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testDeleteHarvestPingDeleteSequence() throws Exception {

        // Create the source to be tested. The URL is served by Jetty below.

        String url = "http://localhost:8999/testResources/simple-rdf.xml";
        HarvestSourceDTO source = new HarvestSourceDTO();
        source.setUrl(url);
        source.setIntervalMinutes(5);
        HarvestSourceDAO sourceDao = DAOFactory.get().getDao(HarvestSourceDAO.class);
        sourceDao.addSource(source);
        source = sourceDao.getHarvestSourceByUrl(url);
        assertNotNull("Expected existing harvest source", source);

        // Harvest the source, served by Jetty.

        Server server = null;
        try {
            server = JettyUtil.startResourceServerMock(8999, "/testResources", "simple-rdf.xml");
            PullHarvest harvest = new PullHarvest(url);
            harvest.execute();
            assertEquals(12, harvest.getStoredTriplesCount());
        } finally {
            JettyUtil.close(server);
        }

        // Assert that source graph has content and there's triples *about* the source.

        source = sourceDao.getHarvestSourceByUrl(url);
        assertNotNull("Expected existing harvest source", source);
        HelperDAO helperDao = DAOFactory.get().getDao(HelperDAO.class);
        List<TripleDTO> sampleTriples = helperDao.getSampleTriplesInSource(url, null);
        assertTrue("Expected some triples in " + url, CollectionUtils.isNotEmpty(sampleTriples));
        SubjectDTO subject = helperDao.getSubject(url);
        assertTrue("Expected triples about " + url, subject != null && subject.getTripleCount() > 0);

        // Try to delete and assert response=forbidden (source not pinged yet, hence cannot be deleted via ping either).
        doAndAssertForbiddenSourceDelete(url);

        // Insert a started and finished PING harvest.
        HarvestDAO harvestDao = DAOFactory.get().getDao(HarvestDAO.class);
        int harvestId =
                harvestDao.insertStartedHarvest(source.getSourceId(), BaseHarvest.TYPE_PULL, CRUser.PING_HARVEST.getUserName(),
                        BaseHarvest.STATUS_STARTED);
        assertTrue("Expected a valid harvest id", harvestId > 0);
        harvestDao.updateFinishedHarvest(harvestId, 12, 200);

        // Now the deletion via PING should work, as we have the source and have a PING harvest for it.
        doAndAssertAllowedSourceDelete(url);

        // Assert that the source has gone, including its graph content and all triples *about* it
        source = sourceDao.getHarvestSourceByUrl(url);
        assertNull("Expected no more harvest source for " + url, source);
        sampleTriples = helperDao.getSampleTriplesInSource(url, null);
        assertTrue("Expected no more triples in " + url, CollectionUtils.isEmpty(sampleTriples));
        subject = helperDao.getSubject(url);
        assertTrue("Expected no more triples about " + url, subject == null || subject.getTripleCount() == 0);
    }

    /**
     *
     * @param url
     * @param create
     * @throws Exception
     */
    private void doAndAssertAllowedSourceDelete(String url) throws Exception {

        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, PingActionBean.class);
        trip.setParameter("uri", url);
        trip.setParameter("delete", "");
        trip.execute();
        PingActionBean bean = trip.getActionBean(PingActionBean.class);

        // Assert response code.
        MockHttpServletResponse response = (MockHttpServletResponse) bean.getContext().getResponse();
        assertEquals(200, response.getStatus());

        // Assert returned content type.
        String contentType = bean.getContext().getResponse().getContentType();
        assertEquals("text/xml", contentType);

        // Assert response message.
        String outputStr = response.getOutputString();
        assertNotNull("Expected non-null response message", outputStr);
        assertTrue("Unexpected message", outputStr.contains("<message>URL deleted: " + url));
        assertTrue("Unexpected error code", outputStr.contains("<flerror>0</flerror>"));
    }

    /**
     *
     * @param url
     * @throws Exception
     */
    private void doAndAssertForbiddenSourceDelete(String url) throws Exception {

        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, PingActionBean.class);
        trip.setParameter("uri", url);
        trip.setParameter("delete", "");
        trip.execute();
        PingActionBean bean = trip.getActionBean(PingActionBean.class);

        // Assert response code.
        MockHttpServletResponse response = (MockHttpServletResponse) bean.getContext().getResponse();
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, response.getStatus());
    }
}
