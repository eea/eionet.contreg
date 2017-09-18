/**
 *
 */
package eionet.cr.harvest;

import java.util.Arrays;
import java.util.List;

import eionet.cr.ApplicationTestContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.test.helpers.CRDatabaseTestCase;
import eionet.cr.test.helpers.RdfLoader;
import eionet.cr.util.sesame.SesameConnectionProvider;
import eionet.cr.util.sesame.SesameUtil;
import org.junit.Ignore;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Risto Alt
 *
 */
//@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationTestContext.class })
public class InferencingIT extends CRDatabaseTestCase {

    /** Rule-set seed file. */
    private static final String RULESET_SEED_FILE = "test-schema.rdf";

    /** Data seed file. */
    private static final String DATA_SEED_FILE = "persons.rdf";

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.test.helpers.CRDatabaseTestCase#getRDFXMLSeedFiles()
     */
    @Override
    protected List<String> getRDFXMLSeedFiles() {
        return Arrays.asList(RULESET_SEED_FILE, DATA_SEED_FILE);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        String rulesetGraphUri = RdfLoader.getSeedFileGraphUri(RULESET_SEED_FILE);
        DAOFactory.get().getDao(HarvestSourceDAO.class).removeSourceFromInferenceRule(rulesetGraphUri);
        DAOFactory.get().getDao(HarvestSourceDAO.class).addSourceIntoInferenceRule(rulesetGraphUri);
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
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
