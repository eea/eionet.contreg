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
package eionet.cr.util;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 * 
 */
public class UtilTest extends TestCase {

    // TODO some tests to be added, a method-less TestCase causes an error in JUnit

    @Test
    public void test_toCSV() throws Exception {

        try {
            assertEquals("", Util.toCSV(null));
        } catch (NullPointerException e) {
            fail("Did not expect " + e.getClass().getSimpleName());
        }
        assertEquals("", Util.toCSV(new ArrayList<String>()));
    }

    @Test
    public void test_normalizeHTTPAcceptedLanguage() throws Exception {

        try {
            assertEquals(null, Util.normalizeHTTPAcceptedLanguage(null));
        } catch (NullPointerException e) {
            fail("Did not expect " + e.getClass().getSimpleName());
        }
        assertEquals("", Util.normalizeHTTPAcceptedLanguage(""));
        assertEquals("", Util.normalizeHTTPAcceptedLanguage(" "));
    }

    @Test
    public void test_calculateHashesCount() {
        assertEquals(1, Util.calculateHashesCount(0, 0));
        assertEquals(1, Util.calculateHashesCount(100000, 100000));
        assertEquals(9642, Util.calculateHashesCount(-9223272718994049566L, 9221558204482793360L));
    }

    @Test
    public void testSplitStringBySpaces() {
        String tags = "tag1 tag2 tag3 \"tag4  tag5\"  tag6 'tag7 tag8' tag9";
        List<String> resultList = Util.splitStringBySpacesExpectBetweenQuotes(tags);

        assertEquals("tag1", resultList.get(0));
        assertEquals("tag2", resultList.get(1));
        assertEquals("tag3", resultList.get(2));
        assertEquals("tag4  tag5", resultList.get(3));
        assertEquals("tag6", resultList.get(4));
        assertEquals("tag7 tag8", resultList.get(5));
        assertEquals("tag9", resultList.get(6));

        String tag = "tag1";
        resultList = Util.splitStringBySpacesExpectBetweenQuotes(tag);
        assertEquals(1, resultList.size());
        assertEquals("tag1", resultList.get(0));

    }

    @Test
    public void testSurrendedWithQuotes() {
        // "
        String s = "\"";
        assertFalse(Util.isSurroundedWithQuotes(s));
        // ""
        s = "\"\"";
        assertTrue(Util.isSurroundedWithQuotes(s));
        // "Fish"
        s = "\"Fish\"";
        assertTrue(Util.isSurroundedWithQuotes(s));
        // ""Cheese"
        s = "\"\"Cheese\"";
        assertTrue(Util.isSurroundedWithQuotes(s));
        // Boo"
        s = "Boo\"";
        assertFalse(Util.isSurroundedWithQuotes(s));
    }

    @Test
    public void testRemoveQuotes() {
        //
        String s = "";
        assertEquals(Util.removeSurroundingQuotes(s), "");
        // "
        s = "\"";
        assertEquals(Util.removeSurroundingQuotes(s), s);
        s = "\"\"";
        assertEquals(Util.removeSurroundingQuotes(s), "");
        s = "fish";
        assertEquals(Util.removeSurroundingQuotes(s), "fish");
        s = "Home\"";
        assertEquals(Util.removeSurroundingQuotes(s), s);
        s = "\"Dataflow\"";
        assertEquals(Util.removeSurroundingQuotes(s), "Dataflow");

    }
}
