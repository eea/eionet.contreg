package eionet.cr.web.action;

import net.sourceforge.stripes.mock.MockHttpServletResponse;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.test.helpers.CRDatabaseTestCase;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Unit tests for the PING action bean.
 *
 * @author Jaanus
 */
public class PingActionBeanIT extends CRDatabaseTestCase {

    /**
     * @throws Exception
     */
    @Test
    public void testPingWithoutURI() throws Exception {

        // Set up and execute round-trip.
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, PingActionBean.class);
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
     * @throws Exception
     */
    @Test
    public void testPingWithInvalidURL() throws Exception {

        // Set up and execute round-trip.
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, PingActionBean.class);
        trip.setParameter("uri", "...............");
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
        assertTrue("Unexpected message", outputStr.contains("<message>Not a valid URL, no action taken.</message>"));
    }

    /**
     * @throws Exception
     */
    @Test
    public void testPingCreateWithInvalidURL() throws Exception {

        // Set up and execute round-trip.
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, PingActionBean.class);
        trip.setParameter("uri", "...............");
        trip.setParameter("create", "true");
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
        assertTrue("Unexpected error code", outputStr.contains("<flerror>" + PingActionBean.ERR_INVALID_URL + "</flerror>"));
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testPingCreateWithFragmentedURL() throws Exception {

        // Set up and execute round-trip.
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, PingActionBean.class);
        trip.setParameter("uri", "http://estonia.ee/test#fragment");
        trip.setParameter("create", "true");
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
        assertTrue("Unexpected error code", outputStr.contains("<flerror>" + PingActionBean.ERR_FRAGMENT_URL + "</flerror>"));
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testPingCreateWithBrokenURL() throws Exception {

        // Set up and execute round-trip.
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, PingActionBean.class);
        trip.setParameter("uri", "http://localhost:1234/never");
        trip.setParameter("create", "true");
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
        assertTrue("Unexpected error code", outputStr.contains("<flerror>" + PingActionBean.ERR_BROKEN_URL + "</flerror>"));
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testPingNonExistingSourceWithoutCreate() throws Exception {

        String url = "http://www.eea.europa.eu/";

        // Set up and execute round-trip.
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, PingActionBean.class);
        trip.setParameter("uri", url);
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
        assertTrue("Unexpected message",
                outputStr.contains("<message>URL not in catalogue of sources, no action taken:"));
        assertTrue("Unexpected error code", outputStr.contains("<flerror>0</flerror>"));

        // Test that source does not exist yet
        HarvestSourceDAO harvestSourceDao = DAOFactory.get().getDao(HarvestSourceDAO.class);
        HarvestSourceDTO harvestSource = harvestSourceDao.getHarvestSourceByUrl(url);
        assertNull("Expected non-existing harvest source", harvestSource);
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testPingNonExistingSourceWithCreate() throws Exception {

        // Test that source does not exist yet.
        String url = "http://www.eea.europa.eu/";
        HarvestSourceDAO harvestSourceDao = DAOFactory.get().getDao(HarvestSourceDAO.class);
        HarvestSourceDTO harvestSource = harvestSourceDao.getHarvestSourceByUrl(url);
        assertNull("Expected non-existing harvest source", harvestSource);

        // Set up and execute round-trip.
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, PingActionBean.class);
        trip.setParameter("uri", url);
        trip.setParameter("create", "true");
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
        assertTrue("Unexpected message", outputStr.contains("<message>URL added to the urgent harvest queue"));
        assertTrue("Unexpected error code", outputStr.contains("<flerror>0</flerror>"));

        // Test that source exists now.
        harvestSource = harvestSourceDao.getHarvestSourceByUrl(url);
        assertNotNull("Expected existing harvest source", harvestSource);
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testPingExistingSource() throws Exception {

        // First, create the source to test.
        HarvestSourceDTO source = new HarvestSourceDTO();
        source.setUrl("http://www.eea.europa.eu/");
        source.setIntervalMinutes(1440);
        HarvestSourceDAO harvestSourceDao = DAOFactory.get().getDao(HarvestSourceDAO.class);
        harvestSourceDao.addSource(source);

        // Set up and execute round-trip.
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, PingActionBean.class);
        trip.setParameter("uri", "http://www.eea.europa.eu/");
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
        assertTrue("Unexpected message", outputStr.contains("<message>URL added to the urgent harvest queue"));
        assertTrue("Unexpected error code", outputStr.contains("<flerror>0</flerror>"));
    }
}
