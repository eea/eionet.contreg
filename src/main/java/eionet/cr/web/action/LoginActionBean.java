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
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.log4j.Logger;

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

    /**
     * Action method deals with user logging in.
     *
     * @return {@link RedirectResolution} to CAS login URL.
     */
    @DefaultHandler
    @HandlesEvent(LOGIN_EVENT)
    @DontSaveLastActionEvent
    public Resolution login() {
        return new RedirectResolution(getContext().getCASLoginURL(), false);
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

        CRUser user = getContext().getCRUser();
        if (user!=null){

            LOGGER.debug("Checking if there is a home folder for user " + user.getUserName());

            // If this is the first time the user has logged in,
            // create his/her home folder. First time means no
            // home folder currently existing in the triple store.
            FolderDAO dao = DAOFactory.get().getDao(FolderDAO.class);
            if (!dao.folderExists(user.getHomeUri())){

                LOGGER.debug("Going to create home folder for user " + user.getUserName());

                dao.createUserHomeFolder(user.getUserName());
            }
            else{
                LOGGER.debug("Home folder exists for user " + user.getUserName());
            }
        }

        String lastActionEventUrl = getContext().getLastActionEventUrl();
        lastActionEventUrl = lastActionEventUrl == null ? MAIN_PAGE_ACTION : lastActionEventUrl;
        return new RedirectResolution(lastActionEventUrl, !lastActionEventUrl.toLowerCase().startsWith("http://"));
    }

    /**
     * Action method deals with user logging out.
     *
     * @return {@link RedirectResolution} to CAS logout URL.
     */
    @HandlesEvent(LOGOUT_EVENT)
    public Resolution logout() {
        getContext().setSessionAttribute(USER_SESSION_ATTR, null);
        return new RedirectResolution(getContext().getCASLogoutURL(), false);
    }
}
