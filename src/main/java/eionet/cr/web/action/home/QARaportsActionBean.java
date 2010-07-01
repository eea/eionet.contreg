package eionet.cr.web.action.home;

import java.util.List;
import java.util.Map;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.tools.ActionBeanInfo;
import eionet.cr.dao.DAOException;

/**
 * 
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 *
 */

@UrlBinding("/home/{username}/qaraports")
public class QARaportsActionBean extends AbstractHomeActionBean {

	List<String> raportsListing;
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.web.action.AbstractSearchActionBean#search()
	 */
	@DefaultHandler
	public Resolution view() throws DAOException {
		setEnvironmentParams(this.getContext(), AbstractHomeActionBean.TYPE_QARAPORTS);
		return new ForwardResolution("/pages/home/qaraports.jsp");
	}
	
	
	public List<String> getRaportsListing() {
		return raportsListing;
	}

	public void setRaportsListing(List<String> raportsListing) {
		this.raportsListing = raportsListing;
	}

}
