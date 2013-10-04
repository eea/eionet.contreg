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

import org.apache.commons.collections.CollectionUtils;
import org.dbunit.DatabaseTestCase;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.CompositeDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.openrdf.OpenRDFException;
import org.openrdf.rio.RDFFormat;

import eionet.cr.test.helpers.dbunit.DbUnitDatabaseConnection;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public abstract class CRDatabaseTestCase extends DatabaseTestCase {

    /**
     * {@inheritDoc}
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setUpTripleStore();
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

        // If at least one seed file given, clear triple store before proceeding to the loading.
        if (CollectionUtils.isNotEmpty(rdfxmlSeedFiles) || CollectionUtils.isNotEmpty(ntSeedFiles)
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
}
