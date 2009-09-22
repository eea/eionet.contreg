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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eionet.cr.common.Predicates;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.HelperDao;
import eionet.cr.dao.mysql.MySQLDAOFactory;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.search.SearchException;
import eionet.cr.search.UriSearch;
import eionet.cr.search.util.SearchExpression;
import eionet.cr.search.util.SortOrder;
import eionet.cr.util.Pair;
import eionet.cr.util.SortingRequest;
import eionet.cr.web.util.columns.SearchResultColumn;
import eionet.cr.web.util.columns.SubjectLastModifiedColumn;
import eionet.cr.web.util.columns.SubjectPredicateColumn;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
@UrlBinding("/simpleSearch.action")
public class SimpleSearchActionBean extends AbstractSearchActionBean<SubjectDTO> {
	
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
	 * @throws DAOException 
	 * @throws IOException 
	 * @throws ParseException 
	 * @throws DAOException
	 */
    public Resolution search() throws SearchException{
		
		try {
			SearchExpression searchExpression = new SearchExpression(this.searchExpression);
			if (!searchExpression.isEmpty()) {

				if (searchExpression.isUri()) {

					UriSearch uriSearch = new UriSearch(searchExpression.toString());
					uriSearch.setPageNumber(getPageN());
					uriSearch.setSorting(getSortP(), getSortO());

					uriSearch.execute();
					resultList = uriSearch.getResultList();
					matchCount = uriSearch.getTotalMatchCount();
				}

				if (resultList == null || resultList.size() == 0) {
					HelperDao helperDao = MySQLDAOFactory.get().getDao(
							HelperDao.class);
					Pair<Integer, List<SubjectDTO>> result = helperDao
							.performSimpleSearch(
									searchExpression,
									getPageN(),
									new SortingRequest(getSortP(), SortOrder.parse(getSortO())));

					resultList = result.getValue();
					matchCount = result.getId();
				}
			}
		} catch (Exception fatal) {
			throw new SearchException("exception in simple search", fatal);
		}
    	
		return new ForwardResolution("/pages/simpleSearch.jsp");
    }
    
    @ValidationMethod(on="search")
    public void validateSearch(ValidationErrors errors) {
        if (this.searchExpression == null || this.searchExpression.equals("")) {
            addCautionMessage(getBundle().getString("search.field.empty"));
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
		
		ArrayList<SearchResultColumn> list = new ArrayList<SearchResultColumn>();
		
		SubjectPredicateColumn col = new SubjectPredicateColumn();
		col.setPredicateUri(Predicates.RDF_TYPE);
		col.setTitle("Type");
		col.setSortable(true);
		list.add(col);
		
		col = new SubjectPredicateColumn();
		col.setPredicateUri(Predicates.RDFS_LABEL);
		col.setTitle("Title");
		col.setSortable(true);
		list.add(col);

		SubjectLastModifiedColumn col2 = new SubjectLastModifiedColumn();
		col2.setTitle("Date");
		col2.setSortable(true);
		list.add(col2);

		return list;
	}
}
