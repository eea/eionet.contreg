package eionet.cr.web.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import net.sourceforge.stripes.mock.MockHttpServletResponse;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;
import net.sourceforge.stripes.validation.ValidationError;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.junit.Test;

import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.test.helpers.CRDatabaseTestCase;
import eionet.cr.web.action.mock.SPARQLEndpointActionBeanMock;
import eionet.cr.web.sparqlClient.helpers.QueryResult;
import eionet.cr.web.sparqlClient.helpers.QueryResultValidator;
import eionet.cr.web.sparqlClient.helpers.ResultValue;

/**
 *
 * Test cases for SPARQLEndpoint sources from query functionality.
 *
 * @author Jaak Kapten
 */
public class SPARQLEndpointBulkActionsIT extends CRDatabaseTestCase {

    /** RDF seed file to be loaded. */
    private static String RDF_SEED_FILE = "testseed_sparqlendpoint_bulk.xml";

    public static final String GLOBAL_ERROR = "__stripes_global_error";

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
     * Tests sources bulk add without error situations
     *
     * @throws Exception
     *             if testing fails
     */
    @Test
    public void testProperQuery() throws Exception {

        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, SPARQLEndpointActionBeanMock.class);
        String sparql =
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                        + "PREFIX cr: <http://cr.eionet.europa.eu/ontologies/contreg.rdf#> " + "select distinct ?s where "
                        + "{?s a ?type " + ". FILTER (strStarts(str(?s),'http://www.eea.europa.eu/')) "
                        + ". FILTER (strStarts(str(?type),'http://www.eea.europa.eu/portal_types'))" + "} order by ?s";
        trip.setParameter("query", sparql);
        trip.setParameter("format", "html");

        trip.execute("executeAddSources");

        SPARQLEndpointActionBeanMock bean = trip.getActionBean(SPARQLEndpointActionBeanMock.class);

        // HTTP response code = 200.
        MockHttpServletResponse response = (MockHttpServletResponse) bean.getContext().getResponse();
        assertEquals(200, response.getStatus());

        QueryResult beanResult = bean.getResult();
        ArrayList<HashMap<String, ResultValue>> resultRows = beanResult == null ? null : beanResult.getRows();
        int resultRowsSize = resultRows == null ? 0 : resultRows.size();
        assertEquals(3, resultRowsSize);

        String firstSubject = resultRowsSize > 0 ? resultRows.get(0).get("s").toString() : null;
        assertEquals("http://www.eea.europa.eu/data-and-maps/data/waterbase-groundwater-6/tables-metadata", firstSubject);

        HarvestSourceDTO source =
                DAOFactory
                .get()
                .getDao(HarvestSourceDAO.class)
                .getHarvestSourceByUrl(
                        "http://www.eea.europa.eu/data-and-maps/data/waterbase-groundwater-6/tables-metadata");
        assertNotNull("Expected harvest source not to be null!", source);

        Date timeCreated = source.getTimeCreated();
        Date lastHarvest = source.getLastHarvest();
        assertFalse("Expected non-equal creation and last harvest times of the source", timeCreated == lastHarvest);

        String todaysDate = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
        assertEquals("Expected source creation date to equal today's", todaysDate, DateFormatUtils.format(timeCreated, "yyyy-MM-dd"));
        assertEquals("Unexpected last harvest date", "2000-01-01", DateFormatUtils.format(lastHarvest, "yyyy-MM-dd"));
    }

    /**
     * Tests a situation where the first column returned includes non url values.
     *
     * @throws Exception
     */
    @Test
    public void testWrongColumns() throws Exception {

        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, SPARQLEndpointActionBeanMock.class);
        String sparql =
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                        + "PREFIX cr: <http://cr.eionet.europa.eu/ontologies/contreg.rdf#> " + "select distinct \"aaa\" ?s where "
                        + "{?s a ?type " + ". FILTER (strStarts(str(?s),'http://www.eea.europa.eu/')) "
                        + ". FILTER (strStarts(str(?type),'http://www.eea.europa.eu/portal_types'))" + "}";
        trip.setParameter("query", sparql);
        trip.setParameter("format", "html");

        trip.execute("executeAddSources");

        SPARQLEndpointActionBeanMock bean = trip.getActionBean(SPARQLEndpointActionBeanMock.class);

        List<ValidationError> messages = bean.getContext().getValidationErrors().get(GLOBAL_ERROR);
        String message = CollectionUtils.isEmpty(messages) ? null : messages.get(0).getMessage(Locale.ENGLISH);

        String expectedMessage = QueryResultValidator.PROPER_BULK_SOURCE_FAIL_RESULT_CONTAINS_NON_URLS;
        assertEquals("Message", expectedMessage, message);
    }

    /**
     * Test a situation where multiple columns are returned, but the first column includes proper urls.
     *
     * @throws Exception
     */
    @Test
    public void testCorrectMultipleColumns() throws Exception {

        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, SPARQLEndpointActionBeanMock.class);
        String sparql =
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                        + "PREFIX cr: <http://cr.eionet.europa.eu/ontologies/contreg.rdf#> "
                        + "select distinct ?s \"aaa\" \"bbb\" where " + "{?s a ?type "
                        + ". FILTER (strStarts(str(?s),'http://www.eea.europa.eu/')) "
                        + ". FILTER (strStarts(str(?type),'http://www.eea.europa.eu/portal_types'))" + "}";
        trip.setParameter("query", sparql);
        trip.setParameter("format", "html");

        trip.execute("executeAddSources");

        SPARQLEndpointActionBeanMock bean = trip.getActionBean(SPARQLEndpointActionBeanMock.class);
        QueryResult beanResult = bean.getResult();
        ArrayList<HashMap<String, ResultValue>> resultRows = beanResult == null ? null : beanResult.getRows();
        int size = resultRows == null ? 0 : resultRows.size();
        assertEquals("Result rows size", 3, size);
    }

    /**
     * Tests a situation where no results are returned.
     *
     * @throws Exception
     */
    @Test
    public void testEmptyResult() throws Exception {

        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, SPARQLEndpointActionBeanMock.class);
        String sparql = "select ?a ?b where {<s> <p> <o>} limit 0";
        trip.setParameter("query", sparql);
        trip.setParameter("format", "html");

        trip.execute("executeAddSources");

        SPARQLEndpointActionBeanMock bean = trip.getActionBean(SPARQLEndpointActionBeanMock.class);

        List<ValidationError> messages = bean.getContext().getValidationErrors().get(GLOBAL_ERROR);
        String message = CollectionUtils.isEmpty(messages) ? null : messages.get(0).getMessage(Locale.ENGLISH);

        String expectedMessage = QueryResultValidator.PROPER_BULK_SOURCE_FAIL_RESULT_EMPTY;
        assertEquals("Message", expectedMessage, message);
    }

    /**
     * Tests admin privileges.
     *
     * @throws Exception
     */
    @Test
    public void testAdminPrivileges() throws Exception {

        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, SPARQLEndpointActionBean.class);
        String sparql = "select ?a ?b where {<s> <p> <o>} limit 0";
        trip.setParameter("query", sparql);
        trip.setParameter("format", "html");

        trip.execute("executeAddSources");

        SPARQLEndpointActionBean bean = trip.getActionBean(SPARQLEndpointActionBean.class);

        List<ValidationError> messages = bean.getContext().getValidationErrors().get(GLOBAL_ERROR);
        String message = CollectionUtils.isEmpty(messages) ? null : messages.get(0).getMessage(Locale.ENGLISH);

        String expectedMessage = SPARQLEndpointActionBean.VALIDATION_ERROR_MUST_BE_ADMINISTRATOR;
        assertEquals("Message", expectedMessage, message);
    }
}
