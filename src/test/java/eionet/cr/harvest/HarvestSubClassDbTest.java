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
 * @author roug
 *
 */
public class HarvestSubClassDbTest extends CRDatabaseTestCase {

    private static final String[] ignoreCols = {"SOURCE", "GEN_TIME"};

    /*
     * (non-Javadoc)
     * @see eionet.cr.test.helpers.CRDatabaseTestCase#getDataSet()
     */
    @Override
    protected IDataSet getDataSet() throws Exception {
        return getXmlDataSet("emptydb.xml");
    }

    @Test
    public void testSubClassOfRdf() {

        try {
            URL url = getClass().getClassLoader().getResource("subclass-rdf.xml");
            Harvest harvest = new PullHarvest(url.toString(), null);
            harvest.execute();

            compareDatasets("subclass-db.xml", false);
        }
        catch (Throwable e) {
            e.printStackTrace();
            fail("Was not expecting this exception: " + e.toString());
        }

    }

    @Test
    public void testSplitSubClassOfRdf() {

        try {
            URL url = getClass().getClassLoader().getResource("subclass-s1-rdf.xml");
            Harvest harvest = new PullHarvest(url.toString(), null);
            harvest.execute();

            url = getClass().getClassLoader().getResource("subclass-s2-rdf.xml");
            harvest = new PullHarvest(url.toString(), null);
            harvest.execute();

            compareDatasets("subclass-db.xml", false);
        }
        catch (Throwable e) {
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

        // Fetch database data after executing your code.
        QueryDataSet queryDataSet = new QueryDataSet(getConnection());
        queryDataSet.addTable("SPO",
                    "SELECT DISTINCT SUBJECT, PREDICATE, OBJECT, OBJECT_HASH, ANON_SUBJ, ANON_OBJ,"
                    + " LIT_OBJ, OBJ_LANG, OBJ_SOURCE_OBJECT FROM SPO"
                    + " WHERE PREDICATE NOT IN ( 8639511163630871821, 3296918264710147612, -2213704056277764256, 333311624525447614 )"

                    + " ORDER BY SUBJECT, PREDICATE, OBJECT");
        ITable actSPOTable = queryDataSet.getTable("SPO");

        queryDataSet = new QueryDataSet(getConnection());
        queryDataSet.addTable("RESOURCE", "SELECT URI,URI_HASH FROM RESOURCE "
                + "WHERE URI NOT LIKE 'file:%' ORDER BY URI, URI_HASH");
        ITable actResTable = queryDataSet.getTable("RESOURCE");

        if (dumpIt){
            FlatXmlDataSet.write(queryDataSet, new FileOutputStream(testData));
        }
        else{
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
