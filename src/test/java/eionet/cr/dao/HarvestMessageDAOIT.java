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
import eionet.cr.ApplicationTestContext;
import org.junit.Test;
import eionet.cr.dto.HarvestMessageDTO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.test.helpers.CRDatabaseTestCase;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

/**
 * JUnit test tests HarvestMessageDAO functionality.
 *
 * @author altnyris
 *
 */
@SqlGroup({
        @Sql({"/sources-harvests-messages.sql"}),
        @Sql(scripts = "/sources-harvests-messages-cleanup.sql", executionPhase = BEFORE_TEST_METHOD)
})
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationTestContext.class })
public class HarvestMessageDAOIT extends CRDatabaseTestCase {

    @Autowired
    private HarvestMessageDAO harvestMessageDAO;
    @Autowired
    private HarvestSourceDAO harvestSourceDAO;

    /**
     *
     * @throws Exception
     */
    @Test
    public void testInsertAndFindHarvestMessage() throws Exception {
        HarvestMessageDTO harvestMessage = new HarvestMessageDTO();
        harvestMessage.setHarvestId(5);
        harvestMessage.setMessage("test");
        harvestMessage.setStackTrace("teststack");
        harvestMessage.setType("01");

        Integer messageID = harvestMessageDAO.insertHarvestMessage(harvestMessage);
        assertNotNull(messageID);

        harvestMessage = harvestMessageDAO.findHarvestMessageByMessageID(messageID.intValue());
        assertEquals(messageID, harvestMessage.getHarvestMessageId());
    }

    @Test
    public void testFindHarvestMessagesByHarvestID() throws Exception {
        List<HarvestMessageDTO> messages = harvestMessageDAO.findHarvestMessagesByHarvestID(5);
        assertEquals(4, messages.size());
    }

    @Test
    public void testDeleteMessage() throws Exception {
        harvestMessageDAO.deleteMessage(5);
        HarvestMessageDTO message = harvestMessageDAO.findHarvestMessageByMessageID(5);
        assertNull(message);
    }

    @Test
    public void testInsertSource() throws Exception {
        HarvestSourceDTO source = new HarvestSourceDTO();
        source.setUrl("http://1.ee");
        source.setIntervalMinutes(1);
        source.setPrioritySource(true);
        source.setEmails("emails");
        Integer id = harvestSourceDAO.addSource(source);
        assertNotNull(id);
        harvestSourceDAO.addSourceIgnoreDuplicate(source);
    }
}
