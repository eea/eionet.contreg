package eionet.cr.web.action;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.queryParser.ParseException;
import eionet.cr.dao.DAOException;
import eionet.cr.search.Searcher;
import eionet.cr.web.util.DefaultSearchResultColumnList;
import eionet.cr.web.util.DisplayUtil;
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
public class SimpleSearchActionBean extends AbstractSearchActionBean {
	
	/** */
	private static Log logger = LogFactory.getLog(SimpleSearchActionBean.class);
	
	/** */
	private String searchExpression;
	
	/**
	 * 
	 * @return
	 */
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
    public Resolution search() throws ParseException, IOException{
		
		resultList = DisplayUtil.listForDisplay(Searcher.simpleSearch(searchExpression));
		//showMessage(String.valueOf(resultList==null ? 0 : resultList.size()) + " hits found");
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
}
