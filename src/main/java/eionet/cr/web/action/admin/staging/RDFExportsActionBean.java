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

import java.util.List;

import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.validation.ValidationMethod;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.StagingDatabaseDAO;
import eionet.cr.dto.StagingDatabaseDTO;
import eionet.cr.staging.exp.ExportDTO;
import eionet.cr.web.action.AbstractActionBean;
import eionet.cr.web.action.admin.AdminWelcomeActionBean;

/**
 * An action bean that lists RDF export from a given staging database. The latter might not be provided, in which case RDF exports
 * from all staging databases are listed.
 *
 * @author jaanus
 */
@UrlBinding("/admin/rdfExports.action")
public class RDFExportsActionBean extends AbstractActionBean {

    /** */
    private static final String LIST_JSP = "/pages/admin/staging/rdfExports.jsp";

    /** The id of the staging database whose RDF exports the bean should list. If <= 0, exports of all databases are listed. */
    private int databaseId;

    /** RDF exports of the given database or all databases if given database id <= 0. */
    private List<ExportDTO> rdfExports;

    /** The database DTO matching the given database id. */
    private StagingDatabaseDTO databaseDTO;

    /**
     * Default event handler.
     *
     * @return resolution
     * @throws DAOException If database access error happens when getting the export DTOs from database.
     */
    @DefaultHandler
    public Resolution defaultHandler() throws DAOException {

        rdfExports = DAOFactory.get().getDao(StagingDatabaseDAO.class).listRDFExports(databaseId);
        // if (getContext().getRequest().getMethod().equalsIgnoreCase("GET")) {
        return new ForwardResolution(LIST_JSP);
    }

    /**
     * Initialization done before event handling.
     *
     * @throws DAOException In case database access error happened.
     */
    @Before(stages = LifecycleStage.EventHandling)
    public void beforeEventHandling() throws DAOException {

        if (databaseId > 0) {
            databaseDTO = DAOFactory.get().getDao(StagingDatabaseDAO.class).getDatabaseById(databaseId);
        }
    }

    /**
     *
     */
    @ValidationMethod(priority = 1)
    public void validateUserAuthorised() {

        if (getUser() == null || !getUser().isAdministrator()) {
            addGlobalValidationError("You are not authorized for this operation!");
            getContext().setSourcePageResolution(new RedirectResolution(AdminWelcomeActionBean.class));
        }
    }

    /**
     * @return the rdfExports
     */
    public List<ExportDTO> getRdfExports() {
        return rdfExports;
    }

    /**
     * @return the databaseId
     */
    public int getDatabaseId() {
        return databaseId;
    }

    /**
     * @param databaseId the databaseId to set
     */
    public void setDatabaseId(int databaseId) {
        this.databaseId = databaseId;
    }

    /**
     * Returns the class of {@link StagingDatabasesActionBean}, used in JSPs. This way the JSPs get automatically fixed, when
     * refactoring the class name.
     *
     * @return The class.
     */
    public Class getDatabasesActionBeanClass() {
        return StagingDatabasesActionBean.class;
    }

    /**
     * Returns the class of {@link StagingDatabaseActionBean}, used in JSPs. This way the JSPs get automatically fixed, when
     * refactoring the class name.
     *
     * @return The class.
     */
    public Class getDatabaseActionBeanClass() {
        return StagingDatabaseActionBean.class;
    }

    /**
     * Returns the class of {@link RDFExportActionBean}, used in JSPs. This way the JSPs get automatically fixed, when refactoring
     * the class name.
     *
     * @return The class.
     */
    public Class getExportActionBeanClass() {
        return RDFExportActionBean.class;
    }

    /**
     * Returns the class of {@link RDFExportWizardActionBean}, used in JSPs. This way the JSPs get automatically fixed, when
     * refactoring the class name.
     *
     * @return The class.
     */
    public Class getExportWizardActionBeanClass() {
        return RDFExportWizardActionBean.class;
    }

    /**
     * @return the databaseDTO
     */
    public StagingDatabaseDTO getDatabaseDTO() {
        return databaseDTO;
    }
}
