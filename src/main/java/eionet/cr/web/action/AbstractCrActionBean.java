package eionet.cr.web.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eionet.cr.util.CrException;
import eionet.cr.web.context.CrActionBeanContext;
import eionet.cr.web.security.CrUser;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.validation.SimpleError;

/**
 * Root class for all CR ActionBeans.
 * 
 * @author altnyris
 *
 */
public abstract class AbstractCrActionBean implements ActionBean {
	
	private static Log logger = LogFactory.getLog(AbstractCrActionBean.class);
	
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
	
	/**
	 * Method handles {@link CrException}
	 * 
	 * @param exception exception to handle.
	 */
	void handleCrException(CrException exception) {
		logger.error(exception.getMessage(), exception);
		getContext().getMessages().add(new SimpleError(exception.getMessage()));
	}
	
	/**
	 * 
	 * @param String exception to handle.
	 */
	void handleCrException(String exception, int severity) {
		logger.error(exception);
		getContext().setSeverity(severity);
		getContext().getMessages().add(new SimpleError(exception));
	}
	

}
