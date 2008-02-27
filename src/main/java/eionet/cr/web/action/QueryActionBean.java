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
@UrlBinding("/query.action")
public class QueryActionBean extends AbstractCRActionBean {
	private String query;
	private List hits;

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}
	
	public List getHits() {
		return hits;
	}

	public void setHits(List hits) {
		this.hits = hits;
	}

	/**
	 * 
	 * @return
	 * @throws DAOException
	 */
	@DefaultHandler
    public Resolution search() throws Exception {
		//this.hits = Searcher.search(query);
		HttpServletRequest request = getContext().getRequest();
		String analyzer = request.getParameter("analyzer");
		
		if (analyzer==null || analyzer.trim().length()==0)
			hits = Searcher.search(query);
		else
			hits = Searcher.search(query, analyzer);
		
		request.setAttribute("hits", hits);
		request.setAttribute("analyzer", analyzer);
        return new ForwardResolution("/pages/query.jsp");
    }

	
}

