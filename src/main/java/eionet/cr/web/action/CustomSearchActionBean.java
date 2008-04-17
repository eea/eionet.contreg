package eionet.cr.web.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eionet.cr.common.Identifiers;

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
	private List<Map<String,String>> availableFilters;
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
	public List<Map<String,String>> getAvailableFilters() {
		
		if (availableFilters==null){
			
			availableFilters = new ArrayList<Map<String,String>>();
			
			Map<String,String> map = new HashMap<String,String>();
			map.put("uri", Identifiers.RDF_TYPE);
			map.put("title", "Type");
			availableFilters.add(map);
			
			map = new HashMap<String,String>();
			map.put("uri", Identifiers.RDFS_LABEL);
			map.put("title", "Label");
			availableFilters.add(map);
			
			map = new HashMap<String,String>();
			map.put("uri", Identifiers.DC_COVERAGE);
			map.put("title", "Coverage");
			availableFilters.add(map);
		}
		
		return availableFilters;
	}
	
	/**
	 * 
	 * @return
	 */
	public List<Map<String,String>> getUnselectedFilters() {
		
		List<Map<String,String>> available = getAvailableFilters();
		if (available==null || available.isEmpty())
			return null;
		else if (selectedFilters==null || selectedFilters.isEmpty())
			return available;
		else{
			List<Map<String,String>> result = new ArrayList<Map<String,String>>();
			for (int i=0; i<available.size(); i++){
				if (!selectedFilters.contains(available.get(i).get("uri")))
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
