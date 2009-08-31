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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HandlesEvent;
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
import eionet.cr.util.Pair;
import eionet.cr.util.Util;
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
	
	private static final String AVAILABLE_TYPES_CACHE = TypeSearchActionBean.class.getName() + ".picklist";
	private static final String SELECTED_COLUMNS_CACHE = TypeSearchActionBean.class.getName() + ".storedSelectedColumns";
	private static final String AVAILABLE_COLUMNS_CACHE = TypeSearchActionBean.class.getName() + ".availableColumnsCache";
	private static final int MAX_DISPLAYED_COLUMNS = 4;
	
	/** */
	private String type;
	
	private List<String> selectedColumns;
	private List<Pair<String,String>> availableColumns;
	private List<Pair<String,String>> availableTypes;
	
	/**
	 * 
	 * @return
	 */
	@DefaultHandler
	public Resolution defaultHandler() throws SearchException {
		//get all available types and cache them;
		availableTypes = getTypesFromCacheOrGenerate();
		
		return new ForwardResolution(FORM_PAGE);
	}
	
	@HandlesEvent("setSearchColumns")
	public Resolution setSearchColumns() throws SearchException {
		if (selectedColumns != null) {
			Map<String,List<String>> cache = (Map<String, List<String>>) getSession().getAttribute(SELECTED_COLUMNS_CACHE);
			if (cache == null) {
				cache = new HashMap<String, List<String>>();
			}
			//cannot use sublist here, as it's not Serializable
			// and we're storing it in the Session.
			List<String> temp = new LinkedList<String>();
			int i = 0;
			for (String selectedColumn : selectedColumns) {
				temp.add(selectedColumn);
				if (++i == MAX_DISPLAYED_COLUMNS) {
					break;
				}
			}
			selectedColumns = temp;
			cache.put(type, selectedColumns);
			getSession().setAttribute(SELECTED_COLUMNS_CACHE, cache);
		}
		return new RedirectResolution(getUrlBinding() + "?search=Search&type=" + Util.urlEncode(type));
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.web.action.AbstractSearchActionBean#search()
	 */
	public Resolution search() throws SearchException {
		availableTypes = getTypesFromCacheOrGenerate();
		if (!StringUtils.isBlank(type)){
			//get available columns for search
			availableColumns = getAvailableColumns(type);
			//check if user has previously selected some columns.
			Map<String,List<String>> cache = (Map<String, List<String>>) getSession().getAttribute(SELECTED_COLUMNS_CACHE);
			if (cache != null && cache.containsKey(type) ) {
				selectedColumns = cache.get(type);
			} else {
				selectedColumns = new LinkedList<String>();
				int i = 0;
				for (Pair<String,String> pair : availableColumns){
					if(i++ == MAX_DISPLAYED_COLUMNS || Predicates.RDF_TYPE.equals(pair.getId())) {
						break;
					}
					selectedColumns.add(pair.getId());
					
				}
			}
								
			//perform the search
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
	 * @param type
	 * @return
	 */
	private List<Pair<String, String>> getAvailableColumns(String type) throws SearchException {
		Map<String, List<Pair<String,String>>> cache = 
				(Map<String, List<Pair<String, String>>>) getSession().getAttribute(AVAILABLE_COLUMNS_CACHE);
		if (cache == null) {
			cache = new HashMap<String, List<Pair<String,String>>>();
		}
		if (!cache.containsKey(type)) {
			List<Pair<String,String>> result = new LinkedList<Pair<String,String>>();
			
			Map<String,String> criteria = new HashMap<String,String>();
			criteria.put(Predicates.RDF_TYPE, Subjects.RDF_PROPERTY);
			criteria.put(Predicates.RDFS_DOMAIN, type);
			
			CustomSearch customSearch = new CustomSearch(criteria);
			customSearch.setPageLength(0); // no limits to the result set size, because there cannot be too many properties for a type
			customSearch.execute();
			
			Collection<SubjectDTO> subjects = customSearch.getResultList();
			if (subjects!=null){
				for(SubjectDTO subject : subjects) {
					if (!subject.isAnonymous()){
						String uri = subject.getUri();
						String label = subject.getObjectValue(Predicates.RDFS_LABEL);
						result.add(new Pair<String, String>(uri, label));
					}
				}
			}
			//hardcode RDF_TYPE as one of the available values
			result.add(new Pair<String, String>(Predicates.RDF_TYPE, "type"));
			
			cache.put(type, result);
			getSession().setAttribute(AVAILABLE_COLUMNS_CACHE, cache);
			return result;
		} else {
			return cache.get(type);
		}
	}
	
	/**
	 * @return
	 */
	private List<Pair<String, String>> getTypesFromCacheOrGenerate() throws SearchException {
		//check if it was already cached
		if (getSession().getAttribute(AVAILABLE_TYPES_CACHE) != null) {
			return (List<Pair<String, String>>) getSession().getAttribute(AVAILABLE_TYPES_CACHE);
		} else {
			List<Pair<String,String>> result = new LinkedList<Pair<String,String>>();
			Map<String,String> criteria = new HashMap<String,String>();
			criteria.put(Predicates.RDF_TYPE, Subjects.RDFS_CLASS);
			
			CustomSearch customSearch = new CustomSearch(criteria);
			customSearch.setSorting(Predicates.RDFS_LABEL, SortOrder.ASCENDING);
			customSearch.setPageLength(0); // we want no limits on result set size
			customSearch.execute();
			Collection<SubjectDTO> subjects = customSearch.getResultList();
			if (subjects!=null){
				for (Iterator<SubjectDTO> it=subjects.iterator(); it.hasNext();){
					SubjectDTO subject = it.next();
					if (!subject.isAnonymous()){
						String label = subject.getObjectValue(Predicates.RDFS_LABEL);
						if (!StringUtils.isBlank(label)){
							result.add(new Pair<String,String>(subject.getUri(), label));
						}
					}
				}
			}
			//cache it 
			getSession().setAttribute(AVAILABLE_TYPES_CACHE, result);
			return result;
		}
	}

	/**
	 * @throws SearchException 
	 * 
	 */
	public List<Pair<String,String>> getAvailableTypes() throws SearchException{
		return availableTypes;
	}
	
	public List<Pair<String,String>> getAvailableColumns() throws SearchException {
		return availableColumns;
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.web.action.AbstractSearchActionBean#getColumns()
	 */
	public List<SearchResultColumn> getColumns() throws SearchException {
		List<SearchResultColumn> columns = new LinkedList<SearchResultColumn>();
		//setSelectedColumns uses POST to submit a form, we have to add needed parameters to the 
		//column headers in order to be able to sort right.
		String actionParameter = "search=Search&amp;type=" + eionet.cr.util.Util.urlEncode(type);
		// let's always include rdfs:label in the columns
		columns.add(new SubjectPredicateColumn("Title", true, Predicates.RDFS_LABEL, actionParameter));
		//add every selected column to the output
		for(String string : selectedColumns) {
			for (Pair<String,String> pair : availableColumns) {
				if (pair.getId().equals(string)) {
					columns.add(new SubjectPredicateColumn(pair.getValue(), true, string, actionParameter));
				}
			}
		}
		
		return columns;
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

	/**
	 * @return the options
	 */
	public List<String> getSelectedColumns() {
		return selectedColumns;
	}

	/**
	 * @param options the options to set
	 */
	public void setSelectedColumns(List<String> options) {
		this.selectedColumns = options;
	}

	/**
	 * @return the maxDisplayedColumns
	 */
	public int getMaxDisplayedColumns() {
		return MAX_DISPLAYED_COLUMNS;
	}
}
