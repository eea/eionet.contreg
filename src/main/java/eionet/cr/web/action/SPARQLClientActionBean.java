package eionet.cr.web.action;

import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.commons.lang.StringUtils;

import eionet.cr.web.sparqlClient.helpers.QueryExecutor;
import eionet.cr.web.sparqlClient.helpers.QueryResult;
import eionet.cr.web.sparqlClient.helpers.SPARQLEndpoints;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tieto.com">Jaanus Heinlaid</a>
 *
 */
@UrlBinding("/sparqlClient.action")
public class SPARQLClientActionBean extends AbstractActionBean{
	
	/** */
	private static final String FORM_PAGE = "/pages/sparqlClient.jsp";
	
	/** */
	private String endpoint;
	private String query;
	private String explore;
	
	
	/** */
	private QueryResult result;
	
	/**
	 * 
	 * @return
	 */
	@DefaultHandler
	public Resolution execute(){
		
		if (!StringUtils.isBlank(endpoint)){
			
			if (!StringUtils.isBlank(explore)){
				QueryExecutor queryExecutor = new QueryExecutor();
				query = queryExecutor.executeExploreQuery(endpoint, explore);
				result = queryExecutor.getResults();
			}
			else if (!StringUtils.isBlank(query)){
				QueryExecutor queryExecutor = new QueryExecutor();
				queryExecutor.executeQuery(endpoint, query);
				result = queryExecutor.getResults();
			}
		}
		
		return new ForwardResolution(FORM_PAGE);
	}

	/**
	 * 
	 * @return
	 */
	public List<String> getEndpoints(){
		
		return SPARQLEndpoints.getInstance();
	}

	/**
	 * @return the endpoint
	 */
	public String getEndpoint() {
		return endpoint;
	}

	/**
	 * @param endpoint the endpoint to set
	 */
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	/**
	 * @return the query
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * @param query the query to set
	 */
	public void setQuery(String query) {
		this.query = query;
	}

	/**
	 * @return the result
	 */
	public QueryResult getResult() {
		return result;
	}

	/**
	 * @param explore the explore to set
	 */
	public void setExplore(String explore) {
		this.explore = explore;
	}

	/**
	 * @return the explore
	 */
	public String getExplore() {
		return explore;
	}
}
