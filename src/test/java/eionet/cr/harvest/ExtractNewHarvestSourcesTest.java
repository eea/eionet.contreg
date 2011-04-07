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
package eionet.cr.harvest;

import java.net.URL;
import java.util.List;

import org.dbunit.dataset.IDataSet;
import org.junit.Test;

import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.test.helpers.CRDatabaseTestCase;
import eionet.cr.util.Hashes;
import eionet.cr.util.Pair;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class ExtractNewHarvestSourcesTest extends CRDatabaseTestCase{

    /*
     * (non-Javadoc)
     * @see eionet.cr.test.helpers.CRDatabaseTestCase#getDataSet()
     */
    protected IDataSet getDataSet() throws Exception {
        return getXmlDataSet("emptydb.xml");
    }

    @Test
    public void test(){

        try {
            URL url = getClass().getClassLoader().getResource("extract-new-sources.xml");
            Harvest harvest = new PullHarvest(url.toString(), null);
            harvest.execute();

            Pair<Integer,List<HarvestSourceDTO>> resultPair =
                DAOFactory.get().getDao(HarvestSourceDAO.class).getPrioritySources(
                                                                            null, null, null);
            assertNotNull(resultPair);
            assertNotNull(resultPair.getLeft());
            assertNotNull(resultPair.getRight());
            assertEquals(1, resultPair.getLeft().intValue());
            assertEquals(1, resultPair.getRight().size());

            HarvestSourceDTO harvestSource = resultPair.getRight().get(0);
            assertNotNull(harvestSource);
            assertEquals("http://test.com/datasets", harvestSource.getUrl());
            assertEquals(Hashes.spoHash("http://test.com/datasets"), harvestSource.getUrlHash().longValue());
        }
        catch (Throwable e) {
            e.printStackTrace();
            fail("Was not expecting this exception: " + e.toString());
        }
    }
}
