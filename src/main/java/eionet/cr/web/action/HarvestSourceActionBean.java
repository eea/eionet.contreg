package eionet.cr.web.action;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dto.HarvestSourceDTO;
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
	private HarvestSourceDTO harvestSource; 
	
	
	public HarvestSourceDTO getHarvestSource() {
		return harvestSource;
	}
	public void setHarvestSource(HarvestSourceDTO harvestSource) {
		this.harvestSource = harvestSource;
	}
	
	@DefaultHandler
    public Resolution add() throws DAOException {
		if(isUserLoggedIn())
			DAOFactory.getDAOFactory().getHarvestSourceDAO().addSource(getHarvestSource(), getUserName());
		else
			handleCrException("You are not logged in!", GeneralConfig.SEVERITY_WARNING);
        return new ForwardResolution("/pages/sources.jsp");
    }
	
	/** Loads a bug on to the form ready for editing. */
    @DontValidate
    public Resolution preEdit() throws DAOException {
    	HarvestSourceDTO harvestSource = new HarvestSourceDTO();
        this.harvestSource = harvestSource.getHarvestSource( this.harvestSource.getSourceId() );
        return new RedirectResolution("/pages/editsource.jsp").flash(this);
    }
    
    public Resolution edit() throws DAOException {
		if(isUserLoggedIn()){
			DAOFactory.getDAOFactory().getHarvestSourceDAO().editSource(getHarvestSource());
			showMessage("Successfully updated!");
		} else {
			handleCrException("You are not logged in!", GeneralConfig.SEVERITY_WARNING);
		}
        return new ForwardResolution("/pages/editsource.jsp");
    }
    
    /** Loads a bug on to the form ready for viewing. */
    @DontValidate
    public Resolution preView() throws DAOException {
    	HarvestSourceDTO harvestSource = new HarvestSourceDTO();
        this.harvestSource = harvestSource.getHarvestSource( this.harvestSource.getSourceId() );
        return new RedirectResolution("/pages/viewsource.jsp").flash(this);
    }

}
