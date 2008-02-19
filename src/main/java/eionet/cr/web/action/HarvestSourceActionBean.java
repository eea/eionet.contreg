package eionet.cr.web.action;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dto.HarvestSourceDTO;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

/**
 * @author altnyris
 */
@UrlBinding("/source.action")
public class HarvestSourceActionBean implements ActionBean {
	private ActionBeanContext context;
	private HarvestSourceDTO harvestSource; 
	
	public ActionBeanContext getContext() { 
		return context; 
	}
    public void setContext(ActionBeanContext context) { 
    	this.context = context; 
    }
	
	public HarvestSourceDTO getHarvestSource() {
		return harvestSource;
	}
	public void setHarvestSource(HarvestSourceDTO harvestSource) {
		this.harvestSource = harvestSource;
	}
	
	@DefaultHandler
    public Resolution add() throws DAOException {
        DAOFactory.getDAOFactory().getHarvestSourceDAO().addSource(getHarvestSource());
        return new ForwardResolution("/pages/addsource.jsp");
    }

}
