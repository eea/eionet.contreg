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

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.SimpleError;

import org.quartz.SchedulerException;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestDAO;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.HarvestDTO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.RawTripleDTO;
import eionet.cr.harvest.HarvestException;
import eionet.cr.harvest.scheduled.UrgentHarvestQueue;
import eionet.cr.util.Pair;
import eionet.cr.util.URLUtil;

/**
 * @author altnyris
 */
@UrlBinding("/source.action")
public class HarvestSourceActionBean extends AbstractActionBean {
	
	/** */
	private static final String ADD_EVENT = "add";
	private static final String EDIT_EVENT = "edit";
	
	/** */
	private HarvestSourceDTO harvestSource;
	private List<HarvestDTO> harvests;
	
	/** */
	private List<RawTripleDTO> sampleTriples;
	
	/** */
	private int noOfResources = 0; // number of distinct resources harvested from this source right now
	
	/** */
	private int intervalMultiplier;
	private static LinkedHashMap<Integer,String> intervalMultipliers;
	
	/** */
	private static final List<Pair<String,String>> tabs;
	private String selectedTab = "view";
	
	/** */
	static {
		tabs = new LinkedList<Pair<String,String>>();
		tabs.add(new Pair<String,String>("view", "View"));
		tabs.add(new Pair<String,String>("sampleTriples", "Sample triples"));
		tabs.add(new Pair<String,String>("history", "History"));
	}
	
	/**
	 * 
	 * @return
	 */
	public HarvestSourceDTO getHarvestSource() {
		return harvestSource;
	}
	
	/**
	 * 
	 * @param harvestSource
	 */
	public void setHarvestSource(HarvestSourceDTO harvestSource) {
		this.harvestSource = harvestSource;
	}

	/**
	 * 
	 * @return
	 */
	public List<HarvestDTO> getHarvests() {
		return harvests;
	}

    /**
     * 
     * @return
     * @throws DAOException
     */
    @DefaultHandler
    @HandlesEvent("view")
    public Resolution view() throws DAOException{
    	selectedTab = "view";
    	prepareDTO();
    	return new ForwardResolution("/pages/viewsource.jsp");
    }
    
    /**
     * @return
     * @throws DAOException
     */
    @HandlesEvent("history")
    public Resolution history() throws DAOException{
    	
    	selectedTab = "history";
    	prepareDTO();
		if (harvestSource!=null){
			// populate history of harvests
			harvests = factory.getDao(HarvestDAO.class).getHarvestsBySourceId(harvestSource.getSourceId());
		}
    
    	return new ForwardResolution("/pages/viewsource.jsp");
    }

    /**
     * @return
     * @throws DAOException
     */
    @HandlesEvent("sampleTriples")
    public Resolution sampleTriples() throws DAOException{
    	selectedTab = "sampleTriples";
    	prepareDTO();
		if (harvestSource!=null){
			// populate sample triples
			populateSampleTriples();
		}
    
    	return new ForwardResolution("/pages/viewsource.jsp");
    }

    private void prepareDTO() throws DAOException {
    	if (harvestSource!=null){
    		Integer sourceId = harvestSource.getSourceId();
    		String url = harvestSource.getUrl();
		
			if (sourceId!=null){
				harvestSource = factory.getDao(HarvestSourceDAO.class).getHarvestSourceById(sourceId);
			}
			else if (url!=null && url.trim().length()>0){
				harvestSource = factory.getDao(HarvestSourceDAO.class).getHarvestSourceByUrl(url);
			}
    	}
    }
    
    /**
     * @throws DAOException 
     * 
     */
    private void populateSampleTriples() throws DAOException{
    	Pair<Integer, List<RawTripleDTO>> temp =
    		DAOFactory.get().getDao(HelperDAO.class).getSampleTriples(
    				harvestSource.getUrl(), 10);
    	noOfResources = temp.getLeft();
    	sampleTriples = temp.getRight();
    }

	/**
	 * 
	 * @return
	 * @throws DAOException
	 * @throws SchedulerException 
	 * @throws HarvestException 
	 */
    public Resolution add() throws DAOException, SchedulerException, HarvestException {
		
		Resolution resolution = new ForwardResolution("/pages/addsource.jsp");
		if(isUserLoggedIn()){
			if (isPostRequest()){
				
				if (validateAddEdit()){
				
					// create new harvest source
					factory.getDao(HarvestSourceDAO.class).addSource(getHarvestSource(), getUserName());

					// set up the resolution
					resolution = new ForwardResolution(HarvestSourcesActionBean.class);

					// schedule urgent harvest, unless explicitly requested not to
					if (getContext().getRequestParameter("dontHarvest")==null){
						UrgentHarvestQueue.addPullHarvest(getHarvestSource().getUrl());
						addSystemMessage("Harvest source successfully created and scheduled for urgent harvest!");
					}
					else{
						addSystemMessage("Harvest source successfully created!");
					}
				}
			}
		}
		else
			addWarningMessage(getBundle().getString("not.logged.in"));
		
		return resolution;
    }
	
    /**
     * 
     * @return
     * @throws DAOException
     * @throws SchedulerException 
     */
    public Resolution edit() throws DAOException, SchedulerException {
    	
    	Resolution resolution = new ForwardResolution("/pages/editsource.jsp");
		if(isUserLoggedIn()){
			if (isPostRequest()){
				
				if (validateAddEdit()){
				
					factory.getDao(HarvestSourceDAO.class).editSource(getHarvestSource());
					addSystemMessage(getBundle().getString("update.success"));
				}
			}
			else
				harvestSource = factory.getDao(HarvestSourceDAO.class).getHarvestSourceById(harvestSource.getSourceId());
		}
		else
			addWarningMessage(getBundle().getString("not.logged.in"));
		
        return resolution;
    }
    
    /**
     * 
     * @return
     * @throws DAOException 
     * @throws HarvestException 
     */
    public Resolution scheduleUrgentHarvest() throws DAOException, HarvestException{

    	// we need to re-fetch this.harvestSource, because the requested post has nulled it
    	harvestSource = factory.getDao(HarvestSourceDAO.class).getHarvestSourceById(harvestSource.getSourceId());
    	
    	// schedule the harvest
    	UrgentHarvestQueue.addPullHarvest(getHarvestSource().getUrl());
    	
		// retrieve list of harvests (for display) 
		harvests = factory.getDao(HarvestDAO.class).getHarvestsBySourceId(harvestSource.getSourceId());

		addSystemMessage("Successfully scheduled for urgent harvest!");
    	return new ForwardResolution("/pages/viewsource.jsp");
    }
    
    /**
     * 
     * @return
     */
    public Resolution goToEdit(){
    	if (harvestSource!=null)
    		return new RedirectResolution(getUrlBinding() + "?edit=&harvestSource.sourceId=" + harvestSource.getSourceId());
    	else
    		return new ForwardResolution("/pages/viewsource.jsp");
    }

    /**
     * 
     */
    public boolean validateAddEdit(){
    	
    	if (isPostRequest()){
    		
	    	if (harvestSource.getUrl()==null || harvestSource.getUrl().trim().length()==0 || !URLUtil.isURL(harvestSource.getUrl())){
	    		addGlobalValidationError(new SimpleError("Invalid URL!"));
	    	}
	    	
	    	if (harvestSource.getUrl()!=null && harvestSource.getUrl().indexOf("#")>=0){
	    		addGlobalValidationError(new SimpleError("URL with a fragment part not allowed!"));
	    	}
	    	
	    	if (harvestSource.getUrl()!=null && URLUtil.isNotExisting(harvestSource.getUrl())){
	    		addGlobalValidationError(new SimpleError("There is no resource existing behind this URL!"));
	    	}

	    	if (harvestSource.getIntervalMinutes()!=null){
		    	if (harvestSource.getIntervalMinutes().intValue()<0 || intervalMultiplier<0){
		    		addGlobalValidationError(new SimpleError("Harvest interval must be >=0"));
		    	}
		    	else{
		    		harvestSource.setIntervalMinutes(new Integer(harvestSource.getIntervalMinutes().intValue() * intervalMultiplier));
		    	}
	    	}
	    	else
	    		harvestSource.setIntervalMinutes(new Integer(0));
    	}
    	
    	return getContext().getValidationErrors()==null || getContext().getValidationErrors().isEmpty();
    }

	/**
	 * @return the sampleTriples
	 */
	public List<RawTripleDTO> getSampleTriples() {
		return sampleTriples;
	}

	/**
	 * @return the noOfResources
	 */
	public int getNoOfResources() {
		return noOfResources;
	}

	/**
	 * @param intervalMultiplier the intervalMultiplier to set
	 */
	public void setIntervalMultiplier(int intervalMultiplier) {
		this.intervalMultiplier = intervalMultiplier;
	}
	
	/**
	 * 
	 * @return
	 */
	public Map<Integer,String> getIntervalMultipliers(){
		
		if (intervalMultipliers==null){
			intervalMultipliers = new LinkedHashMap<Integer,String>();
			intervalMultipliers.put(new Integer(1), "minutes");
			intervalMultipliers.put(new Integer(60), "hours");
			intervalMultipliers.put(new Integer(1440), "days");
			intervalMultipliers.put(new Integer(10080), "weeks");
		}
		
		return intervalMultipliers;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getSelectedIntervalMultiplier(){
		return getIntervalMultipliers().keySet().iterator().next().intValue();
	}
	
	/**
	 * 
	 * @return
	 */
	public String getIntervalMinutesDisplay(){
		
		String result = "";
		if (this.harvestSource!=null && this.harvestSource.getIntervalMinutes()!=null){
			result = HarvestSourceActionBean.getMinutesDisplay(this.harvestSource.getIntervalMinutes().intValue());
		}
		
		return result;
	}

	/**
	 * 
	 * @param minutes
	 * @return
	 */
	private static String getMinutesDisplay(int minutes){
		
		int days = minutes / 1440;
		minutes = minutes - (days * 1440);
		int hours = minutes / 60;
		minutes = minutes - (hours * 60);

		StringBuffer buf = new StringBuffer();
		if (days>0){
			buf.append(days).append(days==1 ? " day" : " days");
		}
		if (hours>0){
			buf.append(buf.length()>0 ? ", " : "").append(hours).append(hours==1 ? " hour" : " hours");
		}
		if (minutes>0){
			buf.append(buf.length()>0 ? ", " : "").append(minutes).append(minutes==1 ? " minute" : " minutes");
		}
		
		return buf.toString();
	}

	/**
	 * @return the tabs
	 */
	public List<Pair<String, String>> getTabs() {
		return HarvestSourceActionBean.tabs;
	}

	/**
	 * @return the selectedTab
	 */
	public String getSelectedTab() {
		return selectedTab;
	}

	/**
	 * @param selectedTab the selectedTab to set
	 */
	public void setSelectedTab(String selectedTab) {
		this.selectedTab = selectedTab;
	}
}
