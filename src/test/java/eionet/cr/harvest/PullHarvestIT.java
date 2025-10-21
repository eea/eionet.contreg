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


import java.util.Arrays;
import java.util.List;

import eionet.cr.ApplicationTestContext;
import eionet.cr.util.TestUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

import eionet.cr.common.Predicates;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.test.helpers.CRDatabaseTestCase;
import eionet.cr.test.helpers.JettyUtil;
import org.junit.Ignore;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author roug
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationTestContext.class })
public class PullHarvestIT extends CRDatabaseTestCase {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.test.helpers.CRDatabaseTestCase#getXMLDataSetFiles()
     */
    @Override
    protected List<String> getXMLDataSetFiles() {
        return Arrays.asList("emptydb.xml");
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.test.helpers.CRDatabaseTestCase#forceClearTriplesOnSetup()
     */
    @Override
    protected boolean forceClearTriplesOnSetup() {
        return true;
    }

    /**
     * @throws Exception
     *
     */    
    @Test
    public void testSimpleRdf() throws Exception {
            String url = TestUtils.getFileUrl("simple-rdf.xml");
            HarvestSourceDTO source = new HarvestSourceDTO();
            source.setUrl(url);
            source.setIntervalMinutes(5);
            DAOFactory daoFactory = DAOFactory.get();
            daoFactory.getDao(HarvestSourceDAO.class).addSource(source);

            PullHarvest harvest = new PullHarvest(url);
            harvest.execute();
            assertTrue(harvest.isSourceAvailable());
            assertEquals(12, harvest.getStoredTriplesCount());

            SubjectDTO subject = daoFactory.getDao(HelperDAO.class).getSubject(url);
            assertNotNull("Expected existing subject for " + url, subject);
            assertEquals("Unexpected subject URI" + url, url, subject.getUri());

            ObjectDTO byteSizeObj = subject.getObject(Predicates.CR_BYTE_SIZE);
            assertNotNull("Expected existing object for " + Predicates.CR_BYTE_SIZE, byteSizeObj);

            URI datatype = byteSizeObj.getDatatype();
            assertNotNull("Expected object datatype", datatype);
            // Although we expect xsd:integer, the Sesame driver wrongly returns xsd:int.
            assertEquals("Unexpected datatype", XMLSchema.INT.stringValue(), datatype.stringValue());
            assertTrue("Unexpected byte size", NumberUtils.toInt(byteSizeObj.getValue(), -1) > 0);
    }

    /**
     * @throws DAOException
     * @throws HarvestException
     *
     */
    @Test
    public void testHarvestNonExistingURL() throws DAOException, HarvestException {

        String url = "http://www.jaanusheinlaid.tw";
        HarvestSourceDTO source = new HarvestSourceDTO();
        source.setUrl(url);
        source.setIntervalMinutes(5);
        DAOFactory.get().getDao(HarvestSourceDAO.class).addSource(source);

        PullHarvest harvest = new PullHarvest(url);
        harvest.execute();
        assertFalse(harvest.isSourceAvailable());
    }

    /**
     * @throws Exception
     *
     */
    @Test
    public void testEncodingRdf() throws Exception {
            String url = TestUtils.getFileUrl("encoding-scheme-rdf.xml");

            HarvestSourceDTO source = new HarvestSourceDTO();
            source.setUrl(url);
            source.setIntervalMinutes(5);
            DAOFactory.get().getDao(HarvestSourceDAO.class).addSource(source);

            Harvest harvest = new PullHarvest(url);
            harvest.execute();

            assertEquals(3, harvest.getStoredTriplesCount());
    }

    /**
     * @throws Exception
     *
     */
    @Test
    public void testInlineRdf() throws Exception {
        String url = TestUtils.getFileUrl("inline-rdf.xml");

        HarvestSourceDTO source = new HarvestSourceDTO();
        source.setUrl(url);
        source.setIntervalMinutes(5);
        DAOFactory.get().getDao(HarvestSourceDAO.class).addSource(source);

        Harvest harvest = new PullHarvest(url.toString());
        harvest.execute();

        assertEquals(6, harvest.getStoredTriplesCount());
    }
}
