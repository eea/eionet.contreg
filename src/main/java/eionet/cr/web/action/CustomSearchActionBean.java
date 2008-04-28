package eionet.cr.web.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import eionet.cr.common.Identifiers;
import eionet.cr.common.ResourceDTO;
import eionet.cr.search.SearchException;
import eionet.cr.search.Searcher;
import eionet.cr.util.Util;
import eionet.cr.web.util.search.CustomSearchFilter;
import eionet.cr.web.util.search.SearchResultColumn;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
@UrlBinding("/customSearch.action")
public class CustomSearchActionBean extends SearchResourcesActionBean{
	
	/** */
	private static final String SELECTED_FILTERS_SESSION_ATTR_NAME = CustomSearchActionBean.class + ".selectedFilters";
	private static final String RESULT_LIST_SESSION_ATTR_NAME = CustomSearchActionBean.class + ".resultList";
	
	/** */
	private static final String SELECTED_VALUE_PREFIX = "value_";
	private static final String SHOW_PICKLIST_VALUE_PREFIX = "showPicklist_";
	private static final String REMOVE_FILTER_VALUE_PREFIX = "removeFilter_";
	
	/** */
	private static final String ASSOCIATED_JSP = "/pages/customSearch.jsp";
	
	/** */
	private static Map<String,CustomSearchFilter> availableFilters;
	private String addedFilter;
	private String picklistFilter;
	private String removedFilter;
	private Collection<String> picklist;
	
	/**
	 * 
	 * @return
	 */
	@DefaultHandler
	public Resolution unspecifiedEvent(){
		
		if (isShowPicklist())
			populateSelectedFilters();
		else if (isRemoveFilter()){
			populateSelectedFilters();
			getSelectedFilters().remove(getRemovedFilter());
		}
		else{
			HttpSession session = getContext().getRequest().getSession();
			session.removeAttribute(RESULT_LIST_SESSION_ATTR_NAME);
			session.removeAttribute(SELECTED_FILTERS_SESSION_ATTR_NAME);
		}
		
		return new ForwardResolution(ASSOCIATED_JSP);
	}
	
	/**
	 * 
	 * @return
	 * @throws SearchException 
	 */
	public Resolution search() throws SearchException{
		
		populateSelectedFilters();
		
		getContext().getRequest().getSession().setAttribute(RESULT_LIST_SESSION_ATTR_NAME,
				Searcher.customSearch(buildSearchCriteria(), true)); 
		
		return new ForwardResolution(ASSOCIATED_JSP);
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.web.action.SearchResourcesActionBean#getResultList()
	 */
	public List<ResourceDTO> getResultList() {
		return (List<ResourceDTO>)getContext().getRequest().getSession().getAttribute(RESULT_LIST_SESSION_ATTR_NAME);
	}
	
	/**
	 * 
	 * @return
	 */
	public Resolution addFilter(){
		
		populateSelectedFilters();
		
		if (addedFilter!=null)
			getSelectedFilters().put(addedFilter, "");
		
		return new ForwardResolution(ASSOCIATED_JSP);
	}
	
	/**
	 * 
	 * @return
	 * @throws SearchException 
	 */
	public Collection<String> getPicklist() throws SearchException{

		if (!isShowPicklist())
			return null;
		else if (!getAvailableFilters().containsKey(getPicklistFilter()))
			return null;
		
		if (picklist==null){
			picklist = Searcher.getLiteralFieldValues(getAvailableFilters().get(getPicklistFilter()).getUri());
			if (picklist==null)
				picklist = new ArrayList<String>();
		}
		
		return picklist;
	}
	
	/**
	 * @return the selectedFilter
	 */
	public String getAddedFilter() {
		return addedFilter;
	}

	/**
	 * @param selectedFilter the selectedFilter to set
	 */
	public void setAddedFilter(String selectedFilter) {
		this.addedFilter = selectedFilter;
	}

	/**
	 * 
	 * @return
	 */
	public Map<String,String> getSelectedFilters(){
		
		HttpSession session = getContext().getRequest().getSession();
		Map<String,String> selectedFilters =
			(Map<String,String>)session.getAttribute(SELECTED_FILTERS_SESSION_ATTR_NAME);
		if (selectedFilters==null){
			selectedFilters = new LinkedHashMap<String,String>();
			session.setAttribute(SELECTED_FILTERS_SESSION_ATTR_NAME, selectedFilters);
		}
		
		return selectedFilters;
	}

	/**
	 * 
	 */
	private void populateSelectedFilters(){
		
		Map<String,String> selected = getSelectedFilters();
		if (!selected.isEmpty()){
			Enumeration paramNames = this.getContext().getRequest().getParameterNames();
			while (paramNames!=null && paramNames.hasMoreElements()){
				String paramName = (String)paramNames.nextElement();
				if (paramName.startsWith(SELECTED_VALUE_PREFIX)){
					String key = paramName.substring(SELECTED_VALUE_PREFIX.length());
					if (key.length()>0 && selected.containsKey(key))
						selected.put(key, getContext().getRequest().getParameter(paramName));
				}
			}
		}		
	}

	/**
	 * @return the availableFilters
	 */
	public Map<String,CustomSearchFilter> getAvailableFilters() {
		
		if (availableFilters==null){
			
			ArrayList<CustomSearchFilter> list = new ArrayList<CustomSearchFilter>();
			
			CustomSearchFilter filter = new CustomSearchFilter();
			filter.setUri(Identifiers.RDF_TYPE);
			filter.setTitle("Type");
			filter.setDescription("");
			filter.setProvideValues(true);
			list.add(filter);

			filter = new CustomSearchFilter();
			filter.setUri(Identifiers.RDFS_LABEL);
			filter.setTitle("Label");
			filter.setDescription("");
			filter.setProvideValues(false);
			list.add(filter);

			filter = new CustomSearchFilter();
			filter.setUri(Identifiers.DC_SUBJECT);
			filter.setTitle("Subject");
			filter.setDescription("");
			filter.setProvideValues(true);
			list.add(filter);

			filter = new CustomSearchFilter();
			filter.setUri(Identifiers.DC_COVERAGE);
			filter.setTitle("Coverage");
			filter.setDescription("");
			filter.setProvideValues(true);
			list.add(filter);

			filter = new CustomSearchFilter();
			filter.setUri(Identifiers.ROD_OBLIGATION_PROPERTY);
			filter.setTitle("Dataflow");
			filter.setDescription("");
			filter.setProvideValues(true);
			list.add(filter);

			filter = new CustomSearchFilter();
			filter.setUri(Identifiers.ROD_LOCALITY_PROPERTY);
			filter.setTitle("Locality");
			filter.setDescription("");
			filter.setProvideValues(true);
			list.add(filter);

			filter = new CustomSearchFilter();
			filter.setUri(Identifiers.ROD_ISSUE_PROPERTY);
			filter.setTitle("Issue");
			filter.setDescription("");
			filter.setProvideValues(true);
			list.add(filter);

			filter = new CustomSearchFilter();
			filter.setUri(Identifiers.ROD_INSTRUMENT_PROPERTY);
			filter.setTitle("Instrument");
			filter.setDescription("");
			filter.setProvideValues(true);
			list.add(filter);

			filter = new CustomSearchFilter();
			filter.setUri(Identifiers.DC_CREATOR);
			filter.setTitle("Creator");
			filter.setDescription("");
			filter.setProvideValues(true);
			list.add(filter);

			filter = new CustomSearchFilter();
			filter.setUri(Identifiers.DC_DESCRIPTION);
			filter.setTitle("Description");
			filter.setDescription("Abstract description of content");
			filter.setProvideValues(false);
			list.add(filter);

			filter = new CustomSearchFilter();
			filter.setUri(Identifiers.DC_PUBLISHER);
			filter.setTitle("Publisher");
			filter.setDescription("");
			filter.setProvideValues(true);
			list.add(filter);

			filter = new CustomSearchFilter();
			filter.setUri(Identifiers.DC_CONTRIBUTOR);
			filter.setTitle("Contributor");
			filter.setDescription("");
			filter.setProvideValues(true);
			list.add(filter);

			filter = new CustomSearchFilter();
			filter.setUri(Identifiers.DC_RELATION);
			filter.setTitle("Relation");
			filter.setDescription("Url to a related resource");
			filter.setProvideValues(false);
			list.add(filter);

			filter = new CustomSearchFilter();
			filter.setUri(Identifiers.DC_LANGUAGE);
			filter.setTitle("Language");
			filter.setDescription("");
			filter.setProvideValues(true);
			list.add(filter);

			availableFilters = new LinkedHashMap<String,CustomSearchFilter>();
			for (int i=0; i<list.size(); i++)
				availableFilters.put(String.valueOf(i+1), list.get(i));
		}
		
		return availableFilters;
	}

	/**
	 * @return the picklistFilter
	 */
	public String getPicklistFilter() {
		
		if (picklistFilter==null){
			picklistFilter = "";
			Enumeration paramNames = this.getContext().getRequest().getParameterNames();
			while (paramNames!=null && paramNames.hasMoreElements()){
				String paramName = (String)paramNames.nextElement();
				if (paramName.startsWith(SHOW_PICKLIST_VALUE_PREFIX)){
					int i = paramName.indexOf('.')<0 ? paramName.length() : paramName.indexOf('.');
					String key = paramName.substring(SHOW_PICKLIST_VALUE_PREFIX.length(), i);
					if (key.length()>0 && getSelectedFilters().containsKey(key)){
						picklistFilter = key;
						break;
					}
				}
			}

		}
		return picklistFilter;
	}

	/**
	 * @return the removedFilter
	 */
	public String getRemovedFilter() {
		
		if (removedFilter==null){
			removedFilter = "";
			Enumeration paramNames = this.getContext().getRequest().getParameterNames();
			while (paramNames!=null && paramNames.hasMoreElements()){
				String paramName = (String)paramNames.nextElement();
				if (paramName.startsWith(REMOVE_FILTER_VALUE_PREFIX)){
					int i = paramName.indexOf('.')<0 ? paramName.length() : paramName.indexOf('.');
					String key = paramName.substring(REMOVE_FILTER_VALUE_PREFIX.length(), i);
					if (key.length()>0 && getSelectedFilters().containsKey(key)){
						removedFilter = key;
						break;
					}
				}
			}

		}
		return removedFilter;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isShowPicklist(){
		return !Util.isNullOrEmpty(getPicklistFilter());
	}

	/**
	 * 
	 * @return
	 */
	public boolean isRemoveFilter(){
		return !Util.isNullOrEmpty(getRemovedFilter());
	}
	
	/**
	 * 
	 * @return
	 */
	private Map<String,String> buildSearchCriteria(){
		
		Map<String,String> result = new HashMap<String,String>();
		
		Map<String,String> selected = getSelectedFilters();
		for (Iterator<String> keys=selected.keySet().iterator(); keys.hasNext();){
			String key = keys.next();
			String value = selected.get(key);
			if (value!=null && value.trim().length()>0){
				CustomSearchFilter filter = getAvailableFilters().get(key);
				if (filter!=null)
					result.put(filter.getUri(), value.trim());
			}
		}
		
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.web.action.SearchResourcesActionBean#getColumns()
	 */
	public List<SearchResultColumn> getColumns(){
		return getDefaultColumns();
	}
}
