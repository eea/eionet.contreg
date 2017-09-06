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
 * Agency.  Portions created by Tieto Estonia are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Enriko Käsper, Tieto Estonia
 */
package eionet.cr.dao;

import java.util.Arrays;
import java.util.List;

import eionet.cr.ApplicationTestContext;
import org.junit.Before;
import org.junit.Test;

import eionet.cr.dto.TagDTO;
import eionet.cr.test.helpers.CRDatabaseTestCase;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author <a href="mailto:enriko.kasper@tieto.com">Enriko Käsper</a>
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationTestContext.class })
public class TagsDAOIT extends CRDatabaseTestCase {

    /** Seed file. */
    private static final String SEED_FILE = "tags.rdf";

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    /*
         * (non-Javadoc)
         *
         * @see eionet.cr.test.helpers.CRDatabaseTestCase#getRDFXMLSeedFiles()
         */
    @Override
    protected List<String> getRDFXMLSeedFiles() {
        return Arrays.asList(SEED_FILE);
    }

    /**
     * Test tag cloud frequencies.
     *
     * @throws Exception When any error happens.
     */
    @Test
    public void testTagCloudFrequencies() throws Exception {

        List<TagDTO> result = DAOFactory.get().getDao(TagsDAO.class).getTagCloud();

        assertTrue(result.contains(new TagDTO("tag1", 1, 4)));
        assertTrue(result.contains(new TagDTO("tag2", 2, 4)));
        assertTrue(result.contains(new TagDTO("tag3", 3, 4)));
        assertTrue(result.contains(new TagDTO("tag4", 4, 4)));
    }
}
