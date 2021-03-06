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
package eionet.cr.util.pagintation;

import eionet.cr.ApplicationTestContext;
import eionet.cr.util.pagination.Pagination;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationTestContext.class })
public class PaginationTest {

    /**
     *
     */
    @Test
    public void testPagination() {

        Pagination pagination = Pagination.createPagination(Pagination.pageLength(), 2, "", null);
        assertNull(pagination);

        pagination = Pagination.createPagination(Pagination.pageLength() + 1, 2, "", null);

        assertNotNull(pagination.getPrev());
        assertEquals(1, pagination.getPrev().getNumber());

        assertNull(pagination.getNext());
        assertNull(pagination.getLast());

        assertNotNull(pagination.getGroup());
        assertEquals(2, pagination.getGroup().size());

        assertEquals(1, pagination.getGroup().get(0).getNumber());
        assertEquals("?" + Pagination.PAGE_NUM_PARAM + "=1", pagination.getGroup().get(0).getHref());
        assertEquals(2, pagination.getGroup().get(1).getNumber());
        assertEquals("?" + Pagination.PAGE_NUM_PARAM + "=2", pagination.getGroup().get(1).getHref());

        assertTrue(pagination.getGroup().get(1).isSelected());
        assertFalse(pagination.getGroup().get(0).isSelected());

        assertEquals(pagination.getRowsFrom(), pagination.getRowsTo());
    }
}
