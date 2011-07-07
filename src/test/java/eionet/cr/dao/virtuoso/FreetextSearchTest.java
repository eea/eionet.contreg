package eionet.cr.dao.virtuoso;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.MockVirtuosoBaseDAOTest;
import eionet.cr.dao.readers.FreeTextSearchReader;

/**
 * Tests Freetext search result processing.
 */
public class FreetextSearchTest extends MockVirtuosoBaseDAOTest {
    private static final String FILE_NAME = "test-freetext.nt";

    public FreetextSearchTest() {
        super(FILE_NAME);
    }

    @Test
    public void testFreetextQuery() {
        // dummy SQL just for understanding how the N3 file was generated
        String sparql = "select distinct ?s where { ?s ?p ?o .   FILTER bif:contains(?o, \"'GEMET'\")}";
        FreeTextSearchReader<String> reader = new FreeTextSearchReader<String>();
        List<String> results = null;
        try {
            results = executeSPARQL(sparql, reader);
        } catch (DAOException e) {
            e.printStackTrace();
        }
        assertTrue(results != null && results.size() == 6);
    }

}
