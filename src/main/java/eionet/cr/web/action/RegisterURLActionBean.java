/*
 * The contents of this file are subject to the Mozilla Public
 *
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
 * Agency. Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency. All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti*/
package eionet.cr.web.action;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.commons.lang.StringUtils;

import eionet.cr.dao.DAOException;
import eionet.cr.harvest.HarvestException;
import eionet.cr.util.URLUtil;
import eionet.cr.web.action.factsheet.FactsheetActionBean;
import eionet.cr.web.util.RegisterUrl;

/**
 * URL registration action bean.
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 */
@UrlBinding("/registerUrl.action")
public class RegisterURLActionBean extends AbstractActionBean {

    /** The URL to register. */
    private String url;

    /** The URL's label to register. */
    private String label;

    /** Should we bookmark the registered bookmark. */
    private boolean bookmark = false;

    /** Shoulw we show factsheet link. */
    private boolean showFactsheetLink = false;

    /** The registration message. */
    private String registrationMessage;

    /**
     *
     * @return
     * @throws DAOException
     */
    @DefaultHandler
    public Resolution unspecified() {

        return new ForwardResolution("/pages/registerUrl.jsp");
    }

    /**
     *
     * @return
     * @throws DAOException
     * @throws HarvestException
     */
    public Resolution save() throws DAOException, HarvestException {

        // register URL
        url = URLUtil.escapeIRI(url);

        if (RegisterUrl.isSubjectRegistered(url)) {

            showFactsheetLink = true;

            // User wants to bookmark the subject and the bookmark is not registered yet.
            if (bookmark && !RegisterUrl.isSubjectBookmarkedByUser(url, getUser())) {
                RegisterUrl.saveSubjectUserBookmark(url, getUser(), label);
                registrationMessage = "Url already registered in the system, but added a personal bookmark.";
            } else if (bookmark && RegisterUrl.isSubjectBookmarkedByUser(url, getUser())) {
                registrationMessage = "Url and bookmark both already registered in the system.";
            } else {
                registrationMessage = "Url already registered in the system.";
            }

            return new ForwardResolution("/pages/registerUrl.jsp");

        } else {
            RegisterUrl.register(url, getUser(), false, label);
        }

        // go to factsheet in edit mode
        return new RedirectResolution(FactsheetActionBean.class, "edit").addParameter("uri", url);
    }

    /**
     * Validate save.
     */
    @ValidationMethod(on = "save")
    public void validateSave() {

        if (StringUtils.isBlank(url) || !URLUtil.isURL(url)) {
            addGlobalValidationError(new SimpleError("Not a valid URL!"));
        }
        if (getUser() == null) {
            addGlobalValidationError(new SimpleError("You are not logged in!"));
        }
    }

    /**
     * Sets the url.
     *
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Gets the url.
     *
     * @return the url
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Sets the bookmark.
     *
     * @param bookmark the bookmark to set
     */
    public void setBookmark(boolean bookmark) {
        this.bookmark = bookmark;
    }

    /**
     * Sets the label.
     *
     * @param label the new label
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Checks if is show factsheet link.
     *
     * @return true, if is show factsheet link
     */
    public boolean isShowFactsheetLink() {
        return showFactsheetLink;
    }

    /**
     * Sets the show factsheet link.
     *
     * @param showFactsheetLink the new show factsheet link
     */
    public void setShowFactsheetLink(boolean showFactsheetLink) {
        this.showFactsheetLink = showFactsheetLink;
    }

    /**
     * Gets the registration message.
     *
     * @return the registration message
     */
    public String getRegistrationMessage() {
        return registrationMessage;
    }

    /**
     * Sets the registration message.
     *
     * @param registrationMessage the new registration message
     */
    public void setRegistrationMessage(String registrationMessage) {
        this.registrationMessage = registrationMessage;
    }
}
