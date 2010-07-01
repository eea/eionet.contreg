package eionet.cr.web.action.home;

import eionet.cr.dao.DAOException;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

/**
 * 
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 *
 */

@UrlBinding("/home/{username}/workspace")
public class WorkspaceActionBean extends AbstractHomeActionBean {

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.web.action.AbstractSearchActionBean#search()
	 */
	@DefaultHandler
	public Resolution view() throws DAOException {
		setEnvironmentParams(this.getContext(), AbstractHomeActionBean.TYPE_WORKSPACE);
		return new ForwardResolution("/pages/home/workspace.jsp");
	}
	
}
