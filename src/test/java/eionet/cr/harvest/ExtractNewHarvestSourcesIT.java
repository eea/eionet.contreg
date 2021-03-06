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

import eionet.cr.ApplicationTestContext;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.test.helpers.CRDatabaseTestCase;
import eionet.cr.util.Hashes;
import eionet.cr.util.Pair;
import eionet.cr.util.TestUtils;
import org.eclipse.jetty.server.Server;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationTestContext.class })
public class ExtractNewHarvestSourcesIT extends CRDatabaseTestCase {

    /** Jetty mock server for serving test resources via HTTP. */
    private static Server resourcesMockServer;

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.test.helpers.CRDatabaseTestCase#getXMLDataSetFiles()
     */
    @Override
    protected List<String> getXMLDataSetFiles() {
        return Arrays.asList("emptydb.xml");
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void test() throws Exception {

        String url = TestUtils.getFileUrl("extract-new-sources.xml");

        HarvestSourceDTO source = new HarvestSourceDTO();
        source.setUrl(url);
        source.setIntervalMinutes(5);
        source.setPrioritySource(true);
        source.setEmails("bob@europe.eu");
        DAOFactory.get().getDao(HarvestSourceDAO.class).addSource(source);

        Harvest harvest = new PullHarvest(url);
        harvest.execute();

        Pair<Integer, List<HarvestSourceDTO>> resultPair =
                DAOFactory.get().getDao(HarvestSourceDAO.class)
                        .getHarvestSources("http://test.com/datasets/dataset2", null, null);
        assertNotNull(resultPair);
        assertNotNull(resultPair.getLeft());
        assertNotNull(resultPair.getRight());
        assertEquals(1, resultPair.getLeft().intValue());
        assertEquals(1, resultPair.getRight().size());

        HarvestSourceDTO harvestSource = resultPair.getRight().get(0);
        assertNotNull(harvestSource);
        assertEquals("http://test.com/datasets/dataset2", harvestSource.getUrl());
        assertEquals(Hashes.spoHash("http://test.com/datasets/dataset2"), harvestSource.getUrlHash().longValue());
    }
}
