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
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Søren Roug, European Environment Agency
 */
package eionet.cr.harvest;

import java.io.FileOutputStream;
import java.net.URL;

import org.dbunit.Assertion;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.junit.Test;

import eionet.cr.test.helpers.CRDatabaseTestCase;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class HarvestSubPropertyTest extends CRDatabaseTestCase {

    /** */
    private static final String[] ignoreCols = {"SOURCE", "GEN_TIME"};

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.test.helpers.CRDatabaseTestCase#getDataSet()
     */
    protected IDataSet getDataSet() throws Exception {

        return getXmlDataSet("emptydb.xml");
    }

    @Test
    public void testSimpleSubProperty() {

        try {
            URL url =
                new URL("http://svn.eionet.europa.eu/repositories"
                        + "/Reportnet/cr2/trunk/src/test/resources/subproperty-rdf.xml");
            Harvest harvest = new PullHarvest(url.toString());
            harvest.execute();

            // Change to true if you want to dump the result into an XML file
            compareDatasets("subproperty-db.xml", true);
        } catch (Throwable e) {
            e.printStackTrace();
            fail("Was not expecting this exception: " + e.toString());
        }

    }

    @Test
    public void testTripleSubProperty() {

        try {
            URL url =
                new URL("http://svn.eionet.europa.eu/repositories"
                        + "/Reportnet/cr2/trunk/src/test/resources/subproperty2-rdf.xml");
            Harvest harvest = new PullHarvest(url.toString());
            harvest.execute();

            compareDatasets("subproperty2-db.xml", false);
        } catch (Throwable e) {
            e.printStackTrace();
            fail("Was not expecting this exception: " + e.toString());
        }

    }

    /**
     *
     * @param testData
     * @param dumpIt
     * @throws Exception
     */
    private void compareDatasets(String testData, boolean dumpIt) throws Exception {

        // Fetch database data after executing your code
        QueryDataSet queryDataSet = new QueryDataSet(getConnection());
        queryDataSet.addTable("SPO", "SELECT DISTINCT SUBJECT, PREDICATE, OBJECT, OBJECT_HASH, ANON_SUBJ, ANON_OBJ,"
                + " LIT_OBJ, OBJ_DERIV_SOURCE, OBJ_SOURCE_OBJECT FROM SPO" + " WHERE PREDICATE NOT IN"
                + " (8639511163630871821,3296918264710147612,-2213704056277764256,333311624525447614)"
                + " ORDER BY SUBJECT, PREDICATE, OBJECT");
        ITable actSPOTable = queryDataSet.getTable("SPO");

        queryDataSet = new QueryDataSet(getConnection());
        queryDataSet.addTable("RESOURCE", "SELECT URI_HASH, URI FROM RESOURCE ORDER BY URI_HASH");
        ITable actResTable = queryDataSet.getTable("RESOURCE");

        if (dumpIt) {
            FlatXmlDataSet.write(queryDataSet, new FileOutputStream(testData));
        } else {
            // Load expected data from an XML dataset
            IDataSet expectedDataSet = getXmlDataSet(testData);
            ITable expSpoTable = expectedDataSet.getTable("SPO");
            ITable expResTable = expectedDataSet.getTable("RESOURCE");

            // Assert that the actual SPO table matches the expected table
            Assertion.assertEqualsIgnoreCols(expSpoTable, actSPOTable, ignoreCols);

            // Assert that the actual RESOURCE table matches the expected table
            Assertion.assertEquals(expResTable, actResTable);
        }
    }
}
