package eionet.cr.dao;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.test.helpers.CRDatabaseTestCase;
import eionet.cr.util.Pair;
import eionet.cr.util.pagination.PagingRequest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

/**
 * Unit tests for operations with {@link SourceDeletionsDAO}.
 *
 * @author Jaanus
 */
@SqlGroup({
        @Sql({"/source-deletions.sql"}),
        @Sql(scripts = "/source-deletions-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
})
public class SourceDeletionsDAOIT extends CRDatabaseTestCase {

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.test.helpers.CRDatabaseTestCase#getXMLDataSetFiles()
     */
    @Override
    protected List<String> getXMLDataSetFiles() {
        return Arrays.asList("source-deletions.xml");
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.test.helpers.CRDatabaseTestCase#getRDFXMLSeedFiles()
     */
    @Override
    protected List<String> getRDFXMLSeedFiles() {
        return Arrays.asList("source-deletions.rdf");
    }

    /**
     * Testing the marking of provided URLs for deletion.
     *
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testMarkForDeletion() throws Exception {

        HarvestSourceDAO harvestSourceDao = DAOFactory.get().getDao(HarvestSourceDAO.class);
        Pair<Integer, List<HarvestSourceDTO>> sources = harvestSourceDao.getHarvestSources(null, null, null);
        assertNotNull("Expected non-null harvest sources result set", sources);
        assertNotNull("Expected non-null count of returned harvest sources", sources.getLeft());
        assertEquals("Unexpected count of harvest sources", 5, sources.getLeft().intValue());

        List<String> sourceUrls =
                Arrays.asList("http://rod.eionet.europa.eu/obligations", "http://rod.eionet.europa.eu/countries",
                        "http://rod.eionet.europa.eu/instruments", "http://www.eionet.europa.eu/seris/rdf",
                        "http://localhost:8080/cr/pages/test.xml", "http://additional.com/1", "http://additional.com/2");

        SourceDeletionsDAO sourceDeletionsDao = DAOFactory.get().getDao(SourceDeletionsDAO.class);
        int updateCount = sourceDeletionsDao.markForDeletion(sourceUrls);
        assertEquals("Unexpected update count", sourceUrls.size(), updateCount);

        sources = harvestSourceDao.getHarvestSources(null, null, null);
        assertNotNull("Expected non-null harvest sources result set", sources);
        assertNotNull("Expected non-null count of returned harvest sources", sources.getLeft());
        assertEquals("Unexpected count of harvest sources", 0, sources.getLeft().intValue());
    }

    /**
     * Testing the marking of SPARQL-returned URLs for deletion.
     *
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testMarkForDeletionSparqlUrls() throws Exception {

        // Prepare SPARQL that will query for URLs to be queued.

        String sparql = "PREFIX cd: <http://www.recshop.fake/cd#> select distinct ?s where {?s a cd:Record} order by ?s";
        int expectedNumberOfSparqlReturnedSubjects = 2;
        int expectedInitialNumberOfHarvestSources = 5;

        // Queue the SPARQL results.

        SourceDeletionsDAO sourceDeletionsDao = DAOFactory.get().getDao(SourceDeletionsDAO.class);
        int updateCount = sourceDeletionsDao.markForDeletionSparql(sparql);

        // Test number of URLs queued.

        assertEquals("Unexpected update count", expectedNumberOfSparqlReturnedSubjects, updateCount);

        // Test number of URLs in harvest sources table.

        HarvestSourceDAO harvestSourceDao = DAOFactory.get().getDao(HarvestSourceDAO.class);
        Pair<Integer, List<HarvestSourceDTO>> sources = harvestSourceDao.getHarvestSources(null, null, null);
        assertNotNull("Expected non-null harvest sources result set", sources);
        assertNotNull("Expected non-null count of returned harvest sources", sources.getLeft());

        assertEquals("Unexpected count of harvest sources", expectedInitialNumberOfHarvestSources, sources.getLeft().intValue());

        // Test the size and contents of un-filtered deletion queue.

        List<Pair<String, Date>> deletionQueue = sourceDeletionsDao.getDeletionQueue(null, null).getRight();
        assertNotNull("Expected non-null deletion queue", deletionQueue);
        assertEquals("Unexpect size of deletion queue", expectedNumberOfSparqlReturnedSubjects, deletionQueue.size());

        List<String> expectedUrls =
                Arrays.asList("http://www.recshop.fake/cd/Empire%20Burlesque", "http://www.recshop.fake/cd/Hide%20your%20heart");
        HashSet<String> queuedUrls = new HashSet<String>();
        for (Pair<String, Date> pair : deletionQueue) {
            queuedUrls.add(pair.getLeft());
        }
        assertEquals("Unexpected deletion queue", new HashSet<String>(expectedUrls), queuedUrls);

        // Test the size and contents of the filtered deletion queue.

        deletionQueue = sourceDeletionsDao.getDeletionQueue("Burlesque", null).getRight();
        assertNotNull("Expected non-null deletion queue", deletionQueue);
        assertEquals("Unexpect size of deletion queue", 1, deletionQueue.size());
        queuedUrls = new HashSet<String>();
        for (Pair<String, Date> pair : deletionQueue) {
            queuedUrls.add(pair.getLeft());
        }
        expectedUrls = Arrays.asList("http://www.recshop.fake/cd/Empire%20Burlesque");
        assertEquals("Unexpected deletion queue", new HashSet<String>(expectedUrls), queuedUrls);

        // Test the size and contents of un-filtered paged deletion queue.

        deletionQueue = sourceDeletionsDao.getDeletionQueue(null, PagingRequest.create(1, 1)).getRight();
        assertNotNull("Expected non-null deletion queue", deletionQueue);
        assertEquals("Unexpect size of deletion queue", 1, deletionQueue.size());
        queuedUrls = new HashSet<String>();
        for (Pair<String, Date> pair : deletionQueue) {
            queuedUrls.add(pair.getLeft());
        }
        expectedUrls = Arrays.asList("http://www.recshop.fake/cd/Empire%20Burlesque");
        assertEquals("Unexpected deletion queue", new HashSet<String>(expectedUrls), queuedUrls);
    }

    /**
     * Testing the marking of SPARQL-returned URLs for deletion, but not returning any URLs actually.
     *
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testMarkForDeletionSparqlNonUrls() throws Exception {

        String sparql = "PREFIX cd: <http://www.recshop.fake/cd#> select distinct ?o where {?s cd:artist ?o} order by ?o";

        SourceDeletionsDAO sourceDeletionsDao = DAOFactory.get().getDao(SourceDeletionsDAO.class);
        int updateCount = sourceDeletionsDao.markForDeletionSparql(sparql);
        assertEquals("Unexpected update count", 0, updateCount);

        HarvestSourceDAO harvestSourceDao = DAOFactory.get().getDao(HarvestSourceDAO.class);
        Pair<Integer, List<HarvestSourceDTO>> sources = harvestSourceDao.getHarvestSources(null, null, null);
        assertNotNull("Expected non-null harvest sources result set", sources);
        assertNotNull("Expected non-null count of returned harvest sources", sources.getLeft());

        int expectedInitialNumberOfHarvestSources = 5;
        assertEquals("Unexpected count of harvest sources", expectedInitialNumberOfHarvestSources, sources.getLeft().intValue());
    }

    /**
     * Testing the cancellation of deletions.
     *
     * @throws Exception
     */
    @Test
    public void testCancellation() throws Exception {

        List<String> initialUrls =
                Arrays.asList("http://rod.eionet.europa.eu/obligations", "http://rod.eionet.europa.eu/countries",
                        "http://rod.eionet.europa.eu/instruments", "http://www.eionet.europa.eu/seris/rdf",
                        "http://localhost:8080/cr/pages/test.xml", "http://additional.com/1", "http://additional.com/2");

        SourceDeletionsDAO sourceDeletionsDao = DAOFactory.get().getDao(SourceDeletionsDAO.class);
        int updateCount = sourceDeletionsDao.markForDeletion(initialUrls);
        assertEquals("Unexpected update count", initialUrls.size(), updateCount);

        int cancelledCount =
                sourceDeletionsDao.unmarkForDeletion(Arrays.asList("http://rod.eionet.europa.eu/obligations",
                        "http://rod.eionet.europa.eu/countries"));
        assertEquals("Unexpected cancelled deletions count", 2, cancelledCount);

        List<Pair<String, Date>> deletionQueue = sourceDeletionsDao.getDeletionQueue(null, null).getRight();
        assertNotNull("Expected non-null deletion queue", deletionQueue);
        assertEquals("Unexpect size of deletion queue", 5, deletionQueue.size());

        List<String> expectedUrls =
                Arrays.asList("http://rod.eionet.europa.eu/instruments", "http://www.eionet.europa.eu/seris/rdf",
                        "http://localhost:8080/cr/pages/test.xml", "http://additional.com/1", "http://additional.com/2");
        HashSet<String> queuedUrls = new HashSet<String>();
        for (Pair<String, Date> pair : deletionQueue) {
            queuedUrls.add(pair.getLeft());
        }
        assertEquals("Unexpected deletion queue", new HashSet<String>(expectedUrls), queuedUrls);
    }

    /**
     * Test picking the first URL from deletion queue (on a FIFO basis).
     *
     * @throws Exception
     */
    @Test
    public void testPicking() throws Exception {

        List<String> initialUrls =
                Arrays.asList("http://rod.eionet.europa.eu/obligations", "http://rod.eionet.europa.eu/countries",
                        "http://rod.eionet.europa.eu/instruments");
        Collections.sort(initialUrls);
        assertEquals("Unexpected element at list 1st position", "http://rod.eionet.europa.eu/countries", initialUrls.get(0));
        assertEquals("Unexpected element at list 2nd position", "http://rod.eionet.europa.eu/instruments", initialUrls.get(1));
        assertEquals("Unexpected element at list 3rd position", "http://rod.eionet.europa.eu/obligations", initialUrls.get(2));

        SourceDeletionsDAO dao = DAOFactory.get().getDao(SourceDeletionsDAO.class);
        int updateCount = dao.markForDeletion(initialUrls);
        assertEquals("Unexpected update count", initialUrls.size(), updateCount);

        assertEquals("Unexpected URL", "http://rod.eionet.europa.eu/countries", dao.pickForDeletion());
        dao.unmarkForDeletion(Arrays.asList("http://rod.eionet.europa.eu/countries"));
        assertEquals("Unexpected URL", "http://rod.eionet.europa.eu/instruments", dao.pickForDeletion());
        dao.unmarkForDeletion(Arrays.asList("http://rod.eionet.europa.eu/instruments"));
        assertEquals("Unexpected URL", "http://rod.eionet.europa.eu/obligations", dao.pickForDeletion());
        dao.unmarkForDeletion(Arrays.asList("http://rod.eionet.europa.eu/obligations"));
        assertNull("Expected no more sources in deletion queue", dao.pickForDeletion());

        List<Pair<String, Date>> deletionQueue = dao.getDeletionQueue(null, null).getRight();
        assertNotNull("Expected non-null deletion queue", deletionQueue);
        assertEquals("Unexpect size of deletion queue", 0, deletionQueue.size());
    }
}
