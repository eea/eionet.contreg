/*
 * The contents of this file are subject uri the Mozilla Public
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationMethod;
import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.search.CustomSearch;
import eionet.cr.search.ReferencesSearch;
import eionet.cr.search.SearchException;
import eionet.cr.search.SimpleSearch;
import eionet.cr.search.util.SearchExpression;
import eionet.cr.search.util.UriLabelPair;
import eionet.cr.util.Hashes;
import eionet.cr.util.URIUtil;
import eionet.cr.web.util.columns.ReferringPredicatesColumn;
import eionet.cr.web.util.columns.SearchResultColumn;
import eionet.cr.web.util.columns.SubjectPredicateColumn;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
@UrlBinding("/references.action")
public class ReferencesActionBean extends AbstractSearchActionBean<SubjectDTO> {
	
	/** */
	private static final String REFERRING_PREDICATES = ReferencesActionBean.class.getName() + ".referringPredicates";
	private static final String PREV_OBJECT = ReferencesActionBean.class.getName() + ".previousObject";

	/** */
	private String object;
	private SearchExpression searchExpression;
	
	/** */
	private HashMap<String,List<String>> referringPredicates;
	
	/** */
	private Map<String,String> predicateLabels;
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.web.action.AbstractSearchActionBean#search()
	 */
	public Resolution search() throws SearchException {

		if (!StringUtils.isBlank(object)){
			
			searchExpression = new SearchExpression(object);
			if (searchExpression.isUri() || searchExpression.isHash()){
				
				ReferencesSearch refSearch = new ReferencesSearch(searchExpression); // validation assures that object!=null
				refSearch.setPageNumber(getPageN());
				refSearch.setSorting(getSortP(), getSortO());
				
				refSearch.execute();
				resultList = refSearch.getResultList();
				matchCount = refSearch.getTotalMatchCount();
				
				predicateLabels = refSearch.getPredicateLabels().getByLanguagePreferences(createPreferredLanguages(), "en");
			}
			else{
				searchExpression = null;
				handleCrException("The provided resource identifier is neither a URI nor a hash!", GeneralConfig.SEVERITY_CAUTION);
			}
		}
		else{
			object = null;
			handleCrException("Resource identifier not specified!", GeneralConfig.SEVERITY_CAUTION);
		}
		
		return new ForwardResolution("/pages/references.jsp");
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.web.action.AbstractSearchActionBean#getColumns()
	 */
	public List<SearchResultColumn> getColumns() throws SearchException {
		
		ArrayList<SearchResultColumn> list = new ArrayList<SearchResultColumn>();

		/* let's always include rdf:type and rdfs:label in the columns */

		SubjectPredicateColumn predCol = new SubjectPredicateColumn();
		predCol.setPredicateUri(Predicates.RDF_TYPE);
		predCol.setTitle("Type");
		predCol.setSortable(true);
		list.add(predCol);

		predCol = new SubjectPredicateColumn();
		predCol.setPredicateUri(Predicates.RDFS_LABEL);
		predCol.setTitle("Title");
		predCol.setSortable(true);
		list.add(predCol);
		
		SearchResultColumn col = new ReferringPredicatesColumn(this);
		col.setTitle("Relationship");
		col.setSortable(true);
		list.add(col);		
		
		return list;
	}

	/**
	 * 
	 * @param object
	 */
	public void setObject(String object) {
		this.object = object;
	}

	/**
	 * @return the searchExpression
	 */
	public SearchExpression getSearchExpression() {
		return searchExpression;
	}

	/**
	 * 
	 * @return
	 */
	public String getObject() {
		return object;
	}

	/**
	 * @return the predicateLabels
	 */
	public Map<String, String> getPredicateLabels() {
		return predicateLabels;
	}
}
