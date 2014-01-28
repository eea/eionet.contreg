package eionet.cr.dao;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.test.helpers.CRDatabaseTestCase;
import eionet.cr.util.Hashes;
import eionet.cr.util.Pair;

/**
 * Unit tests for operations with {@link SourceDeletionsDAO}.
 *
 * @author Jaanus
 */
public class SourceDeletionsDAOTest extends CRDatabaseTestCase {

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.test.helpers.CRDatabaseTestCase#getXMLDataSetFiles()
     */
    @Override
    protected List<String> getXMLDataSetFiles() {
        return Arrays.asList("source-deletions.xml");
    }

    /**
     * Test marking for deletion.
     *
     * @throws Exception
     */
    @Test
    public void testMarkForDeletion() throws Exception {

        List<String> sourceUrls = Arrays.asList("http://rod.eionet.europa.eu/obligations", "http://rod.eionet.europa.eu/countries",
                "http://rod.eionet.europa.eu/instruments", "http://www.eionet.europa.eu/seris/rdf",
                "http://localhost:8080/cr/pages/test.xml", "http://additional.com/1", "http://additional.com/2");

        for (String string : sourceUrls) {
            System.out.println(string + " = " + Hashes.spoHash(string));
        }

        SourceDeletionsDAO sourceDeletionsDao = DAOFactory.get().getDao(SourceDeletionsDAO.class);
        int updateCount = sourceDeletionsDao.markForDeletion(sourceUrls);
        assertEquals("Unexpected update count", sourceUrls.size(), updateCount);

        HarvestSourceDAO harvestSourceDao = DAOFactory.get().getDao(HarvestSourceDAO.class);
        Pair<Integer, List<HarvestSourceDTO>> sources = harvestSourceDao.getHarvestSources(null, null, null);
        assertNotNull("Expected non-null harvest sources result set", sources);
        assertNotNull("Expected non-null count of returned harvest sources", sources.getLeft());
        assertEquals("Unexpected count of harvest sources", sourceUrls.size(), sources.getLeft().intValue());
    }
}
