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
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        jaanus
 */

package eionet.cr.web.action.admin;

import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.log4j.Logger;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.StagingDatabaseDAO;
import eionet.cr.dto.StagingDatabaseDTO;
import eionet.cr.web.action.AbstractActionBean;

/**
 * An action bean for listing the currently available "staging databases" and performing bulk operations with them (e.g. delete).
 * The feature of "staging databases" has been developed specifically for the European Commission's "Digital Agenda Scoreboard"
 * project (https://ec.europa.eu/digital-agenda/en/scoreboard) but as of Jan 2013 looks perfectly useful for other projects as well.
 *
 * @author jaanus
 */
@UrlBinding("/admin/stagingDbs.action")
public class StagingDatabasesActionBean extends AbstractActionBean {

    /** The static logger. */
    private static final Logger LOGGER = Logger.getLogger(StagingDatabasesActionBean.class);

    /** Location of the JSP that lists the databases. */
    public static final String STAGING_DATABASES_JSP = "/pages/admin/stagingDb/stagingDatabases.jsp";

    /** The list of currently available staging databases. */
    private List<StagingDatabaseDTO> databases;

    /**
     * The bean's default event handler method.
     *
     * @return Resolution to go to.
     * @throws DAOException
     */
    @DefaultHandler
    public Resolution defaultHandler() throws DAOException {
        databases = DAOFactory.get().getDao(StagingDatabaseDAO.class).listAll();
        return new ForwardResolution(STAGING_DATABASES_JSP);
    }

    /**
     * @return the databases
     */
    public List<StagingDatabaseDTO> getDatabases() {
        return databases;
    }

    /**
     *
     * @return
     */
    public Class getDatabaseActionBeanClass() {
        return StagingDatabaseActionBean.class;
    }

    /**
     * Validates the the user is authorised for any operations on this action bean.
     * If user not authorised, redirects to the {@link AdminWelcomeActionBean} which displays a proper error message.
     * Will be run on any events, since no specific events specified in the {@link ValidationMethod} annotation.
     */
    @ValidationMethod(priority = 1)
    public void validateUserAuthorised() {

        if (getUser() == null || !getUser().isAdministrator()) {
            addGlobalValidationError("You are not authorized for this operation!");
            getContext().setSourcePageResolution(new RedirectResolution(AdminWelcomeActionBean.class));
        }
    }
}
