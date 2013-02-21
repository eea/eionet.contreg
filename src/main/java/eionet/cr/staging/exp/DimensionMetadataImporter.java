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

import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import eionet.cr.common.Predicates;
import eionet.cr.util.sesame.SesameUtil;
import eionet.cr.util.sql.SQLUtil;

/**
 * A utility class for importing a Scoreboard dimension's metadata from a staging database.
 *
 * @author jaanus
 */
public class DimensionMetadataImporter extends Thread {

    /** */
    public enum Dimension {
        INDICATOR, BREAKDOWN, UNIT;
    }

    /** */
    private static final Logger LOGGER = Logger.getLogger(DimensionMetadataImporter.class);

    /** */
    private String dbName;

    /** */
    private String query;

    /** */
    private Dimension dimension;

    /** */
    private URI graphURI;

    /** */
    private URI rdfsLabelURI;

    /** */
    private URI skosAltLabelURI;

    /** */
    private URI skosPrefLabelURI;

    /** */
    private URI skosNotationURI;

    /** */
    private URI skosTopConceptOfURI;

    /** */
    private URI rdfTypeURI;

    /** */
    private URI codelistIndicatorURI;

    /** */
    private URI codelistBreakdownURI;

    /** */
    private URI codelistUnitMeasureURI;

    /** */
    private URI skosConceptClassURI;

    /** */
    private URI indicatorClassURI;

    /** */
    private URI breakdownClassURI;

    /** */
    private URI unitClassURI;

    /** */
    private URI sdmxConceptURI;

    /**
     * Class constructor.
     *
     * @param dbName
     * @param query
     * @param dimension
     */
    public DimensionMetadataImporter(String dbName, String query, Dimension dimension) {
        super();
        this.dbName = dbName;
        this.query = query;
        this.dimension = dimension;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {

        long started = System.currentTimeMillis();
        LOGGER.debug("Started");

        RepositoryConnection repoConn = null;
        try {
            repoConn = SesameUtil.getRepositoryConnection();
            repoConn.setAutoCommit(false);

            doExecute(repoConn);
            repoConn.commit();

            long millis = System.currentTimeMillis() - started;
            LOGGER.debug("Finished in " + millis + " ms");
        } catch (Exception e) {
            SesameUtil.rollback(repoConn);
            LOGGER.error(e.toString(), e);
        } finally {
            SesameUtil.close(repoConn);
        }
    }

    /**
     * @param repoConn
     * @throws SQLException
     * @throws RepositoryException
     */
    private void doExecute(RepositoryConnection repoConn) throws SQLException, RepositoryException  {

        int rowIndex = 1;
        Connection sqlConn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            ValueFactory valueFactory = repoConn.getValueFactory();
            prepareValues(valueFactory);

            sqlConn = SesameUtil.getSQLConnection(dbName);
            pstmt = sqlConn.prepareStatement(query);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                importRow(rs, rowIndex, repoConn, valueFactory);
                if (rowIndex % 100 == 0) {
                    LOGGER.debug(rowIndex + " rows imported so far");
                }
                rowIndex++;
            }
        } catch (SQLException e) {
            LOGGER.error("SQLException at row #" + rowIndex, e);
            throw e;
        } catch (RepositoryException e) {
            LOGGER.error("RepositoryException at row #" + rowIndex, e);
            throw e;
        } finally {
            try {
                SQLUtil.close(rs);
                SQLUtil.close(pstmt);
                SQLUtil.close(sqlConn);
            } catch (Throwable t) {
                LOGGER.warn("Failed to close rs, stmt or conn", t);
            }
        }
    }

    /**
     *
     * @param vf
     */
    private void prepareValues(ValueFactory vf) {

        graphURI = vf.createURI("http://semantic.digital-agenda-data.eu/scoreboard-metadata-codelists.rdf");

        rdfTypeURI = vf.createURI(Predicates.RDF_TYPE);
        rdfsLabelURI = vf.createURI(Predicates.RDFS_LABEL);

        skosAltLabelURI = vf.createURI("http://www.w3.org/2004/02/skos/core#altLabel");
        skosNotationURI = vf.createURI("http://www.w3.org/2004/02/skos/core#notation");
        skosPrefLabelURI = vf.createURI("http://www.w3.org/2004/02/skos/core#prefLabel");

        codelistIndicatorURI = vf.createURI("http://semantic.digital-agenda-data.eu/codelist/indicator");
        codelistBreakdownURI = vf.createURI("http://semantic.digital-agenda-data.eu/codelist/breakdown");
        codelistUnitMeasureURI = vf.createURI("http://semantic.digital-agenda-data.eu/codelist/unit-measure");

        skosConceptClassURI = vf.createURI("http://www.w3.org/2004/02/skos/core#Concept");
        skosTopConceptOfURI = vf.createURI("http://www.w3.org/2004/02/skos/core#topConceptOf");

        indicatorClassURI = vf.createURI("http://semantic.digital-agenda-data.eu/def/class/Indicator");
        breakdownClassURI = vf.createURI("http://semantic.digital-agenda-data.eu/def/class/Breakdown");
        unitClassURI = vf.createURI("http://semantic.digital-agenda-data.eu/def/class/UnitMeasure");

        sdmxConceptURI = vf.createURI("http://purl.org/linked-data/sdmx#Concept");
    }


    /**
     * @param rs
     * @param rowIndex
     * @param repoConn
     * @param valueFactory
     * @throws SQLException
     * @throws RepositoryException
     */
    private void importRow(ResultSet rs, int rowIndex, RepositoryConnection repoConn, ValueFactory valueFactory) throws RepositoryException, SQLException {

        if (dimension == null) {
            return;
        }
        else if (dimension.equals(Dimension.INDICATOR)) {
            importIndicator(rs, rowIndex, repoConn, valueFactory);
        }
        else if (dimension.equals(Dimension.BREAKDOWN)) {
            importBreakdown(rs, rowIndex, repoConn, valueFactory);
        }
        else if (dimension.equals(Dimension.UNIT)) {
            importUnit(rs, rowIndex, repoConn, valueFactory);
        }
    }

    /**
     *
     * @param rs
     * @param rowIndex
     * @param repoConn
     * @param valueFactory
     * @throws SQLException
     * @throws RepositoryException
     */
    private void importIndicator(ResultSet rs, int rowIndex, RepositoryConnection repoConn, ValueFactory valueFactory) throws SQLException, RepositoryException {

        String notation = rs.getString("notation");
        String label = rs.getString("label");

        URI indicURI = valueFactory.createURI("http://semantic.digital-agenda-data.eu/codelist/indicator/" + notation);
        Value labelValue = valueFactory.createLiteral(label);
        Value notationValue = valueFactory.createLiteral(notation);

        repoConn.add(indicURI, rdfTypeURI, skosConceptClassURI, graphURI);
        repoConn.add(indicURI, rdfTypeURI, indicatorClassURI, graphURI);

        repoConn.add(indicURI, rdfsLabelURI, labelValue, graphURI);
        repoConn.add(indicURI, skosAltLabelURI, labelValue, graphURI);
        repoConn.add(indicURI, skosPrefLabelURI, labelValue, graphURI);

        repoConn.add(indicURI, skosNotationURI, notationValue, graphURI);
        repoConn.add(indicURI, skosTopConceptOfURI, codelistIndicatorURI, graphURI);
    }

    /**
     *
     * @param rs
     * @param rowIndex
     * @param repoConn
     * @param valueFactory
     * @throws SQLException
     * @throws RepositoryException
     */
    private void importBreakdown(ResultSet rs, int rowIndex, RepositoryConnection repoConn, ValueFactory valueFactory) throws SQLException, RepositoryException {

        String notation = rs.getString("notation");
        String label = "";
        try {
            label = rs.getString("label");
        } catch (Throwable t) {
            LOGGER.warn("Failed to get label for breakdown " + notation + ": " + t.toString());
            label = "";
        }

        URI brkdwnURI = valueFactory.createURI("http://semantic.digital-agenda-data.eu/codelist/breakdown/" + notation);
        Value labelValue = valueFactory.createLiteral(label);
        Value notationValue = valueFactory.createLiteral(notation);

        repoConn.add(brkdwnURI, rdfTypeURI, skosConceptClassURI, graphURI);
        repoConn.add(brkdwnURI, rdfTypeURI, breakdownClassURI, graphURI);

        repoConn.add(brkdwnURI, rdfsLabelURI, labelValue, graphURI);
        repoConn.add(brkdwnURI, skosAltLabelURI, labelValue, graphURI);
        repoConn.add(brkdwnURI, skosPrefLabelURI, labelValue, graphURI);

        repoConn.add(brkdwnURI, skosNotationURI, notationValue, graphURI);
        repoConn.add(brkdwnURI, skosTopConceptOfURI, codelistBreakdownURI, graphURI);
    }

    /**
     *
     * @param rs
     * @param rowIndex
     * @param repoConn
     * @param valueFactory
     * @throws SQLException
     * @throws RepositoryException
     */
    private void importUnit(ResultSet rs, int rowIndex, RepositoryConnection repoConn, ValueFactory valueFactory) throws SQLException, RepositoryException {

        String notation = rs.getString("notation");
        String label = rs.getString("label");

        URI unitURI = valueFactory.createURI("http://semantic.digital-agenda-data.eu/codelist/unit-measure/" + notation);
        Value labelValue = valueFactory.createLiteral(label);
        Value notationValue = valueFactory.createLiteral(notation);

        repoConn.add(unitURI, rdfTypeURI, skosConceptClassURI, graphURI);
        repoConn.add(unitURI, rdfTypeURI, unitClassURI, graphURI);
        repoConn.add(unitURI, rdfTypeURI, sdmxConceptURI, graphURI);

        repoConn.add(unitURI, rdfsLabelURI, labelValue, graphURI);
        repoConn.add(unitURI, skosAltLabelURI, labelValue, graphURI);
        repoConn.add(unitURI, skosPrefLabelURI, labelValue, graphURI);

        repoConn.add(unitURI, skosNotationURI, notationValue, graphURI);
        repoConn.add(unitURI, skosTopConceptOfURI, codelistUnitMeasureURI, graphURI);
    }
}
