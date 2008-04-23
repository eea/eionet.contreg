package eionet.cr.web.action;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import eionet.cr.common.Identifiers;
import eionet.cr.web.util.CustomSearchFilter;

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
public class CustomSearchActionBean extends AbstractSearchActionBean{
	
	/** */
	private static final String SELECTED_FILTERS_SESSION_ATTR_NAME = CustomSearchActionBean.class + ".selectedFilters";
	private static final String ASSOCIATED_JSP = "/pages/customSearch.jsp";
	private static final String SELECTED_VALUE_PREFIX = "value_";
	
	/** */
	private static Map<String,CustomSearchFilter> availableFilters;
	private String addedFilter;
	private String removedFilter;
	private String picklistFilter;
	private List<String> picklist;
	
	/**
	 * 
	 * @return
	 */
	@DefaultHandler
	public Resolution init(){
		getContext().getRequest().getSession().removeAttribute(SELECTED_FILTERS_SESSION_ATTR_NAME);
		return new ForwardResolution(ASSOCIATED_JSP);
	}
	
	/**
	 * 
	 * @return
	 */
	public Resolution search(){
		populateSelectedValues();
		showMessage("Not yet implemented!");
		return new ForwardResolution(ASSOCIATED_JSP);
	}
	
	/**
	 * 
	 * @return
	 */
	public Resolution addFilter(){
		
		if (addedFilter!=null)
			getSelectedFilters().put(addedFilter, "");
		
//		if (addedFilter!=null && addedFilter.length()>0){
//			if (selected==null)
//				selected = new ArrayList<String>();
//			selected.add(addedFilter);
//		}
		
		populateSelectedValues();
		
		return new ForwardResolution(ASSOCIATED_JSP);
	}
	
	/**
	 * 
	 * @return
	 */
	public Resolution removeFilter(){
		
		if (removedFilter!=null)
			getSelectedFilters().remove(removedFilter);
		
//		if (addedFilter!=null && addedFilter.length()>0){
//			if (selected!=null)
//				selected.remove(addedFilter);
//		}
		
		populateSelectedValues();
		
		return new ForwardResolution(ASSOCIATED_JSP);
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
			filter.setUri(Identifiers.DC_COVERAGE);
			filter.setTitle("Coverage");
			filter.setDescription("");
			filter.setProvideValues(true);
			list.add(filter);

			filter = new CustomSearchFilter();
			filter.setUri(Identifiers.DC_SUBJECT);
			filter.setTitle("Subject");
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
	 * 
	 * @return
	 */
	public Resolution showPicklist(){

		populateSelectedValues();
		
		picklist = new ArrayList<String>();
		picklist.add("Sir Matt Busby, Sir Matt Busby, Sir Matt Busby");
		picklist.add("Wilf McGuinness, Wilf McGuinness, Wilf McGuinness");
		picklist.add("Sir Matt Busby, Sir Matt Busby, Sir Matt Busby");
		picklist.add("Frank O'Farrell, Frank O'Farrell, Frank O'Farrell");
		picklist.add("Tommy Docherty, Tommy Docherty, Tommy Docherty");
		picklist.add("Dave Sexton. Dave Sexton, Dave Sexton");
		picklist.add("Ron Atkinson, Ron Atkinson, Ron Atkinson");
		picklist.add("Sir Alex Ferguson, Sir Alex Ferguson, Sir Alex Ferguson");
		
		return new ForwardResolution(ASSOCIATED_JSP);
	}
	
	/**
	 * 
	 * @return
	 */
	public List<String> getPicklist(){
		return picklist;
	}
	
	/**
	 * 
	 */
	private void populateSelectedValues(){
		
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
	 * @return the removedFilter
	 */
	public String getRemovedFilter() {
		return removedFilter;
	}

	/**
	 * @param removedFilter the removedFilter to set
	 */
	public void setRemovedFilter(String removedFilter) {
		this.removedFilter = removedFilter;
	}

	/**
	 * @return the picklistFilter
	 */
	public String getPicklistFilter() {
		return picklistFilter;
	}

	/**
	 * @param picklistFilter the picklistFilter to set
	 */
	public void setPicklistFilter(String picklistFilter) {
		this.picklistFilter = picklistFilter;
	}
}
