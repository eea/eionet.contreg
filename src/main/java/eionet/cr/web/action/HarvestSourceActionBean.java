package eionet.cr.web.action;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
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
	 * @throws DAOException
	 */
	@DefaultHandler
    public Resolution add() throws DAOException {
		if(isUserLoggedIn())
			DAOFactory.getDAOFactory().getHarvestSourceDAO().addSource(getHarvestSource(), getUserName());
		else
			handleCrException("You are not logged in!", GeneralConfig.SEVERITY_WARNING);
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
			showMessage("Successfully updated!");
		} else {
			handleCrException("You are not logged in!", GeneralConfig.SEVERITY_WARNING);
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
    	return new RedirectResolution("/pages/viewsource.jsp").flash(this);
    }
    
    /**
     * 
     * @return
     * @throws DAOException
     */
    public Resolution delete() throws DAOException {
		if(isUserLoggedIn()){
			DAOFactory.getDAOFactory().getHarvestSourceDAO().deleteSource(getHarvestSource());
			showMessage("Harvesting source deleted!");
		} else {
			handleCrException("You are not logged in!", GeneralConfig.SEVERITY_WARNING);
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
    	
    	harvestSource = DAOFactory.getDAOFactory().getHarvestSourceDAO().getHarvestSourceById(harvestSource.getSourceId());
		DefaultHarvestListener harvestListener = new DefaultHarvestListener(harvestSource, "pull", null);
		Harvester.pull(harvestListener);
		
		showMessage("Statements in total: " + harvestListener.getCountTotalStatements());
		showMessage("Statements with literal objects: " + harvestListener.getCountLitObjStatements());
		showMessage("Statements with resource objects: " + harvestListener.getCountResObjStatements());
		showMessage("Resources in total: " + harvestListener.getCountTotalResources());
		showMessage("Resources as encoding schemes: " + harvestListener.getCountEncodingSchemes());
		
		
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
