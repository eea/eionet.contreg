package eionet.cr.web.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	private static List<CustomSearchFilter> availableFilters;
	private List<String> selected;
	private String filterKey;
	
	/**
	 * 
	 * @return
	 */
	@DefaultHandler
	public Resolution init(){
		return new ForwardResolution("/pages/customSearch.jsp");
	}
	
	/**
	 * 
	 * @return
	 */
	public Resolution search(){
		showMessage("Not yet implemented!");
		return new ForwardResolution("/pages/customSearch.jsp");
	}

	/**
	 * 
	 * @return
	 */
	public Resolution addFilter(){
		
		if (filterKey!=null && filterKey.length()>0){
			if (selected==null)
				selected = new ArrayList<String>();
			selected.add(filterKey);
		}
		return new ForwardResolution("/pages/customSearch.jsp");
	}
	
	/**
	 * 
	 * @return
	 */
	public Resolution removeFilter(){
		
		if (filterKey!=null && filterKey.length()>0){
			if (selected!=null)
				selected.remove(filterKey);
		}
		return new ForwardResolution("/pages/customSearch.jsp");		
	}
	
	/**
	 * @return the availableFilters
	 */
	public List<CustomSearchFilter> getAvailableFilters() {
		
		if (availableFilters==null){
			
			availableFilters = new ArrayList<CustomSearchFilter>();
			
			CustomSearchFilter filter = new CustomSearchFilter();
			filter.setUri(Identifiers.RDF_TYPE);
			filter.setTitle("Type");
			filter.setDescription("");
			filter.setProvideValues(true);
			filter.setKey(String.valueOf(availableFilters.size()+1));
			availableFilters.add(filter);

			filter = new CustomSearchFilter();
			filter.setUri(Identifiers.RDFS_LABEL);
			filter.setTitle("Label");
			filter.setDescription("");
			filter.setProvideValues(false);
			filter.setKey(String.valueOf(availableFilters.size()+1));
			availableFilters.add(filter);

			filter = new CustomSearchFilter();
			filter.setUri(Identifiers.DC_COVERAGE);
			filter.setTitle("Coverage");
			filter.setDescription("");
			filter.setProvideValues(true);
			filter.setKey(String.valueOf(availableFilters.size()+1));
			availableFilters.add(filter);

			filter = new CustomSearchFilter();
			filter.setUri(Identifiers.DC_SUBJECT);
			filter.setTitle("Subject");
			filter.setDescription("");
			filter.setProvideValues(true);
			filter.setKey(String.valueOf(availableFilters.size()+1));
			availableFilters.add(filter);
		}
		
		return availableFilters;
	}
	
	/**
	 * 
	 * @return
	 */
	public List<CustomSearchFilter> getUnselectedFilters() {
		
		List<CustomSearchFilter> available = getAvailableFilters();
		if (available==null || available.isEmpty())
			return null;
		else if (selected==null || selected.isEmpty())
			return available;
		else{
			List<CustomSearchFilter> result = new ArrayList<CustomSearchFilter>();
			for (int i=0; i<available.size(); i++){
				if (!selected.contains(available.get(i).getKey()))
					result.add(available.get(i));
			}
			return result;
		}
	}

	/**
	 * 
	 * @return
	 */
	public List<CustomSearchFilter> getSelectedFilters() {
		
		List<CustomSearchFilter> result = new ArrayList<CustomSearchFilter>();
		if (selected!=null && selected.size()>0){
			List<CustomSearchFilter> available = getAvailableFilters();
			for (int i=0; available!=null && i<available.size(); i++){
				if (selected.contains(available.get(i).getKey()))
					result.add(available.get(i));
			}
		}

		return result;
	}

	/**
	 * @return the selected
	 */
	public List<String> getSelected() {
		return selected;
	}

	/**
	 * @return the selectedFilter
	 */
	public String getFilterKey() {
		return filterKey;
	}

	/**
	 * @param selectedFilter the selectedFilter to set
	 */
	public void setFilterKey(String selectedFilter) {
		this.filterKey = selectedFilter;
	}

	/**
	 * @param selected the selected to set
	 */
	public void setSelected(List<String> selected) {
		this.selected = selected;
	}
}
