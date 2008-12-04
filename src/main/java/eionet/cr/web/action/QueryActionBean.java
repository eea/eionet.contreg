package eionet.cr.web.action;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import eionet.cr.search.Searcher;


/**
 * @author altnyris
 *
 */
@UrlBinding("/luceneQuery.action")
public class QueryActionBean extends AbstractCRActionBean {
	
	/** */
	private String query;
	private String analyzer;
	
	/** */
	private List hits;
	private List<String> analyzers;
	
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	@DefaultHandler
	public Resolution init() throws Exception {
		return new ForwardResolution("/pages/query.jsp");
	}

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
    public Resolution search() throws Exception {
		
		HttpServletRequest request = getContext().getRequest();
		String analyzer = request.getParameter("analyzer");
		
		if (analyzer==null || analyzer.trim().length()==0)
			hits = Searcher.luceneQuery(query);
		else
			hits = Searcher.luceneQuery(query, analyzer);
		
		request.setAttribute("hits", hits);
		request.setAttribute("analyzer", analyzer);
		
		showMessage(String.valueOf(hits==null ? 0 : hits.size()) + " hits found");
        return new ForwardResolution("/pages/query.jsp");
    }

    /**
     * 
     * @return
     */
    public List<String> getAnalyzers(){
    	if (analyzers==null){
    		analyzers = new ArrayList<String>();
    		analyzers.add(StandardAnalyzer.class.getName());
    		analyzers.add(KeywordAnalyzer.class.getName());
    	}
    	return analyzers;
    }

	/**
	 * 
	 * @return
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * @param query
	 */
	public void setQuery(String query) {
		this.query = query;
	}
	
	/**
	 * @return
	 */
	public List getHits() {
		return hits;
	}

	/**
	 * @return the analyzer
	 */
	public String getAnalyzer() {
		return analyzer;
	}

	/**
	 * @param analyzer the analyzer to set
	 */
	public void setAnalyzer(String analyzer) {
		this.analyzer = analyzer;
	}
}

