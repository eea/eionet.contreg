package eionet.cr.web.sparqlClient.helpers;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.OpenRDFException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.http.HTTPRepository;

/**
 *
 * @author jaanus
 *
 */
public class QueryExecutor {

    /** */
    private static Log logger = LogFactory.getLog(QueryExecutor.class);

    /** */
    private QueryResult results;
    private long executionTime;

    /**
     *
     */
    public QueryExecutor(){
    }

    /**
     *
     * @param endpoint
     * @param query
     * @throws OpenRDFException
     */
    public void executeQuery(String endpoint, String query) throws OpenRDFException{

        HTTPRepository httpRepository = null;
        RepositoryConnection conn = null;
        TupleQueryResult queryResult = null;
        try {
            httpRepository =  new HTTPRepository(endpoint, "");
            httpRepository.initialize();
            conn = httpRepository.getConnection();

            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);

            long startTime = System.currentTimeMillis();
            queryResult = tupleQuery.evaluate();
            executionTime = System.currentTimeMillis() - startTime;

            if (queryResult==null || !queryResult.hasNext()){
                logger.info("The query gave no results");
            }
            else{
                results = new QueryResult(queryResult);
            }
        }
        finally {
            try{
                if (queryResult!=null){
                    queryResult.close();
                }
            }
            catch (OpenRDFException e){}

            try{
                if (conn!=null){
                    conn.close();
                }
            }
            catch (OpenRDFException e){}

            try{
                if (httpRepository!=null){
                    httpRepository.shutDown();
                }
            }
            catch (OpenRDFException e){}
        }
    }

    /** */
    private static final String exploreQueryTempl = "SELECT DISTINCT ?subj ?pred ?obj WHERE {\n" +
            " {?subj ?pred ?obj . FILTER (?subj = <@exploreSubject@>) . }\n" +
            " UNION {?subj ?pred ?obj . FILTER (?obj = <@exploreSubject@> ) . }\n} LIMIT 50";
    /**
     *
     * @param endpoint
     * @param exploreSubject
     * @throws OpenRDFException
     */
    public String executeExploreQuery(String endpoint, String exploreSubject) throws OpenRDFException{

        String exploreQuery = StringUtils.replace(exploreQueryTempl, "@exploreSubject@", exploreSubject);
        executeQuery(endpoint, exploreQuery);
        return exploreQuery;
    }

    /**
     *
     * @return
     */
    public QueryResult getResults(){
        return results;
    }

    /**
     *
     * @return
     */
    public long getExecutionTime() {
        return executionTime;
    }
}
