/**
 *
 */
package eionet.cr.harvest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.test.helpers.RdfLoader;
import eionet.cr.util.sesame.SesameConnectionProvider;
import eionet.cr.util.sesame.SesameUtil;

/**
 * @author Risto Alt
 *
 */
public class InferencingTest {

    /** Rule-set seed file. */
    private static final String RULESET_SEED_FILE = "test-schema.rdf";

    /** Data seed file. */
    private static final String DATA_SEED_FILE = "persons.rdf";

    /**
     * Test set-up method.
     *
     * @throws Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        RdfLoader rdfLoader = new RdfLoader();
        rdfLoader.clearAllTriples();

        // Load inference rule-set.
        rdfLoader.loadIntoTripleStore(RULESET_SEED_FILE, RDFFormat.RDFXML);
        String rulesetGraphUri = RdfLoader.getSeedFileGraphUri(RULESET_SEED_FILE);
        DAOFactory.get().getDao(HarvestSourceDAO.class).removeSourceFromInferenceRule(rulesetGraphUri);
        DAOFactory.get().getDao(HarvestSourceDAO.class).addSourceIntoInferenceRule(rulesetGraphUri);

        // Load the data upon which we shall try the above-loaded rule-set.
        rdfLoader.loadIntoTripleStore("persons.rdf", RDFFormat.RDFXML);
    }

    /**
     *
     * @throws Exception
     *
     * @Deprecated Inferencing is removed from CR
     */
    @Deprecated
    @Test
    public void testInverseOf() throws Exception {
        RepositoryConnection con = null;

        // run this test only if inversion is turned on
        if (GeneralConfig.isUseInferencing()) {
            String query =
                    " PREFIX test: <http://test.com/test/test-schema.rdf#>" + " SELECT ?s"
                            + " FROM <http://test.com/test/persons.rdf>" + " WHERE" + "{"
                            + "?s test:hasParent <http://test.com/test/person/1>" + "}";
            try {
                con = SesameConnectionProvider.getRepositoryConnection();
                TupleQuery resultsTable = con.prepareTupleQuery(QueryLanguage.SPARQL, query);
                assertNotNull(resultsTable);
                TupleQueryResult queryResult = resultsTable.evaluate();
                assertNotNull(queryResult);
                assertTrue(queryResult.hasNext());

            } finally {
                SesameUtil.close(con);
            }
        }
    }

}
