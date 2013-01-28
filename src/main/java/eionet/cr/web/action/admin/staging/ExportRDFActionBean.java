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
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.SessionScope;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.commons.lang.StringUtils;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.StagingDatabaseDAO;
import eionet.cr.staging.exp.QueryConfiguration;
import eionet.cr.web.action.AbstractActionBean;

/**
 * Action bean that serves the "wizard" that helps user to run a RDF export query from a selected staging database. <b>Note that
 * because of its "wizardly" nature, this bean is kept in {@link SessionScope}, hence some add patterns below.</b>
 *
 * @author jaanus
 */
@SessionScope
@UrlBinding("/admin/exportRDF.action")
public class ExportRDFActionBean extends AbstractActionBean {

    /** */
    private static final String STEP1_JSP = "/pages/admin/staging/exportRDF1.jsp";

    /** */
    private String dbName;

    /** */
    private QueryConfiguration queryConf;

    /**
     * Event handler for the wizard's first step.
     *
     * @return
     * @throws DAOException
     */
    @DefaultHandler
    public Resolution step1() throws DAOException {

        // If GET request, just forward to the JSP and that's all.
        if (getContext().getRequest().getMethod().equalsIgnoreCase("GET")) {
            return new ForwardResolution(STEP1_JSP);
        }

        List<String> selectedColumns = DAOFactory.get().getDao(StagingDatabaseDAO.class).prepareStatement(queryConf.getQuery());
        queryConf.addColumnNames(selectedColumns);

        return new ForwardResolution(STEP1_JSP);
    }

    /**
     * Called before {@link LifecycleStage#BindingAndValidation} and ensures that query configuration gets nullified if new database
     * name supplied by the user.
     */
    @Before(stages = {LifecycleStage.BindingAndValidation})
    public void nullifyQueryConfIfNewDatabase() {
        String dbName = getContext().getRequest().getParameter("dbName");
        if (dbName != null && !dbName.equals(this.dbName)) {
            queryConf = null;
        }
    }

    /**
     * @throws DAOException
     *
     */
    @ValidationMethod(on = {"step1"})
    public void validateStep1() throws DAOException {

        // Validate the database name.
        if (StringUtils.isBlank(dbName)) {
            addGlobalValidationError("Database name must be given!");
        } else if (!DAOFactory.get().getDao(StagingDatabaseDAO.class).exists(dbName)) {
            addGlobalValidationError("Found no staging database by this name: " + dbName);
        }

        // More validations if POST method.
        if (getContext().getRequest().getMethod().equalsIgnoreCase("POST")) {
            String query = queryConf == null ? null : queryConf.getQuery();
            if (StringUtils.isBlank(query)) {
                addGlobalValidationError("The query must not be blank!");
            }
        }

        // Set source page resolution to which the user will be returned.
        getContext().setSourcePageResolution(new ForwardResolution(STEP1_JSP));
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
     * @return the queryConf
     */
    public QueryConfiguration getQueryConf() {
        return queryConf;
    }

    /**
     * @param queryConf the queryConf to set
     */
    public void setQueryConf(QueryConfiguration queryConf) {
        this.queryConf = queryConf;
    }
}
