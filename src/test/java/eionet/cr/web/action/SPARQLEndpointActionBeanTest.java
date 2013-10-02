package eionet.cr.web.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import net.sourceforge.stripes.mock.MockHttpServletResponse;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;
import net.sourceforge.stripes.mock.MockServletOutputStream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;

import eionet.cr.test.helpers.RdfLoader;

/**
 * tests for SPARQL endpoint action bean.
 *
 * @author kaido
 */
public class SPARQLEndpointActionBeanTest {

    /** RDF seed file to be loaded. */
    private static final String RDF_SEED_FILE = "rdf_national_chars_utf8.rdf.xml";

    /**
     * Test set-up.
     *
     * @throws Exception When any sort of error happens.
     */
    @Before
    public void setUp() throws Exception {
        RdfLoader rdfLoader = new RdfLoader();
        rdfLoader.clearAllTriples();
        rdfLoader.loadIntoTripleStore(RDF_SEED_FILE, RDFFormat.RDFXML);
    }

    /**
     * tests script execute if no format specified but query is OK.
     *
     * @throws Exception if testing fails
     */
    @Test
    public void testExecuteNoFormat() throws Exception {

        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, SPARQLEndpointActionBean.class);
        String sparql = "SELECT ?s ?p ?o WHERE {?s ?p ?o} limit 1";
        trip.setParameter("query", sparql);

        trip.execute();

        SPARQLEndpointActionBean bean = trip.getActionBean(SPARQLEndpointActionBean.class);

        // http response code = 200
        MockHttpServletResponse response = (MockHttpServletResponse) bean.getContext().getResponse();
        assertEquals(200, response.getStatus());

        MockServletOutputStream os = (MockServletOutputStream) bean.getContext().getResponse().getOutputStream();
        assertTrue(os.getString().indexOf("<sparql xmlns='http://www.w3.org/2005/sparql-results#'>") != -1);

        // default content type if no format specified
        String contentType = bean.getContext().getResponse().getContentType();
        assertEquals("application/sparql-results+xml", contentType);

    }

    /**
     * tests script execute if query is not valid SPARQL.
     * must return http code 400
     *
     * @throws Exception if testing fails
     */
    @Test
    public void testExecuteBadQuery() throws Exception {

        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, SPARQLEndpointActionBean.class);

        // syntax error:
        String sparql = "SELECT ?s ?p ?o WHERE {?s ?p ?o limit 1}";

        trip.setParameter("query", sparql);

        trip.execute();

        SPARQLEndpointActionBean bean = trip.getActionBean(SPARQLEndpointActionBean.class);

        // http response code = 400
        MockHttpServletResponse response = (MockHttpServletResponse) bean.getContext().getResponse();
        assertEquals(400, response.getStatus());

        // default content type text/plain if error returned to an external client
        String contentType = bean.getContext().getResponse().getContentType();
        assertEquals("text/plain", contentType);

    }

    /**
     * tests script execute if request does not have query parameter.
     * must return http code 400
     *
     * @throws Exception if testing fails
     */
    @Test
    public void testExecuteMalformedRequest() throws Exception {

        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, SPARQLEndpointActionBean.class);

        trip.execute();

        SPARQLEndpointActionBean bean = trip.getActionBean(SPARQLEndpointActionBean.class);

        // http response code = 400
        MockHttpServletResponse response = (MockHttpServletResponse) bean.getContext().getResponse();
        assertEquals(400, response.getStatus());

        // default content type text/plain if error returned to an external client
        String contentType = bean.getContext().getResponse().getContentType();
        assertEquals("text/plain", contentType);

    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testConstructQueries() throws Exception {

        testConstructQuery("application/rdf+xml");
        testConstructQuery("text/turtle");
        testConstructQuery("application/x-turtle");
        testConstructQuery("text/n3");
        testConstructQuery("text/plain");
    }

    /**
     *
     * @param acceptedContentType
     * @throws Exception
     */
    private void testConstructQuery(String acceptedContentType) throws Exception {

        String graphUri = RdfLoader.getSeedFileGraphUri(RDF_SEED_FILE);

        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, SPARQLEndpointActionBean.class);
        String sparql = "CONSTRUCT {?s ?p ?o} FROM <" + graphUri + "> WHERE {?s ?p ?o} order by ?s ?p ?o";
        trip.setParameter("query", sparql);
        trip.getRequest().addHeader("Accept", acceptedContentType);

        trip.execute();

        SPARQLEndpointActionBean bean = trip.getActionBean(SPARQLEndpointActionBean.class);

        // Check HTTP response code
        MockHttpServletResponse response = (MockHttpServletResponse) bean.getContext().getResponse();
        assertEquals("Was expecting HTTP response 200", 200, response.getStatus());

        // Check response headers.
        CaseInsensitiveMap responseHeaders = new CaseInsensitiveMap(response.getHeaderMap());
        assertFalse("Expected response headers to not be empty", responseHeaders.isEmpty());

        // Check response content type.
        List<Object> responseContentTypes = (List<Object>) responseHeaders.get("content-type");
        assertTrue("Expected non-empty response content type", CollectionUtils.isNotEmpty(responseContentTypes));
        assertEquals("Expected response content type to be", acceptedContentType, responseContentTypes.iterator().next()
                .toString());

        // Check response output
        byte[] outputBytes = response.getOutputBytes();
        String outputString = new String(outputBytes, "UTF-8");
        assertTrue("Expected a non-blank response output", StringUtils.isNotBlank(outputString));
    }
}
