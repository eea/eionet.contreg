package eionet.cr.web.action;

import java.util.List;

import sun.security.action.GetBooleanAction;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dto.HarvestDTO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.harvest.DefaultHarvestListener;
import eionet.cr.harvest.HarvestException;
import eionet.cr.harvest.Harvester;
import eionet.cr.index.EncodingSchemes;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.DontValidate;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

/**
 * @author altnyris
 */
@UrlBinding("/source.action")
public class HarvestSourceActionBean extends AbstractCRActionBean {
	
	/** */
	private HarvestSourceDTO harvestSource;
	private List<HarvestDTO> harvests;
	private HarvestDTO harvest;
	
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
	

	public List<HarvestDTO> getHarvests() {
		return harvests;
	}

	public void setHarvests(List<HarvestDTO> harvests) {
		this.harvests = harvests;
	}

	public HarvestDTO getHarvest() {
		return harvest;
	}

	public void setHarvest(HarvestDTO harvest) {
		this.harvest = harvest;
	}

	/**
	 * 
	 * @return
	 * @throws DAOException
	 */
	@DefaultHandler
    public Resolution add() throws DAOException {
		if(isUserLoggedIn())
			DAOFactory.getDAOFactory().getHarvestSourceDAO().addSource(getHarvestSource(), getUserName());
		else
			handleCrException(getBundle().getString("not.logged.in"), GeneralConfig.SEVERITY_WARNING);
        return new ForwardResolution("/pages/sources.jsp");
    }
	
	/**
	 * 
	 * @return
	 * @throws DAOException
	 */
    @DontValidate
    public Resolution preEdit() throws DAOException {
    	harvestSource = DAOFactory.getDAOFactory().getHarvestSourceDAO().getHarvestSourceById(harvestSource.getSourceId());
        return new RedirectResolution("/pages/editsource.jsp").flash(this);
    }
    
    /**
     * 
     * @return
     * @throws DAOException
     */
    public Resolution edit() throws DAOException {
		if(isUserLoggedIn()){
			DAOFactory.getDAOFactory().getHarvestSourceDAO().editSource(getHarvestSource());
			showMessage(getBundle().getString("update.success"));
		} else {
			handleCrException(getBundle().getString("not.logged.in"), GeneralConfig.SEVERITY_WARNING);
		}
        return new ForwardResolution("/pages/editsource.jsp");
    }
    
    /**
     * 
     * @return
     * @throws DAOException
     */
    @DontValidate
    public Resolution preView() throws DAOException {
    	harvestSource = DAOFactory.getDAOFactory().getHarvestSourceDAO().getHarvestSourceById(harvestSource.getSourceId());
    	harvests = DAOFactory.getDAOFactory().getHarvestDAO().getHarvestsBySourceId(harvestSource.getSourceId());
    	return new RedirectResolution("/pages/viewsource.jsp").flash(this);
    }
    
    /**
     * 
     * @return
     * @throws DAOException
     */
    @DontValidate
    public Resolution preViewHarvest() throws DAOException {
    	harvest = DAOFactory.getDAOFactory().getHarvestSourceDAO().getHarvestById(harvest.getHarvestId());
    	harvestSource = DAOFactory.getDAOFactory().getHarvestSourceDAO().getHarvestSourceById(harvest.getHarvestSourceId());
    	return new RedirectResolution("/pages/harvest.jsp").flash(this);
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
		} else {
			handleCrException(getBundle().getString("not.logged.in"), GeneralConfig.SEVERITY_WARNING);
		}
        return new ForwardResolution("/pages/sources.jsp");
    }
    
    /**
     * 
     * @return
     * @throws HarvestException 
     * @throws DAOException 
     */
    public Resolution harvestNow() throws HarvestException, DAOException{
    	
    	if(isUserLoggedIn()){
	    	harvestSource = DAOFactory.getDAOFactory().getHarvestSourceDAO().getHarvestSourceById(harvestSource.getSourceId());
			DefaultHarvestListener harvestListener = new DefaultHarvestListener(harvestSource, "pull", getCRUser());
			Harvester.pull(harvestListener);
			
			showMessage("Statements in total: " + harvestListener.getCountTotalStatements());
			showMessage("Statements with literal objects: " + harvestListener.getCountLiteralStatements());
			showMessage("Statements with resource objects: " + (harvestListener.getCountTotalStatements() - harvestListener.getCountLiteralStatements()));
			showMessage("Resources in total: " + harvestListener.getCountTotalResources());
			showMessage("Resources as encoding schemes: " + harvestListener.getCountEncodingSchemes());
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
     */
    public Resolution scheduleImmediateHarvest() throws DAOException{
    	harvestSource = DAOFactory.getDAOFactory().getHarvestSourceDAO().getHarvestSourceById(harvestSource.getSourceId());
    	showMessage("Not implemented yet");
    	return new ForwardResolution("/pages/viewsource.jsp");
    }
}
