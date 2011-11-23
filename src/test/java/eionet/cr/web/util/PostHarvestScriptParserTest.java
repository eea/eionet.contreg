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

import static eionet.cr.web.action.admin.postHarvest.PostHarvestScriptParser.deriveConstruct;
import junit.framework.TestCase;
import eionet.cr.web.action.admin.postHarvest.ScriptParseException;

/**
 *
 * @author Jaanus Heinlaid
 */
public class PostHarvestScriptParserTest extends TestCase {

    /**
     *
     * @throws ScriptParseException
     */
    public void testDeriveConstructFromModify() throws ScriptParseException {

        // test MODIFY with one target graph
        String input = "PREFIX dc: <http://purl.org/dc/elements/1.1/> " +
        "MODIFY <http://my.ee> DELETE {deleteTemplate} INSERT {insertTemplate} WHERE {pattern}";
        String expected = "PREFIX dc: <http://purl.org/dc/elements/1.1/> CONSTRUCT {insertTemplate} FROM <http://my.ee> WHERE {pattern} LIMIT 500";
        assertEquals(expected, deriveConstruct(input, null));

        // test MODIFY with multiple target graphs
        input = "PREFIX dc: <http://purl.org/dc/elements/1.1/> " +
        "MODIFY <http://my1.ee> <http://my2.ee> DELETE {deleteTemplate} INSERT {insertTemplate} WHERE {pattern}";
        expected = "PREFIX dc: <http://purl.org/dc/elements/1.1/> CONSTRUCT {insertTemplate} " +
        "FROM <http://my1.ee> FROM <http://my2.ee> WHERE {pattern} LIMIT 500";
        assertEquals(expected, deriveConstruct(input, null));

        // test MODIFY without a specific target graph
        input = "PREFIX dc: <http://purl.org/dc/elements/1.1/> " +
        "MODIFY DELETE {deleteTemplate} INSERT {insertTemplate} WHERE {pattern}";
        expected = "PREFIX dc: <http://purl.org/dc/elements/1.1/> CONSTRUCT {insertTemplate} WHERE {pattern} LIMIT 500";
        assertEquals(expected, deriveConstruct(input, null));

        // test MODIFY with one target graph and no WHERE pattern
        input = "PREFIX dc: <http://purl.org/dc/elements/1.1/> " +
        "MODIFY <http://my.ee> DELETE {deleteTemplate} INSERT {insertTemplate}";
        expected = "PREFIX dc: <http://purl.org/dc/elements/1.1/> CONSTRUCT {insertTemplate} FROM <http://my.ee> WHERE {?s ?p ?o} LIMIT 500";
        assertEquals(expected, deriveConstruct(input, null));

        // test MODIFY with multiple target graphs and no WHERE pattern
        input = "PREFIX dc: <http://purl.org/dc/elements/1.1/> " +
        "MODIFY <http://my1.ee> <http://my2.ee> DELETE {deleteTemplate} INSERT {insertTemplate}";
        expected = "PREFIX dc: <http://purl.org/dc/elements/1.1/> CONSTRUCT {insertTemplate} " +
        "FROM <http://my1.ee> FROM <http://my2.ee> WHERE {?s ?p ?o} LIMIT 500";
        assertEquals(expected, deriveConstruct(input, null));

        // test MODIFY without a specific target graph and no WHERE pattern
        input = "PREFIX dc: <http://purl.org/dc/elements/1.1/> " +
        "MODIFY DELETE {deleteTemplate} INSERT {insertTemplate}";
        expected = "PREFIX dc: <http://purl.org/dc/elements/1.1/> CONSTRUCT {insertTemplate} WHERE {?s ?p ?o} LIMIT 500";
        assertEquals(expected, deriveConstruct(input, null));

    }

    /**
     *
     * @throws ScriptParseException
     */
    public void testDeriveConstructFromInsert() throws ScriptParseException {

        // test INSERT with one target graph
        String input = "PREFIX dc: <http://purl.org/dc/elements/1.1/> " +
        "INSERT INTO <http://my.ee> {insertTemplate} WHERE {pattern}";
        String expected = "PREFIX dc: <http://purl.org/dc/elements/1.1/> CONSTRUCT {insertTemplate} FROM <http://my.ee> WHERE {pattern} LIMIT 500";
        assertEquals(expected, deriveConstruct(input, null));

        // test INSERT with multiple target graphs
        input = "PREFIX dc: <http://purl.org/dc/elements/1.1/> " +
        "INSERT INTO <http://my1.ee> INTO <http://my2.ee> {insertTemplate} WHERE {pattern}";
        expected = "PREFIX dc: <http://purl.org/dc/elements/1.1/> CONSTRUCT {insertTemplate} FROM <http://my1.ee> FROM <http://my2.ee> WHERE {pattern} LIMIT 500";
        assertEquals(expected, deriveConstruct(input, null));

        // test INSERT without a specific target graph
        input = "PREFIX dc: <http://purl.org/dc/elements/1.1/> INSERT {insertTemplate} WHERE {pattern}";
        expected = "PREFIX dc: <http://purl.org/dc/elements/1.1/> CONSTRUCT {insertTemplate} WHERE {pattern} LIMIT 500";
        assertEquals(expected, deriveConstruct(input, null));

        // test INSERT with one target graph and no WHERE pattern
        input = "PREFIX dc: <http://purl.org/dc/elements/1.1/> INSERT INTO <http://my.ee> {insertTemplate}";
        expected = "PREFIX dc: <http://purl.org/dc/elements/1.1/> CONSTRUCT {insertTemplate} FROM <http://my.ee> WHERE {?s ?p ?o} LIMIT 500";
        assertEquals(expected, deriveConstruct(input, null));

        // test INSERT with multiple target graphs and no WHERE pattern
        input = "PREFIX dc: <http://purl.org/dc/elements/1.1/> INSERT INTO <http://my1.ee> INTO <http://my2.ee> {insertTemplate}";
        expected = "PREFIX dc: <http://purl.org/dc/elements/1.1/> CONSTRUCT {insertTemplate} FROM <http://my1.ee> FROM <http://my2.ee> WHERE {?s ?p ?o} LIMIT 500";
        assertEquals(expected, deriveConstruct(input, null));

        // test INSERT without a specific target graph and no WHERE pattern
        input = "PREFIX dc: <http://purl.org/dc/elements/1.1/> INSERT {insertTemplate}";
        expected = "PREFIX dc: <http://purl.org/dc/elements/1.1/> CONSTRUCT {insertTemplate} WHERE {?s ?p ?o} LIMIT 500";
        assertEquals(expected, deriveConstruct(input, null));
    }

    /**
     *
     * @throws ScriptParseException
     */
    public void testDeriveConstructFromDelete() throws ScriptParseException {

        // test DELETE with one target graph
        String input = "PREFIX dc: <http://purl.org/dc/elements/1.1/> " +
        "DELETE FROM <http://my.ee> {deleteTemplate} WHERE {pattern}";
        String expected = "PREFIX dc: <http://purl.org/dc/elements/1.1/> CONSTRUCT {deleteTemplate} FROM <http://my.ee> WHERE {pattern} LIMIT 500";
        assertEquals(expected, deriveConstruct(input, null));

        // test DELETE with multiple target graphs
        input = "PREFIX dc: <http://purl.org/dc/elements/1.1/> " +
        "DELETE FROM <http://my1.ee> FROM <http://my2.ee> {deleteTemplate} WHERE {pattern}";
        expected = "PREFIX dc: <http://purl.org/dc/elements/1.1/> CONSTRUCT {deleteTemplate} FROM <http://my1.ee> FROM <http://my2.ee> WHERE {pattern} LIMIT 500";
        assertEquals(expected, deriveConstruct(input, null));

        // test DELETE without a specific target graph
        input = "PREFIX dc: <http://purl.org/dc/elements/1.1/> INSERT {deleteTemplate} WHERE {pattern}";
        expected = "PREFIX dc: <http://purl.org/dc/elements/1.1/> CONSTRUCT {deleteTemplate} WHERE {pattern} LIMIT 500";
        assertEquals(expected, deriveConstruct(input, null));

        // test DELETE with one target graph and no WHERE pattern
        input = "PREFIX dc: <http://purl.org/dc/elements/1.1/> DELETE FROM <http://my.ee> {deleteTemplate}";
        expected = "PREFIX dc: <http://purl.org/dc/elements/1.1/> CONSTRUCT {deleteTemplate} FROM <http://my.ee> WHERE {?s ?p ?o} LIMIT 500";
        assertEquals(expected, deriveConstruct(input, null));

        // test DELETE with multiple target graphs and no WHERE pattern
        input = "PREFIX dc: <http://purl.org/dc/elements/1.1/> DELETE FROM <http://my1.ee> FROM <http://my2.ee> {deleteTemplate}";
        expected = "PREFIX dc: <http://purl.org/dc/elements/1.1/> CONSTRUCT {deleteTemplate} FROM <http://my1.ee> FROM <http://my2.ee> WHERE {?s ?p ?o} LIMIT 500";
        assertEquals(expected, deriveConstruct(input, null));

        // test DELETE without a specific target graph and no WHERE pattern
        input = "PREFIX dc: <http://purl.org/dc/elements/1.1/> INSERT {deleteTemplate}";
        expected = "PREFIX dc: <http://purl.org/dc/elements/1.1/> CONSTRUCT {deleteTemplate} WHERE {?s ?p ?o} LIMIT 500";
        assertEquals(expected, deriveConstruct(input, null));
    }

}
