package eionet.cr.web.action;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dto.HarvestSourceDTO;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

/**
 * @author altnyris
 */
@UrlBinding("/source.action")
public class HarvestSourceActionBean extends AbstractCrActionBean {
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
        return new ForwardResolution("/pages/addsource.jsp");
    }

}
