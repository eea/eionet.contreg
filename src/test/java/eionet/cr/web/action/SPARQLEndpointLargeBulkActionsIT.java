package eionet.cr.web.action;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import eionet.cr.ApplicationTestContext;
import net.sourceforge.stripes.mock.MockHttpServletResponse;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;

import org.junit.Before;
import org.junit.Test;

import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.test.helpers.CRDatabaseTestCase;
import eionet.cr.util.Util;
import eionet.cr.web.action.mock.SPARQLEndpointActionBeanMock;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * Test cases for SPARQLEndpoint bulk source add from query results using very large data amounts.
 *
 * @author Jaak Kapten
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationTestContext.class })
public class SPARQLEndpointLargeBulkActionsIT extends CRDatabaseTestCase {

    /** RDF seed file to be loaded. */
    private static String RDF_SEED_FILE = "testseed_sparqlendpoint_largebulk.xml";
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
                        + ". FILTER (strStarts(str(?type),'http://www.eea.europa.eu/portal_types'))" + "}"
                        + " ORDER BY ?s";
        trip.setParameter("query", sparql);
        trip.setParameter("format", "html");

        trip.execute("executeAddSources");

        SPARQLEndpointActionBeanMock bean = trip.getActionBean(SPARQLEndpointActionBeanMock.class);

        // http response code = 200
        MockHttpServletResponse response = (MockHttpServletResponse) bean.getContext().getResponse();
        assertEquals(200, response.getStatus());
        assertEquals(10000, bean.getResult().getRows().size());
        assertEquals("http://www.eea.europa.eu/data-and-maps/figures1", bean.getResult().getRows().get(0).get("s").toString());

        HarvestSourceDTO source =
                DAOFactory.get().getDao(HarvestSourceDAO.class)
                .getHarvestSourceByUrl("http://www.eea.europa.eu/data-and-maps/figures9979");

        assertEquals(false, source.getTimeCreated() == source.getLastHarvest());
        Date currentDate = new Date();
        String today = Util.dateToString(currentDate, "yyyy-MM-dd");

        assertEquals(today, Util.dateToString(source.getTimeCreated(), "yyyy-MM-dd"));
        assertEquals("2000-01-01", Util.dateToString(source.getLastHarvest(), "yyyy-MM-dd"));
    }

}
