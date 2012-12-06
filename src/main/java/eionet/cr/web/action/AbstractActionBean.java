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

import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.http.HttpSession;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.Message;
import net.sourceforge.stripes.action.SimpleMessage;
import net.sourceforge.stripes.controller.AnnotatedClassActionResolver;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.ValidationError;
import nl.bitwalker.useragentutils.Browser;
import nl.bitwalker.useragentutils.BrowserType;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eionet.cr.dao.DAOFactory;
import eionet.cr.harvest.CurrentHarvests;
import eionet.cr.harvest.Harvest;
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

    /** session attribute name for scripts in clipboard. */
    protected static final String SCRIPTS_CLIPBOARD = AbstractActionBean.class.getName() + ".clipboardScripts";
    /** session attribute name for last action in clipboard. */
    protected static final String SCRIPTS_CLIPBOARD_ACTION = AbstractActionBean.class.getName() + ".clipboardAction";

    /** session attribute name for scripts type in clipboard. */
    protected static final String SCRIPTS_CLIPBOARD_TYPE = AbstractActionBean.class.getName() + ".clipboardType";

    /** */
    protected static final Logger logger = Logger.getLogger(AbstractActionBean.class);

    /** */
    private CRActionBeanContext context;

    /** */
    private final Harvest currentQueuedHarvest = CurrentHarvests.getQueuedHarvest();

    /** */
    protected DAOFactory factory = DAOFactory.get();

    /** */
    private List<String> acceptedLanguages;

    /** */
    protected boolean isHomeContext;

    /*
     * (non-Javadoc)
     *
     * @see net.sourceforge.stripes.action.ActionBean#getContext()
     */
    @Override
    public CRActionBeanContext getContext() {
        return this.context;
    }

    /**
     * Sets ActionBean context.
     *
     * @param context ActionBeanContext
     * @see net.sourceforge.stripes.action.ActionBean#setContext(net.sourceforge.stripes.action.ActionBeanContext)
     */
    @Override
    public void setContext(final ActionBeanContext context) {

        if (context instanceof CRActionBeanContext) {
            this.context = (CRActionBeanContext) context;
        } else {
            throw new IllegalArgumentException("Context must be that of " + CRActionBeanContext.class.getSimpleName());
        }
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
        return getUser() != null;
    }

    /**
     * Current user in servlet context.
     *
     * @return CRUser
     */
    public CRUser getUser() {
        return getContext().getCRUser();
    }

    /**
     * Adds system message. The message will be shown in a simple rectangle and is to provide information on <i>successful</i>
     * actions.
     *
     * @param message Message text in HTML format.
     */
    public void addSystemMessage(String message) {
        getContext().getMessages(SYSTEM_MESSAGES).add(new SimpleMessage(StringEscapeUtils.escapeXml(message)));
    }

    /**
     * Same as {@link #addSystemMessage(String)}, but message added at specified index.
     *
     * @param index
     * @param message
     */
    public void addSystemMessage(int index, String message) {
        getContext().getMessages(SYSTEM_MESSAGES).add(index, new SimpleMessage(StringEscapeUtils.escapeXml(message)));
    }

    /**
     * Adds caution message. The message will be shown wrapped in the &lt;div class="caution-msg"&lt; element. A caution is less
     * severe than a warning. It can e.g. be used when the application has to say to the user that it has ignored some input.
     *
     * @param message Message text in HTML format.
     */
    public void addCautionMessage(final String message) {
        getContext().getMessages(CAUTION_MESSAGES).add(new SimpleMessage(StringEscapeUtils.escapeXml(message)));
    }

    /**
     * Adds warning message. The message will be shown wrapped in the &lt;div class="warning-msg"&lt; element.
     *
     * @param message Message text in HTML format.
     */
    public void addWarningMessage(String message) {
        getContext().getMessages(WARNING_MESSAGES).add(new SimpleMessage(StringEscapeUtils.escapeXml(message)));
    }

    /**
     * Returns Stripes resource bundle.
     *
     * @return ResourceBundle resources
     */
    public ResourceBundle getBundle() {
        ResourceBundle bundle = ResourceBundle.getBundle("/StripesResources");
        return bundle;
    }

    /**
     *
     * @return
     */
    public String getUrlBinding() {
        AnnotatedClassActionResolver resolver = new AnnotatedClassActionResolver();
        return resolver.getUrlBinding(this.getClass());
    }

    /**
     *
     * @return
     */
    public boolean isPostRequest() {
        return getContext().getRequest().getMethod().equalsIgnoreCase("POST");
    }

    /**
     *
     * @return
     */
    public boolean isGetRequest() {
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
    * Adds all given validation errors to the context.
    * @param errors List of error texts
    */
   public void addGlobalValidationErrors(List<String> errors) {
       for (String error : errors) {
           addGlobalValidationError(error);
       }
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
        return context.getValidationErrors() != null && !context.getValidationErrors().isEmpty();
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
    public Harvest getCurrentQueuedHarvest() {
        return currentQueuedHarvest;
    }

    /**
     * Get the user's preferred languages from the browser's accept-language header.
     *
     * @return an unsorted HashSet of languages.
     */
    public List<String> getAcceptedLanguages() {

        if (acceptedLanguages == null) {
            acceptedLanguages = Util.getAcceptedLanguages(getContext().getRequest().getHeader("Accept-Language"));
        }
        return acceptedLanguages;
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
    public String[] excludeFromSortAndPagingUrls() {
        return null;
    }

    /**
     *
     * @return
     */
    public boolean isHomeContext() {
        return isHomeContext;
    }

    /**
     *
     * @param isHomeContext
     */
    public void setHomeContext(boolean isHomeContext) {
        this.isHomeContext = isHomeContext;
    }

    /**
     *
     * @param context
     * @return
     */
    public String getBaseUrl(CRActionBeanContext context) {
        String url = context.getRequest().getRequestURL().toString();
        return url.substring(0, url.lastIndexOf("/pages/"));
    }

    /**
     *
     * @return
     */
    public boolean isEeaTemplate() {
        boolean ret = false;
        String use = getContext().getInitParameter("useEeaTemplate");
        if (!StringUtils.isBlank(use)) {
            ret = Boolean.valueOf(use).booleanValue();
        }
        return ret;
    }

    /**
     * Checks is client request comes from web browser.
     * @return true if request comes from web browser or Mobile browser
     */
    protected boolean isWebBrowser() {
        boolean isWebBrowser = false;

        String userAgentString = getContext().getRequest().getHeader("User-Agent");
        if (userAgentString != null && userAgentString.trim().length() > 0) {

            Browser browser = Browser.parseUserAgentString(userAgentString);
            if (browser != null) {

                BrowserType browserType = browser.getBrowserType();
                if (browserType != null) {

                    if (browserType.equals(BrowserType.WEB_BROWSER) || browserType.equals(BrowserType.MOBILE_BROWSER)) {
                        isWebBrowser = true;
                    }
                }
            }
        }

        return isWebBrowser;
    }
}
