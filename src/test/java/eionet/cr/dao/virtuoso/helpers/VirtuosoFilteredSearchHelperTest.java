package eionet.cr.dao.virtuoso.helpers;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.openrdf.repository.RepositoryConnection;

import eionet.cr.util.Bindings;
import eionet.cr.util.SortOrder;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.util.sesame.SesameConnectionProvider;
import eionet.cr.util.sesame.SesameUtil;
import eionet.cr.util.sql.SingleObjectReader;

public class VirtuosoFilteredSearchHelperTest {

    @Test
    public void testBuildTypeSearchQuery() {
        PagingRequest pagingRequest = PagingRequest.create(1);
        SortingRequest sortingRequest = new SortingRequest(null, SortOrder.parse(SortOrder.ASCENDING.toString()));

        Map<String, String> filters = new HashMap<String, String>();
        filters.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://rod.eionet.europa.eu/schema.rdf#Obligation");

        VirtuosoFilteredSearchHelper helper = new VirtuosoFilteredSearchHelper(filters, null, pagingRequest, sortingRequest, true);

        ArrayList<Object> inParams = new ArrayList<Object>();

        String query = helper.getQuery(inParams);

        // assertEquals(query, "DEFINE input:inference'CRInferenceRule' select distinct ?s where { ?s ?p ?o  . "
        // + "{{?s ?p1 ?o1 . ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?o1} . { ?s ?p1 ?o1 . "
        // + "FILTER (?o1 = <http://rod.eionet.europa.eu/schema.rdf#Obligation> || "
        // + "?o1 = \"http://rod.eionet.europa.eu/schema.rdf#Obligation\")}}} limit 15 offset 0");

        assertEquals(query, "DEFINE input:inference'CRInferenceRule' select distinct ?s where { ?s ?p ?o  . "
                + "{{?s ?p1 ?o1 . ?s ?predicateValue1 ?o1} . { ?s ?p1 ?o1 . " + "FILTER (?o1 = ?objectValue1Uri || "
                + "?o1 = ?objectValue1Lit)}}} limit 15 offset 0");

    }

    @Test
    public void testOrderedQuery() {
        PagingRequest pagingRequest = PagingRequest.create(1);
        SortingRequest sortingRequest =
            new SortingRequest("http://www.w3.org/2000/01/rdf-schema#label", SortOrder.parse(SortOrder.DESCENDING.toString()));

        Map<String, String> filters = new HashMap<String, String>();
        filters.put("http://purl.org/dc/elements/1.1/creator", "Roug");
        VirtuosoFilteredSearchHelper helper = new VirtuosoFilteredSearchHelper(filters, null, pagingRequest, sortingRequest, true);

        ArrayList<Object> inParams = new ArrayList<Object>();

        String query = helper.getOrderedQuery(inParams);

        assertEquals(query, "DEFINE input:inference'CRInferenceRule' select distinct ?s where { ?s ?p ?o  . {{?s ?p1 ?o1 . "
                + "?s ?predicateValue1 ?o1} . { ?s ?p1 ?o1 . FILTER bif:contains(?o1, ?objectValue1)}} . "
                + "OPTIONAL {?s ?sortPredicateValue ?oorderby}} ORDER BY "
                + "desc(bif:either( bif:isnull(?oorderby) , (bif:lcase(bif:subseq (bif:replace (?s, '/', '#'), "
                + "bif:strrchr (bif:replace (?s, '/', '#'), '#')+1))) , bif:lcase(?oorderby)))");
        //
        // assertEquals(query, "DEFINE input:inference'CRInferenceRule' select distinct ?s where { ?s ?p ?o  . {{?s ?p1 ?o1 . "
        // + "?s <http://purl.org/dc/elements/1.1/creator> ?o1} . { ?s ?p1 ?o1 . FILTER bif:contains(?o1, \"'Roug'\")}} . "
        // + "OPTIONAL {?s <http://www.w3.org/2000/01/rdf-schema#label> ?oorderby }} ORDER BY "
        // + "desc(bif:either( bif:isnull(?oorderby) , (bif:lcase(bif:subseq (bif:replace (?s, '/', '#'), "
        // + "bif:strrchr (bif:replace (?s, '/', '#'), '#')+1))) , bif:lcase(?oorderby)))");
    }

    @Test
    public void testUnOrderedQuery() {
        PagingRequest pagingRequest = PagingRequest.create(1);
        SortingRequest sortingRequest = new SortingRequest(null, SortOrder.parse(SortOrder.ASCENDING.toString()));

        Map<String, String> filters = new HashMap<String, String>();
        // http://rod.eionet.europa.eu/schema.rdf#obligation=http://rod.eionet.europa.eu/obligations/15,
        // http://www.w3.org/2000/01/rdf-schema#label=CLRTAP}
        filters.put("http://rod.eionet.europa.eu/schema.rdf#obligation", "http://rod.eionet.europa.eu/obligations/15");
        filters.put("http://www.w3.org/2000/01/rdf-schema#label", "CLRTAP");
        VirtuosoFilteredSearchHelper helper = new VirtuosoFilteredSearchHelper(filters, null, pagingRequest, sortingRequest, true);

        ArrayList<Object> inParams = new ArrayList<Object>();

        String query = helper.getUnorderedQuery(inParams);

        assertEquals(query, "DEFINE input:inference'CRInferenceRule' select distinct ?s where { ?s ?p ?o  . {{?s ?p1 ?o1 . "
                + "?s ?predicateValue1 ?o1} . { ?s ?p1 ?o1 . " + "FILTER (?o1 = ?objectValue1Uri || "
                + "?o1 = ?objectValue1Lit)}} . " + "{{?s ?p2 ?o2 . ?s ?predicateValue2 ?o2} . { ?s ?p2 ?o2 . "
                + "FILTER bif:contains(?o2, ?objectValue2)}}}");

        // assertEquals(query, "DEFINE input:inference'CRInferenceRule' select distinct ?s where { ?s ?p ?o  . {{?s ?p1 ?o1 . "
        // + "?s <http://rod.eionet.europa.eu/schema.rdf#obligation> ?o1} . { ?s ?p1 ?o1 . "
        // + "FILTER (?o1 = <http://rod.eionet.europa.eu/obligations/15> || "
        // + "?o1 = \"http://rod.eionet.europa.eu/obligations/15\")}} . "
        // + "{{?s ?p2 ?o2 . ?s <http://www.w3.org/2000/01/rdf-schema#label> ?o2} . { ?s ?p2 ?o2 . "
        // + "FILTER bif:contains(?o2, \"'CLRTAP'\")}}}");
    }

    @Test
    public void testQueryParameters() {
        PagingRequest pagingRequest = PagingRequest.create(1);
        SortingRequest sortingRequest = new SortingRequest(null, SortOrder.parse(SortOrder.ASCENDING.toString()));

        Map<String, String> filters = new HashMap<String, String>();
        // http://rod.eionet.europa.eu/schema.rdf#obligation=http://rod.eionet.europa.eu/obligations/15,
        // http://www.w3.org/2000/01/rdf-schema#label=CLRTAP}
        filters.put("http://rod.eionet.europa.eu/schema.rdf#obligation", "http://rod.eionet.europa.eu/obligations/15");
        filters.put("http://www.w3.org/2000/01/rdf-schema#label", "CLRTAP");
        filters.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://rod.eionet.europa.eu/schema.rdf#Delivery");
        VirtuosoFilteredSearchHelper helper = new VirtuosoFilteredSearchHelper(filters, null, pagingRequest, sortingRequest, true);

        ArrayList<Object> inParams = new ArrayList<Object>();
        String paramStr = helper.getQueryParameters(inParams);

        assertEquals(" . {{?s ?p1 ?o1 . ?s ?predicateValue1 ?o1} . { ?s ?p1 ?o1 . "
                + "FILTER (?o1 = ?objectValue1Uri || ?o1 = ?objectValue1Lit)}} "
                + ". {{?s ?p2 ?o2 . ?s ?predicateValue2 ?o2} . { ?s ?p2 ?o2 . "
                + "FILTER (?o2 = ?objectValue2Uri || ?o2 = ?objectValue2Lit)}} . "
                + "{{?s ?p3 ?o3 . ?s ?predicateValue3 ?o3} . { ?s ?p3 ?o3 . FILTER bif:contains(?o3, ?objectValue3)}}", paramStr);

        String query = helper.getUnorderedQuery(inParams);

        //checkQuery(query, helper.getQueryBindings());

        //
        // assertEquals(paramStr, " . {{?s ?p1 ?o1 . ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?o1} . { ?s ?p1 ?o1 . "
        // +"FILTER (?o1 = <http://rod.eionet.europa.eu/schema.rdf#Delivery> || ?o1 = \"http://rod.eionet.europa.eu/schema.rdf#Delivery\")}} "
        // +". {{?s ?p2 ?o2 . ?s <http://rod.eionet.europa.eu/schema.rdf#obligation> ?o2} . { ?s ?p2 ?o2 . "
        // +"FILTER (?o2 = <http://rod.eionet.europa.eu/obligations/15> || ?o2 = \"http://rod.eionet.europa.eu/obligations/15\")}} . "
        // +"{{?s ?p3 ?o3 . ?s <http://www.w3.org/2000/01/rdf-schema#label> ?o3} . { ?s ?p3 ?o3 . FILTER bif:contains(?o3, \"'CLRTAP'\")}}");
    }

    private void checkQuery(String query, Bindings bindings) {
        RepositoryConnection conn = null;
        try {
            conn = SesameConnectionProvider.getReadOnlyRepositoryConnection();
            SingleObjectReader<String> reader = new SingleObjectReader<String>();

            SesameUtil.executeQuery(query, bindings, reader, conn);

            List<String> results = reader.getResultList();

            for (String url : results) {
                System.out.println("URL " + url);
            }
        } catch (Exception e) {
            System.out.println("ERROR " + e);
        } finally {
            SesameUtil.close(conn);
        }

    }

    @Test
    public void testDateSorting() {
        PagingRequest pagingRequest = PagingRequest.create(1);
        SortingRequest sortingRequest =
            new SortingRequest("http://cr.eionet.europa.eu/ontologies/contreg.rdf#contentLastModified",
                    SortOrder.parse(SortOrder.DESCENDING.toString()));

        Map<String, String> filters = new HashMap<String, String>();
        filters.put("http://www.w3.org/2000/01/rdf-schema#label", "CLRTAP");
        VirtuosoFilteredSearchHelper helper = new VirtuosoFilteredSearchHelper(filters, null, pagingRequest, sortingRequest, true);

        ArrayList<Object> inParams = new ArrayList<Object>();

        String query = helper.getOrderedQuery(inParams);
        assertEquals(
                "DEFINE input:inference'CRInferenceRule' select distinct ?s max(?time) AS ?oorderby where {graph ?g { ?s ?p ?o  . {{?s ?p1 ?o1 . ?s ?predicateValue1 ?o1} . { ?s ?p1 ?o1 . FILTER bif:contains(?o1, ?objectValue1)}} . OPTIONAL {?g ?sortPredicateValue ?time}}} ORDER BY desc(?oorderby)",
                query);
    }

    @Test
    public void testOrderedQueryIncludingLabel() {
        PagingRequest pagingRequest = PagingRequest.create(1);
        SortingRequest sortingRequest =
            new SortingRequest("http://www.w3.org/2000/01/rdf-schema#label", SortOrder.parse(SortOrder.ASCENDING.toString()));

        Map<String, String> filters = new HashMap<String, String>();
        filters.put("http://www.w3.org/2000/01/rdf-schema#label", "ippc");
        VirtuosoFilteredSearchHelper helper = new VirtuosoFilteredSearchHelper(filters, null, pagingRequest, sortingRequest, true);

        ArrayList<Object> inParams = new ArrayList<Object>();

        String query = helper.getOrderedQuery(inParams);

        assertEquals("DEFINE input:inference'CRInferenceRule' select distinct ?s where { ?s ?p ?o  "
                + ". {{?s ?p1 ?oorderby . ?s ?predicateValue1 ?oorderby} . { ?s ?p1 ?oorderby . FILTER bif:contains(?oorderby, "
                + "?objectValue1)}}} ORDER BY asc(bif:either( bif:isnull(?oorderby) , "
                + "(bif:lcase(bif:subseq (bif:replace (?s, '/', '#'), bif:strrchr (bif:replace (?s, '/', '#'), '#')+1))) "
                + ", bif:lcase(?oorderby)))", query);
        //
        // assertEquals(query, "DEFINE input:inference'CRInferenceRule' select distinct ?s where { ?s ?p ?o  . {{?s ?p1 ?o1 . "
        // + "?s <http://purl.org/dc/elements/1.1/creator> ?o1} . { ?s ?p1 ?o1 . FILTER bif:contains(?o1, \"'Roug'\")}} . "
        // + "OPTIONAL {?s <http://www.w3.org/2000/01/rdf-schema#label> ?oorderby }} ORDER BY "
        // + "desc(bif:either( bif:isnull(?oorderby) , (bif:lcase(bif:subseq (bif:replace (?s, '/', '#'), "
        // + "bif:strrchr (bif:replace (?s, '/', '#'), '#')+1))) , bif:lcase(?oorderby)))");
    }

    @Test
    public void testOrderedQueryIncludingLabelNoInference() {
        PagingRequest pagingRequest = PagingRequest.create(1);
        SortingRequest sortingRequest =
            new SortingRequest("http://www.w3.org/2000/01/rdf-schema#label", SortOrder.parse(SortOrder.ASCENDING.toString()));

        Map<String, String> filters = new HashMap<String, String>();
        filters.put("http://www.w3.org/2000/01/rdf-schema#label", "ippc");
        VirtuosoFilteredSearchHelper helper = new VirtuosoFilteredSearchHelper(filters, null, pagingRequest, sortingRequest, false);
        helper.setUseInferencing(false);
        ArrayList<Object> inParams = new ArrayList<Object>();

        String query = helper.getOrderedQuery(inParams);

        assertEquals("select distinct ?s where { ?s ?p ?o  "
                + ". {{?s ?p1 ?oorderby . ?s ?predicateValue1 ?oorderby} . { ?s ?p1 ?oorderby . FILTER bif:contains(?oorderby, "
                + "?objectValue1)}}} ORDER BY asc(bif:either( bif:isnull(?oorderby) , "
                + "(bif:lcase(bif:subseq (bif:replace (?s, '/', '#'), bif:strrchr (bif:replace (?s, '/', '#'), '#')+1))) "
                + ", bif:lcase(?oorderby)))", query);
    }

}
