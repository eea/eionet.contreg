package eionet.cr.web.action;

import java.io.IOException;
import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.queryParser.ParseException;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.search.SearchException;
import eionet.cr.search.SimpleSearch;
import eionet.cr.search.UriSearch;
import eionet.cr.search.util.SearchExpression;
import eionet.cr.web.util.search.SearchResultColumn;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
@UrlBinding("/simpleSearch.action")
public class SimpleSearchActionBean extends AbstractSubjectSearchActionBean {
	
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
    public Resolution search() throws SearchException{
		
    	SearchExpression searchExpression = new SearchExpression(this.searchExpression);
    	if (!searchExpression.isEmpty()){
	    	if (searchExpression.isUri()){
	    		
	    		UriSearch uriSearch = new UriSearch(searchExpression);
	    		uriSearch.setPageNumber(getPageN());
	    		uriSearch.setSorting(getSortP(), getSortO());
	    		
	    		uriSearch.execute();
	    		resultList = uriSearch.getResultList();
	    		matchCount = uriSearch.getTotalMatchCount();
	    	}
	    	
	    	if (resultList==null || resultList.size()==0){
	    		
	    		SimpleSearch simpleSearch = new SimpleSearch(searchExpression);
	    		simpleSearch.setPageNumber(getPageN());
	    		simpleSearch.setSorting(getSortP(), getSortO());
	    		
	    		simpleSearch.execute();
	    		resultList = simpleSearch.getResultList();
	    		matchCount = simpleSearch.getTotalMatchCount();
	    	}
    	}
    	
		return new ForwardResolution("/pages/simpleSearch.jsp");
    }
    
    @ValidationMethod(on="search")
    public void validateSearch(ValidationErrors errors) {
        if (this.searchExpression == null || this.searchExpression.equals("")) {
            handleCrException(getBundle().getString("search.field.empty"), GeneralConfig.SEVERITY_CAUTION);
        }
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
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.web.action.AbstractSubjectSearchActionBean#getColumns()
	 */
	public List<SearchResultColumn> getColumns(){
		return getDefaultColumns();
	}
}
