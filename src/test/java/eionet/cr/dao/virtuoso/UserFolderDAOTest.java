package eionet.cr.dao.virtuoso;

import java.util.ArrayList;
import eionet.cr.ApplicationTestContext;
import org.junit.Test;
import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.dao.virtuoso.helpers.VirtuosoUserFolderSearchHelper;
import eionet.cr.util.SortOrder;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.util.sesame.SPARQLQueryUtil;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * user folder test.
 *
 * @author kaido
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationTestContext.class })
public class UserFolderDAOTest {

    /** test count query. */
    @Test
    public void testCountQuery() {
        PagingRequest pagingRequest = PagingRequest.create(1);
        SortingRequest sortingRequest = new SortingRequest(null, SortOrder.parse(SortOrder.ASCENDING.toString()));
        VirtuosoUserFolderSearchHelper helper =
            new VirtuosoUserFolderSearchHelper("http://127.0.0.1:8080/cr/home", pagingRequest, sortingRequest);

        String query = helper.getCountQuery(new ArrayList<Object>());
        assertEquals(
                "SELECT count(distinct(?s)) WHERE {?s ?p ?o . ?s a <http://cr.eionet.europa.eu/ontologies/contreg.rdf#UserFolder>}",
                query);
    }

    /** test unordered query. */
    @Test
    public void testUnorderedQuery() {
        String parentFolder = "http://127.0.0.1:8080/cr/home";
        PagingRequest pagingRequest = PagingRequest.create(1);
        SortingRequest sortingRequest = new SortingRequest(null, SortOrder.parse(SortOrder.ASCENDING.toString()));
        VirtuosoUserFolderSearchHelper helper = new VirtuosoUserFolderSearchHelper(parentFolder, pagingRequest, sortingRequest);

        String query = helper.getUnorderedQuery(new ArrayList<Object>());
        assertEquals("SELECT ?parent ?subject bif:either( bif:isnull(?lbl) , "
                + "?subject, ?lbl) as ?label ?fileCount ?folderCount WHERE {?subject a <" + Subjects.CR_USER_FOLDER
                + "> .?parent ?hasPredicate ?subject .  FILTER (?hasPredicate IN (<"
                + Predicates.CR_HAS_FILE + ">, <" + Predicates.CR_HAS_FOLDER + ">)) .  FILTER(?parent= ?parentFolder)  "
                + "{ SELECT ?subject (count(?file) AS ?fileCount)  WHERE { ?subject <" + Predicates.CR_HAS_FILE
                +  "> ?file    } GROUP BY ?subject}  { SELECT ?subject (count(?folder) AS ?folderCount)  WHERE "
                + "{ ?subject <"  + Predicates.CR_HAS_FOLDER +  "> ?folder     } GROUP BY ?subject}OPTIONAL "
                + "{  {SELECT ?subject ?lbl WHERE { ?subject rdfs:label ?lbl }  }  }} GROUP BY ?subject ", query);

        assertTrue(helper.getQueryBindings().toString().indexOf("parentFolder=" + parentFolder) != -1);
    }

}
