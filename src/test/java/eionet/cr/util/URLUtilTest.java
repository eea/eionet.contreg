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
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Kaido Laine
 */

package eionet.cr.util;

import eionet.cr.ApplicationTestContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

/**
 * Type definition ...
 *
 * @author Kaido Laine
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationTestContext.class })
public class URLUtilTest {

    @Test
    public void testReplaceBadCharsOK() {
        String url = "http://ok";
        assertEquals("http://ok", URLUtil.escapeIRI(url));
    }

    @Test
    public void testReplaceBadCharsnotOK() {
        String url = "http://a.b.c/{aaa}";
        assertEquals("http://a.b.c/%7Baaa%7D", URLUtil.escapeIRI(url));
    }

    @Test
    public void testReplaceSpaces() {
        String url = "http://a.b.c/ aaa b ";
        assertEquals("http://a.b.c/%20aaa%20b%20", URLUtil.escapeIRI(url));
    }

}
