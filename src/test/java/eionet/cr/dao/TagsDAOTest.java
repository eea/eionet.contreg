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

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import eionet.cr.dto.TagDTO;
import eionet.cr.test.helpers.RdfLoader;

/**
 * 
 * @author <a href="mailto:enriko.kasper@tieto.com">Enriko Käsper</a>
 * 
 */
public class TagsDAOTest {

    private static final String seedFile = "tags.rdf";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        new RdfLoader(seedFile);
    }

    @Test
    public void testTagCloudFrequencies() throws Exception {
        List<TagDTO> result = DAOFactory.get().getDao(TagsDAO.class).getTagCloud();

        assertTrue(result.contains(new TagDTO("tag1", 1, 4)));
        assertTrue(result.contains(new TagDTO("tag2", 2, 4)));
        assertTrue(result.contains(new TagDTO("tag3", 3, 4)));
        assertTrue(result.contains(new TagDTO("tag4", 4, 4)));
    }
}
