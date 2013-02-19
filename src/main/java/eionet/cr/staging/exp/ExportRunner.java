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

package eionet.cr.staging.exp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import eionet.cr.common.Predicates;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.StagingDatabaseDAO;
import eionet.cr.dto.StagingDatabaseDTO;
import eionet.cr.util.LogUtil;
import eionet.cr.util.sesame.SesameUtil;
import eionet.cr.util.sql.SQLUtil;

// TODO: Auto-generated Javadoc
/**
 * A thread runs a given RDF export query with a given query configuration on a given staging database.
 *
 * @author jaanus
 */
public class ExportRunner extends Thread {

    /** */
    private static final Logger LOGGER = Logger.getLogger(ExportRunner.class);

    /** */
    private StagingDatabaseDTO dbDTO;

    /** */
    private int exportId;

    /** */
    private String userName;

    /** */
    private QueryConfiguration queryConf;

    /** */
    private URI objectTypeURI;

    /** */
    private URI rdfTypeURI;

    /** */
    private int tripleCount;

    /** */
    private int subjectCount;

    /** */
    private HashSet<String> distinctGraphs = new HashSet<String>();

    /** */
    private Logger exportLogger;

    /** The {@link StagingDatabaseDAO} used by this thread to access the database. */
    private StagingDatabaseDAO dao;

    /** The export's descriptive name. */
    private String exportName;

    /** */
    private Set<ObjectHiddenProperty> hiddenProperties;

    /** */
    private URI graphURI;
    private URI indicatorPredicateURI;
    private URI indicatorValueURI;
    private URI importURI;
    private URI importedResourceURI;

    /**
     * Private class constructor.
     *
     * @param dbDTO The DTO of the staging database on which the query shall be run.
     * @param exportId The ID of the export being run.
     * @param exportName The export's descriptive name.
     * @param userName User who initiated the export.
     * @param queryConf The query configuration to run.
     */
    private ExportRunner(StagingDatabaseDTO dbDTO, int exportId, String exportName, String userName, QueryConfiguration queryConf) {

        super();

        if (dbDTO == null || queryConf == null) {
            throw new IllegalArgumentException("Staging database DTO and query configuration must not be null!");
        }
        if (StringUtils.isBlank(userName)) {
            throw new IllegalArgumentException("User name must not be blank!");
        }

        this.dbDTO = dbDTO;
        this.exportId = exportId;
        this.exportName = exportName;
        this.queryConf = queryConf;
        this.userName = userName;
        this.exportLogger = createLogger(exportId);

        ObjectType objectType = ObjectTypes.getByUri(queryConf.getObjectTypeUri());
        if (objectType != null) {
            hiddenProperties = objectType.getHiddenProperties();
        }
    }

    /**
     * Creates the logger.
     *
     * @param exportId the export id
     * @return the export logger
     */
    private ExportLogger createLogger(int exportId) {

        String loggerName = "RDF_export_" + exportId;
        ExportLogger logger = (ExportLogger) Logger.getLogger(loggerName, ExportLoggerFactory.INSTANCE);
        logger.setExportId(exportId);
        logger.setLevel(Level.TRACE);
        return logger;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {

        long started = System.currentTimeMillis();
        LogUtil.debug("RDF export (id=" + exportId + ") started by " + userName, exportLogger, LOGGER);

        boolean failed = false;
        RepositoryConnection repoConn = null;
        try {
            repoConn = SesameUtil.getRepositoryConnection();
            repoConn.setAutoCommit(false);

            doRun(repoConn);
            repoConn.commit();

            long millis = System.currentTimeMillis() - started;
            LogUtil.debug("RDF export (id=" + exportId + ") finished in " + (millis / 1000L) + " sec", exportLogger, LOGGER);

        } catch (Exception e) {
            failed = true;
            SesameUtil.rollback(repoConn);
            LogUtil.debug("RDF export (id=" + exportId + ") failed with error", e, exportLogger, LOGGER);
        } finally {
            SesameUtil.close(repoConn);
        }

        try {
            getDao().finishRDFExport(exportId, this, failed ? ExportStatus.ERROR : ExportStatus.COMPLETED);
        } catch (DAOException e) {
            LOGGER.error("Failed to finish RDF export record with id = " + exportId, e);
        }
    }

    /**
     * Update export status.
     *
     * @param status the status
     */
    private void updateExportStatus(ExportStatus status) {
        try {
            getDao().updateExportStatus(exportId, status);
        } catch (DAOException e) {
            LOGGER.error("Failed to update the status of RF export with id = " + exportId, e);
        }
    }

    /**
     * Do run.
     *
     * @param repoConn the repo conn
     * @throws RepositoryException the repository exception
     * @throws SQLException the sQL exception
     */
    private void doRun(RepositoryConnection repoConn) throws RepositoryException, SQLException {

        // Nothing to do here if query or column mappings is empty.
        if (StringUtils.isBlank(queryConf.getQuery()) || queryConf.getColumnMappings().isEmpty()) {
            return;
        }

        Connection sqlConn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            ValueFactory valueFactory = repoConn.getValueFactory();
            prepareValues(valueFactory);

            sqlConn = SesameUtil.getSQLConnection(dbDTO.getName());
            pstmt = sqlConn.prepareStatement(queryConf.getQuery());
            rs = pstmt.executeQuery();
            int rowIndex = 1;
            while (rs.next()) {
                exportRow(rs, rowIndex, repoConn, valueFactory);
                if (rowIndex % 1000 == 0) {
                    LogUtil.debug(rowIndex + " rows exported so far", exportLogger, LOGGER);
                }
                rowIndex++;
            }
        } finally {
            SQLUtil.close(rs);
            SQLUtil.close(pstmt);
            SQLUtil.close(sqlConn);
        }
    }

    /**
     * @param vf
     */
    private void prepareValues(ValueFactory vf) {

        setPredicateURIs(vf);
        setHiddenPropertiesValues(vf);

        objectTypeURI = vf.createURI(queryConf.getObjectTypeUri());
        rdfTypeURI = vf.createURI(Predicates.RDF_TYPE);
        graphURI = vf.createURI("http://semantic.digital-agenda-data.eu/dataset/scoreboard");
        indicatorPredicateURI = vf.createURI("http://semantic.digital-agenda-data.eu/def/property/indicator");

        String indicator = queryConf.getIndicator();
        if (StringUtils.isNotBlank(indicator)) {
            indicatorValueURI = vf.createURI("http://semantic.digital-agenda-data.eu/codelist/indicator/" + indicator);
        }

        importURI = vf.createURI("http://semantic.digital-agenda-data.eu/import/"+ exportId);
        importedResourceURI = vf.createURI("http://semantic.digital-agenda-data.eu/importedResource");
    }

    /**
     * Sets the predicate ur is.
     *
     * @param vf the new predicate ur is
     */
    private void setPredicateURIs(ValueFactory vf) {

        Map<String, ObjectProperty> columnMappings = queryConf.getColumnMappings();
        Collection<ObjectProperty> objectProperties = columnMappings.values();
        for (ObjectProperty objectProperty : objectProperties) {
            objectProperty.setPredicateURI(vf);
        }
    }

    /**
     * Export row.
     *
     * @param rs the rs
     * @param rowIndex the row index
     * @param repoConn the repo conn
     * @param vf the vf
     * @throws SQLException the sQL exception
     * @throws RepositoryException the repository exception
     */
    private void exportRow(ResultSet rs, int rowIndex, RepositoryConnection repoConn, ValueFactory vf) throws SQLException,
    RepositoryException {

        // Prepare subject ID on the basis of ID template in query configuration. If it's blank, auto-generate it.
        String subjectId = queryConf.getObjectIdTemplate();
        if (StringUtils.isBlank(subjectId)) {
            subjectId = String.valueOf(exportId) + "_" + rowIndex;
        }

        // The dataset (i.e. target graph) ID is the value of the columns designated as "dataset column" in query configuration.
        // String datasetId = queryConf.getDatasetIdTemplate();
        // if (StringUtils.isBlank(datasetId)) {
        // datasetId = String.valueOf(exportId);
        // }

        // Prepare the map of ObjectDTO to be added to the subject later.
        LinkedHashMap<URI, ArrayList<Value>> valuesByPredicate = new LinkedHashMap<URI, ArrayList<Value>>();

        // Add rdf:type predicate-value.
        addPredicateValue(valuesByPredicate, rdfTypeURI, objectTypeURI);

        // Add predicate-value pairs for hidden properties.
        if (hiddenProperties != null) {
            for (ObjectHiddenProperty hiddenProperty : hiddenProperties) {
                addPredicateValue(valuesByPredicate, hiddenProperty.getPredicateURI(), hiddenProperty.getValueValue());
            }
        }

        boolean hasIndicatorMapping = false;

        // Loop through the query configuration's column mappings, construct ObjectDTO for each.
        for (Entry<String, ObjectProperty> entry : queryConf.getColumnMappings().entrySet()) {

            String colName = entry.getKey();
            String colValue = rs.getString(colName);
            ObjectProperty property = entry.getValue();
            if (property.getId().equals("indicator")) {
                hasIndicatorMapping = true;
            }

            if (StringUtils.isNotBlank(colValue)) {

                // Replace this column's place-holder in the subject ID template and dataset (i.e. graph) ID template.
                // subjectId = StringUtils.replace(subjectId, "<" + colName + ">", colValue);
                // datasetId = StringUtils.replace(datasetId, "<" + colName + ">", colValue);

                subjectId = StringUtils.replace(subjectId, "<" + property.getId() + ">", colValue);

                URI predicateURI = property.getPredicateURI();
                if (predicateURI != null) {

                    String valueStr = property.getValueTemplate();
                    if (valueStr == null) {
                        valueStr = colValue;
                    } else {
                        // Replace the column's value placeholder in the value template (the latter cannot be specified by user)
                        valueStr = StringUtils.replace(valueStr, "<value>", colValue);
                    }

                    Value value = null;
                    if (property.isLiteralRange()) {
                        try {
                            String dataTypeUri = property.getDataType();
                            value = vf.createLiteral(valueStr, dataTypeUri == null ? null : vf.createURI(dataTypeUri));
                        } catch (IllegalArgumentException e) {
                            value = vf.createLiteral(valueStr);
                        }
                    } else {
                        value = vf.createURI(valueStr);
                    }

                    addPredicateValue(valuesByPredicate, predicateURI, value);
                }
            }
        }

        if (!hasIndicatorMapping && StringUtils.isNotBlank(queryConf.getIndicator())) {
            addPredicateValue(valuesByPredicate, indicatorPredicateURI, indicatorValueURI);
        }

        if (subjectId.indexOf("<indicator>") != -1) {
            String indicator = queryConf.getIndicator();
            if (StringUtils.isBlank(indicator)) {
                indicator = "*";
            }
            subjectId = StringUtils.replace(subjectId, "<indicator>", indicator);
        }

        if (subjectId.indexOf("<breakdown>") != -1) {
            subjectId = StringUtils.replace(subjectId, "<breakdown>", "total");
        }

        if (!valuesByPredicate.isEmpty()) {

            int tripleCountBefore = tripleCount;
            URI subjectURI = vf.createURI(queryConf.getObjectIdNamespace() + subjectId);
            // URI graphURI = vf.createURI(queryConf.getDatasetIdNamespace() + datasetId);
            for (Entry<URI, ArrayList<Value>> entry : valuesByPredicate.entrySet()) {

                ArrayList<Value> values = entry.getValue();
                if (values != null && !values.isEmpty()) {
                    URI predicateURI = entry.getKey();
                    for (Value value : values) {
                        repoConn.add(subjectURI, predicateURI, value, graphURI);
                        tripleCount++;
                        if (tripleCount % 5000 == 0) {
                            LOGGER.debug(tripleCount + " triples exported so far");
                        }
                        //                        distinctGraphs.add(graphURI.toString());
                    }
                }
            }

            if (tripleCount > tripleCountBefore) {
                subjectCount++;
            }

            repoConn.add(importURI, importedResourceURI, subjectURI, importURI);
        }
    }

    /**
     * Adds the predicate value.
     *
     * @param valuesByPredicate the values by predicate
     * @param predicateURI the predicate uri
     * @param value the value
     */
    private void addPredicateValue(LinkedHashMap<URI, ArrayList<Value>> valuesByPredicate, URI predicateURI, Value value) {

        ArrayList<Value> values = valuesByPredicate.get(predicateURI);
        if (values == null) {
            values = new ArrayList<Value>();
            valuesByPredicate.put(predicateURI, values);
        }
        values.add(value);
    }

    /**
     * Lazy getter for the {@link #dao}.
     *
     * @return the DAO
     */
    private StagingDatabaseDAO getDao() {

        if (dao == null) {
            dao = DAOFactory.get().getDao(StagingDatabaseDAO.class);
        }

        return dao;
    }

    /**
     * Start.
     *
     * @param dbDTO the db dto
     * @param exportName the export name
     * @param userName the user name
     * @param queryConf the query conf
     * @return the export runner
     * @throws DAOException the dAO exception
     */
    public static synchronized ExportRunner start(StagingDatabaseDTO dbDTO, String exportName, String userName,
            QueryConfiguration queryConf) throws DAOException {

        // Create the export record in the database.
        int exportId =
                DAOFactory.get().getDao(StagingDatabaseDAO.class).startRDEExport(dbDTO.getId(), exportName, userName, queryConf);

        ExportRunner exportRunner = new ExportRunner(dbDTO, exportId, exportName, userName, queryConf);
        exportRunner.start();
        return exportRunner;
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
     * Gets the triple count.
     *
     * @return the tripleCount
     */
    public int getTripleCount() {
        return tripleCount;
    }

    /**
     * Gets the subject count.
     *
     * @return the subjectCount
     */
    public int getSubjectCount() {
        return subjectCount;
    }

    /**
     * Gets the distinct graphs.
     *
     * @return the distinctGraphs
     */
    public HashSet<String> getDistinctGraphs() {
        return distinctGraphs;
    }

    /**
     * Gets the export name.
     *
     * @return the exportName
     */
    public String getExportName() {
        return exportName;
    }

    /**
     * Sets the hidden properties values.
     *
     * @param vf the new hidden properties values
     */
    private void setHiddenPropertiesValues(ValueFactory vf) {

        if (hiddenProperties != null && !hiddenProperties.isEmpty() && vf != null) {
            for (ObjectHiddenProperty hiddenProperty : hiddenProperties) {
                hiddenProperty.setValues(vf);
            }
        }
    }
}
