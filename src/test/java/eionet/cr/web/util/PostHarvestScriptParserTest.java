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
 * Agency. Portions created by Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Jaanus Heinlaid
 */

package eionet.cr.web.util;

import static eionet.cr.web.util.PostHarvestScriptParser.parseForExecution;
import static eionet.cr.web.util.PostHarvestScriptParser.parseForTest;
import junit.framework.TestCase;

/**
 *
 * @author Jaanus Heinlaid
 */
public class PostHarvestScriptParserTest extends TestCase {

    /**
     *
     */
    public void test() {

        String[] ss = new String[4];
        ss[0] = "prefix xxx:yyy delete {1} where {2}";
        ss[1] = "prefix xxx:yyy insert {1} where {2}";
        ss[2] = "prefix xxx:yyy delete {1} insert {2} where {3}";
        ss[3] = "prefix xxx:yyy insert {2}";
        String graph = "http://www.neti.ee";

        assertEquals("prefix xxx:yyy DELETE FROM <http://www.neti.ee> {1} where {2}", parseForExecution(ss[0], graph));
        assertEquals("prefix xxx:yyy SELECT {1} FROM <http://www.neti.ee> WHERE {2}", parseForTest(ss[0], graph));

        assertEquals("prefix xxx:yyy INSERT INTO <http://www.neti.ee> {1} where {2}", parseForExecution(ss[1], graph));
        assertEquals("prefix xxx:yyy SELECT {1} FROM <http://www.neti.ee> WHERE {2}", parseForTest(ss[1], graph));

        assertEquals("prefix xxx:yyy MODIFY <http://www.neti.ee> DELETE {1} insert {2} where {3}", parseForExecution(ss[2], graph));
        assertEquals("prefix xxx:yyy SELECT {2} FROM <http://www.neti.ee> WHERE {3}", parseForTest(ss[2], graph));

        assertEquals("prefix xxx:yyy INSERT INTO <http://www.neti.ee> {2}", parseForExecution(ss[3], graph));
        assertEquals("prefix xxx:yyy SELECT {2} FROM <http://www.neti.ee> WHERE {?s ?p ?o}", parseForTest(ss[3], graph));
    }
}
