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
	private List<CustomSearchFilter> availableFilters;
	private List<String> selectedFilters = new ArrayList<String>();
	
	/** */
	private String filter;
	
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
	public Resolution addFilter(){
		
		if (filter!=null && filter.length()>0)
			selectedFilters.add(filter);
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
			availableFilters.add(filter);

			filter = new CustomSearchFilter();
			filter.setUri(Identifiers.RDFS_LABEL);
			filter.setTitle("Label");
			filter.setDescription("");
			filter.setProvideValues(false);
			availableFilters.add(filter);

			filter = new CustomSearchFilter();
			filter.setUri(Identifiers.DC_COVERAGE);
			filter.setTitle("Coverage");
			filter.setDescription("");
			filter.setProvideValues(true);
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
		else if (selectedFilters==null || selectedFilters.isEmpty())
			return available;
		else{
			List<CustomSearchFilter> result = new ArrayList<CustomSearchFilter>();
			for (int i=0; i<available.size(); i++){
				if (!selectedFilters.contains(available.get(i).getUri()))
					result.add(available.get(i));
			}
			return result;
		}
	}

	/**
	 * @return the selectedFilters
	 */
	public List<String> getSelectedFilters() {
		return selectedFilters;
	}

	/**
	 * @return the selectedFilter
	 */
	public String getFilter() {
		return filter;
	}

	/**
	 * @param selectedFilter the selectedFilter to set
	 */
	public void setFilter(String selectedFilter) {
		this.filter = selectedFilter;
	}
}
