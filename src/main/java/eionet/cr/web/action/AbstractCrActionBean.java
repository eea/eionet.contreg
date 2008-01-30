package eionet.cr.web.action;

import eionet.cr.web.context.CrActionBeanContext;
import eionet.cr.web.security.CrUser;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;

import static eionet.cr.web.util.ICrWebConstants.*;

/**
 * Root class for all CR ActionBeans.
 * 
 * @author gerasvad, altnyris
 *
 */
public abstract class AbstractCrActionBean implements ActionBean {
	
	private CrActionBeanContext context;
	
	/**
	 * @return action bean context.
	 */
	@Override
	public CrActionBeanContext getContext() {
		return this.context;
	}

	/**
	 * Method sets {@link CrActionBeanContext}.
	 */
	@Override
	public void setContext(ActionBeanContext context) {
		this.context = (CrActionBeanContext) context; 
	}
	
	/**
	 * @return logged in user name or default value for not logged in users.
	 */
	public final String getUserName() {
		CrUser crUser = getContext().getCrUser();
		return crUser.getUserName();
	}
	
	/**
	 * Method checks whether user is logged in or not.
	 * 
	 * @return true if user is logged in.
	 */
	public final boolean isUserLoggedIn() {
		return getContext().getCrUser() != null;
	}

}
