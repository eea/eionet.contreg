package eionet.cr.web.action;

import java.util.List;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestDAO;
import eionet.cr.dto.HarvestDTO;
import eionet.cr.dto.HarvestMessageDTO;
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
	private List<HarvestMessageDTO> harvestMessages;
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

	public List<HarvestMessageDTO> getHarvestMessages() {
		return harvestMessages;
	}

	public void setHarvestMessages(List<HarvestMessageDTO> harvestMessages) {
		this.harvestMessages = harvestMessages;
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
    public Resolution addHarvestSource() throws DAOException {
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
    public Resolution preEditHarvestSource() throws DAOException {
    	harvestSource = DAOFactory.getDAOFactory().getHarvestSourceDAO().getHarvestSourceById(harvestSource.getSourceId());
        return new RedirectResolution("/pages/editsource.jsp").flash(this);
    }
    
    /**
     * 
     * @return
     * @throws DAOException
     */
    public Resolution editHarvestSource() throws DAOException {
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
    public Resolution preViewHarvestSource() throws DAOException {
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
    	harvestMessages = DAOFactory.getDAOFactory().getHarvestMessageDAO().findHarvestMessagesByHarvestID(harvest.getHarvestId());
    	return new RedirectResolution("/pages/harvest.jsp").flash(this);
    }
    
    /**
     * 
     * @return
     * @throws DAOException
     */
    public Resolution deleteHarvestSource() throws DAOException {
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
