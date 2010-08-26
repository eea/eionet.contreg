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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.http.HttpSession;

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
import eionet.cr.dao.DAOFactory;
import eionet.cr.harvest.CurrentHarvests;
import eionet.cr.harvest.Harvest;
import eionet.cr.harvest.scheduled.HarvestingJob;
import eionet.cr.util.Util;
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
	private static final String SYSTEM_MESSAGES = "systemMessages";
	private static final String CAUTION_MESSAGES = "cautionMessages";
	private static final String WARNING_MESSAGES = "warningMessages";
	
	/** */
	protected static Log logger = LogFactory.getLog(AbstractActionBean.class);
	
	/** */
	private CRActionBeanContext context;
	
	/** */
	private Harvest currentHarvest = CurrentHarvests.getQueuedHarvest();//HarvestingJob.getCurrentHarvest();
	
	/** */
	protected DAOFactory factory = DAOFactory.get();
	
	/** */
	private HashSet<String> acceptedLanguages;
	private List<String> acceptedLanguagesByImportance;
	
	protected boolean homeContext;
	
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
		return getUser().getUserName();
	}
	
	/**
	 * Method checks whether user is logged in or not.
	 * 
	 * @return true if user is logged in.
	 */
	public final boolean isUserLoggedIn() {
		return getUser()!=null;
	}
	
	/**
	 * 
	 * @return
	 */
	public CRUser getUser(){
		return getContext().getCRUser();
	}
	
	/**
	 * 
	 * @param message
	 */
	protected void addSystemMessage(String message){
		getContext().getMessages(SYSTEM_MESSAGES).add(new SimpleMessage(message));
	}

	/**
	 * 
	 * @param message
	 */
	protected void addCautionMessage(String message){
		getContext().getMessages(CAUTION_MESSAGES).add(new SimpleMessage(message));
	}

	/**
	 * 
	 * @param message
	 */
	protected void addWarningMessage(String message){
		getContext().getMessages(WARNING_MESSAGES).add(new SimpleMessage(message));
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
	public void addFieldValidationError(String field, ValidationError error) {
		context.getValidationErrors().add(field, error);
	}

	/**
	 * 
	 * @param error
	 */
	public void addGlobalValidationError(ValidationError error) {
		context.getValidationErrors().addGlobalError(error);
	}

	/**
	 * 
	 * @param simpleErrorMessage
	 */
	public void addGlobalValidationError(String simpleErrorMessage) {
		context.getValidationErrors().addGlobalError(new SimpleError(simpleErrorMessage));
	}

	/**
	 * 
	 * @return
	 */
	public boolean hasValidationErrors() {
		return context.getValidationErrors()!=null && !context.getValidationErrors().isEmpty();
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
	public HashSet<String> getAcceptedLanguages(){
		
		if (acceptedLanguages==null){
			acceptedLanguages = Util.getAcceptedLanguages(getContext().getRequest().getHeader("Accept-Language"));
		}
		return acceptedLanguages;
	}

	/**
	 * 
	 * @return
	 */
	public List<String> getAcceptedLanguagesByImportance(){
		if (acceptedLanguagesByImportance == null){
			acceptedLanguagesByImportance = Util.getAcceptedLanguagesByImportance(getContext().getRequest().getHeader("Accept-Language"));
		}
		return acceptedLanguagesByImportance;
	}
	
	
	/**
	 * @return session associated with current request
	 */
	protected HttpSession getSession() {
		return context.getRequest().getSession();
	}
	
	/**
	 * 
	 * @return
	 */
	public String[] excludeFromSortAndPagingUrls(){
		return null;
	}

	public boolean isHomeContext() {
		return homeContext;
	}

	public void setHomeContext(boolean homeContext) {
		this.homeContext = homeContext;
	}
}
