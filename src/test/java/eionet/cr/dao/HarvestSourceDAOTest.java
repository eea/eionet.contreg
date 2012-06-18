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
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.cr.dao;

import java.util.List;

import org.dbunit.dataset.IDataSet;
import org.junit.Test;

import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.test.helpers.CRDatabaseTestCase;
import eionet.cr.util.Pair;
import eionet.cr.util.pagination.PagingRequest;

/**
 * JUnit test tests HarvestSourceDAO functionality.
 * 
 * @author altnyris
 * 
 */
public class HarvestSourceDAOTest extends CRDatabaseTestCase {

    /*
     * (non-Javadoc)
     * 
     * @see org.dbunit.DatabaseTestCase#getDataSet()
     */
    @Override
    protected IDataSet getDataSet() throws Exception {
        return getXmlDataSet("sources-harvests-messages.xml");
    }

    @Test
    public void testAddSource() throws Exception {

        HarvestSourceDAO dao = DAOFactory.get().getDao(HarvestSourceDAO.class);

        HarvestSourceDTO source = new HarvestSourceDTO();
        source.setUrl("http://rod.eionet.europa.eu/testObligations");
        source.setIntervalMinutes(0);
        source.setPrioritySource(false);
        source.setEmails("bob@europe.eu");

        Integer harvestSourceID = dao.addSource(source);
        assertNotNull(harvestSourceID);

        HarvestSourceDTO harvestSource = dao.getHarvestSourceById(harvestSourceID);
        assertEquals("bob@europe.eu", harvestSource.getEmails());
        assertEquals("http://rod.eionet.europa.eu/testObligations", harvestSource.getUrl());
        assertEquals("bob@europe.eu", harvestSource.getEmails());
    }

    @Test
    public void testGetHarvestSourceByUrl() throws Exception {

        HarvestSourceDTO dto =
                DAOFactory.get().getDao(HarvestSourceDAO.class).getHarvestSourceByUrl("http://www.eionet.europa.eu/seris/rdf");
        assertNotNull(dto);
    }

    @Test
    public void testGetHarvestSources() throws Exception {

        Pair<Integer, List<HarvestSourceDTO>> result =
                DAOFactory.get().getDao(HarvestSourceDAO.class).getHarvestSources("", PagingRequest.create(1, 100), null);
        assertNotNull(result);
        assertNotNull(result.getRight());
        assertEquals(5, result.getRight().size());
    }

    @Test
    public void testEditSource() throws Exception {

        // get the source by URL
        HarvestSourceDAO dao = DAOFactory.get().getDao(HarvestSourceDAO.class);
        HarvestSourceDTO harvestSource = dao.getHarvestSourceByUrl("http://www.eionet.europa.eu/seris/rdf");
        assertNotNull(harvestSource);

        // change the URL of the source
        harvestSource.setUrl("http://www.eionet.europa.eu/seris/rdf-dummy");
        dao.editSource(harvestSource);

        // get the source by previous URL again- now it must be null
        assertNull(dao.getHarvestSourceByUrl("http://www.eionet.europa.eu/seris/rdf"));

        // get the source by new URL, it must not be null
        assertNotNull(dao.getHarvestSourceByUrl("http://www.eionet.europa.eu/seris/rdf-dummy"));
    }
}
