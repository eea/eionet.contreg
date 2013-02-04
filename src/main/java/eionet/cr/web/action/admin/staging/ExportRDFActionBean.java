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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.SessionScope;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.commons.lang.StringUtils;

import virtuoso.jdbc3.VirtuosoException;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.StagingDatabaseDAO;
import eionet.cr.dto.StagingDatabaseDTO;
import eionet.cr.staging.exp.ObjectProperty;
import eionet.cr.staging.exp.ObjectType;
import eionet.cr.staging.exp.ObjectTypes;
import eionet.cr.staging.exp.QueryConfiguration;
import eionet.cr.staging.exp.QueryRunner;
import eionet.cr.web.action.AbstractActionBean;

// TODO: Auto-generated Javadoc
/**
 * Action bean that serves the "wizard" that helps user to run a RDF export query from a selected staging database. <b>Note that
 * because of its "wizardly" nature, this bean is kept in {@link SessionScope}, hence some add patterns below.</b>
 *
 * @author jaanus
 */
@SessionScope
@UrlBinding("/admin/exportRDF.action")
public class ExportRDFActionBean extends AbstractActionBean {

    /**  */
    private static final String COLUMN_PROPERTY_PARAM_SUFFIX = ".property";

    /** */
    private static final String STEP1_JSP = "/pages/admin/staging/exportRDF1.jsp";

    /** */
    private static final String STEP2_JSP = "/pages/admin/staging/exportRDF2.jsp";

    /** */
    private String dbName;

    /** */
    private QueryConfiguration queryConf;

    /** */
    private String prevDbName;

    /** */
    private String prevObjectTypeUri;

    /** */
    private List<String> prevColumnNames;

    /**
     * Event handler for the wizard's first step.
     *
     * @return the resolution
     * @throws DAOException the dAO exception
     */
    @DefaultHandler
    public Resolution step1() throws DAOException {

        // Handle GET request, just forward to the JSP and that's all.
        if (getContext().getRequest().getMethod().equalsIgnoreCase("GET")) {

            // If this event is GET-requested with new database name, nullify the query configuration.
            if (!dbName.equals(prevDbName)) {
                queryConf = null;
                prevDbName = dbName;
            }

            return new ForwardResolution(STEP1_JSP);
        }

        // Handle POST request.

        // If object type changed, reset query configuration.
        boolean objectTypeChanged = false;
        if (!queryConf.getObjectTypeUri().equals(prevObjectTypeUri)) {

            queryConf.clearColumnMappings();
            queryConf.setDatasetColumn(null);
            queryConf.setObjectIdTemplate(null);
            objectTypeChanged = true;
            prevObjectTypeUri = queryConf.getObjectTypeUri();
        }

        try {
            // Compile the query on the database side, get the names of columns selected by the query.
            List<String> columnNames =
                    DAOFactory.get().getDao(StagingDatabaseDAO.class).prepareStatement(queryConf.getQuery(), dbName);

            // If column names changed, clear the column mappings in the query configuration.
            if (!columnNames.equals(prevColumnNames)) {
                queryConf.clearColumnMappings();
                queryConf.putColumnNames(columnNames);
                prevColumnNames = columnNames;
            }

            if (objectTypeChanged) {
                queryConf.setDefaults();
            }
            return new ForwardResolution(STEP2_JSP);
        } catch (DAOException e) {
            Throwable cause = e.getCause();
            if (cause instanceof VirtuosoException) {
                VirtuosoException ve = (VirtuosoException) cause;
                if (ve.getErrorCode() == VirtuosoException.SQLERROR) {
                    addGlobalValidationError("An SQL error occurred:\n" + ve.getMessage());
                } else {
                    addGlobalValidationError("A database error occurred:\n" + ve.getMessage());
                }
                return new ForwardResolution(STEP1_JSP);
            } else {
                throw e;
            }
        }
    }

    /**
     * Step2.
     *
     * @return the resolution
     * @throws DAOException the dAO exception
     */
    public Resolution step2() throws DAOException {

        // If GET request, just forward to the JSP and that's all.
        if (getContext().getRequest().getMethod().equalsIgnoreCase("GET")) {
            return new ForwardResolution(STEP2_JSP);
        }

        StagingDatabaseDTO dbDTO = DAOFactory.get().getDao(StagingDatabaseDAO.class).getDatabaseByName(dbName);
        QueryRunner queryRunner = new QueryRunner(dbDTO, queryConf);
        queryRunner.start();

        addSystemMessage("RDF export successfully started! Use operations menu to monitor progress.");
        return new RedirectResolution(StagingDatabaseActionBean.class).addParameter("dbName", dbName);
    }

    /**
     * Back to step1.
     *
     * @return the resolution
     */
    public Resolution backToStep1() {
        return new ForwardResolution(STEP1_JSP);
    }

    /**
     * Back to step2.
     *
     * @return the resolution
     */
    public Resolution backToStep2() {
        return new ForwardResolution(STEP2_JSP);
    }

    /**
     * Cancel.
     *
     * @return the resolution
     */
    public Resolution cancel() {
        return new RedirectResolution(StagingDatabaseActionBean.class).addParameter("dbName", dbName);
    }

    /**
     * Validate step2.
     */
    @ValidationMethod(on = {"step2", "backToStep1"})
    public void validateStep2() {

        HttpServletRequest request = getContext().getRequest();
        ObjectType objectType = getObjectType();

        if (objectType != null) {

            Map<String, ObjectProperty> colMappings = queryConf == null ? null : queryConf.getColumnMappings();
            if (colMappings != null && !colMappings.isEmpty()) {

                for (Entry<String, ObjectProperty> entry : colMappings.entrySet()) {

                    String colName = entry.getKey();
                    String propertyPredicate = request.getParameter(colName + COLUMN_PROPERTY_PARAM_SUFFIX);
                    if (StringUtils.isBlank(propertyPredicate)) {
                        addGlobalValidationError("Missing property selection for this column: " + colName);
                    } else {
                        entry.setValue(objectType.getPropertyByPredicate(propertyPredicate));
                    }
                }
            }
        }

        String datasetColumn = request.getParameter("queryConf.datasetColumn");
        String objectIdTemplate = request.getParameter("queryConf.objectIdTemplate");

        if (StringUtils.isBlank(datasetColumn)) {
            addGlobalValidationError("You must specify the dataset column!");
        }
        if (StringUtils.isBlank(objectIdTemplate)) {
            addGlobalValidationError("You must specify the identifier template!");
        }

        getContext().setSourcePageResolution(new ForwardResolution(STEP2_JSP));
    }

    /**
     * Validate step1.
     *
     * @throws DAOException the dAO exception
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

            String objectTypeUri = queryConf == null ? null : queryConf.getObjectTypeUri();
            if (StringUtils.isBlank(objectTypeUri)) {
                addGlobalValidationError("The type of objects must not be blank!");
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

    /**
     *
     * @return
     */
    public Class getDatabaseActionBeanClass() {
        return StagingDatabaseActionBean.class;
    }

    /**
     *
     * @return
     */
    public Collection<ObjectType> getObjectTypes() {
        return ObjectTypes.getMap().values();
    }

    /**
     *
     * @return
     */
    private ObjectType getObjectType() {

        String objTypeUri = queryConf == null ? null : queryConf.getObjectTypeUri();
        if (StringUtils.isNotBlank(objTypeUri)) {
            return ObjectTypes.getByUri(objTypeUri);
        }

        return null;
    }

    /**
     *
     * @return
     */
    public List<ObjectProperty> getTypeProperties() {

        ObjectType objectType = getObjectType();
        if (objectType != null) {
            return objectType.getProperties();
        }

        return null;
    }
}
