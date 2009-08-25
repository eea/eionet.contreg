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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.commons.lang.StringUtils;

import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.search.CustomSearch;
import eionet.cr.search.SearchException;
import eionet.cr.search.util.SortOrder;
import eionet.cr.search.util.UriLabelPair;
import eionet.cr.web.util.columns.SearchResultColumn;
import eionet.cr.web.util.columns.SubjectPredicateColumn;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
@UrlBinding("/typeSearch.action")
public class TypeSearchActionBean extends AbstractSearchActionBean<SubjectDTO>{
	
	/** */
	private static final String FORM_PAGE = "/pages/typeSearch.jsp";
	
	/** */
	private static final String PICKLIST = TypeSearchActionBean.class.getName() + ".picklist";
	private static final String PROPERTIES = TypeSearchActionBean.class.getName() + ".properties";
	private static final String PREV_TYPE = TypeSearchActionBean.class.getName() + ".prevType";
	
	/** */
	private String type;

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.web.action.AbstractSearchActionBean#search()
	 */
	public Resolution search() throws SearchException {
		
		if (!StringUtils.isBlank(type)){
			
			Map<String,String> criteria = new HashMap<String,String>();
			criteria.put(Predicates.RDF_TYPE, type);
			
			CustomSearch customSearch = new CustomSearch(criteria);
			customSearch.setPageNumber(getPageN());
			customSearch.setSorting(getSortP(), getSortO());

			customSearch.execute();
			
			resultList = customSearch.getResultList();
    		matchCount = customSearch.getTotalMatchCount();
		}
		
		return new ForwardResolution(FORM_PAGE);
	}

	/**
	 * 
	 * @return
	 * @throws SearchException
	 */
	public Resolution introspect() throws SearchException {
		
		if (!StringUtils.isBlank(type)){			
			return new RedirectResolution(FactsheetActionBean.class).addParameter("uri", type);
		}
		else
			return new ForwardResolution(FORM_PAGE);
	}
	
	/**
	 * 
	 * @return
	 */
	@DefaultHandler
	public Resolution unspecified(){
		
		clearSession();
		return new ForwardResolution(FORM_PAGE);
	}
	
	/**
	 * 
	 */
	private void clearSession(){
		
		HttpSession session = getContext().getRequest().getSession();
		session.removeAttribute(PICKLIST);
		session.removeAttribute(PROPERTIES);
		session.removeAttribute(PREV_TYPE);
	}
	
	/**
	 * @throws SearchException 
	 * 
	 */
	public List<UriLabelPair> getPicklist() throws SearchException{
		
		List<UriLabelPair> picklist = (List<UriLabelPair>)getContext().getRequest().getSession().getAttribute(PICKLIST);
		if (picklist==null || picklist.isEmpty()){
			
			Map<String,String> criteria = new HashMap<String,String>();
			criteria.put(Predicates.RDF_TYPE, Subjects.RDFS_CLASS);
			
			CustomSearch customSearch = new CustomSearch(criteria);
			customSearch.setSorting(Predicates.RDFS_LABEL, SortOrder.ASCENDING);
			customSearch.setPageLength(0); // we want no limits on result set size
			customSearch.execute();
			Collection<SubjectDTO> subjects = customSearch.getResultList();
			if (subjects!=null){
				
				picklist = new ArrayList<UriLabelPair>();
				for (Iterator<SubjectDTO> it=subjects.iterator(); it.hasNext();){
					SubjectDTO subject = it.next();
					if (!subject.isAnonymous()){
						String label = subject.getObjectValue(Predicates.RDFS_LABEL);
						if (!StringUtils.isBlank(label)){
							picklist.add(UriLabelPair.create(subject.getUri(), label));
						}
					}
				}
			}
			
			if (picklist!=null && !picklist.isEmpty()){
				getContext().getRequest().getSession().setAttribute(PICKLIST, picklist);
			}
		}
		
		return picklist;
	}
	
	/**
	 * 
	 * @return
	 * @throws SearchException 
	 */
	private List<UriLabelPair> getProperties() throws SearchException{
		
		// take out the previous type, replace it with the new one
		HttpSession session = getContext().getRequest().getSession();
		String previousType = (String)session.getAttribute(PREV_TYPE);
		session.setAttribute(PREV_TYPE, type);
		if (previousType==null)
			previousType = "";
		
		// if submitted type is blank anyway, there's nothing to do here
		if (StringUtils.isBlank(type))
			return null;
		
		// find properties only if none in the session or type differs from previous
		
		List<UriLabelPair> properties = (List<UriLabelPair>)session.getAttribute(PROPERTIES);
		if (properties==null || properties.isEmpty() || !previousType.equals(type)){						
			
			Map<String,String> criteria = new HashMap<String,String>();
			criteria.put(Predicates.RDF_TYPE, Subjects.RDF_PROPERTY);
			criteria.put(Predicates.RDFS_DOMAIN, type);
			
			CustomSearch customSearch = new CustomSearch(criteria);
			customSearch.setPageLength(0); // no limits to the result set size, because there cannot be too many properties for a type
			customSearch.execute();
			
			Collection<SubjectDTO> subjects = customSearch.getResultList();
			if (subjects!=null){
				
				properties = new ArrayList<UriLabelPair>();
				for (Iterator<SubjectDTO> it=subjects.iterator(); it.hasNext();){
					
					SubjectDTO subject = it.next();
					if (!subject.isAnonymous()){
						
						String uri = subject.getUri();
						if (!uri.equals(Predicates.RDFS_LABEL)){ // we skip label, because we include by default later
							
							String label = subject.getObjectValue(Predicates.RDFS_LABEL);
							if (!StringUtils.isBlank(label)){
								properties.add(UriLabelPair.create(uri, label));
							}
						}
					}
				}
			}
			
			if (properties!=null && !properties.isEmpty()){
				getContext().getRequest().getSession().setAttribute(PROPERTIES, properties);
			}
		}
		
		return properties;
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.web.action.AbstractSearchActionBean#getColumns()
	 */
	public List<SearchResultColumn> getColumns() throws SearchException {
		
		ArrayList<SearchResultColumn> list = new ArrayList<SearchResultColumn>();

		// let's always include rdfs:label in the columns
		SubjectPredicateColumn col = new SubjectPredicateColumn();
		col.setPredicateUri(Predicates.RDFS_LABEL);
		col.setTitle("Title");
		col.setSortable(true);
		list.add(col);
		
		// query the rest of the columns
		
		List<UriLabelPair> properties = getProperties();
		if (properties!=null && !properties.isEmpty()){
			
			int i=0;
			for (Iterator<UriLabelPair> it=properties.iterator(); i<4 && it.hasNext();i++){ // only interested in first 4 columns
				
				UriLabelPair property = it.next();
				
				col = new SubjectPredicateColumn();
				col.setPredicateUri(property.getUri());
				col.setTitle(property.getLabel());
				col.setSortable(true);
				list.add(col);
			}
		}

		return list;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
}
