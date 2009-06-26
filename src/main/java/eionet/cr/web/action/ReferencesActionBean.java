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

import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationMethod;
import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.search.CustomSearch;
import eionet.cr.search.ReferringPredicatesSearch;
import eionet.cr.search.SearchException;
import eionet.cr.search.SimpleSearch;
import eionet.cr.search.util.SearchExpression;
import eionet.cr.search.util.UriLabelPair;
import eionet.cr.util.URIUtil;
import eionet.cr.web.util.search.PredicateBasedColumn;
import eionet.cr.web.util.search.SearchResultColumn;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
@UrlBinding("/references.action")
public class ReferencesActionBean extends AbstractSearchActionBean{
	
	/** */
	private static final String REFERRING_PREDICATES = ReferencesActionBean.class.getName() + ".referringPredicates";
	private static final String PREV_OBJECT = ReferencesActionBean.class.getName() + ".previousObject";

	/** */
	private String object;
	private SearchExpression searchExpression;
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.web.action.AbstractSearchActionBean#search()
	 */
	public Resolution search() throws SearchException {

		searchExpression = new SearchExpression(object);
		SimpleSearch simpleSearch = new SimpleSearch(searchExpression); // validation assures that object!=null
		simpleSearch.setPageNumber(getPageN());
		simpleSearch.setSorting(getSortP(), getSortO());
		
		simpleSearch.execute();
		resultList = simpleSearch.getResultList();
		matchCount = simpleSearch.getTotalMatchCount();
		
		return new ForwardResolution("/pages/references.jsp");
	}
	
	/**
	 * 
	 * @return
	 * @throws SearchException
	 */
	private List<UriLabelPair> getReferringPredicates() throws SearchException{
		
		/* remove the previous object, replace it with the new one */
		
		HttpSession session = getContext().getRequest().getSession();
		String previousObject = (String)session.getAttribute(PREV_OBJECT);
		session.setAttribute(PREV_OBJECT, object);
		if (previousObject==null)
			previousObject = "";
		
		/* if submitted object is blank anyway, there's nothing to do here */
		
		if (StringUtils.isBlank(object))
			return null;
		
		/* find referring predicates only if none in the session or object differs from previous one */
		
		List<UriLabelPair> predicates = (List<UriLabelPair>)session.getAttribute(REFERRING_PREDICATES);
		if (predicates==null || predicates.isEmpty() || !previousObject.equals(object)){						
			
			ReferringPredicatesSearch search = new ReferringPredicatesSearch(object);
			search.setPageLength(0); // no limits to the result set size, we want all the referring predicates
			search.execute();
			
			Collection<SubjectDTO> list = search.getResultList();
			if (list!=null){
				
				predicates = new ArrayList<UriLabelPair>();
				for (Iterator<SubjectDTO> it=list.iterator(); it.hasNext();){
					
					SubjectDTO predicate = it.next();
					if (!predicate.isAnonymous()){
						
						String uri = predicate.getUri();
						String label = predicate.getObjectValue(Predicates.RDFS_LABEL);
						if (!StringUtils.isBlank(label)){
							predicates.add(UriLabelPair.create(uri, label));
						}
					}
				}
			}
			
			if (predicates!=null && !predicates.isEmpty()){
				getContext().getRequest().getSession().setAttribute(REFERRING_PREDICATES, predicates);
			}
		}
		
		return predicates;
	}

	/**
	 * 
	 * @param errors
	 */
    @ValidationMethod(on="search")
    public void validateSearch(ValidationErrors errors) {
    	
        if (StringUtils.isBlank(object)) {
            handleCrException("No search criteria specified!", GeneralConfig.SEVERITY_CAUTION);
        }
    }

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.web.action.AbstractSearchActionBean#getColumns()
	 */
	public List<SearchResultColumn> getColumns() throws SearchException {
		
		ArrayList<SearchResultColumn> list = new ArrayList<SearchResultColumn>();

		/* let's always include rdf:type and rdfs:label in the columns */

		PredicateBasedColumn col = new PredicateBasedColumn();
		col.setPredicateUri(Predicates.RDF_TYPE);
		col.setTitle("Type");
		col.setSortable(true);
		list.add(col);

		col = new PredicateBasedColumn();
		col.setPredicateUri(Predicates.RDFS_LABEL);
		col.setTitle("Title");
		col.setSortable(true);
		list.add(col);
		
		// query the rest of the columns
		
		List<UriLabelPair> referringPredicates = getReferringPredicates();
		if (referringPredicates!=null && !referringPredicates.isEmpty()){
			
			int i=0;
			for (Iterator<UriLabelPair> it=referringPredicates.iterator(); i<4 && it.hasNext();i++){ // only interested in first 4
				
				UriLabelPair predicate = it.next();
				
				String uri = predicate.getUri();
				if (uri!=null && !uri.equals(Predicates.RDF_TYPE) && !uri.equals(Predicates.RDFS_LABEL)){
					col = new PredicateBasedColumn();
					col.setPredicateUri(predicate.getUri());
					col.setTitle(predicate.getLabel());
					col.setSortable(true);
					list.add(col);
				}
			}
		}

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
}
