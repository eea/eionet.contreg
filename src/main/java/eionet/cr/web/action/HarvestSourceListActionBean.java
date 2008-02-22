package eionet.cr.web.action;

import java.util.List;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dto.HarvestSourceDTO;
import net.sourceforge.stripes.action.UrlBinding;

/**
 * @author altnyris
 *
 */
public class HarvestSourceListActionBean extends AbstractCRActionBean {
	private List<HarvestSourceDTO> harvestSources;

	public List<HarvestSourceDTO> getHarvestSources() throws DAOException {
		if(harvestSources == null)
			harvestSources = DAOFactory.getDAOFactory().getHarvestSourceDAO().getHarvestSources();
		return harvestSources;
	}

	public void setHarvestSources(List<HarvestSourceDTO> harvestSources) {
		this.harvestSources = harvestSources;
	} 
}
