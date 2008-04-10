package eionet.cr.web.action;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.lucene.queryParser.ParseException;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.index.Searcher;
import eionet.cr.util.DefaultColumnList;
import eionet.cr.util.Util;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
@UrlBinding("/simpleSearch.action")
public class SimpleSearchActionBean extends AbstractCRActionBean {
	
	/** */
	private String searchExpression;
	private List<Map<String,String>> hits;
	
	@DefaultHandler
	public Resolution init(){
		return new ForwardResolution("/pages/simpleSearch.jsp");
	}

	/**
	 * 
	 * @return
	 * @throws IOException 
	 * @throws ParseException 
	 * @throws DAOException
	 */
	@DefaultHandler
    public Resolution doSimpleSearch() throws ParseException, IOException{
		
		List<Map<String,String[]>> hits = Searcher.simpleSearch(searchExpression);
		this.hits = Util.listForDisplay(hits);
		
		getContext().getRequest().setAttribute("hits", this.hits);
		getContext().getRequest().setAttribute("collist", new DefaultColumnList());		
		
		showMessage(String.valueOf(hits==null ? 0 : hits.size()) + " hits found");
		
		return new ForwardResolution("/pages/simpleSearch.jsp");
    }

	/**
	 * @return the searchExpression
	 */
	public String getSearchExpression() {
		return searchExpression;
	}

	/**
	 * @param searchExpression the searchExpression to set
	 */
	public void setSearchExpression(String searchExpression) {
		this.searchExpression = searchExpression;
	}

	/**
	 * @return the hits
	 */
	public List<Map<String, String>> getHits() {
		return hits;
	}

	/**
	 * @param hits the hits to set
	 */
	public void setHits(List<Map<String, String>> hits) {
		this.hits = hits;
	}

}
