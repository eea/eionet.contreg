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
 * Aleksandr Ivanov, Tieto Eesti
 */
package eionet.cr.web.action;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.commons.lang.StringUtils;

import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.ISearchDao;
import eionet.cr.dao.mysql.MySQLDAOFactory;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.search.SearchException;
import eionet.cr.search.util.SortOrder;
import eionet.cr.util.PageRequest;
import eionet.cr.util.Pair;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.Util;
import eionet.cr.web.util.ApplicationCache;
import eionet.cr.web.util.columns.SearchResultColumn;
import eionet.cr.web.util.columns.SubjectPredicateColumn;

/**
 * @author Aleksandr Ivanov
 * <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
@UrlBinding("/typeSearch.action")
public class TypeSearchActionBean extends AbstractSearchActionBean<SubjectDTO> {
	
	private enum LastAction {
		ADD_FILTER, REMOVE_FILTER, APPLY_FILTERS, SET_COLUMNS, SEARCH, SORTING;
	}

	private static final String AVAILABLE_TYPES_CACHE = TypeSearchActionBean.class.getName() + ".availableTypesCache";
	private static final String AVAILABLE_COLUMNS_CACHE = TypeSearchActionBean.class.getName() + ".availableColumnsCache";
	private static final String SELECTED_COLUMNS_CACHE = TypeSearchActionBean.class.getName() + ".selectedColumnsCache";
	private static final String SELECTED_FILTERS_CACHE = TypeSearchActionBean.class.getName() + ".selectedFiltersCache";
	private static final String RESULT_LIST_CACHED = TypeSearchActionBean.class.getName() + ".resultListCached";
	private static final String LAST_ACTION = TypeSearchActionBean.class.getName() + ".lastAction";
	private static final String MATCH_COUNT = TypeSearchActionBean.class.getName() + ".matchCount";
	
	
	private static final String PREVIOUS_TYPE =  TypeSearchActionBean.class.getName() + ".previousType";
	

	private static final String TYPE_SEARCH_PATH = "/pages/typeSearch.jsp";


	private static final int MAX_DISPLAYED_COLUMNS = 4;

	//type to search
	private String type;
	//selected columns
	private List<String> selectedColumns;
	//filter to add
	private String newFilter;
	//filter to be removed
	private String clearFilter;
	//selected filters
	private Map<String,String> selectedFilters;
	//selected filters with labels.
	private Map<String, Pair<String,String>> displayFilters;
	

	/**
	 * @return
	 * @throws Exception
	 */
	@DefaultHandler
	public Resolution preparePage () throws Exception {
		Enumeration<String> names = getContext().getRequest().getParameterNames();
		while(names.hasMoreElements()) {
			String next = names.nextElement();
			if(next.startsWith("removeFilter_")) {
				clearFilter = next.replaceFirst("removeFilter_", "");
				return removeFilter();
			}
		}
		return new ForwardResolution(TYPE_SEARCH_PATH);
	}
	
	/**
	 * @return
	 * @throws SearchException
	 */
	public Resolution introspect() throws SearchException {
		
		if (!StringUtils.isBlank(type)){			
			return new RedirectResolution(FactsheetActionBean.class).addParameter("uri", type);
		}
		else
			return new ForwardResolution(TYPE_SEARCH_PATH);
	}
	
	/**
	 * @return
	 * @throws SearchException
	 */
	public Resolution addFilter() throws SearchException {
		Map<String, Map<String,String>> cache = (Map<String, Map<String, String>>) getSession().getAttribute(SELECTED_FILTERS_CACHE);
		if (cache == null) {
			cache = new HashMap<String, Map<String,String>>();
		}
		if (!cache.containsKey(type)) {
			Map<String,String> filters = new LinkedHashMap<String, String>();
			cache.put(type, filters);
		}
		cache.get(type).put(newFilter, null);
		getSession().setAttribute(SELECTED_FILTERS_CACHE, cache);
		getSession().setAttribute(LAST_ACTION, LastAction.ADD_FILTER);
		return new RedirectResolution(getUrlBinding() + "?search=Search&type=" + Util.urlEncode(type));		
	}
	
	/**
	 * @return
	 * @throws SearchException
	 */
	public Resolution removeFilter() throws SearchException {
		Map<String,Map<String,String>> cache = (Map<String, Map<String, String>>) getSession().getAttribute(SELECTED_FILTERS_CACHE);
		if(cache != null && cache.containsKey(type) && cache.get(type) != null 
				&& cache.get(type).containsKey(clearFilter)) {
			cache.get(type).remove(clearFilter);
		}
		getSession().setAttribute(LAST_ACTION, LastAction.REMOVE_FILTER);
		return new RedirectResolution(getUrlBinding() + "?search=Search&type=" + Util.urlEncode(type));		
	}
	
	/**
	 * @return
	 * @throws SearchException
	 */
	public Resolution applyFilters() throws SearchException {
		if(selectedFilters != null && !selectedFilters.isEmpty()) {
			//save selected filters for future use
			Map<String,Map<String,String>> cache = (Map<String, Map<String, String>>) getSession().getAttribute(SELECTED_FILTERS_CACHE);
			if (cache != null && cache.containsKey(type)) {
				cache.get(type).putAll(selectedFilters);
			}
		}
		getSession().setAttribute(LAST_ACTION, LastAction.REMOVE_FILTER);
		return search();
	}
	
	/** 
	 * @see eionet.cr.web.action.AbstractSearchActionBean#search()
	 * {@inheritDoc}
	 */
	@Override
	public Resolution search() throws SearchException {
		if (!StringUtils.isBlank(type)) {
			restoreStateFromSession();
			LastAction lastAction = getLastAction();
			if (resultList == null || !(lastAction != null && lastAction.equals(LastAction.ADD_FILTER))) {
				Map<String,String> criteria = new HashMap<String,String>();
				criteria.put(Predicates.RDF_TYPE, type);
				if (selectedFilters != null && !selectedFilters.isEmpty()) {
					for (Entry<String,String> entry : selectedFilters.entrySet()) {
						if (!StringUtils.isBlank(entry.getValue())) {
							criteria.put(entry.getKey(), StringUtils.trim(entry.getValue().trim()));
						}
					}
				}
				
				Pair<Integer, List<SubjectDTO>> customSearch;
				try {
					customSearch = MySQLDAOFactory.get().getDao(ISearchDao.class)
							.performCustomSearch(
									criteria,
									null,
									new PageRequest(getPageN()),
									new SortingRequest(getSortP(), SortOrder.parse(getSortO())));
				} catch (DAOException e) {
					throw new SearchException("Exception in type search action bean", e);
				}
				
				resultList = customSearch.getValue();
	    		matchCount = customSearch.getId();
			}
			//cache result list.
			getSession().setAttribute(RESULT_LIST_CACHED, resultList);
			getSession().setAttribute(LAST_ACTION, LastAction.SEARCH);
			getSession().setAttribute(MATCH_COUNT, matchCount);
		}
		return new ForwardResolution(TYPE_SEARCH_PATH);
	}

	
	/**
	 * @return
	 */
	private LastAction getLastAction() {
		if (getContext().getRequest().getParameter("pageN") != null) {
			return LastAction.SORTING;
		} else {
			return (LastAction) getSession().getAttribute(LAST_ACTION);
		}
	}

	/**
	 * restores actionBean state from session.
	 */
	private void restoreStateFromSession() throws SearchException {

		//restoring the result list
		resultList = (Collection<SubjectDTO>) getSession().getAttribute(RESULT_LIST_CACHED);
		Integer temp = (Integer) getSession().getAttribute(MATCH_COUNT);
		matchCount = temp == null ? 0 : temp.intValue();
		
		//check if user has previously selected some columns.
		Map<String,List<String>> cache = (Map<String, List<String>>) getSession().getAttribute(SELECTED_COLUMNS_CACHE);
		if (cache != null && cache.containsKey(type) ) {
			selectedColumns = cache.get(type);
		} else {
			selectedColumns = new LinkedList<String>();
			int i = 0;
			for (Entry<String,String> pair : getAvailableColumns().entrySet()){
				if(i++ == MAX_DISPLAYED_COLUMNS || Predicates.RDF_TYPE.equals(pair.getKey())) {
					break;
				}
				selectedColumns.add(pair.getKey());
			}
		}

		//saving to session selected type.
		String previousType = (String) getSession().getAttribute(PREVIOUS_TYPE);
		getSession().setAttribute(PREVIOUS_TYPE, type);
		if (!type.equals(previousType)) {
			Map<String,Map<String,String>> filterCache = (Map<String, Map<String, String>>) getSession().getAttribute(SELECTED_FILTERS_CACHE);
			if (filterCache != null && filterCache.containsKey(type)) {
				filterCache.remove(type);
			}
		} else {
			//check for selected filters and add labels to them
			Map<String,Map<String,String>> filterCache = (Map<String, Map<String, String>>) getSession().getAttribute(SELECTED_FILTERS_CACHE);
			if (filterCache != null && filterCache.containsKey(type)) {
				selectedFilters = filterCache.get(type);
				displayFilters = new HashMap<String, Pair<String,String>>();
				Map<String,String> availableColumns = getAvailableColumns();
				for(Entry<String,String> entry : selectedFilters.entrySet()){
					if(availableColumns.containsKey(entry.getKey())) {
						displayFilters.put(
								availableColumns.get(entry.getKey()),
								new Pair<String,String>(entry.getKey(), entry.getValue()));
					}
				}
			}
		}
	}

	public Resolution setSearchColumns() throws SearchException {
		Map<String,List<String>> cache = (Map<String, List<String>>) getSession().getAttribute(SELECTED_COLUMNS_CACHE);
		if (cache == null) {
			cache = new HashMap<String, List<String>>();
		}
		if (selectedColumns != null) {
			selectedColumns = new LinkedList<String>(
					selectedColumns.subList(
							0,
							Math.min(
									MAX_DISPLAYED_COLUMNS,
									selectedColumns.size())));
		}
		cache.put(type, selectedColumns);
		getSession().setAttribute(SELECTED_COLUMNS_CACHE, cache);
		getSession().setAttribute(LAST_ACTION, LastAction.SET_COLUMNS);
		return new RedirectResolution(getUrlBinding() + "?search=Search&type=" + Util.urlEncode(type));
	}
	
	/** 
	 * @see eionet.cr.web.action.AbstractSearchActionBean#getColumns()
	 * {@inheritDoc}
	 */
	@Override
	public List<SearchResultColumn> getColumns() throws SearchException {
		List<SearchResultColumn> columns = new LinkedList<SearchResultColumn>();
		//setSelectedColumns uses POST to submit a form, we have to add needed parameters to the 
		//column headers in order to be able to sort right.
		String actionParameter = "search=Search&amp;type=" + eionet.cr.util.Util.urlEncode(type);
		// let's always include rdfs:label in the columns
		columns.add(new SubjectPredicateColumn("Title", true, Predicates.RDFS_LABEL, actionParameter));
		//add every selected column to the output
		if (selectedColumns != null && !selectedColumns.isEmpty()) {
			for(String string : selectedColumns) {
				for (Entry<String,String> pair : getAvailableColumns().entrySet()) {
					if (pair.getKey().equals(string)) {
						columns.add(new SubjectPredicateColumn(pair.getValue(), true, string, actionParameter));
					}
				}
			}
		}
		
		return columns;

	}
	
	public List<Pair<String, String>> getAvailableTypes() throws SearchException {
		return ApplicationCache.getTypes();
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
	 * @return the selectedColumns
	 */
	public List<String> getSelectedColumns() {
		return selectedColumns;
	}

	/**
	 * @param selectedColumns the selectedColumns to set
	 */
	public void setSelectedColumns(List<String> selectedColumns) {
		this.selectedColumns = selectedColumns;
	}

	/**
	 * @return the availableColumns
	 */
	public Map<String, String> getAvailableColumns() throws SearchException {
		Map<String, Map<String,String>> cache = 
			(Map<String, Map<String, String>>) getSession().getAttribute(AVAILABLE_COLUMNS_CACHE);
		if (cache == null) {
			cache = new HashMap<String, Map<String,String>>();
		}
		if (!cache.containsKey(type)) {
			Map<String,String> result = new LinkedHashMap<String,String>();
			
			Map<String,String> criteria = new HashMap<String,String>();
			criteria.put(Predicates.RDF_TYPE, Subjects.RDF_PROPERTY);
			criteria.put(Predicates.RDFS_DOMAIN, type);
			
			Pair<Integer, List<SubjectDTO>> customSearch;
			try {
				customSearch = MySQLDAOFactory.get().getDao(ISearchDao.class)
						.performCustomSearch(criteria, null, new PageRequest(1,0), null);
			} catch (DAOException e) {
				throw new SearchException("Exception in type search action bean", e);
			}
			
			if (customSearch.getValue() != null){
				for(SubjectDTO subject : customSearch.getValue()) {
					if (!subject.isAnonymous()){
						String uri = subject.getUri();
						String label = subject.getObjectValue(Predicates.RDFS_LABEL);
						result.put(uri, label);
					}
				}
			}
			//hardcode RDF_TYPE as one of the available values
			result.put(Predicates.RDF_TYPE, "type");
			
			cache.put(type, result);
			getSession().setAttribute(AVAILABLE_COLUMNS_CACHE, cache);
			return result;
		} else {
			return cache.get(type);
		}
	}
	
	/**
	 * @return available filters.
	 * @throws SearchException
	 */
	public Map<String,String> getAvailableFilters() throws SearchException {
		Map<String,String> result = new HashMap<String, String>();
		result.putAll(getAvailableColumns());
		result.remove(Predicates.RDF_TYPE);
		if (selectedFilters != null) {
			for (String key : selectedFilters.keySet()) {
				result.remove(key);
			}
		}
		return result;
	}

	/**
	 * @return the selectedFilter
	 */
	public String getNewFilter() {
		return newFilter;
	}

	/**
	 * @param selectedFilter the selectedFilter to set
	 */
	public void setNewFilter(String selectedFilter) {
		this.newFilter = selectedFilter;
	}

	/**
	 * @return the selectedFilters
	 */
	public Map<String, String> getSelectedFilters() {
		return selectedFilters;
	}

	/**
	 * @param selectedFilters the selectedFilters to set
	 */
	public void setSelectedFilters(Map<String, String> selectedFilters) {
		this.selectedFilters = selectedFilters;
	}

	/**
	 * @return the removeFilter
	 */
	public String getClearFilter() {
		return clearFilter;
	}

	/**
	 * @param removeFilter the removeFilter to set
	 */
	public void setClearFilter(String removeFilter) {
		this.clearFilter = removeFilter;
	}

	/**
	 * @return the displayFilters
	 */
	public Map<String, Pair<String, String>> getDisplayFilters() {
		return displayFilters;
	}

}
