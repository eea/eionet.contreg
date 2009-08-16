/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.cr.web.action;

import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.Message;
import net.sourceforge.stripes.action.SimpleMessage;
import net.sourceforge.stripes.controller.AnnotatedClassActionResolver;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.ValidationError;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eionet.cr.common.CRException;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.mysql.MySQLDAOFactory;
import eionet.cr.harvest.Harvest;
import eionet.cr.harvest.scheduled.HarvestingJob;
import eionet.cr.search.util.PredicateLabels;
import eionet.cr.web.context.CRActionBeanContext;
import eionet.cr.web.security.CRUser;

/**
 * Root class for all CR ActionBeans.
 * 
 * @author altnyris
 *
 */
public abstract class AbstractActionBean implements ActionBean {
	
	/** */
	private static final String SESSION_MESSAGES = AbstractActionBean.class.getName() + ".sessionMessages";
	
	/** */
	private static Log logger = LogFactory.getLog(AbstractActionBean.class);
	
	/** */
	private CRActionBeanContext context;
	
	/** */
	private Harvest currentHarvest = HarvestingJob.getCurrentHarvest();
	
	protected MySQLDAOFactory factory = MySQLDAOFactory.get();
	
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
		return getCRUser().getUserName();
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
	 * @param msg
	 */
	void showMessage(Message msg) {
		getContext().setSeverity(GeneralConfig.SEVERITY_INFO);
		getContext().getMessages().add(msg);
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
    
    /**
     * 
     * @return
     */
    public boolean isPostRequest(){
    	return getContext().getRequest().getMethod().equalsIgnoreCase("POST");
    }

    /**
     * 
     * @return
     */
    public boolean isGetRequest(){
    	return getContext().getRequest().getMethod().equalsIgnoreCase("GET");
    }

    /**
     * 
     * @param field
     * @param error
     */
	public void addFieldError(String field, ValidationError error) {
		context.getValidationErrors().add(field, error);
	}

	/**
	 * 
	 * @param error
	 */
	public void addGlobalError(ValidationError error) {
		context.getValidationErrors().addGlobalError(error);
	}

	/**
	 * 
	 * @param message
	 */
	public void addMessage(Message message) {
		context.getMessages().add(message);
	}

	/**
	 * 
	 * @param message
	 */
	public void addMessage(String message) {
		addMessage(new SimpleMessage(message));
	}
	
	/**
	 * 
	 * @return
	 */
	public Harvest getCurrentHarvest(){
		return currentHarvest;
	}
	
	/**
	 * 
	 * @return
	 */
	protected Set<String> createPreferredLanguages(){
		
		Set<String> result = new HashSet<String>();
		
		String languagePreferences = getContext().getRequest().getHeader("Accept-Language");
		if (!StringUtils.isBlank(languagePreferences)){
			String[] languages = StringUtils.split(languagePreferences, ',');
			for (int i=0; i<languages.length; i++){
				result.add(PredicateLabels.parseHTTPAcceptedLanguage(languages[i]));
			}
		}
			
		return result;
	}
}
