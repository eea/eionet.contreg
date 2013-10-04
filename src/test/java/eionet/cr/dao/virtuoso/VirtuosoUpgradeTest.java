package eionet.cr.dao.virtuoso;

import java.util.Arrays;
import java.util.List;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.FactsheetDTO;
import eionet.cr.test.helpers.CRDatabaseTestCase;

/**
 * Unit tests for SPARQL and SQL queries that are potentially sensitive to Virtuoso upgrade.
 *
 * @author Jaanus
 */
public class VirtuosoUpgradeTest extends CRDatabaseTestCase {

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.test.helpers.CRDatabaseTestCase#getRDFXMLSeedFiles()
     */
    @Override
    protected List<String> getRDFXMLSeedFiles() {
        return Arrays.asList("rdf_national_chars_utf8.rdf.xml", "obligations.rdf", "persons.rdf", "tags.rdf");
    }

    /**
     * Factsheet query is VERY complex. Test that it doesn't throw any exceptions.
     * The query uses the following Virtuoso functions and/or Virtuoso impementations of standard SPARQL functions:
     * from standard SPARQL:
     * - min()
     * - isBlank()
     * - lang()
     * - str()
     * - datatype()
     * - iri()
     * from Virtuoso:
     * - bif:either()
     * - bif:substring()
     * - bif:concat()
     * - bif:length()
     * - bif:md5()
     * - bif:left()
     * - bif:coalesce()
     * - type casting like xsd:int(some_string)
     *
     *
     * @throws DAOException If query fails.
     */
    public void testFactsheetQuery() throws DAOException {

        // "select ?pred min(xsd:int(isBlank(?s))) as ?anonSubj "
        // + "min(bif:either(isLiteral(?o),"
        // +
        // "bif:concat(bif:substring(str(?o),1,LEN),'<|>',lang(?o),'<|>',str(datatype(?o)),'<|><|>0<|>',str(?g),'<|>',str(bif:length(str(?o))),'<|>',bif:md5(str(?o))),"
        // +
        // "bif:concat(bif:coalesce(str(?oLabel),bif:left(str(?o),LEN)),'<|>',lang(?oLabel),'<|>',str(datatype(?oLabel)),'<|>',bif:left(str(?o),LEN),'<|>',str(isBlank(?o)),'<|>',str(?g),'<|><|>')"
        // + ")) as ?objData " + "count(distinct ?o) as ?objCount " + "where {" + "graph ?g {"
        // + "?s ?pred ?o. filter(?s=iri(?subjectUri))}" + ". optional {?o <" + Predicates.RDFS_LABEL + "> ?oLabel}"
        // + "} group by ?pred";

        String subjectUri = "http://rod.eionet.europa.eu/obligations/171";
        FactsheetDTO factsheet = DAOFactory.get().getDao(HelperDAO.class).getFactsheet(subjectUri, null, null);

        assertNotNull("Was expecting a factsheet for " + subjectUri, factsheet);
        assertEquals("Was expecting factsheet URI to be same as " + subjectUri, subjectUri, factsheet.getUri());

        int predicateCount = factsheet.getPredicateCount();
        System.out.println("predicateCount = " + predicateCount);
        assertTrue("Was expecting a factsheet with at least 1 triple", predicateCount > 0);
    }
}