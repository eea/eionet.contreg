package eionet.cr.web.action;

import java.util.List;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dto.HarvestDTO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.harvest.Harvest;
import eionet.cr.harvest.HarvestDAOWriter;
import eionet.cr.harvest.HarvestException;
import eionet.cr.harvest.PullHarvest;
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
	 */
    public Resolution add() throws DAOException {
		
		Resolution resolution = new ForwardResolution("/pages/addsource.jsp");
		if(isUserLoggedIn()){
			if (isPostRequest()){
				DAOFactory.getDAOFactory().getHarvestSourceDAO().addSource(getHarvestSource(), getUserName());
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
     */
    public Resolution edit() throws DAOException {
    	
    	Resolution resolution = new ForwardResolution("/pages/editsource.jsp");
		if(isUserLoggedIn()){
			if (isPostRequest()){
				DAOFactory.getDAOFactory().getHarvestSourceDAO().editSource(getHarvestSource());
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
    		
    		harvestSource = DAOFactory.getDAOFactory().getHarvestSourceDAO().getHarvestSourceById(harvestSource.getSourceId());
    		Harvest harvest = new PullHarvest(harvestSource.getUrl(),
    				new HarvestDAOWriter(harvestSource.getSourceId().intValue(), Harvest.TYPE_PULL, getCRUser()==null ? null : getCRUser().getUserName()),
    				null);
    		harvest.execute();
    		
			harvests = DAOFactory.getDAOFactory().getHarvestDAO().getHarvestsBySourceId(harvestSource.getSourceId());

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
     */
    public Resolution scheduleImmediateHarvest() throws DAOException{
    	harvestSource = DAOFactory.getDAOFactory().getHarvestSourceDAO().getHarvestSourceById(harvestSource.getSourceId());
    	showMessage("Not implemented yet");
    	return new ForwardResolution("/pages/viewsource.jsp");
    }
}
