package eionet.cr.dao.virtuoso.helpers;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sparql.SPARQLRepository;

import eionet.cr.web.sparqlClient.helpers.QueryResult;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tieto.com">Jaanus Heinlaid</a>
 * 
 */
public class QueryExecutor {

    /** */
    private static Log logger = LogFactory.getLog(QueryExecutor.class);

    /** */
    private QueryResult results;

    /**
     *
     */
    public QueryExecutor() {
    }

    /**
     * 
     * @param endpoint
     * @param query
     */
    public void executeQuery(String endpoint, String query) {

        RepositoryConnection conn = null;
        try {
            SPARQLRepository repo = new SPARQLRepository(endpoint);
            repo.initialize();

            conn = repo.getConnection();

            TupleQuery q = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
            TupleQueryResult bindings = q.evaluate();

            if (bindings == null || !bindings.hasNext()) {
                logger.info("The query gave no results");
            } else {
                results = new QueryResult(bindings, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    logger.info("Failed to close QueryExecution object: " + e.toString());
                }
            }
        }
    }

    /**
     * 
     * @param endpoint
     * @param query
     * @return boolean
     */
    public boolean executeASKQuery(String endpoint, String query) {

        RepositoryConnection conn = null;
        boolean ret = false;
        try {
            SPARQLRepository repo = new SPARQLRepository(endpoint);
            repo.initialize();

            conn = repo.getConnection();

            BooleanQuery resultsTableBoolean = conn.prepareBooleanQuery(QueryLanguage.SPARQL, query);
            Boolean result = resultsTableBoolean.evaluate();

            ret = result.booleanValue();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    logger.info("Failed to close RepositoryConnection object: " + e.toString());
                }
            }
        }

        return ret;
    }

    /** */
    private static final String EXPLORE_QUERY_TEMPL = "SELECT DISTINCT ?subj ?pred ?obj WHERE {\n"
            + " {?subj ?pred ?obj . FILTER (?subj = <@exploreSubject@>) . }\n"
            + " UNION {?subj ?pred ?obj . FILTER (?obj = <@exploreSubject@> ) . }\n} LIMIT 50";

    /**
     * 
     * @param endpoint
     * @param exploreSubject
     * @return String
     */
    public String executeExploreQuery(String endpoint, String exploreSubject) {

        String exploreQuery = StringUtils.replace(EXPLORE_QUERY_TEMPL, "@exploreSubject@", exploreSubject);
        executeQuery(endpoint, exploreQuery);
        return exploreQuery;
    }

    /**
     * 
     * @return QueryResult
     */
    public QueryResult getResults() {
        return results;
    }
}
