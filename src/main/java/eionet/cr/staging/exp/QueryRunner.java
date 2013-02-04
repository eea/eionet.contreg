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
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.StagingDatabaseDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.URIUtil;
import eionet.cr.util.sesame.SesameUtil;
import eionet.cr.util.sql.SQLUtil;

/**
 * A thread runs a given RDF export query with a given query configuration on a given staging database.
 *
 * @author jaanus
 */
public class QueryRunner extends Thread {

    /** */
    private static final Logger LOGGER = Logger.getLogger(QueryRunner.class);

    /** */
    private StagingDatabaseDTO dbDTO;

    /** */
    private QueryConfiguration queryConf;

    /** */
    private String runnerId = UUID.randomUUID().toString();

    /** */
    private HelperDAO dao;

    /** */
    private int tripleCount;

    /** */
    private HashSet<String> distinctGraphs = new HashSet<String>();

    /**
     * Class constructor.
     *
     * @param dbDTO The DTO of the staging database on which the query shall be run.
     * @param queryConf The query configuration to run.
     */
    public QueryRunner(StagingDatabaseDTO dbDTO, QueryConfiguration queryConf) {
        super();

        if (dbDTO == null || queryConf == null) {
            throw new IllegalArgumentException("Staging database DTOa dn query configuration must not be null!");
        }

        // TODO validate that the given query configuration is sufficient to run it (i.e. no empty query, etc)

        this.dbDTO = dbDTO;
        this.queryConf = queryConf;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {

        long started = System.currentTimeMillis();
        LOGGER.debug("Started ...");

        RepositoryConnection repoConn = null;
        try {
            repoConn = SesameUtil.getRepositoryConnection();
            repoConn.setAutoCommit(false);

            doRun(repoConn);

            repoConn.commit();
            LOGGER.debug("Finished, total time " + (System.currentTimeMillis() - started) + " ms, tripleCount = " + tripleCount + ", distinctGraphs:\n" + distinctGraphs);
        } catch (Exception e) {
            SesameUtil.rollback(repoConn);
            LOGGER.error("Query run failed with " + e.getClass().getSimpleName(), e);
        }
        finally {
            SesameUtil.close(repoConn);
        }
    }

    /**
     *
     * @param repoConn
     * @throws RepositoryException
     * @throws SQLException
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
            setPredicateURIs(valueFactory);

            sqlConn = SesameUtil.getSQLConnection(dbDTO.getName());
            pstmt = sqlConn.prepareStatement(queryConf.getQuery());
            rs = pstmt.executeQuery();
            int rowIndex = 1;
            while (rs.next()) {
                exportRow(rs, rowIndex++, repoConn, valueFactory);
            }
        } finally {
            SQLUtil.close(rs);
            SQLUtil.close(pstmt);
            SQLUtil.close(sqlConn);
        }
    }

    /**
     *
     * @param vf
     */
    private void setPredicateURIs(ValueFactory vf) {

        Map<String, ObjectProperty> columnMappings = queryConf.getColumnMappings();
        Collection<ObjectProperty> objectProperties = columnMappings.values();
        for (ObjectProperty objectProperty : objectProperties) {
            objectProperty.setPredicateURI(vf);
        }
    }

    /**
     *
     * @param rs
     * @param rowIndex
     * @param vf
     * @param repoConn
     * @throws SQLException
     * @throws RepositoryException
     */
    private void exportRow(ResultSet rs, int rowIndex, RepositoryConnection repoConn, ValueFactory vf) throws SQLException,
    RepositoryException {

        // Prepare subject ID on the basis of ID template in query configuration. If it's blank, auto-generate it.
        String subjectId = queryConf.getObjectIdTemplate();
        if (StringUtils.isBlank(subjectId)) {
            subjectId = runnerId + "_" + rowIndex;
        }

        // The dataset (i.e. target graph) ID is the value of the columns designated as "dataset column" in query configuration.
        String datasetId = rs.getString(queryConf.getDatasetColumn());
        if (StringUtils.isBlank(datasetId)) {
            datasetId = runnerId;
        }

        // Prepare the map of ObjectDTO to be added to the subject later.
        LinkedHashMap<URI, ArrayList<Value>> valuesByPredicate = new LinkedHashMap<URI, ArrayList<Value>>();

        // Loop through the query configuration's column mappings, construct ObjectDTO for each.
        for (Entry<String, ObjectProperty> entry : queryConf.getColumnMappings().entrySet()) {

            String colName = entry.getKey();
            String colValue = rs.getString(colName);

            if (StringUtils.isNotBlank(colValue)) {

                // Replace this column's place-holder in the subject ID template.
                subjectId = StringUtils.replace(subjectId, "<" + colName + ">", colValue);

                ObjectProperty property = entry.getValue();
                URI predicateURI = property.getPredicateURI();
                if (predicateURI != null) {

                    String valueStr = property.getValueTemplate();
                    if (valueStr == null) {
                        valueStr = colValue;
                    } else {
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

                    ArrayList<Value> values = valuesByPredicate.get(predicateURI);
                    if (values == null) {
                        values = new ArrayList<Value>();
                        valuesByPredicate.put(predicateURI, values);
                    }
                    values.add(value);
                }
            }
        }

        if (!valuesByPredicate.isEmpty()) {

            URI subjectURI = vf.createURI(queryConf.getObjectIdNamespace() + subjectId);
            URI graphURI = vf.createURI(queryConf.getDatasetNamespace() + datasetId);
            for (Entry<URI, ArrayList<Value>> entry : valuesByPredicate.entrySet()) {

                ArrayList<Value> values = entry.getValue();
                if (values != null && !values.isEmpty()) {
                    URI predicateURI = entry.getKey();
                    for (Value value : values) {
                        repoConn.add(subjectURI, predicateURI, value, graphURI);
                        tripleCount++;
                        if (tripleCount % 1000 == 0) {
                            LOGGER.debug(tripleCount  + " triples exported so far");
                        }
                        distinctGraphs.add(graphURI.toString());
                    }
                }
            }
        }
    }

    /**
     *
     * @param rs
     * @param rowIndex
     * @throws SQLException
     * @throws DAOException
     */
    private void exportRow2(ResultSet rs, int rowIndex) throws SQLException, DAOException {

        // Prepare subject ID on the basis of ID template in query configuration. If it's blank, auto-generate it.
        String subjectId = queryConf.getObjectIdTemplate();
        if (StringUtils.isBlank(subjectId)) {
            subjectId = runnerId + "_" + rowIndex;
        }

        // The dataset (i.e. target graph) ID is the value of the columns designated as "dataset column" in query configuration.
        String datasetId = rs.getString(queryConf.getDatasetColumn());
        if (StringUtils.isBlank(datasetId)) {
            datasetId = runnerId;
        }

        // Prepare the map of ObjectDTO to be added to the subject later.
        LinkedHashMap<String, ArrayList<ObjectDTO>> objectsByPredicate = new LinkedHashMap<String, ArrayList<ObjectDTO>>();

        // Loop through the query configuration's column mappings, construct ObjectDTO for each.
        for (Entry<String, ObjectProperty> entry : queryConf.getColumnMappings().entrySet()) {

            String colName = entry.getKey();
            Object value = rs.getObject(colName);

            if (value != null) {

                // Replace this column's place-holder in the subject ID template.
                subjectId = StringUtils.replace(subjectId, "<" + colName + ">", value.toString());

                ObjectProperty property = entry.getValue();
                String predicateUri = property.getPredicate();
                if (StringUtils.isNotBlank(predicateUri)) {

                    String valueStr = property.getValueTemplate();
                    if (valueStr == null) {
                        valueStr = value.toString();
                    } else {
                        valueStr = StringUtils.replace(valueStr, "<value>", value.toString());
                    }

                    ObjectDTO objectDTO = new ObjectDTO(valueStr, property.isLiteralRange());
                    if (property.isLiteralRange()) {

                        String dataTypeUri = property.getDataType();
                        if (URIUtil.isURI(dataTypeUri)) {
                            objectDTO.setDatatype(new URIImpl(dataTypeUri));
                        }
                    }
                    objectDTO.setSourceUri(queryConf.getDatasetNamespace() + datasetId);

                    ArrayList<ObjectDTO> objects = objectsByPredicate.get(predicateUri);
                    if (objects == null) {
                        objects = new ArrayList<ObjectDTO>();
                        objectsByPredicate.put(predicateUri, objects);
                    }
                    objects.add(objectDTO);
                }
            }
        }

        if (!objectsByPredicate.isEmpty()) {

            SubjectDTO subjectDTO = new SubjectDTO(queryConf.getObjectIdNamespace() + subjectId, false);
            for (Entry<String, ArrayList<ObjectDTO>> entry : objectsByPredicate.entrySet()) {
                ArrayList<ObjectDTO> objects = entry.getValue();
                if (objects != null && !objects.isEmpty()) {
                    String predicateUri = entry.getKey();
                    for (ObjectDTO objectDTO : objects) {
                        subjectDTO.addObject(predicateUri, objectDTO);
                    }
                }
            }

            getDao().addTriples(subjectDTO);
        }
    }

    /**
     *
     * @return
     */
    private HelperDAO getDao() {
        if (dao == null) {
            dao = DAOFactory.get().getDao(HelperDAO.class);
        }
        return dao;
    }

    public static void main(String[] args) {
        String s = null;
        URIImpl uriImpl = new URIImpl(s);
        System.out.println(uriImpl);
    }
}
