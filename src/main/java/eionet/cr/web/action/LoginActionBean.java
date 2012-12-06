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
/**
 *
 */
package eionet.cr.web.action;

import static eionet.cr.web.util.WebConstants.AFTER_LOGIN_EVENT;
import static eionet.cr.web.util.WebConstants.LOGIN_ACTION;
import static eionet.cr.web.util.WebConstants.LOGIN_EVENT;
import static eionet.cr.web.util.WebConstants.LOGOUT_EVENT;
import static eionet.cr.web.util.WebConstants.MAIN_PAGE_ACTION;
import static eionet.cr.web.util.WebConstants.USER_SESSION_ATTR;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.tee.uit.security.AuthMechanism;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.FolderDAO;
import eionet.cr.web.interceptor.annotation.DontSaveLastActionEvent;
import eionet.cr.web.security.CRUser;

/**
 * Action bean that deals with user login/logout.
 * <p>
 * In CAS context Action bean simply delegates user login/logout to CAS.
 *
 * @author gerasvad, altnyris
 *
 */
@UrlBinding(LOGIN_ACTION)
public class LoginActionBean extends AbstractActionBean {

    /** */
    private static final Logger LOGGER = Logger.getLogger(LoginActionBean.class);

    /** */
    private static final String LOGIN_JSP = "/pages/login.jsp";

    /** */
    private String username;
    private String password;

    /** */
    private boolean loginFailure;

    /**
     * Action method deals with user logging in.
     *
     * @return {@link RedirectResolution} to CAS login URL.
     * @throws DAOException
     */
    @DefaultHandler
    @HandlesEvent(LOGIN_EVENT)
    @DontSaveLastActionEvent
    public Resolution login() throws DAOException {

        if (GeneralConfig.isUseCentralAuthenticationService()) {
            return new RedirectResolution(getContext().getCASLoginURL(), false);
        }

        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            if (isPostRequest()) {
                addWarningMessage("User name or password missing!");
            }
            return new ForwardResolution(LOGIN_JSP);
        }

        try {
            LOGGER.debug("Doing " + AuthMechanism.class.getSimpleName() + " login for user " + username);
            AuthMechanism.sessionLogin(username, password);
            CRUser user = new CRUser(username);
            getContext().setSessionAttribute(USER_SESSION_ATTR, user);
            return afterLogin();

        } catch (Exception e) {
            loginFailure = true;
            LOGGER.warn("Login failed for user " + username, e);
            return new ForwardResolution(LOGIN_JSP);
        }
    }

    /**
     * Method redirects to last event occurred before logging in.
     *
     * @return redirect resolution to last event occurred before logging in.
     * @throws DAOException
     */
    @HandlesEvent(AFTER_LOGIN_EVENT)
    @DontSaveLastActionEvent
    public Resolution afterLogin() throws DAOException {

        createReservedFilesAndFolders(getContext().getCRUser());

        String lastActionEventUrl = getContext().getLastActionEventUrl();
        lastActionEventUrl = lastActionEventUrl == null ? MAIN_PAGE_ACTION : lastActionEventUrl;
        return new RedirectResolution(lastActionEventUrl, !lastActionEventUrl.toLowerCase().startsWith("http://"));
    }

    /**
     *
     * @param user
     * @throws DAOException
     */
    private void createReservedFilesAndFolders(CRUser user) throws DAOException {
        if (user != null) {
            // Creating reserved files and folders into user home
            FolderDAO dao = DAOFactory.get().getDao(FolderDAO.class);
            dao.createUserHomeFolder(user.getUserName());

            user.createDefaultAcls();
        }
    }

    /**
     * Action method deals with user logging out.
     *
     * @return {@link RedirectResolution} to CAS logout URL.
     */
    @HandlesEvent(LOGOUT_EVENT)
    public Resolution logout() {
        getContext().setSessionAttribute(USER_SESSION_ATTR, null);
        getSession().invalidate();
        if (GeneralConfig.isUseCentralAuthenticationService()) {
            return new RedirectResolution(getContext().getCASLogoutURL(), false);
        } else {
            // if not using Central Authentication Service, just redirect to the index page
            return new RedirectResolution("");
        }
    }

    /**
     * @param userName the userName to set
     */
    public void setUsername(String userName) {
        this.username = userName;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     *
     * @return
     */
    public boolean isLoginFailure() {
        return loginFailure;
    }
}
