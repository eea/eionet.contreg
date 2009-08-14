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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.quartz.SchedulerException;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.HarvestDAO;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dto.HarvestDTO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.RawTripleDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.harvest.HarvestException;
import eionet.cr.harvest.scheduled.UrgentHarvestQueue;
import eionet.cr.search.SearchException;
import eionet.cr.search.SourceContentsSearch;
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
     * @throws SearchException 
     */
    @DefaultHandler
    public Resolution view() throws DAOException, SearchException {
    	
    	if (harvestSource!=null){
    		Integer sourceId = harvestSource.getSourceId();
    		String url = harvestSource.getUrl();
    		
    		if (sourceId!=null){
    			harvestSource = factory.getDao(HarvestSourceDAO.class).getHarvestSourceById(sourceId);
    		}
    		else if (url!=null && url.trim().length()>0){
    			harvestSource = factory.getDao(HarvestSourceDAO.class).getHarvestSourceByUrl(url);
    		}
    		
    		if (harvestSource!=null){
    			
    			// populate history of harvests
    			harvests = factory.getDao(HarvestDAO.class).getHarvestsBySourceId(harvestSource.getSourceId());
    			
    			// populate sample triples
    			populateSampleTriples();
    		}
    	}
    
    	return new ForwardResolution("/pages/viewsource.jsp");
    }
    
    /**
     * @throws SearchException 
     * 
     */
    private void populateSampleTriples() throws SearchException{
    	
    	SourceContentsSearch search = new SourceContentsSearch(harvestSource.getUrl());
    	search.execute();
    	noOfResources = search.getTotalMatchCount();
    	
    	sampleTriples = new ArrayList<RawTripleDTO>();
    	Collection<SubjectDTO> subjects = search.getResultList();
    	if (subjects!=null && !subjects.isEmpty()){
    		
    		for (Iterator<SubjectDTO> subjectsIter=subjects.iterator(); subjectsIter.hasNext() && sampleTriples.size()<10;){
    			
    			SubjectDTO subjectDTO = subjectsIter.next();
    			for (Iterator<String> predicatesIter=subjectDTO.getPredicates().keySet().iterator(); predicatesIter.hasNext() && sampleTriples.size()<10;){
    				
    				String predicate = predicatesIter.next();
    				
    				RawTripleDTO tripleDTO = new RawTripleDTO();
    				tripleDTO.setSubject(subjectDTO.getUri());
    				tripleDTO.setPredicate(predicate);
    				
    				ObjectDTO object = subjectDTO.getObject(predicate);
    				if (object!=null){
    					tripleDTO.setObject(object.getValue());
    					tripleDTO.setObjectDerivSource(object.getDerivSource());
    				}
    				
    				sampleTriples.add(tripleDTO);
    			}
    		}
    	}
    }

	/**
	 * 
	 * @return
	 * @throws DAOException
	 * @throws SchedulerException 
	 */
    public Resolution add() throws DAOException, SchedulerException {
		
		Resolution resolution = new ForwardResolution("/pages/addsource.jsp");
		if(isUserLoggedIn()){
			if (isPostRequest()){
				factory.getDao(HarvestSourceDAO.class).addSource(getHarvestSource(), getUserName());
				resolution = new ForwardResolution(HarvestSourcesActionBean.class);
			}
		}
		else
			handleCrException(getBundle().getString("not.logged.in"), GeneralConfig.SEVERITY_WARNING);
		
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
				factory.getDao(HarvestSourceDAO.class).editSource(getHarvestSource());
				showMessage(getBundle().getString("update.success"));
			}
			else
				harvestSource = factory.getDao(HarvestSourceDAO.class).getHarvestSourceById(harvestSource.getSourceId());
		}
		else
			handleCrException(getBundle().getString("not.logged.in"), GeneralConfig.SEVERITY_WARNING);
		
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

    	showMessage("Successfully scheduled for urgent harvest!");
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

    @ValidationMethod(on={ADD_EVENT,EDIT_EVENT})
    public void validateAddEdit(){
    	
    	if (isPostRequest()){
    		
	    	if (harvestSource.getUrl()==null || harvestSource.getUrl().trim().length()==0 || !URLUtil.isURL(harvestSource.getUrl()))
	    		addGlobalError(new SimpleError("Invalid URL"));
	    	
	    	if (harvestSource.getIntervalMinutes()!=null){
		    	if (harvestSource.getIntervalMinutes().intValue()<0 || intervalMultiplier<0){
		    		addGlobalError(new SimpleError("Harvest interval must be >=0"));
		    	}
		    	else{
		    		harvestSource.setIntervalMinutes(new Integer(harvestSource.getIntervalMinutes().intValue() * intervalMultiplier));
		    	}
	    	}
	    	else
	    		harvestSource.setIntervalMinutes(new Integer(0));
    	}
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
}
