package eionet.repgen;

import org.apache.commons.lang.StringUtils;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.QueryResult;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;

public class SPARQLUtil {

    // FIXME - to be propertized
    private static String testEndpointURL = "http://test.tripledev.ee/cr/sparql";
    private static String endpointURL = "https://cr.eionet.europa.eu/sparql";

    private static SPARQLRepository crEndpoint;
    private static SPARQLRepository testEndpoint;

    static {
        crEndpoint = new SPARQLRepository(endpointURL);
        testEndpoint = new SPARQLRepository(testEndpointURL);
        try {
            crEndpoint.initialize();
            testEndpoint.initialize();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    /**
     * Search XML files through CR SPARQL endpoint.
     *
     * @return the list of CR file objects
     * @throws DCMException
     */
    public static String getSparqlBookmarkByName(String name) throws Exception {

        RepositoryConnection conn = null;

        try {

            // FIXME get sparql bookmarks from *production* repository
            conn = testEndpoint.getConnection();

            // TODO CR url is hardcoded
            String query =
                    "select  ?o where {?s ?p ?o . ?s a <http://cr.eionet.europa.eu/ontologies/contreg.rdf#SparqlBookmark> . "
                    + "?s rdfs:label ?lbl .  filter (?p = <http://cr.eionet.europa.eu/ontologies/contreg.rdf#sparqlQuery>) . "
                            + "filter (str(?lbl)='" + name + "') }";

            TupleQuery q = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
            TupleQueryResult bindings = q.evaluate();
            if (bindings.hasNext()) {
                BindingSet b = bindings.next();
                String bookMarkSparql = b.getBinding("o").getValue().stringValue();
                return bookMarkSparql;
            }

        } catch (Exception e) {

            Logger.error("Operation failed while searching XML files from Content Registry. The following error was reported:\n"
                    + e.toString());
            throw new Exception("Error getting data from Content Registry " + e.toString());
        } finally {
            try {
                conn.close();
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Returns SPARQL query results.
     *
     * @param sparql
     * @return
     * @throws Exception
     */
    public static QueryResult<BindingSet> getSparqlQueryResult(String sparql) throws Exception {

        RepositoryConnection conn = null;
        TupleQueryResult bindings = null;

        try {

            conn = crEndpoint.getConnection();
            TupleQuery q = conn.prepareTupleQuery(QueryLanguage.SPARQL, sparql);
            bindings = q.evaluate();
            Logger.log("Bindings got, size: " + bindings.getBindingNames().size());
        } catch (Throwable e) {
            Logger.error("Operation failed while executing sparql. The following error was reported:\n" + e.toString());

            throw new Exception("Error getting data from Content Registry " + e.toString());
        } finally {
            try {
                conn.close();
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
        }
        return bindings;
    }

    // TODO handle different - obj, literals etc
    public static String replaceSparqlParam(String sparql, String paramName, String value) {
        return StringUtils.replace(sparql, "?" + paramName, "'" + value + "'");
    }

    // TODO handle different - obj, literals etc
    public static String replaceJasperParam(String sparql, String paramName, String value) {
        return StringUtils.replace(sparql, "$P{" + paramName + "}", "'" + value + "'");
    }
}
