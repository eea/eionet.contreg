package eionet.cr.web.action;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;

import net.sourceforge.stripes.mock.MockHttpServletResponse;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;
import net.sourceforge.stripes.mock.MockServletOutputStream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.helpers.RDFParserBase;
import org.openrdf.rio.ntriples.NTriplesParser;
import org.openrdf.rio.rdfxml.RDFXMLParser;
import org.openrdf.rio.turtle.TurtleParser;

import eionet.cr.test.helpers.CRDatabaseTestCase;
import eionet.cr.test.helpers.RdfLoader;
import eionet.cr.test.helpers.SimpleStatementRecorder;
import eionet.cr.web.security.CRUser;
import eionet.cr.web.util.WebConstants;

/**
 * Tests for SPARQL endpoint action bean.
 *
 * @author Kaido
 * @author Jaanus
 */
@Ignore
public class SPARQLEndpointActionBeanIT extends CRDatabaseTestCase {

    /** RDF seed file to be loaded. */
    private static final String RDF_SEED_FILE = "rdf_national_chars_utf8.rdf.xml";

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    /*
         * (non-Javadoc)
         *
         * @see eionet.cr.test.helpers.CRDatabaseTestCase#getRDFXMLSeedFiles()
         */
    @Override
    protected List<String> getRDFXMLSeedFiles() {
        return Arrays.asList(RDF_SEED_FILE);
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
    @Ignore
    public void testConstructQueries() throws Exception {

        testConstructQuery("application/rdf+xml");
        testConstructQuery("text/turtle");
        testConstructQuery("application/x-turtle");
        testConstructQuery("text/n3");
        testConstructQuery("text/plain");
    }

    @Test
    public void testCreateLargeBookmark() throws Exception {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, SPARQLEndpointActionBean.class);
        trip.getRequest().setMethod("POST");
        trip.getRequest().getSession().setAttribute(WebConstants.USER_SESSION_ATTR, new CRUser("user"));
        trip.setParameter("saveBookmark", StringUtils.EMPTY);
        trip.setParameter("bookmarkName", "Huge query");
        trip.setParameter("query", this.createLargeDummySparqlQuery());
        trip.execute();
        
        assertEquals(0, trip.getValidationErrors().size());
    }
    
    /**
     * Create and return new instance of {@link RDFParserBase} for the given MIME type.
     * Inject given {@link RDFHandler} to the parser's {@link RDFParserBase#setRDFHandler(RDFHandler)} method.
     *
     * @param mimeType The given MIME type.
     * @param handler {@link RDFHandler} to be injected into the created parser.
     * @return The parser.
     */
    private RDFParserBase createRDFParser(String mimeType, RDFHandler handler) {

        if (StringUtils.isBlank(mimeType)) {
            throw new IllegalArgumentException("Mime type must not be blank!");
        }

        RDFParserBase parser = null;
        if (mimeType.equals("application/rdf+xml")) {
            parser = new RDFXMLParser();
        } else if (mimeType.equals("text/turtle")) {
            parser = new TurtleParser();
        } else if (mimeType.equals("application/x-turtle")) {
            parser = new TurtleParser();
        } else if (mimeType.equals("text/n3")) {
            parser = new TurtleParser();
        } else if (mimeType.equals("text/plain")) {
            parser = new NTriplesParser();
        } else {
            throw new IllegalArgumentException("Unsupported mime type: " + mimeType);
        }

        parser.setRDFHandler(handler);
        return parser;
    }

    /**
     *
     * @param acceptedContentType
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
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
        String responseContentType = responseContentTypes.iterator().next().toString();
        assertEquals("Expected response content type to be", acceptedContentType, responseContentType);

        // Check response output
        byte[] outputBytes = response.getOutputBytes();
        String outputString = new String(outputBytes, "UTF-8");
        assertTrue("Expected a non-blank response output", StringUtils.isNotBlank(outputString));

        SimpleStatementRecorder statementRecorder = new SimpleStatementRecorder();
        RDFParserBase rdfParser = createRDFParser(responseContentType, statementRecorder);
        ByteArrayInputStream inputStream = null;
        try {
            inputStream = new ByteArrayInputStream(outputBytes);
            String baseUri = graphUri;
            rdfParser.parse(inputStream, baseUri);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

        int numberOfRecordedStatements = statementRecorder.getNumberOfRecordedStatements();
        assertTrue("Expected at least one RDF statement in the response output", numberOfRecordedStatements > 0);

        assertRecordedStatement(statementRecorder, "http://www.recshop.fake/cd/CD_title_1", "http://www.recshop.fake/cd#artist",
                "Artist 1");
        assertRecordedStatement(statementRecorder, "http://www.recshop.fake/cd/CD_title_1", "http://www.recshop.fake/cd#company",
                "Cømpany");
    }

    /**
     * Assert that the given RDF statement has been recorded by the given statement recorder.
     *
     * @param recorder The statement recorder.
     * @param subject Subject of the statement to assert.
     * @param predicate Predicate of the statement to assert
     * @param object Object of the statement to assert
     */
    private void assertRecordedStatement(SimpleStatementRecorder recorder, String subject, String predicate, String object) {

        boolean hasStatement = recorder.hasStatement(subject, predicate, object);
        assertTrue("Was expecting this triple in the output:  " + subject + "  " + predicate + "  " + object, hasStatement);
    }
    
    private String createLargeDummySparqlQuery() {
        StringBuilder sb = new StringBuilder();
        
        for (int line = 0; line < 100; line++) {
            for (int i = 0; i < 10; i++) {
                sb.append('#');
            }
            
            sb.append(" ");
        }
        
        return sb.toString();
    }
}
