package eionet.cr.web.action;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import eionet.cr.dao.DAOException;
import eionet.cr.index.Searcher;


/**
 * @author altnyris
 *
 */
@UrlBinding("/luceneQuery.action")
public class QueryActionBean extends AbstractCRActionBean {
	
	/** */
	private String query;
	
	/** */
	private List hits;

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
	 * @param hits
	 */
	public void setHits(List hits) {
		this.hits = hits;
	}

	/**
	 * 
	 * @return
	 * @throws DAOException
	 */
	/**
	 * @return
	 * @throws Exception
	 */
	@DefaultHandler
    public Resolution search() throws Exception {
		//this.hits = Searcher.search(query);
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

	
}

