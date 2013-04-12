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

import static eionet.cr.web.action.admin.postHarvest.PostHarvestScriptParser.HARVESTED_SOURCE_VARIABLE;
import static eionet.cr.web.action.admin.postHarvest.PostHarvestScriptParser.TEST_RESULTS_LIMIT;
import static eionet.cr.web.action.admin.postHarvest.PostHarvestScriptParser.deriveConstruct;
import junit.framework.TestCase;
import eionet.cr.web.action.admin.postHarvest.ScriptParseException;

/**
 *
 * @author Jaanus Heinlaid
 */
public class PostHarvestScriptParserTest extends TestCase {

    /** */
    private int limit = TEST_RESULTS_LIMIT;
    private String harvestedSource = "?" + HARVESTED_SOURCE_VARIABLE;

    /**
     *
     * @throws ScriptParseException
     */
    public void testDeriveConstructFromModify() throws ScriptParseException {

        // test MODIFY with one FROM
        String input = "PREFIX dc:<x> MODIFY <y> DELETE {1} INSERT {2} FROM " + harvestedSource + " WHERE {3}";
        String exptd = "PREFIX dc:<x> CONSTRUCT {2} FROM <http://> WHERE {3} LIMIT " + limit;
        assertEquals(exptd, deriveConstruct(input, "http://", null));

        // test MODIFY with multiple FROMs
        input = "PREFIX dc:<x> MODIFY <y> DELETE {1} INSERT {2} FROM " + harvestedSource + " FROM <z> WHERE {3}";
        exptd = "PREFIX dc:<x> CONSTRUCT {2} FROM <http://> FROM <z> WHERE {3} LIMIT " + limit;
        assertEquals(exptd, deriveConstruct(input, "http://", null));

        // test MODIFY without target graph
        input = "PREFIX dc:<x> MODIFY DELETE {1} INSERT {2} WHERE {3}";
        exptd = "PREFIX dc:<x> CONSTRUCT {2} WHERE {3} LIMIT " + limit;
        assertEquals(exptd, deriveConstruct(input, "http://", null));

        // test MODIFY with target graph and no WHERE pattern
        input = "PREFIX dc:<x> MODIFY <y> DELETE {1} INSERT {2}";
        exptd = "PREFIX dc:<x> CONSTRUCT {2} WHERE {?s ?p ?o} LIMIT " + limit;
        assertEquals(exptd, deriveConstruct(input, "http://", null));

        // test MODIFY with multiple target graphs and no WHERE pattern
        input = "PREFIX dc:<x> " + "MODIFY <y> <z> DELETE {1} INSERT {2}";
        exptd = "PREFIX dc:<x> CONSTRUCT {2} WHERE {?s ?p ?o} LIMIT " + limit;
        assertEquals(exptd, deriveConstruct(input, "http://", null));

        // test MODIFY without a specific target graph and no WHERE pattern
        input = "PREFIX dc:<x> MODIFY DELETE {1} INSERT {2}";
        exptd = "PREFIX dc:<x> CONSTRUCT {2} WHERE {?s ?p ?o} LIMIT " + limit;
        assertEquals(exptd, deriveConstruct(input, "http://", null));
    }

    /**
     *
     * @throws ScriptParseException
     */
    public void testDeriveConstructFromInsert() throws ScriptParseException {

        // test INSERT with one FROM
        String input = "PREFIX dc:<x> INSERT INTO <y> {2} FROM " + harvestedSource + " WHERE {3}";
        String exptd = "PREFIX dc:<x> CONSTRUCT {2} FROM <http://> WHERE {3} LIMIT " + limit;
        assertEquals(exptd, deriveConstruct(input, "http://", null));

        // test INSERT with multiple FROMs
        input = "PREFIX dc:<x> " + "INSERT INTO <y> {2} FROM " + harvestedSource + " FROM <z> WHERE {3}";
        exptd = "PREFIX dc:<x> CONSTRUCT {2} FROM <http://> FROM <z> WHERE {3} LIMIT " + limit;
        assertEquals(exptd, deriveConstruct(input, "http://", null));

        // test INSERT without a specific target graph
        input = "PREFIX dc:<x> INSERT {2} WHERE {3}";
        exptd = "PREFIX dc:<x> CONSTRUCT {2} WHERE {3} LIMIT " + limit;
        assertEquals(exptd, deriveConstruct(input, "http://", null));

        // test INSERT with one target graph and no WHERE pattern
        input = "PREFIX dc:<x> INSERT INTO <y> {2}";
        exptd = "PREFIX dc:<x> CONSTRUCT {2} WHERE {?s ?p ?o} LIMIT " + limit;
        assertEquals(exptd, deriveConstruct(input, "http://", null));

        // test INSERT with multiple target graphs and no WHERE pattern
        input = "PREFIX dc:<x> INSERT INTO <y> INTO <z> {2}";
        exptd = "PREFIX dc:<x> CONSTRUCT {2} WHERE {?s ?p ?o} LIMIT " + limit;
        assertEquals(exptd, deriveConstruct(input, "http://", null));

        // test INSERT without a specific target graph and no WHERE pattern
        input = "PREFIX dc:<x> INSERT {2}";
        exptd = "PREFIX dc:<x> CONSTRUCT {2} WHERE {?s ?p ?o} LIMIT " + limit;
        assertEquals(exptd, deriveConstruct(input, "http://", null));
    }

    /**
     *
     * @throws ScriptParseException
     */
    public void testDeriveConstructFromDelete() throws ScriptParseException {

        // test DELETE with one selection dataset
        String input = "PREFIX dc:<x> " + "DELETE FROM <y> {3} FROM " + harvestedSource + " WHERE {4}";
        String exptd = "PREFIX dc:<x> CONSTRUCT {3} FROM <http://> WHERE {4} LIMIT " + limit;
        assertEquals(exptd, deriveConstruct(input, "http://", null));

        // test DELETE with multiple selection dataset
        input = "PREFIX dc:<x> " + "DELETE FROM <y> {3} FROM " + harvestedSource + " FROM <z> WHERE {4}";
        exptd = "PREFIX dc:<x> CONSTRUCT {3} FROM <http://> FROM <z> WHERE {4} LIMIT " + limit;
        assertEquals(exptd, deriveConstruct(input, "http://", null));

        // test DELETE without a specific target graph
        input = "PREFIX dc:<x> INSERT {3} WHERE {3}";
        exptd = "PREFIX dc:<x> CONSTRUCT {3} WHERE {3} LIMIT " + limit;
        assertEquals(exptd, deriveConstruct(input, "http://", null));

        // test DELETE with one target graph and no WHERE pattern
        input = "PREFIX dc:<x> DELETE FROM <y> {3}";
        exptd = "PREFIX dc:<x> CONSTRUCT {3} WHERE {?s ?p ?o} LIMIT " + limit;
        assertEquals(exptd, deriveConstruct(input, "http://", null));

        // test DELETE with multiple target graphs and no WHERE pattern
        input = "PREFIX dc:<x> DELETE FROM <y> FROM <z> {3}";
        exptd = "PREFIX dc:<x> CONSTRUCT {3} WHERE {?s ?p ?o} LIMIT " + limit;
        assertEquals(exptd, deriveConstruct(input, "http://", null));

        // test DELETE without a specific target graph and no WHERE pattern
        input = "PREFIX dc:<x> INSERT {3}";
        exptd = "PREFIX dc:<x> CONSTRUCT {3} WHERE {?s ?p ?o} LIMIT " + limit;
        assertEquals(exptd, deriveConstruct(input, "http://", null));
    }

}
