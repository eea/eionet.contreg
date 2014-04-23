package eionet.cr.harvest;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dao.virtuoso.VirtuosoBaseDAO;
import eionet.cr.dto.FactsheetDTO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.test.helpers.CRDatabaseTestCase;

/**
 * Test that links and display values of blank nodes are properly returned in a factsheet, whether expanded or collapsed.
 *
 * @author Jaanus
 */
public class FactsheetMultipleBNodesTest extends CRDatabaseTestCase {

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.test.helpers.CRDatabaseTestCase#getRDFXMLSeedFiles()
     */
    @Override
    protected List<String> getRDFXMLSeedFiles() {
        return Arrays.asList("blank_nodes.rdf");
    }

    /**
     *
     * @throws Exception
     */
    public void test() throws Exception {

        String subjectUri = "http://example.org/Person#John";
        String predicateUri = "http://xmlns.com/foaf/0.1/knows";
        HelperDAO helperDao = DAOFactory.get().getDao(HelperDAO.class);

        // Try expanded first.

        HashMap<String, Integer> predicatePages = new HashMap<String, Integer>();
        predicatePages.put(predicateUri, Integer.valueOf(1));

        FactsheetDTO factsheet = helperDao.getFactsheet(subjectUri, null, predicatePages);
        assertNotNull("Expected a factsheet", factsheet);
        assertEquals("Unexpected factsheet subject", subjectUri, factsheet.getUri());

        Collection<ObjectDTO> objects = factsheet.getObjects(predicateUri);
        assertTrue("Expected 3 objects for " + predicateUri, objects != null && objects.size() == 3);

        ObjectDTO objectDTO = objects.iterator().next();
        testBlankObjectResource(objectDTO);

        // Now try collapsed.

        factsheet = helperDao.getFactsheet(subjectUri, null, null);
        assertNotNull("Expected a factsheet", factsheet);
        assertEquals("Unexpected factsheet subject", subjectUri, factsheet.getUri());

        objects = factsheet.getObjects(predicateUri);
        assertTrue("Expected 1 objects for " + predicateUri, objects != null && objects.size() == 1);

        objectDTO = objects.iterator().next();
        testBlankObjectResource(objectDTO);
    }

    /**
     *
     * @param objectDTO
     */
    private void testBlankObjectResource(ObjectDTO objectDTO) {

        assertNotNull(objectDTO);
        String value = objectDTO.getValue();
        String displayValue = objectDTO.getDisplayValue();
        assertNotNull(value);
        assertNotNull(displayValue);
        assertTrue("Unexpected start of blank node uri", value.startsWith(VirtuosoBaseDAO.VIRTUOSO_BNODE_PREFIX));
        assertTrue("Unexpected start of blank node displayValue", displayValue.startsWith("b"));
    }
}
