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

package eionet.cr.web.action.admin.staging;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.log4j.Logger;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.StagingDatabaseDAO;
import eionet.cr.dto.StagingDatabaseDTO;
import eionet.cr.staging.imp.ImportStatus;
import eionet.cr.staging.util.ImportExportLogUtil;
import eionet.cr.web.action.AbstractActionBean;
import eionet.cr.web.action.admin.AdminWelcomeActionBean;

/**
 * An action bean for listing the currently available "staging databases" and performing bulk operations with them (e.g. delete).
 * The feature of "staging databases" has been developed specifically for the European Commission's "Digital Agenda Scoreboard"
 * project (https://ec.europa.eu/digital-agenda/en/scoreboard) but as of Jan 2013 looks perfectly useful for other projects as well.
 *
 * @author jaanus
 */
@UrlBinding("/admin/stagingDbs.action")
public class StagingDatabasesActionBean extends AbstractActionBean {

    /** */
    public static final Map<String, String> IMPORT_STATUSES = createImportStatuses();

    /** The static logger. */
    private static final Logger LOGGER = Logger.getLogger(StagingDatabasesActionBean.class);

    /** Location of the JSP that lists the databases. */
    public static final String STAGING_DATABASES_JSP = "/pages/admin/staging/databases.jsp";

    /** The list of currently available staging databases. */
    private List<StagingDatabaseDTO> databases;

    /** */
    private List<String> dbNames;

    /** */
    private int databaseId;

    /**
     * The bean's default event handler method.
     *
     * @return Resolution to go to.
     * @throws DAOException If database access error happens.
     */
    @DefaultHandler
    public Resolution defaultHandler() throws DAOException {
        databases = DAOFactory.get().getDao(StagingDatabaseDAO.class).listAll();
        return new ForwardResolution(STAGING_DATABASES_JSP);
    }

    /**
     * Handles the delete event.
     *
     * @return the resolution
     */
    public Resolution delete() {

        if (dbNames != null && !dbNames.isEmpty()) {
            try {
                DAOFactory.get().getDao(StagingDatabaseDAO.class).delete(dbNames);
                addSystemMessage("Selected database(s) successfully selected!");
            } catch (DAOException e) {
                LOGGER.error("Failed to delete selected databases", e);
                addWarningMessage("Deletion of the selected database(s) failed with technical error: " + e.getMessage());
            }
        } else {
            addCautionMessage("No databases selected!");
        }

        return new RedirectResolution(this.getClass());
    }

    /**
     * Handles the "Open import log" event.
     *
     * @return the resolution
     * @throws DAOException If database access error happens.
     */
    public Resolution openLog() throws DAOException {

        logger.trace("Retrieving import log for this database id: " + databaseId);
        String log = DAOFactory.get().getDao(StagingDatabaseDAO.class).getImportLog(databaseId);
        if (log == null) {
            log = "Found no import log for this database!";
        } else if (log.trim().length() == 0) {
            log = "Import log for this database is empty!";
        } else {
            log = ImportExportLogUtil.formatLogForDisplay(log);
        }

        return new StreamingResolution("text/html", log);
    }

    /**
     * Gets the databases.
     *
     * @return the databases
     */
    public List<StagingDatabaseDTO> getDatabases() {
        return databases;
    }

    /**
     * Gets the database action bean class.
     *
     * @return the database action bean class
     */
    public Class getDatabaseActionBeanClass() {
        return StagingDatabaseActionBean.class;
    }

    /**
     * Gets the available files action bean class.
     *
     * @return the available files action bean class
     */
    public Class getAvailableFilesActionBeanClass() {
        return AvailableFilesActionBean.class;
    }

    /**
     * Gets the rdf exports action bean class.
     *
     * @return the rdf exports action bean class
     */
    public Class getRdfExportsActionBeanClass() {
        return RDFExportsActionBean.class;
    }

    /**
     * Validates the the user is authorised for any operations on this action bean. If user not authorised, redirects to the
     * {@link AdminWelcomeActionBean} which displays a proper error message. Will be run on any events, since no specific events
     * specified in the {@link ValidationMethod} annotation.
     */
    @ValidationMethod(priority = 1)
    public void validateUserAuthorised() {

        if (getUser() == null || !getUser().isAdministrator()) {
            addGlobalValidationError("You are not authorized for this operation!");
            getContext().setSourcePageResolution(new RedirectResolution(AdminWelcomeActionBean.class));
        }
    }

    /**
     * Sets the db names.
     *
     * @param dbNames the dbNames to set
     */
    public void setDbNames(List<String> dbNames) {
        this.dbNames = dbNames;
    }

    /**
     * Gets the import statuses.
     *
     * @return the import statuses
     */
    public Map<String, String> getImportStatuses() {
        return IMPORT_STATUSES;
    }

    /**
     * Creates and returns a map of database import statuses. The keys are the status names, the values are their friendly names.
     *
     * @return the map
     */
    private static Map<String, String> createImportStatuses() {

        HashMap<String, String> result = new HashMap<String, String>();
        ImportStatus[] importStatuses = ImportStatus.values();
        for (int i = 0; i < importStatuses.length; i++) {
            ImportStatus importStatus = importStatuses[i];
            result.put(importStatus.name(), importStatus.toString());
        }
        return result;
    }

    /**
     * Sets the database id.
     *
     * @param databaseId the databaseId to set
     */
    public void setDatabaseId(int databaseId) {
        this.databaseId = databaseId;
    }
}
