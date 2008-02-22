package eionet.cr.web.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eionet.cr.config.GeneralConfig;
import eionet.cr.util.CRException;
import eionet.cr.web.context.CRActionBeanContext;
import eionet.cr.web.security.CRUser;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.SimpleMessage;
import net.sourceforge.stripes.validation.SimpleError;

/**
 * Root class for all CR ActionBeans.
 * 
 * @author altnyris
 *
 */
public abstract class AbstractCRActionBean implements ActionBean {
	
	private static Log logger = LogFactory.getLog(AbstractCRActionBean.class);
	
	private CRActionBeanContext context;
	
	/**
	 * @return action bean context.
	 */
	@Override
	public CRActionBeanContext getContext() {
		return this.context;
	}

	/**
	 * Method sets {@link CRActionBeanContext}.
	 */
	@Override
	public void setContext(ActionBeanContext context) {
		this.context = (CRActionBeanContext) context; 
	}
	
	/**
	 * @return logged in user name or default value for not logged in users.
	 */
	public final String getUserName() {
		CRUser crUser = getContext().getCrUser();
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
	void handleCrException(CRException exception) {
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
	
	/**
	 * 
	 * @param String message
	 */
	void showMessage(String msg) {
		getContext().setSeverity(GeneralConfig.SEVERITY_INFO);
		getContext().getMessages().add(new SimpleMessage(msg));
	}
	

}
