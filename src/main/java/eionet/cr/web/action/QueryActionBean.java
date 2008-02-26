package eionet.cr.web.action;

import java.util.List;

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
		getContext().getRequest().setAttribute("hits", Searcher.search(query));
        return new ForwardResolution("/pages/query.jsp");
    }

	
}

