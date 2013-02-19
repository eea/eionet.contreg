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

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.StagingDatabaseDAO;
import eionet.cr.staging.exp.ExportDTO;
import eionet.cr.staging.util.ImportExportLogUtil;
import eionet.cr.web.action.AbstractActionBean;
import eionet.cr.web.action.admin.AdminWelcomeActionBean;
import eionet.cr.web.action.factsheet.FactsheetActionBean;
import eionet.cr.web.action.factsheet.ObjectsInSourceActionBean;

/**
 * Action bean meant for operations with a particular RDF export run.
 *
 * @author jaanus
 */
@UrlBinding("/admin/rdfExport.action")
public class RDFExportActionBean extends AbstractActionBean {

    /** */
    private static final String VIEW_JSP = "/pages/admin/staging/rdfExport.jsp";

    /** The id of the RDF export for which this bean offers operations. */
    private int exportId;

    /** The export's DTO matching the given export id. */
    private ExportDTO exportDTO;

    /** */
    private List<String> exportedResources;

    /**
     * Default event handler.
     *
     * @return resolution
     * @throws DAOException If database access error happens when getting the export DTOs from database.
     */
    @DefaultHandler
    public Resolution defaultHandler() throws DAOException {

        exportDTO = DAOFactory.get().getDao(StagingDatabaseDAO.class).getRDFExport(exportId);
        if (exportDTO == null) {
            addWarningMessage("Found no RDF export by the given id!");
        }

        return new ForwardResolution(VIEW_JSP);
    }

    /**
     * Validate default handler.
     *
     * @return the resolution
     */
    @ValidationMethod
    public Resolution validateDefaultHandler() {

        if (!getContext().getRequest().getParameterMap().containsKey("exportId")) {
            addGlobalValidationError("Missing parameter: exportId");
        }

        return new ForwardResolution(VIEW_JSP);
    }

    /**
     * Validate user authorised.
     */
    @ValidationMethod(priority = 1)
    public void validateUserAuthorised() {

        if (getUser() == null || !getUser().isAdministrator()) {
            addGlobalValidationError("You are not authorized for this operation!");
            getContext().setSourcePageResolution(new RedirectResolution(AdminWelcomeActionBean.class));
        }
    }

    /**
     * Retrieves the given export's log and writes it into the returned {@link StreamingResolution} of content type "text/html".
     *
     * @return The {@link StreamingResolution} as described.
     * @throws DAOException If database access error happens.
     */
    public Resolution openLog() throws DAOException {

        logger.trace("Retrieving export log for this export id: " + exportId);
        String log = DAOFactory.get().getDao(StagingDatabaseDAO.class).getExportLog(exportId);
        if (log == null) {
            log = "Found no RDF export for which the log is requested!";
        } else if (log.trim().length() == 0) {
            log = "The log of this RDF export run is empty!";
        } else {
            log = ImportExportLogUtil.formatLogForDisplay(log);
        }

        return new StreamingResolution("text/html", log);
    }

    /**
     * Gets the export id.
     *
     * @return the exportId
     */
    public int getExportId() {
        return exportId;
    }

    /**
     * Sets the export id.
     *
     * @param exportId the exportId to set
     */
    public void setExportId(int exportId) {
        this.exportId = exportId;
    }

    /**
     * Returns the class of {@link ObjectsInSourceActionBean}, used in JSPs. This way the JSPs get automatically fixed, when
     * refactoring the class name.
     *
     * @return The class.
     */
    public Class getObjectsInSourceActionBeanClass() {
        return ObjectsInSourceActionBean.class;
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
     * Gets the export's DTO.
     *
     * @return the export's DTO
     */
    public ExportDTO getExportDTO() {
        return exportDTO;
    }

    /**
     * Gets the query configuration dump.
     *
     * @return the query configuration dump
     */
    public String getQueryConfigurationDump() {

        StringBuilder sb = new StringBuilder("");

        String queryConf = exportDTO == null ? null : exportDTO.getQueryConf();
        if (StringUtils.isNotBlank(queryConf)) {

            queryConf = StringEscapeUtils.escapeXml(queryConf);
            String[] lines = queryConf.split("\\n");
            if (lines != null) {

                for (int i = 0; i < lines.length; i++) {

                    String line = lines[i];
                    if (line.startsWith("[") && line.endsWith("]")) {

                        line = StringUtils.replace(line, "[", "<b>");
                        line = StringUtils.replace(line, "]", ":</b>");
                    }

                    int j = 0;
                    String indentation = "";
                    for (; j < line.length(); j++) {
                        if (line.charAt(j) == ' ') {
                            indentation = indentation + "&nbsp;";
                        } else {
                            break;
                        }
                    }
                    line = indentation + (j >= line.length() ? "" : line.substring(j));
                    line = StringUtils.replace(line, "\t", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");

                    sb.append(line).append("<br/>");
                }
            }
        }

        return sb.toString();
    }

    /**
     * @return the exportedResources
     * @throws DAOException
     */
    public List<String> getExportedResources() throws DAOException {
        if (exportedResources == null) {
            exportedResources = DAOFactory.get().getDao(StagingDatabaseDAO.class).getExportedResourceUris(exportId);
        }
        return exportedResources;
    }

    /**
     *
     * @return
     */
    public Class getFactsheetActionBeanClass() {
        return FactsheetActionBean.class;
    }
}
