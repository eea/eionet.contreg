package eionet.cr.web.sparqlClient.helpers;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;

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
	 */
	public void executeQuery(String endpoint, String query){
				
		QueryExecution queryExecution = null;
		try{
			queryExecution = QueryExecutionFactory.sparqlService(endpoint, query);
			long start = System.currentTimeMillis();
			ResultSet rs = queryExecution.execSelect();
			long end = System.currentTimeMillis();
			
			executionTime = end - start;

			if (rs==null || !rs.hasNext()){
				logger.info("The query gave no results");
			}
			else{
//				ResultSetFormatter.outputAsXML(System.out, rs);
				results = new QueryResult(rs);
			}
		}
		finally{
			if (queryExecution!=null){
				try{
					queryExecution.close();
				}
				catch (Exception e){
					logger.info("Failed to close QueryExecution object: " + e.toString());
				}
			}
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
	 */
	public String executeExploreQuery(String endpoint, String exploreSubject){
		
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

	public long getExecutionTime() {
		return executionTime;
	}
}
