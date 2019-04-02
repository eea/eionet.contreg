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
 * Aleksandr Ivanov, Tieto Eesti
 */
package eionet.cr.util;

import java.util.LinkedList;
import java.util.List;
import eionet.cr.ApplicationTestContext;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import eionet.cr.web.util.ApplicationCache;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

/**
 * @author Aleksandr Ivanov <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationTestContext.class })
public class RecentDiscoveredFilesCacheTest {

    @BeforeClass
    public static void beforeClass() {
        new ApplicationCache().contextDestroyed(null);
    }

    @Before
    public void setUp() throws Exception {
        new ApplicationCache().contextInitialized(null);
    }

    @After
    public void tearDown() throws Exception {
        new ApplicationCache().contextDestroyed(null);
    }

    @Test
    public void testCache() {
        for (int i = 0; i < 100; i++) {
            ApplicationCache.updateRecentResourceCache(getTestData(11));
            assertEquals(10, ApplicationCache.getRecentDiscoveredFiles(10).size());
        }
        assertEquals(3, ApplicationCache.getRecentDiscoveredFiles(3).size());
        assertEquals("8", ApplicationCache.getRecentDiscoveredFiles(3).get(0).getLeft());
    }

    /**
     * @return
     */
    private List<Pair<String, String>> getTestData(int size) {
        List<Pair<String, String>> result = new LinkedList<Pair<String, String>>();
        for (int i = 0; i < size; i++) {
            result.add(new Pair<String, String>(i + "", "value"));
        }
        return result;
    }

}
