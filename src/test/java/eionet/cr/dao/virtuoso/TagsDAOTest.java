package eionet.cr.dao.virtuoso;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.MockVirtuosoBaseDAOTest;
import eionet.cr.dao.readers.TagCloudReader;
import eionet.cr.dto.TagDTO;

public class TagsDAOTest extends MockVirtuosoBaseDAOTest {

    private static final String FILENAME = "tagscount-triples.nt";

    public TagsDAOTest() {
        super(FILENAME);
    }

    @Test
    public void testTagsCloud() {
        String sparql =
                "define input:inference 'CRInferenceRule' SELECT ?o (count(?o) as ?c) WHERE { ?s <http://cr.eionet.europa.eu/ontologies/contreg.rdf#tag> ?o } ORDER BY DESC(?c)";
        TagCloudReader reader = new TagCloudReader();
        List<TagDTO> results = null;
        try {
            results = executeSPARQL(sparql, reader);
        } catch (DAOException e) {
            e.printStackTrace();
        }
        assertNotSame(results.get(0), results.get(1));

    }

    @Test
    public void testTagsCloudOrder() {
        String sparql =
                "define input:inference 'CRInferenceRule' SELECT ?o (count(?o) as ?c) WHERE { ?s <http://cr.eionet.europa.eu/ontologies/contreg.rdf#tag> ?o } ORDER BY DESC(?c)";
        TagCloudReader reader = new TagCloudReader();
        List<TagDTO> results = null;
        try {
            results = executeSPARQL(sparql, reader);
        } catch (DAOException e) {
            e.printStackTrace();
        }
        int firstCount = results.get(0).getCount();
        int lastCount = results.get(results.size() - 1).getCount();
        assertTrue(firstCount >= lastCount);
    }

}
