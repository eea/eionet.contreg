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

package eionet.cr.web.action.admin.postHarvest;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.Ignore;


/**
 *
 * @author Jaanus Heinlaid
 */
public class PostHarvestScriptParserTest {

    /** */
    private int limit = PostHarvestScriptParser.TEST_RESULTS_LIMIT;
    private String harvestedSource = "?" + PostHarvestScriptParser.HARVESTED_SOURCE_VARIABLE;

    /** The name of the variable to bind the RDF type to. */
    private String thisType = "?" + PostHarvestScriptParser.ASSOCIATED_TYPE_VARIABLE;

    /**
     *
     * @throws ScriptParseException
     */
    @Test
    public void testDeriveConstructFromModify() throws ScriptParseException {

        // test MODIFY with one FROM
        String input = "PREFIX dc:<x> MODIFY <y> DELETE {1} INSERT {2} FROM " + harvestedSource + " WHERE {3}";
        String exptd = "PREFIX dc:<x> CONSTRUCT {2} FROM <http://> WHERE {3} LIMIT " + limit;
        assertEquals(exptd, PostHarvestScriptParser.deriveConstruct(input, "http://", null));

        // test MODIFY with multiple FROMs
        input = "PREFIX dc:<x> MODIFY <y> DELETE {1} INSERT {2} FROM " + harvestedSource + " FROM <z> WHERE {3}";
        exptd = "PREFIX dc:<x> CONSTRUCT {2} FROM <http://> FROM <z> WHERE {3} LIMIT " + limit;
        assertEquals(exptd, PostHarvestScriptParser.deriveConstruct(input, "http://", null));

        // test MODIFY without target graph
        input = "PREFIX dc:<x> MODIFY DELETE {1} INSERT {2} WHERE {3}";
        exptd = "PREFIX dc:<x> CONSTRUCT {2} WHERE {3} LIMIT " + limit;
        assertEquals(exptd, PostHarvestScriptParser.deriveConstruct(input, "http://", null));

        // test MODIFY with target graph and no WHERE pattern
        input = "PREFIX dc:<x> MODIFY <y> DELETE {1} INSERT {2}";
        exptd = "PREFIX dc:<x> CONSTRUCT {2} WHERE {?s ?p ?o} LIMIT " + limit;
        assertEquals(exptd, PostHarvestScriptParser.deriveConstruct(input, "http://", null));

        // test MODIFY with multiple target graphs and no WHERE pattern
        input = "PREFIX dc:<x> " + "MODIFY <y> <z> DELETE {1} INSERT {2}";
        exptd = "PREFIX dc:<x> CONSTRUCT {2} WHERE {?s ?p ?o} LIMIT " + limit;
        assertEquals(exptd, PostHarvestScriptParser.deriveConstruct(input, "http://", null));

        // test MODIFY without a specific target graph and no WHERE pattern
        input = "PREFIX dc:<x> MODIFY DELETE {1} INSERT {2}";
        exptd = "PREFIX dc:<x> CONSTRUCT {2} WHERE {?s ?p ?o} LIMIT " + limit;
        assertEquals(exptd, PostHarvestScriptParser.deriveConstruct(input, "http://", null));
    }

    /**
     * Derive a CONSTRUCT statement from a MODIFY with a FROM and a SUB-SELECT.
     */
    @Ignore("This combination is not yet supported")
    @Test
    public void testConstructFromSubselect() throws ScriptParseException {
        String input = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> "
            + "MODIFY GRAPH ?harvestedSource "
            + "DELETE { ?record skos:exactMatch ?url } "
            + "INSERT { ?record skos:narrowMatch ?url } "
            + "WHERE { "
            + "{ SELECT ?record count(?eunisurl) AS ?c "
            + "FROM <http://dbpedia.org/sparql> "
            + "WHERE { ?record skos:exactMatch ?eunisurl FILTER(REGEX(?eunisurl,'http://eunis.eea.europa.eu/species/')) } "
            + "GROUP BY ?record ORDER BY DESC(?c) LIMIT 200 "
            + "} "
            + "FILTER(?c > 1) "
            + "?record skos:exactMatch ?url FILTER(REGEX(?url,'http://eunis.eea.europa.eu/species/')) "
            + "} "
            + "} ";

        String exptd = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> "
            + "CONSTRUCT { ?record skos:narrowMatch ?url } "
            + "WHERE { "
            + "{ SELECT ?record count(?eunisurl) AS ?c "
            + "FROM <http://dbpedia.org/sparql> "
            + "WHERE { ?record skos:exactMatch ?eunisurl FILTER(REGEX(?eunisurl,'http://eunis.eea.europa.eu/species/')) } "
            + "GROUP BY ?record ORDER BY DESC(?c) LIMIT 200 "
            + "} "
            + "FILTER(?c > 1) "
            + "?record skos:exactMatch ?url FILTER(REGEX(?url,'http://eunis.eea.europa.eu/species/')) "
            + "} "
            + "} LIMIT " + limit;
        assertEquals(exptd, PostHarvestScriptParser.deriveConstruct(input, "http://", null));
    }

    /**
     * Derive a CONSTRUCT statement from a MODIFY with a GRAPH and a SUB-SELECT.
     */
    @Test
    public void testConstructFromSubselectGraph() throws ScriptParseException {
        String input = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> "
            + "MODIFY GRAPH ?harvestedSource "
            + "DELETE { ?record skos:exactMatch ?url } "
            + "INSERT { ?record skos:narrowMatch ?url } "
            + "WHERE { "
            + "{ SELECT ?record count(?eunisurl) AS ?c "
            + "WHERE { GRAPH ?harvestedSource {?record skos:exactMatch ?eunisurl FILTER(REGEX(?eunisurl,'http://eunis.eea.europa.eu/species/')) }}"
            + "GROUP BY ?record ORDER BY DESC(?c) LIMIT 200 "
            + "} "
            + "FILTER(?c > 1) "
            + "?record skos:exactMatch ?url FILTER(REGEX(?url,'http://eunis.eea.europa.eu/species/')) "
            + "} ";

        String exptd = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> "
            + "CONSTRUCT { ?record skos:narrowMatch ?url } "
            + "WHERE { "
            + "{ SELECT ?record count(?eunisurl) AS ?c "
            + "WHERE { GRAPH <http://> {?record skos:exactMatch ?eunisurl FILTER(REGEX(?eunisurl,'http://eunis.eea.europa.eu/species/')) }}"
            + "GROUP BY ?record ORDER BY DESC(?c) LIMIT 200 "
            + "} "
            + "FILTER(?c > 1) "
            + "?record skos:exactMatch ?url FILTER(REGEX(?url,'http://eunis.eea.europa.eu/species/')) "
            + "} LIMIT " + limit;
        assertEquals(exptd, PostHarvestScriptParser.deriveConstruct(input, "http://", null));
    }
    /**
     *
     * @throws ScriptParseException
     */
    @Test
    public void testDeriveConstructFromInsert() throws ScriptParseException {

        // test INSERT with one FROM
        String input = "PREFIX dc:<x> INSERT INTO <y> {2} FROM " + harvestedSource + " WHERE {3}";
        String exptd = "PREFIX dc:<x> CONSTRUCT {2} FROM <http://> WHERE {3} LIMIT " + limit;
        assertEquals(exptd, PostHarvestScriptParser.deriveConstruct(input, "http://", null));

        // test INSERT with multiple FROMs
        input = "PREFIX dc:<x> " + "INSERT INTO <y> {2} FROM " + harvestedSource + " FROM <z> WHERE {3}";
        exptd = "PREFIX dc:<x> CONSTRUCT {2} FROM <http://> FROM <z> WHERE {3} LIMIT " + limit;
        assertEquals(exptd, PostHarvestScriptParser.deriveConstruct(input, "http://", null));

        // test INSERT without a specific target graph
        input = "PREFIX dc:<x> INSERT {2} WHERE {3}";
        exptd = "PREFIX dc:<x> CONSTRUCT {2} WHERE {3} LIMIT " + limit;
        assertEquals(exptd, PostHarvestScriptParser.deriveConstruct(input, "http://", null));

        // test INSERT with one target graph and no WHERE pattern
        input = "PREFIX dc:<x> INSERT INTO <y> {2}";
        exptd = "PREFIX dc:<x> CONSTRUCT {2} WHERE {?s ?p ?o} LIMIT " + limit;
        assertEquals(exptd, PostHarvestScriptParser.deriveConstruct(input, "http://", null));

        // test INSERT with multiple target graphs and no WHERE pattern
        input = "PREFIX dc:<x> INSERT INTO <y> INTO <z> {2}";
        exptd = "PREFIX dc:<x> CONSTRUCT {2} WHERE {?s ?p ?o} LIMIT " + limit;
        assertEquals(exptd, PostHarvestScriptParser.deriveConstruct(input, "http://", null));

        // test INSERT without a specific target graph and no WHERE pattern
        input = "PREFIX dc:<x> INSERT {2}";
        exptd = "PREFIX dc:<x> CONSTRUCT {2} WHERE {?s ?p ?o} LIMIT " + limit;
        assertEquals(exptd, PostHarvestScriptParser.deriveConstruct(input, "http://", null));
    }

    /**
     *
     * @throws ScriptParseException
     */
    @Test
    public void testDeriveConstructFromDelete() throws ScriptParseException {

        // test DELETE with one selection dataset
        String input = "PREFIX dc:<x> " + "DELETE FROM <y> {3} FROM " + harvestedSource + " WHERE {4}";
        String exptd = "PREFIX dc:<x> CONSTRUCT {3} FROM <http://> WHERE {4} LIMIT " + limit;
        assertEquals(exptd, PostHarvestScriptParser.deriveConstruct(input, "http://", null));

        // test DELETE with multiple selection dataset
        input = "PREFIX dc:<x> " + "DELETE FROM <y> {3} FROM " + harvestedSource + " FROM <z> WHERE {4}";
        exptd = "PREFIX dc:<x> CONSTRUCT {3} FROM <http://> FROM <z> WHERE {4} LIMIT " + limit;
        assertEquals(exptd, PostHarvestScriptParser.deriveConstruct(input, "http://", null));

        // test DELETE without a specific target graph
        input = "PREFIX dc:<x> INSERT {3} WHERE {3}";
        exptd = "PREFIX dc:<x> CONSTRUCT {3} WHERE {3} LIMIT " + limit;
        assertEquals(exptd, PostHarvestScriptParser.deriveConstruct(input, "http://", null));

        // test DELETE with one target graph and no WHERE pattern
        input = "PREFIX dc:<x> DELETE FROM <y> {3}";
        exptd = "PREFIX dc:<x> CONSTRUCT {3} WHERE {?s ?p ?o} LIMIT " + limit;
        assertEquals(exptd, PostHarvestScriptParser.deriveConstruct(input, "http://", null));

        // test DELETE with multiple target graphs and no WHERE pattern
        input = "PREFIX dc:<x> DELETE FROM <y> FROM <z> {3}";
        exptd = "PREFIX dc:<x> CONSTRUCT {3} WHERE {?s ?p ?o} LIMIT " + limit;
        assertEquals(exptd, PostHarvestScriptParser.deriveConstruct(input, "http://", null));

        // test DELETE without a specific target graph and no WHERE pattern
        input = "PREFIX dc:<x> INSERT {3}";
        exptd = "PREFIX dc:<x> CONSTRUCT {3} WHERE {?s ?p ?o} LIMIT " + limit;
        assertEquals(exptd, PostHarvestScriptParser.deriveConstruct(input, "http://", null));
    }

    /**
     *
     * @throws ScriptParseException
     */
    @Test
    public void checkTypeExpansionForInsert() throws ScriptParseException {

        String testSource = "http://";
        String expandedSource = "<" + testSource + ">";

        String testType = "urn:nowhere:type";
        String expnType = "<" + testType + ">";

        String input = "PREFIX dc:<x> INSERT INTO <y> {2} FROM " + harvestedSource + " WHERE { ?s a "
            + thisType + "; a ?rdftype FILTER(?rdftype != " + thisType + ") }";
        String exptd = "PREFIX dc:<x> CONSTRUCT {2} FROM "+ expandedSource + " WHERE { ?s a "
            + expnType + "; a ?rdftype FILTER(?rdftype != " + expnType + ") } LIMIT " + limit;
        assertEquals(exptd, PostHarvestScriptParser.deriveConstruct(input, testSource, testType));
    }

    /**
     *
     * @throws ScriptParseException
     */
    @Test
    public void checkTypeExpansionForModify() throws ScriptParseException {

        String testSource = "http://mysource";
        String expandedSource = "<" + testSource + ">";

        String testType = "http://nowhere/type";
        String expandedType = "<" + testType + ">";

        String input = "PREFIX dc:<x> MODIFY <y> DELETE {1} INSERT {2} FROM " + harvestedSource + " WHERE {?s a " + thisType + "}";
        String exptd = "PREFIX dc:<x> CONSTRUCT {2} FROM " + expandedSource + " WHERE {?s a " + expandedType + "} LIMIT " + limit;

        assertEquals(exptd, PostHarvestScriptParser.deriveConstruct(input, testSource, testType));
    }

    /**
     *
     * @throws ScriptParseException
     */
    @Test
    public void checkTypeExpansionForDelete() throws ScriptParseException {

        String testSource = "urn:nowhere:source";
        String expandedSource = "<" + testSource + ">";

        String testType = "urn:nowhere:type";
        String expandedType = "<" + testType + ">";

        String input = "PREFIX dc:<x> " + "DELETE FROM <y> {3} FROM " + harvestedSource + " WHERE { GRAPH "
            + harvestedSource + " {    ?s a " + thisType + "\n  }\n}";
        String exptd = "PREFIX dc:<x> CONSTRUCT {3} FROM " + expandedSource + " WHERE { GRAPH "
            + expandedSource +  " {    ?s a " + expandedType + "\n  }\n} LIMIT " + limit;

        assertEquals(exptd, PostHarvestScriptParser.deriveConstruct(input, testSource, testType));
    }

}
