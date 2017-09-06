/*
 * The contents of this file are subject to the Mozilla Public
 *
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency. All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti*/
package eionet.cr.test.helpers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eionet.cr.ApplicationTestContext;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.dbunit.DatabaseTestCase;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.CompositeDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.junit.runner.RunWith;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;

import eionet.cr.test.helpers.dbunit.DbUnitDatabaseConnection;
import eionet.cr.util.sesame.SesameUtil;
import org.junit.Before;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationTestContext.class })
public abstract class CRDatabaseTestCase extends DatabaseTestCase {

    /** Repository connection to be used for checking existence of expected triples in the repository */
    private RepositoryConnection repoConn;

    /*
     * (non-Javadoc)
     *
     * @see org.dbunit.DatabaseTestCase#setUp()
     */
    //@Override
    @Before
    public void setUp() throws Exception {

        super.setUp();
        setUpTripleStore();
        repoConn = SesameUtil.getRepositoryConnection();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dbunit.DatabaseTestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {

        SesameUtil.close(repoConn);
    }

    /**
     * Set-up actions for the triple-store.
     *
     * @throws SQLException
     * @throws OpenRDFException
     * @throws IOException
     */
    private void setUpTripleStore() throws SQLException, IOException, OpenRDFException {

        RdfLoader rdfLoader = new RdfLoader();

        List<String> rdfxmlSeedFiles = getRDFXMLSeedFiles();
        List<String> ntSeedFiles = getNTriplesSeedFiles();
        List<String> turtleSeedFiles = getTurtleSeedFiles();
        List<String> n3SeedFiles = getN3SeedFiles();

        // If there is forced clearance of triplestore, or if there is at least one triplestore seed file given,
        // then clear triple store before proceeding.
        if (forceClearTriplesOnSetup() || CollectionUtils.isNotEmpty(rdfxmlSeedFiles) || CollectionUtils.isNotEmpty(ntSeedFiles)
                || CollectionUtils.isNotEmpty(turtleSeedFiles) || CollectionUtils.isNotEmpty(n3SeedFiles)) {
            rdfLoader.clearAllTriples();
        }

        for (String fileName : rdfxmlSeedFiles) {
            rdfLoader.loadIntoTripleStore(fileName, RDFFormat.RDFXML);
        }

        for (String fileName : ntSeedFiles) {
            rdfLoader.loadIntoTripleStore(fileName, RDFFormat.NTRIPLES);
        }

        for (String fileName : turtleSeedFiles) {
            rdfLoader.loadIntoTripleStore(fileName, RDFFormat.TURTLE);
        }

        for (String fileName : n3SeedFiles) {
            rdfLoader.loadIntoTripleStore(fileName, RDFFormat.N3);
        }
    }

    /**
     * If this method returns true, the test setup will clear triple store regardless of whether there are any
     * triplestore seed files to load. In this abstract class it always returns false, but subclasses may oveerride it.
     *
     * @return True/false as indicated above.
     */
    protected boolean forceClearTriplesOnSetup() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dbunit.DatabaseTestCase#getConnection()
     */
    @Override
    protected IDatabaseConnection getConnection() throws Exception {

        return DbUnitDatabaseConnection.get();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dbunit.DatabaseTestCase#getDataSet()
     */
    @SuppressWarnings("deprecation")
    @Override
    protected IDataSet getDataSet() throws Exception {

        List<String> xmlDataSetFiles = getXMLDataSetFiles();
        if (CollectionUtils.isEmpty(xmlDataSetFiles)) {
            return null;
        }

        int i = 0;
        IDataSet[] dataSets = new IDataSet[xmlDataSetFiles.size()];
        for (String fileName : xmlDataSetFiles) {
            dataSets[i++] = new FlatXmlDataSet(getClass().getClassLoader().getResourceAsStream(fileName));
        }

        CompositeDataSet compositeDataSet = new CompositeDataSet(dataSets);
        return compositeDataSet;
    }

    /**
     * Returns list of names of XML-formatted DBUnit dataset seed files.
     * To be overwritten by extending classes.
     *
     * @return List of names of XML-formatted DBUnit dataset seed files. These will be loaded in the test set-up.
     */
    protected List<String> getXMLDataSetFiles() {
        return Arrays.asList("emptydb.xml");
    }

    /**
     * Returns list of RDF/XML files to be loaded into triplestore.
     *
     * @return The list.
     */
    protected List<String> getRDFXMLSeedFiles() {
        return new ArrayList<String>();
    }

    /**
     * Returns list of N-Triples files to be loaded into triplestore.
     *
     * @return The list.
     */
    protected List<String> getNTriplesSeedFiles() {
        return new ArrayList<String>();
    }

    /**
     * Returns list of Turtle files to be loaded into triplestore.
     *
     * @return The list.
     */
    protected List<String> getTurtleSeedFiles() {
        return new ArrayList<String>();
    }

    /**
     * Returns list of N3 files to be loaded into triplestore.
     *
     * @return The list.
     */
    protected List<String> getN3SeedFiles() {
        return new ArrayList<String>();
    }

    /**
     * Returns generated graph URI for the given seed file name.
     *
     * @param fileName Seed file name.
     * @return The generated graph URI.
     */
    protected String getSeedFileGraphUri(String fileName) {
        return RdfLoader.getSeedFileGraphUri(fileName);
    }

    /**
     * A convenience method for checking if the given statement exists in the repository. The object is expected to be a literal.
     * Graph is optional. Datatype of the literal may be null in which case the literal is considered to have no datatype.
     *
     * @param subj The subject.
     * @param pred The predicate.
     * @param obj The literal object.
     * @param datatype The literal's datatype, may be null in which case the literal is considered to have no datatype.
     * @param graph The graph.
     * @return True if statement exists, otherwise false.
     * @throws OpenRDFException When problems with querying the repository.
     */
    protected boolean hasLiteralStatement(String subj, String pred, String obj, URI datatype, String... graph)
            throws OpenRDFException {

        if (repoConn == null) {
            throw new IllegalStateException("Expected the repository connection to be already created!");
        }

        boolean result = false;

        ValueFactory vf = repoConn.getValueFactory();
        org.openrdf.model.URI subjURI = StringUtils.isBlank(subj) ? null : vf.createURI(subj);
        org.openrdf.model.URI predURI = StringUtils.isBlank(pred) ? null : vf.createURI(pred);
        Literal objLiteral =
                StringUtils.isBlank(obj) ? null : (datatype == null ? vf.createLiteral(obj) : vf.createLiteral(obj, datatype));

        if (graph != null && graph.length > 0) {

            org.openrdf.model.URI[] graphs = new org.openrdf.model.URI[graph.length];
            for (int i = 0; i < graph.length; i++) {
                graphs[i] = vf.createURI(graph[i]);
            }

            result = repoConn.hasStatement(subjURI, predURI, objLiteral, false, graphs);
        } else {
            result = repoConn.hasStatement(subjURI, predURI, objLiteral, false);
        }

        return result;
    }

    /**
     * A convenience method for checking if the given statement exists in the repository. The object is expected to be a resource.
     * Graph is optional.
     *
     * @param subj The subject.
     * @param pred The predicate.
     * @param obj The resource object.
     * @param graph The graph.
     * @return True if statement exists, otherwise false.
     * @throws OpenRDFException When problems with querying the repository.
     */
    protected boolean hasResourceStatement(String subj, String pred, String obj, String... graph) throws OpenRDFException {

        if (repoConn == null) {
            throw new IllegalStateException("Expected the repository connection to be already created!");
        }

        boolean result = false;

        ValueFactory vf = repoConn.getValueFactory();
        org.openrdf.model.URI subjURI = StringUtils.isBlank(subj) ? null : vf.createURI(subj);
        org.openrdf.model.URI predURI = StringUtils.isBlank(pred) ? null : vf.createURI(pred);
        org.openrdf.model.URI objURI = StringUtils.isBlank(obj) ? null : vf.createURI(obj);

        if (graph != null && graph.length > 0) {

            org.openrdf.model.URI[] graphs = new org.openrdf.model.URI[graph.length];
            for (int i = 0; i < graph.length; i++) {
                graphs[i] = vf.createURI(graph[i]);
            }

            result = repoConn.hasStatement(subjURI, predURI, objURI, false, graphs);
        } else {
            result = repoConn.hasStatement(subjURI, predURI, objURI, false);
        }

        return result;
    }

    /**
     * A convenience method for checking if the given statement exists in the repository.
     * Calls {@link #hasLiteralStatement(String, String, String, String...)} by forming the required arguments from the respective
     * positions in the given statement array.
     *
     * @param statement The given statement array.
     * @return Exists or not.
     * @throws OpenRDFException When problems with querying the repository.
     */
    protected boolean hasLiteralStatement(String[] statement) throws OpenRDFException {

        if (statement == null || statement.length < 3) {
            throw new IllegalArgumentException("The given statement array must be at least of length 3!");
        }

        boolean result = false;
        if (statement.length == 3) {
            result = hasLiteralStatement(statement[0], statement[1], statement[2], null);
        } else {
            String[] graphs = Arrays.copyOfRange(statement, 3, statement.length);
            result = hasLiteralStatement(statement[0], statement[1], statement[2], null, graphs);
        }
        return result;
    }

    /**
     * A convenience method for checking if the given statement exists in the repository.
     * Calls {@link #hasResourceStatement(String, String, String, String...)} by forming the required arguments from the respective
     * positions in the given statement array.
     *
     * @param statement The given statement array.
     * @return Exists or not.
     * @throws OpenRDFException When problems with querying the repository.
     */
    protected boolean hasResourceStatement(String[] statement) throws OpenRDFException {

        if (statement == null || statement.length < 3) {
            throw new IllegalArgumentException("The given statement array must be at least of length 3!");
        }

        boolean result = false;
        if (statement.length == 3) {
            result = hasResourceStatement(statement[0], statement[1], statement[2]);
        } else {
            String[] graphs = Arrays.copyOfRange(statement, 3, statement.length);
            result = hasResourceStatement(statement[0], statement[1], statement[2], graphs);
        }
        return result;
    }

}
