/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti
 */
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

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.search.SearchException;
import eionet.cr.search.SimpleSearch;
import eionet.cr.search.UriSearch;
import eionet.cr.search.util.SearchExpression;
import eionet.cr.web.util.columns.SearchResultColumn;
import eionet.cr.web.util.columns.SubjectPredicateColumn;

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
    public Resolution search() throws SearchException{
		
    	SearchExpression searchExpression = new SearchExpression(this.searchExpression);
    	if (!searchExpression.isEmpty()){
    		
	    	if (searchExpression.isUri()){
	    		
	    		UriSearch uriSearch = new UriSearch(searchExpression.toString());
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
	 * @see eionet.cr.web.action.AbstractSearchActionBean#getColumns()
	 */
	public List<SearchResultColumn> getColumns(){
		return getDefaultColumns();
	}
}
