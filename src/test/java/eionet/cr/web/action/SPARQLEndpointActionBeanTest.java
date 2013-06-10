package eionet.cr.web.action;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import net.sourceforge.stripes.mock.MockHttpServletResponse;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;
import net.sourceforge.stripes.mock.MockServletOutputStream;

import org.junit.Test;

/**
 * tests for SPARQL endpoint action bean.
 *
 * @author kaido
 */
public class SPARQLEndpointActionBeanTest {
    /**
     * tests script execute if no format specified but query is OK.
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

        //http response code = 200
        MockHttpServletResponse response = (MockHttpServletResponse) bean.getContext().getResponse();
        assertEquals(200, response.getStatus());


        MockServletOutputStream os = (MockServletOutputStream) bean.getContext().getResponse().getOutputStream();
        assertTrue(os.getString().indexOf("<sparql xmlns='http://www.w3.org/2005/sparql-results#'>") != -1);

        //default content type if no format specified
        String contentType = bean.getContext().getResponse().getContentType();
        assertEquals("application/sparql-results+xml", contentType);


    }


    /**
     * tests script execute if query is not valid SPARQL.
     * must return http code 400
     * @throws Exception if testing fails
     */
    @Test
    public void testExecuteBadQuery() throws Exception {

        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, SPARQLEndpointActionBean.class);

        //syntax error:
        String sparql = "SELECT ?s ?p ?o WHERE {?s ?p ?o limit 1}";

        trip.setParameter("query", sparql);

        trip.execute();

        SPARQLEndpointActionBean bean = trip.getActionBean(SPARQLEndpointActionBean.class);

        //http response code = 400
        MockHttpServletResponse response = (MockHttpServletResponse) bean.getContext().getResponse();
        assertEquals(400, response.getStatus());


        //default content type text/plain if error returned to an external client
        String contentType = bean.getContext().getResponse().getContentType();
        assertEquals("text/plain", contentType);

    }

    /**
     * tests script execute if request does not have query parameter.
     * must return http code 400
     * @throws Exception if testing fails
     */
    @Test
    public void testExecuteMalformedRequest() throws Exception {

        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, SPARQLEndpointActionBean.class);


        trip.execute();

        SPARQLEndpointActionBean bean = trip.getActionBean(SPARQLEndpointActionBean.class);

        //http response code = 400
        MockHttpServletResponse response = (MockHttpServletResponse) bean.getContext().getResponse();
        assertEquals(400, response.getStatus());

        //default content type text/plain if error returned to an external client
        String contentType = bean.getContext().getResponse().getContentType();
        assertEquals("text/plain", contentType);

    }


}
