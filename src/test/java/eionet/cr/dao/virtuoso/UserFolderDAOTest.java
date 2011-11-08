package eionet.cr.dao.virtuoso;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

import eionet.cr.common.Subjects;
import eionet.cr.dao.virtuoso.helpers.VirtuosoUserFolderSearchHelper;
import eionet.cr.util.SortOrder;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.pagination.PagingRequest;

public class UserFolderDAOTest {

    @Test
    public void testGetParameters() {
        PagingRequest pagingRequest = PagingRequest.create(1);
        SortingRequest sortingRequest = new SortingRequest(null, SortOrder.parse(SortOrder.ASCENDING.toString()));
        VirtuosoUserFolderSearchHelper helper =
            new VirtuosoUserFolderSearchHelper("http://127.0.0.1:8080/cr/home", pagingRequest, sortingRequest);

        String params = helper.getQueryParameters(new ArrayList<Object>());
        assertEquals(
                " . ?s a ?type . FILTER (?type  IN (?crFile, ?crUserFolder)) . ?parentFolder ?hasPredicate ?s . " +
                "FILTER (?hasPredicate IN (?hasFile, ?hasFolder))",
                params);

    }

    @Test
    public void testCountQuery() {
        PagingRequest pagingRequest = PagingRequest.create(1);
        SortingRequest sortingRequest = new SortingRequest(null, SortOrder.parse(SortOrder.ASCENDING.toString()));
        VirtuosoUserFolderSearchHelper helper =
            new VirtuosoUserFolderSearchHelper("http://127.0.0.1:8080/cr/home", pagingRequest, sortingRequest);

        String query = helper.getCountQuery(new ArrayList<Object>());
        assertEquals(
                "DEFINE input:inference'CRInferenceRule' select count(distinct ?s) where { ?s ?p ?o  . ?s a ?type . " +
                "FILTER (?type  IN (?crFile, ?crUserFolder)) . ?parentFolder ?hasPredicate ?s . FILTER (?hasPredicate IN (?hasFile, ?hasFolder))}",
                query);
    }

    @Test
    public void testUnorderedQuery() {
        String parentFolder = "http://127.0.0.1:8080/cr/home";
        PagingRequest pagingRequest = PagingRequest.create(1);
        SortingRequest sortingRequest = new SortingRequest(null, SortOrder.parse(SortOrder.ASCENDING.toString()));
        VirtuosoUserFolderSearchHelper helper = new VirtuosoUserFolderSearchHelper(parentFolder, pagingRequest, sortingRequest);

        String query = helper.getUnorderedQuery(new ArrayList<Object>());
        assertEquals(
                "DEFINE input:inference'CRInferenceRule' PREFIX cr: <http://cr.eionet.europa.eu/ontologies/contreg.rdf#> " +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> select distinct ?s where { ?s ?p ?o  . ?s a ?type . " +
                "FILTER (?type  IN (?crFile, ?crUserFolder)) . ?parentFolder ?hasPredicate ?s . " +
                "FILTER (?hasPredicate IN (?hasFile, ?hasFolder))}",
                query);
        assertTrue(helper.getQueryBindings().toString().indexOf("crUserFolder=" + Subjects.CR_USER_FOLDER) != -1);
        assertTrue(helper.getQueryBindings().toString().indexOf("parentFolder=" + parentFolder) != -1);
    }

}
