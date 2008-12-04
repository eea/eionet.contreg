package eionet.cr.web.action;

import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.quartz.SchedulerException;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dto.HarvestDTO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.harvest.Harvest;
import eionet.cr.harvest.HarvestDAOWriter;
import eionet.cr.harvest.HarvestException;
import eionet.cr.harvest.PullHarvest;
import eionet.cr.harvest.scheduled.CronHarvestQueueingJob;
import eionet.cr.harvest.scheduled.HarvestQueue;
import eionet.cr.util.URLUtil;
import eionet.cr.util.Util;

/**
 * @author altnyris
 */
@UrlBinding("/source.action")
public class HarvestSourceActionBean extends AbstractCRActionBean {
	
	/** */
	private static final String ADD_EVENT = "add";
	private static final String EDIT_EVENT = "edit";
	
	/** */
	private HarvestSourceDTO harvestSource;
	private List<HarvestDTO> harvests;
	
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
    public Resolution view() throws DAOException {
    	harvestSource = DAOFactory.getDAOFactory().getHarvestSourceDAO().getHarvestSourceById(harvestSource.getSourceId());
    	harvests = DAOFactory.getDAOFactory().getHarvestDAO().getHarvestsBySourceId(harvestSource.getSourceId());
    	return new ForwardResolution("/pages/viewsource.jsp");
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
				DAOFactory.getDAOFactory().getHarvestSourceDAO().addSource(getHarvestSource(), getUserName());
				CronHarvestQueueingJob.scheduleCronHarvest(getHarvestSource().getScheduleCron());
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
				DAOFactory.getDAOFactory().getHarvestSourceDAO().editSource(getHarvestSource());
				CronHarvestQueueingJob.scheduleCronHarvest(getHarvestSource().getScheduleCron());
				showMessage(getBundle().getString("update.success"));
			}
			else
				harvestSource = DAOFactory.getDAOFactory().getHarvestSourceDAO().getHarvestSourceById(harvestSource.getSourceId());
		}
		else
			handleCrException(getBundle().getString("not.logged.in"), GeneralConfig.SEVERITY_WARNING);
		
        return resolution;
    }
    
    
    /**
     * 
     * @return
     * @throws DAOException
     */
    public Resolution delete() throws DAOException {
		if(isUserLoggedIn()){
			DAOFactory.getDAOFactory().getHarvestSourceDAO().deleteSource(getHarvestSource());
			showMessage(getBundle().getString("harvet.source.deleted"));
		}
		else
			handleCrException(getBundle().getString("not.logged.in"), GeneralConfig.SEVERITY_WARNING);
		
        return new ForwardResolution(HarvestSourcesActionBean.class);
    }
    
    /**
     * 
     * @return
     * @throws HarvestException 
     * @throws DAOException 
     */
    public Resolution harvestNow() throws HarvestException, DAOException{
    	
    	if(isUserLoggedIn()){
    		
    		// do the harvest
    		harvestSource = DAOFactory.getDAOFactory().getHarvestSourceDAO().getHarvestSourceById(harvestSource.getSourceId());
    		Harvest harvest = new PullHarvest(harvestSource.getUrl(), null); // TODO - use proper lastHarvestTimestamp instead of null
    		harvest.setDaoWriter(new HarvestDAOWriter(
    				harvestSource.getSourceId().intValue(), Harvest.TYPE_PULL, getCRUser()==null ? null : getCRUser().getUserName()));
    		harvest.execute();
    		
    		// retrieve list of harvests (for display) 
			harvests = DAOFactory.getDAOFactory().getHarvestDAO().getHarvestsBySourceId(harvestSource.getSourceId());

			// set messages to show to user
			showMessage("Resources in total: " + harvest.getCountTotalResources());
			showMessage("Resources as encoding schemes: " + harvest.getCountEncodingSchemes());
			showMessage("Statements in total: " + harvest.getCountTotalStatements());
			showMessage("Statements with literal objects: " + harvest.getCountLiteralStatements());
    	}
    	else{
    		handleCrException(getBundle().getString("not.logged.in"), GeneralConfig.SEVERITY_WARNING);
    	}
		
    	return new ForwardResolution("/pages/viewsource.jsp");
    }
    
    /**
     * 
     * @return
     * @throws DAOException 
     * @throws HarvestException 
     */
    public Resolution scheduleUrgentHarvest() throws DAOException, HarvestException{

    	// we need to re-fect this.harvestSource, because the post request has nulled it
    	harvestSource = DAOFactory.getDAOFactory().getHarvestSourceDAO().getHarvestSourceById(harvestSource.getSourceId());
    	
    	HarvestQueue.addPullHarvest(getHarvestSource().getUrl(), HarvestQueue.PRIORITY_URGENT);
    	showMessage("Successfully scheduled for urgent harvest!");
    	return new ForwardResolution("/pages/viewsource.jsp");
    }

    @ValidationMethod(on={ADD_EVENT,EDIT_EVENT})
    public void validateAddEdit(){
    	
    	if (isPostRequest()){
	    	if (harvestSource.getUrl()==null || harvestSource.getUrl().trim().length()==0 || !URLUtil.isURL(harvestSource.getUrl()))
	    		addGlobalError(new SimpleError("Invalid URL"));
	    	
	    	if (!Util.isNullOrEmpty(harvestSource.getScheduleCron()) && !Util.isValidQuartzCronExpression(harvestSource.getScheduleCron())){
	    		addGlobalError(new SimpleError("Invalid Quartz cron expression"));
	    	}

    	}
    }
}
