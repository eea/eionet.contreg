/**
 *
 */
package eionet.cr.harvest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.test.helpers.RdfLoader;
import eionet.cr.util.sesame.SPARQLQueryUtil;
import eionet.cr.util.sesame.SesameConnectionProvider;
import eionet.cr.util.sesame.SesameUtil;

/**
 * @author Risto Alt
 *
 */
public class InferencingTest {

    private static RdfLoader loader;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // Load schema
        loader = new RdfLoader("test-schema.rdf");
        String schemaUri = loader.getGraphUri();
        if (!StringUtils.isBlank(schemaUri)) {
            DAOFactory.get().getDao(HarvestSourceDAO.class).removeSourceFromInferenceRule(schemaUri);
            DAOFactory.get().getDao(HarvestSourceDAO.class).addSourceIntoInferenceRule(schemaUri);
        }

        // Load data
        loader = new RdfLoader("persons.rdf");
    }

    @Test
    public void testInverseOf() throws Exception {
        RepositoryConnection con = null;

        //run this test only if inversion is turned on
        if (GeneralConfig.isUseInferencing()){
            String query =
                SPARQLQueryUtil.getCrInferenceDefinitionStr() + " PREFIX test: <http://test.com/test/test-schema.rdf#>"
                + " SELECT ?s" + " FROM <http://test.com/test/persons.rdf>" + " WHERE" + "{"
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
