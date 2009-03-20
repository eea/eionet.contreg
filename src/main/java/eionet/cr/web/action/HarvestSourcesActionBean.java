package eionet.cr.web.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.harvest.HarvestException;
import eionet.cr.harvest.scheduled.HarvestQueue;
import eionet.cr.harvest.util.DedicatedHarvestSourceTypes;

/**
 * @author altnyris
 *
 */
@UrlBinding("/sources.action")
public class HarvestSourcesActionBean extends AbstractActionBean {
	
	/** */
	private static final String UNAVAILABLE_TYPE = "unavail";
	
	/** */
	private List<HarvestSourceDTO> harvestSources;
	
	/** */
	public static List<Map<String,String>> sourceTypes;
	
	/** */
	private String type;

	/** */
	private List<String> sourceUrl;

	/** */
	public HarvestSourcesActionBean(){
		this.type = "data";
	}
	
	/**
	 * 
	 * @return
	 * @throws DAOException 
	 */
	@DefaultHandler
	public Resolution view() throws DAOException{
		
		if (type!=null && type.length()>0){
			if (type.equals(UNAVAILABLE_TYPE))
				harvestSources = DAOFactory.getDAOFactory().getHarvestSourceDAO().getHarvestSourcesUnavailable();
			else
				harvestSources = DAOFactory.getDAOFactory().getHarvestSourceDAO().getHarvestSourcesByType(type);
		}
		
		return new ForwardResolution("/pages/sources.jsp");
	}

	/**
	 * 
	 * @return
	 * @throws DAOException 
	 */
	public Resolution delete() throws DAOException{
		
		if(isUserLoggedIn()){
			if (sourceUrl!=null && !sourceUrl.isEmpty()){
				DAOFactory.getDAOFactory().getHarvestSourceDAO().deleteSourcesByUrl(sourceUrl);
				showMessage("Harvest source(s) deleted!");
			}
		}
		else
			handleCrException(getBundle().getString("not.logged.in"), GeneralConfig.SEVERITY_WARNING);

		return view();
	}
	
	/**
	 * @throws DAOException 
	 * @throws HarvestException 
	 * 
	 */
	public Resolution harvest() throws DAOException, HarvestException{
		
		if(isUserLoggedIn()){
			if (sourceUrl!=null && !sourceUrl.isEmpty()){
				HarvestQueue.addPullHarvests(sourceUrl, HarvestQueue.PRIORITY_URGENT);
				if (sourceUrl.size()==1)
					showMessage("The source has been scheduled for urgent harvest!");
				else
					showMessage("The sources have been scheduled for urgent harvest!");
			}
		}
		else
			handleCrException(getBundle().getString("not.logged.in"), GeneralConfig.SEVERITY_WARNING);
		
		return view();
	}

	/**
	 * @return the harvestSources
	 */
	public List<HarvestSourceDTO> getHarvestSources() {
		return harvestSources;
	}
	
	/**
	 * 
	 * @return
	 */
	public List<Map<String, String>> getSourceTypes(){
		
		if (sourceTypes==null){
			
			sourceTypes = new ArrayList<Map<String,String>>();
			
			Map<String,String> typeMap = new HashMap<String,String>();
			typeMap.put("title", "Data");
			typeMap.put("type", "data");
			sourceTypes.add(typeMap);
			
			typeMap = new HashMap<String,String>();
			typeMap.put("title", "Schemas");
			typeMap.put("type", "schema");
			sourceTypes.add(typeMap);
			
			typeMap = new HashMap<String,String>();
			typeMap.put("title", "Delivered files");
			typeMap.put("type", DedicatedHarvestSourceTypes.deliveredFile);
			sourceTypes.add(typeMap);
			
			typeMap = new HashMap<String,String>();
			typeMap.put("title", "QAW sources");
			typeMap.put("type", DedicatedHarvestSourceTypes.qawSource);
			sourceTypes.add(typeMap);

			typeMap = new HashMap<String,String>();
			typeMap.put("title", "Unavailable");
			typeMap.put("type", UNAVAILABLE_TYPE);
			sourceTypes.add(typeMap);
		}
		
		return sourceTypes;
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
	 * @param sourceUrl the sourceUrl to set
	 */
	public void setSourceUrl(List<String> sourceUrl) {
		this.sourceUrl = sourceUrl;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getPagingUrl(){
		
		String urlBinding = getUrlBinding();
		if (urlBinding.startsWith("/")){
			urlBinding = urlBinding.substring(1);
		}
		
		StringBuffer buf = new StringBuffer(urlBinding);
		return buf.append("?view=").toString();
	}
}
