package eionet.cr.web.action;

import java.util.List;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dto.HarvestSourceDTO;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

/**
 * @author altnyris
 *
 */
@UrlBinding("/sources.action")
public class HarvestSourcesActionBean extends AbstractCRActionBean {
	
	/** */
	private List<HarvestSourceDTO> harvestSources;

	/**
	 * 
	 * @return
	 * @throws DAOException 
	 */
	@DefaultHandler
	public Resolution view() throws DAOException{
		harvestSources = DAOFactory.getDAOFactory().getHarvestSourceDAO().getHarvestSources();
		return new ForwardResolution("/pages/sources.jsp");
	}

	/**
	 * @return the harvestSources
	 */
	public List<HarvestSourceDTO> getHarvestSources() {
		return harvestSources;
	}
}
