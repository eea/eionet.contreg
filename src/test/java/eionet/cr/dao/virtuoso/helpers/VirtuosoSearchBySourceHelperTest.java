package eionet.cr.dao.virtuoso.helpers;

import eionet.cr.ApplicationTestContext;
import eionet.cr.util.SortOrder;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.pagination.PagingRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationTestContext.class })
public class VirtuosoSearchBySourceHelperTest {

    @Test
    public void testOrderByLabel() {
        String sourceUrl = "http://www.eionet.europa.eu/rdf/portal_types.rdf";
        PagingRequest pagingRequest = PagingRequest.create(1);
        SortingRequest sortingRequest =
                new SortingRequest("http://www.w3.org/2000/01/rdf-schema#label", SortOrder.parse(SortOrder.ASCENDING.toString()));

        VirtuosoSearchBySourceHelper helper = new VirtuosoSearchBySourceHelper(sourceUrl, pagingRequest, sortingRequest);

        String sparql = helper.getQuery(null);
        assertEquals("select distinct ?s from ?sourceUrl where {?s ?p ?o .optional {?s ?sortPredicate ?ord} } "
                + "ORDER BY asc(bif:lcase(bif:either(bif:isnull(?ord), (bif:subseq "
                + "(bif:replace (?s, '/', '#'), bif:strrchr (bif:replace (?s, '/', '#'), '#')+1)), ?ord))) limit 15 offset 0",
                sparql);

        assertTrue(helper.getQueryBindings().toString().indexOf("sourceUrl=http://www.eionet.europa.eu/rdf/portal_types.rdf") != -1);
        assertTrue(helper.getQueryBindings().toString().indexOf("sortPredicate=http://www.w3.org/2000/01/rdf-schema#label") != -1);
        // select distinct ?s from <http://www.eionet.europa.eu/rdf/portal_types.rdf>
        // where {?s ?p ?o .optional {?s <http://www.w3.org/2000/01/rdf-schema#label> ?ord} }
        // ORDER BY asc(bif:lcase(bif:either(bif:isnull(?ord), (bif:subseq (bif:replace (?s, '/', '#'),
        // bif:strrchr (bif:replace (?s, '/', '#'), '#')+1)), ?ord)))
    }
}
