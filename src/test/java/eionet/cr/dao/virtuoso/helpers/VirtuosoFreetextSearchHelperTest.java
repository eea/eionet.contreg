package eionet.cr.dao.virtuoso.helpers;

import java.text.ParseException;
import eionet.cr.ApplicationTestContext;
import eionet.cr.dao.helpers.FreeTextSearchHelper.FilterType;
import eionet.cr.dao.util.SearchExpression;
import eionet.cr.util.SortOrder;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.util.sql.VirtuosoFullTextQuery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

/**
 * VirtuosoFreetextSearchHelper tests.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationTestContext.class })
public class VirtuosoFreetextSearchHelperTest {

    /**
     * Test creating ordered query string.
     */
    @Test
    public void testNonExactAnyObject() {
        PagingRequest pagingRequest = PagingRequest.create(1);
        SortingRequest sortingRequest = new SortingRequest(null, SortOrder.parse(SortOrder.ASCENDING.toString()));
        boolean exactMatch = false;
        SearchExpression expression = new SearchExpression("water");
        VirtuosoFullTextQuery virtExpression = null;
        try {
            virtExpression = VirtuosoFullTextQuery.parse(expression);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        VirtuosoFreeTextSearchHelper helper =
                new VirtuosoFreeTextSearchHelper(expression, virtExpression, exactMatch, pagingRequest, sortingRequest);
        helper.setFilter(FilterType.ANY_OBJECT);
        String sparql = helper.getQuery(null);

        assertEquals("select distinct ?s where { ?s ?p ?o . filter bif:contains(?o, ?objectVal)} limit 15 offset 0", sparql);
        // assertTrue(helper.getQueryBindings().toString().indexOf("objectValue='water'") != -1);
    }

    /**
     * Test creating unordered query string.
     */
    @Test
    public void testExactAnyObject() {
        PagingRequest pagingRequest = PagingRequest.create(2);
        SortingRequest sortingRequest = new SortingRequest(null, SortOrder.parse(SortOrder.ASCENDING.toString()));
        boolean exactMatch = true;
        SearchExpression expression = new SearchExpression("water");
        VirtuosoFullTextQuery virtExpression = null;
        try {
            virtExpression = VirtuosoFullTextQuery.parse(expression);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        VirtuosoFreeTextSearchHelper helper =
                new VirtuosoFreeTextSearchHelper(expression, virtExpression, exactMatch, pagingRequest, sortingRequest);
        helper.setFilter(FilterType.ANY_OBJECT);
        String sparql = helper.getQuery(null);
        assertEquals("select distinct ?s where { ?s ?p ?o . filter (?o = ?objectVal)} limit 15 offset 15", sparql);
        // assertTrue(helper.getQueryBindings().toString().indexOf("objectValue=water") != -1);
    }

    /**
     * test Freetext search by any file.
     */
    @Test
    public void testAnyFile() {
        PagingRequest pagingRequest = PagingRequest.create(1);
        SortingRequest sortingRequest = new SortingRequest(null, SortOrder.parse(SortOrder.ASCENDING.toString()));
        boolean exactMatch = false;
        SearchExpression expression = new SearchExpression("report");
        VirtuosoFullTextQuery virtExpression = null;
        try {
            virtExpression = VirtuosoFullTextQuery.parse(expression);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        VirtuosoFreeTextSearchHelper helper =
                new VirtuosoFreeTextSearchHelper(expression, virtExpression, exactMatch, pagingRequest, sortingRequest);
        helper.setFilter(FilterType.ANY_FILE);
        String sparql = helper.getQuery(null);

        assertEquals(
                "select distinct ?s where { ?s ?p ?o . ?s a ?subjType . filter bif:contains(?o, ?objectVal)} limit 15 offset 0",
                sparql);

        // assertTrue(helper.getQueryBindings().toString().indexOf("subjectType=http://cr.eionet.europa.eu/ontologies/contreg.rdf#File")
        // != -1);
        // assertTrue(helper.getQueryBindings().toString().indexOf("objectValue='report") != -1);
    }

    /**
     * test freetext sort by label.
     */
    @Test
    public void testSortedQueryLabel() {
        PagingRequest pagingRequest = PagingRequest.create(1);
        SortingRequest sortingRequest =
                new SortingRequest("http://www.w3.org/2000/01/rdf-schema#label", SortOrder.parse(SortOrder.ASCENDING.toString()));
        boolean exactMatch = false;
        SearchExpression expression = new SearchExpression("ippc");
        VirtuosoFullTextQuery virtExpression = null;
        try {
            virtExpression = VirtuosoFullTextQuery.parse(expression);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        VirtuosoFreeTextSearchHelper helper =
                new VirtuosoFreeTextSearchHelper(expression, virtExpression, exactMatch, pagingRequest, sortingRequest);
        helper.setFilter(FilterType.ANY_OBJECT);
        String sparql = helper.getQuery(null);

        assertEquals("select distinct ?s where {?s ?p ?o . filter bif:contains(?o, ?objectVal) . "
                + "optional {?s ?sortPredicate ?ord}} ORDER BY asc(bif:lcase(bif:either(bif:isnull(?ord), "
                + "(bif:subseq (bif:replace (?s, '/', '#'), bif:strrchr "
                + "(bif:replace (?s, '/', '#'), '#')+1)), ?ord))) limit 15 offset 0", sparql);
        // assertTrue(helper.getQueryBindings().toString().indexOf("objectValue='ippc'") != -1);
        // assertTrue(helper.getQueryBindings().toString().indexOf("sortPredicate=http://www.w3.org/2000/01/rdf-schema#label") !=
        // -1);
    }

    /**
     * test exact match search. Search expression is a URI.
     */
    @Test
    public void testExactUri() {
        PagingRequest pagingRequest = PagingRequest.create(2);
        SortingRequest sortingRequest = new SortingRequest(null, SortOrder.parse(SortOrder.ASCENDING.toString()));
        boolean exactMatch = true;
        SearchExpression expression = new SearchExpression("http://uri.com");
        VirtuosoFullTextQuery virtExpression = null;
        try {
            virtExpression = VirtuosoFullTextQuery.parse(expression);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        VirtuosoFreeTextSearchHelper helper =
                new VirtuosoFreeTextSearchHelper(expression, virtExpression, exactMatch, pagingRequest, sortingRequest);
        helper.setFilter(FilterType.ANY_OBJECT);
        String sparql = helper.getQuery(null);

        assertEquals("select distinct ?s where { ?s ?p ?o . filter (?o = ?objectVal)} limit 15 offset 15", sparql);
        // assertTrue(helper.getQueryBindings().toString().indexOf("objectValueUri=http://uri.com") != -1);
        // assertTrue(helper.getQueryBindings().toString().indexOf("objectValueLit=http://uri.com") != -1);
    }
}
