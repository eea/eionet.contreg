package eionet.cr.web.action;

import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eionet.cr.common.CRException;
import eionet.cr.config.GeneralConfig;
import eionet.cr.web.context.CRActionBeanContext;
import eionet.cr.web.security.CRUser;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.SimpleMessage;
import net.sourceforge.stripes.controller.AnnotatedClassActionResolver;
import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.validation.SimpleError;

/**
 * Root class for all CR ActionBeans.
 * 
 * @author altnyris
 *
 */
public abstract class AbstractCRActionBean implements ActionBean {
	
	/** */
	private static Log logger = LogFactory.getLog(AbstractCRActionBean.class);
	
	/** */
	private CRActionBeanContext context;
	
	/*
	 * (non-Javadoc)
	 * @see net.sourceforge.stripes.action.ActionBean#getContext()
	 */
	public CRActionBeanContext getContext() {
		return this.context;
	}

	/*
	 * (non-Javadoc)
	 * @see net.sourceforge.stripes.action.ActionBean#setContext(net.sourceforge.stripes.action.ActionBeanContext)
	 */
	public void setContext(ActionBeanContext context) {
		this.context = (CRActionBeanContext) context; 
	}
	
	/**
	 * @return logged in user name or default value for not logged in users.
	 */
	public final String getUserName() {
		CRUser crUser = getContext().getCRUser();
		return crUser.getUserName();
	}
	
	/**
	 * Method checks whether user is logged in or not.
	 * 
	 * @return true if user is logged in.
	 */
	public final boolean isUserLoggedIn() {
		return getCRUser()!=null;
	}
	
	/**
	 * 
	 * @return
	 */
	protected CRUser getCRUser(){
		return getContext().getCRUser();
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

	/**
	 * 
	 * @return
	 */
    public ResourceBundle getBundle() {
    	ResourceBundle bundle = ResourceBundle.getBundle("/StripesResources");
        return bundle;
    }
    
    /**
     * 
     * @return
     */
    public String getUrlBinding(){
    	AnnotatedClassActionResolver resolver = new AnnotatedClassActionResolver();
    	return resolver.getUrlBinding(this.getClass());
    }
}
