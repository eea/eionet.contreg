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

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.commons.lang.StringUtils;

import eionet.cr.staging.exp.DimensionMetadataImporter;
import eionet.cr.staging.exp.DimensionMetadataImporter.Dimension;
import eionet.cr.web.action.AbstractActionBean;
import eionet.cr.web.action.admin.AdminWelcomeActionBean;

/**
 * A utility bean for importing the metadata of Scoreboard dimensions from a given staging database.
 *
 * @author jaanus
 */
@UrlBinding("/admin/dimImport.action")
public class DimensionsMetadataImportActionBean extends AbstractActionBean {

    /** */
    private static final String JSP = "/pages/admin/staging/dimImport.jsp";

    /** */
    private String dbName;

    /** */
    private String query;

    /** */
    private Dimension dimension;

    /**
     *
     * @return
     */
    @DefaultHandler
    public Resolution defaultHandler() {

        ForwardResolution resolution = new ForwardResolution(JSP);
        if (getContext().getRequest().getMethod().equalsIgnoreCase("POST")) {
            DimensionMetadataImporter importer = new DimensionMetadataImporter(dbName, query, dimension);
            importer.start();
            addSystemMessage("Import started!");
        }

        return resolution;
    }

    /**
     *
     */
    private void execute() {

    }

    /**
     *
     */
    @ValidationMethod(on = {"defaultHandler"})
    public void validateDefaultHandler() {

        if (StringUtils.isBlank(dbName)) {
            addGlobalValidationError("DB name must not be blank!");
        }

        if (getContext().getRequest().getMethod().equalsIgnoreCase("POST")) {
            if (StringUtils.isBlank(query)) {
                addGlobalValidationError("Query must not be blank!");
            }

            if (dimension == null) {
                addGlobalValidationError("Dimension must not be blank!");
            }
        }

        getContext().setSourcePageResolution(new ForwardResolution(JSP));
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
     * @return the dbName
     */
    public String getDbName() {
        return dbName;
    }

    /**
     * @param dbName the dbName to set
     */
    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    /**
     * @return the dimension
     */
    public Dimension getDimension() {
        return dimension;
    }

    /**
     * @param dimension the dimension to set
     */
    public void setDimension(Dimension dimension) {
        this.dimension = dimension;
    }

    /**
     *
     * @return
     */
    public Dimension[] getDimensions() {
        return Dimension.values();
    }

    /**
     * @return the query
     */
    public String getQuery() {
        return query;
    }

    /**
     * @param query the query to set
     */
    public void setQuery(String query) {
        this.query = query;
    }

}
