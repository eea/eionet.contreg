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
package eionet.cr.web.action;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import eionet.cr.ApplicationTestContext;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import eionet.cr.dto.TagDTO;
import eionet.cr.web.util.ApplicationCache;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author <a href="mailto:enriko.kasper@tieto.com">Enriko Käsper</a>
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationTestContext.class })
public class JsonActionBeanTest {

    @Autowired
    private MockServletContext ctx;

    @BeforeClass
    public static void beforeClass() {
        new ApplicationCache().contextDestroyed(null);
    }

    @Before
    public void setUp() {
        ActionBeanUtils.addFilter(ctx);
        new ApplicationCache().contextInitialized(null);
    }

    @After
    public void cleanUp() {
        ActionBeanUtils.clearFilters(ctx);
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testJsonTagsResult() throws Exception {
        ApplicationCache.updateTagCloudCache(getTestData());

        MockRoundtrip trip = new MockRoundtrip(ctx, JsonActionBean.class);
        trip.setParameter("query", "tag");
        trip.execute();

        assertEquals("{\"query\":\"\",\"suggestions\":[]}", trip.getResponse().getOutputString());

    }

    /**
     *
     * @return
     */
    private List<TagDTO> getTestData() {
        List<TagDTO> result = new ArrayList<TagDTO>();
        result.add(new TagDTO("tag4", 10, 10));
        result.add(new TagDTO("tag2", 10, 10));
        result.add(new TagDTO("tag1", 10, 10));
        result.add(new TagDTO("unknown2", 10, 10));
        result.add(new TagDTO("tag3", 10, 10));

        return result;
    }
}
