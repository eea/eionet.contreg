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
package eionet.cr.dao;

import java.util.Arrays;
import java.util.List;

import eionet.cr.ApplicationTestContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eionet.cr.dto.TripleDTO;
import eionet.cr.test.helpers.CRDatabaseTestCase;
import eionet.cr.test.helpers.RdfLoader;
import eionet.cr.util.pagination.PagingRequest;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author Risto Alt
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationTestContext.class })
public class HelperDAOIT extends CRDatabaseTestCase {

    /** Seed file. */
    private static final String SEED_FILE = "obligations.rdf";

    @Autowired
    private HelperDAO helperDAO;

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
         * @see eionet.cr.test.helpers.CRDatabaseTestCase#getRDFXMLSeedFiles()
         */
    @Override
    protected List<String> getRDFXMLSeedFiles() {
        return Arrays.asList(SEED_FILE);
    }

    /**
     * Test getting sample triples.
     * @throws Exception
     */
    @Test
    public void testGetSampleTriples() throws Exception {
        List<TripleDTO> result = helperDAO.getSampleTriplesInSource(RdfLoader.getSeedFileGraphUri(SEED_FILE), PagingRequest.create(1, 10));
        assertNotNull(result);
        assertEquals(100, result.size());
    }
}
