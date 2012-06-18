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
package eionet.cr.web.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eionet.cr.dto.TagDTO;

/**
 * 
 * @author <a href="mailto:enriko.kasper@tieto.com">Enriko Käsper</a>
 * 
 */

public class TagCloudCacheTest {

    @Before
    public void initializeContext() throws Exception {
        new ApplicationCache().contextInitialized(null);
    }

    @After
    public void destroyContext() throws Exception {
        new ApplicationCache().contextDestroyed(null);
    }

    @Test
    public void testTagCacheLimit() {

        ApplicationCache.updateTagCloudCache(getTestData(15));

        assertEquals(15, ApplicationCache.getTagCloud(0).size());
        assertEquals(15, ApplicationCache.getTagCloud(10000).size());
        assertEquals(10, ApplicationCache.getTagCloud(10).size());
        assertEquals(3, ApplicationCache.getTagCloud(3).size());
        assertEquals("tag100", ApplicationCache.getTagCloud(3).get(0).getTag());
        assertEquals(99, ApplicationCache.getTagCloud(3).get(1).getCount());
        assertEquals(4, ApplicationCache.getTagCloud(3).get(1).getScale());
    }

    @Test
    public void testTagCacheSorting() {

        ApplicationCache.updateTagCloudCache(getTestData(10));

        assertEquals("tag100", ApplicationCache.getTagCloudSortedByName(10).get(0).getTag());
        assertEquals("tag91", ApplicationCache.getTagCloudSortedByName(10).get(1).getTag());
        assertEquals("tag92", ApplicationCache.getTagCloudSortedByName(10).get(2).getTag());

        assertEquals("tag100", ApplicationCache.getTagCloudSortedByCount(10).get(0).getTag());
        assertEquals("tag99", ApplicationCache.getTagCloudSortedByCount(10).get(1).getTag());
        assertEquals("tag98", ApplicationCache.getTagCloudSortedByCount(10).get(2).getTag());
        // if equal count, then sort by name
        assertEquals("tag95", ApplicationCache.getTagCloudSortedByCount(10).get(4).getTag());
        assertEquals("tag96", ApplicationCache.getTagCloudSortedByCount(10).get(5).getTag());
    }

    /**
     * @return
     */
    private List<TagDTO> getTestData(int size) {
        List<TagDTO> result = new ArrayList<TagDTO>();
        for (int i = 0; i < size; i++) {
            result.add(new TagDTO("tag" + (100 - i), (i == 5 ? 96 : 100 - i), 100));
        }
        return result;
    }

}
