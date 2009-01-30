package eionet.cr.web.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
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
	private List<HarvestSourceDTO> harvestSources;
	
	/** */
	public static List<Map<String,String>> sourceTypes;
	
	/** */
	private String type;

	/** */
	private String harvestUrl;

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
		if (type!=null && type.length()>0)
			harvestSources = DAOFactory.getDAOFactory().getHarvestSourceDAO().getHarvestSourcesByType(type);
		return new ForwardResolution("/pages/sources.jsp");
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
	 * @throws DAOException 
	 * @throws HarvestException 
	 * 
	 */
	public Resolution harvest() throws DAOException, HarvestException{
		
		if (type!=null && type.length()>0){
			
			harvestSources = DAOFactory.getDAOFactory().getHarvestSourceDAO().getHarvestSourcesByType(type);
			
			if (harvestUrl!=null && harvestUrl.trim().length()>0){
				HarvestQueue.addPullHarvest(harvestUrl.trim(), HarvestQueue.PRIORITY_URGENT);
				showMessage("The source has been scheduled for urgent harvest!");
			}
			else if (type.equals("data") || type.equals("schema") && harvestSources!=null && !harvestSources.isEmpty()){
				
				for (int i=0; i<harvestSources.size(); i++){
					HarvestQueue.addPullHarvest(harvestSources.get(i).getUrl(), HarvestQueue.PRIORITY_URGENT);
				}
				showMessage("Successfully scheduled harvest of all " + type + " sources");
			}
		}
		
		return new ForwardResolution("/pages/sources.jsp");
	}

	/**
	 * @return the harvestUrl
	 */
	public String getHarvestUrl() {
		return harvestUrl;
	}

	/**
	 * @param harvestUrl the harvestUrl to set
	 */
	public void setHarvestUrl(String harvestUrl) {
		this.harvestUrl = harvestUrl;
	}
}
