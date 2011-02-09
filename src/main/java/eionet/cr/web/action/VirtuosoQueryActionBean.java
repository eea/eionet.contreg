package eionet.cr.web.action;

import java.util.HashMap;
import java.util.Iterator;

import com.hp.hpl.jena.query.ResultSet;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.web.sparqlClient.helpers.QueryResult;
import eionet.cr.web.sparqlClient.helpers.ResultValue;

@UrlBinding("/virtuosoQuery.action")
public class VirtuosoQueryActionBean extends AbstractActionBean {
	
	private String query;
	
	/**
	 * 
	 * @return
	 * @throws DAOException TODO
	 */
	@DefaultHandler
	public Resolution view() throws DAOException {
		return new ForwardResolution("/pages/virtuosoQuery.jsp");
	}
	
	public StreamingResolution query() throws Exception {
		
		StringBuffer ret = new StringBuffer();
		
		if(query == null)
			query = "";
		
		String repoUrl = GeneralConfig.getProperty("virtuoso.db.url");
		String repoUsr = GeneralConfig.getProperty("virtuoso.db.username");
		String repoPwd = GeneralConfig.getProperty("virtuoso.db.password");
		
		VirtGraph set = new VirtGraph(repoUrl, repoUsr, repoPwd);
		
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (query, set);
		long start = System.currentTimeMillis();
		ResultSet rs = vqe.execSelect();
		long end = System.currentTimeMillis();
		QueryResult results = new QueryResult(rs);
		
		long executionTime = end - start;
		
		if(results != null && results.getRows() != null && results.getVariables() != null){
			ret.append("<table class=\"datatable\">");
			ret.append("<thead>");
			for(Iterator<String> cols = results.getVariables().iterator(); cols.hasNext(); ){
				String col = cols.next();
				ret.append("<th>").append(col).append("</th>");
			}
			ret.append("</thead>");
			ret.append("<tbody>");
			int cnt = 0;
			for(Iterator<HashMap<String, ResultValue>> it = results.getRows().iterator(); it.hasNext(); ){
				HashMap<String, ResultValue> row = it.next();
				if(cnt % 2 == 0)
					ret.append("<tr class=\"odd\">");
				else
					ret.append("<tr class=\"even\">");
				
				for(Iterator<String> it2 = results.getVariables().iterator(); it2.hasNext(); ){
					String col = it2.next();
					ResultValue val = row.get(col);
					ret.append("<td>");
					ret.append(val.getValue());
					ret.append("</td>");
				}
				ret.append("<tr>");
				cnt++;
			}
			ret.append("</tbody>");
			ret.append("</table>");
			ret.append("<br/> Done, -- ").append(executionTime).append(" msec.");
		} else {
			ret.append("The query gave no results!");
		}

		return new StreamingResolution("text/html", ret.toString());
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}
	
	

}
