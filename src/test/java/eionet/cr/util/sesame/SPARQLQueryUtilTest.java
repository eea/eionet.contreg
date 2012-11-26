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
 * The Original Code is cr3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s): Enriko Käsper
 */

package eionet.cr.util.sesame;

import java.util.ArrayList;

import junit.framework.TestCase;
import eionet.cr.common.Namespace;
import eionet.cr.util.Bindings;

/**
 * Test SPARQLQueryUtil methods.
 *
 * @author Enriko Käsper
 */
public class SPARQLQueryUtilTest extends TestCase {

    private static final String CR_INFERENCE_DEF = SPARQLQueryUtil.getCrInferenceDefinitionStr();
    private static final String CR_NAMESPACE_DEF = "PREFIX cr: <http://cr.eionet.europa.eu/ontologies/contreg.rdf#> ";
    private static final String RDF_NAMESPACE_DEF = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ";
    private static final String RDFS_NAMESPACE_DEF = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> ";

    /**
     * Test getCrInferenceDefinition method.
     */
    public static void testCrInference() {

        assertEquals(CR_INFERENCE_DEF, SPARQLQueryUtil.getCrInferenceDefinitionStr());
    }

    /**
     * Test getPrefixes method.
     */
    public static void testNamespaceDefs() {

        assertEquals(CR_NAMESPACE_DEF, SPARQLQueryUtil.getPrefixes(Namespace.CR).toString());
        assertEquals(CR_NAMESPACE_DEF.concat(RDF_NAMESPACE_DEF).concat(RDFS_NAMESPACE_DEF),
                SPARQLQueryUtil.getPrefixes(Namespace.CR, Namespace.RDF, Namespace.RDFS).toString());
    }

    /**
     * Test getSparqlQueryHeader method.
     */
    public static void testSparqlQueryHeader() {

        assertEquals("", SPARQLQueryUtil.getSparqlQueryHeader(false).toString());
        assertEquals(CR_INFERENCE_DEF, SPARQLQueryUtil.getSparqlQueryHeader(true).toString());
        assertEquals(CR_INFERENCE_DEF.concat(CR_NAMESPACE_DEF), SPARQLQueryUtil.getSparqlQueryHeader(true, Namespace.CR)
                .toString());
        assertEquals(CR_NAMESPACE_DEF, SPARQLQueryUtil.getSparqlQueryHeader(false, Namespace.CR).toString());
        assertEquals(CR_INFERENCE_DEF.concat(CR_NAMESPACE_DEF).concat(RDF_NAMESPACE_DEF).concat(RDFS_NAMESPACE_DEF),
                SPARQLQueryUtil.getSparqlQueryHeader(true, Namespace.CR, Namespace.RDF, Namespace.RDFS).toString());
        assertEquals(CR_NAMESPACE_DEF.concat(RDF_NAMESPACE_DEF).concat(RDFS_NAMESPACE_DEF),
                SPARQLQueryUtil.getSparqlQueryHeader(false, Namespace.CR, Namespace.RDF, Namespace.RDFS).toString());
    }

    public void testUrisToCSV() {
        ArrayList<String> uris = new ArrayList<String>();
        uris.add("http://uri1.somewhere.nonono.com");
        uris.add("http://uri2.somewhere.nonono.com");
        Bindings bindings = new Bindings();
        String s = SPARQLQueryUtil.urisToCSV(uris, "subjectValue", bindings);

        assertEquals("?subjectValue1,?subjectValue2", s);
        assertTrue(bindings.toString().indexOf("subjectValue1=http://uri1.somewhere.nonono.com") != -1);
    }

    public void testUrisWithSpaceToCSV() {
        ArrayList<String> uris = new ArrayList<String>();
        uris.add("http://uri1.somewhere.nonono.com");
        uris.add("tel:+123 456 789");
        uris.add("http://uri2.somewhere.nonono.com");
        Bindings bindings = new Bindings();
        String s = SPARQLQueryUtil.urisToCSV(uris, "subjectValue", bindings);

        assertEquals("?subjectValue1,IRI(?subjectValue2),?subjectValue3", s);
        assertTrue(bindings.toString().indexOf("subjectValue1=http://uri1.somewhere.nonono.com") != -1);
        assertTrue(bindings.toString().indexOf("subjectValue2=tel:+123 456 789") != -1);
    }

    public void testOrderByClause() {
        String orderBy = SPARQLQueryUtil.getOrderByClause("oorderby", "asc");

        assertEquals(
                "ORDER BY asc(bif:either( bif:isnull(?oorderby) , (bif:lcase(bif:subseq (bif:replace (?s, '/', '#'), bif:strrchr (bif:replace (?s, '/', '#'), '#')+1))) , bif:lcase(?oorderby)))",
                orderBy);
    }

    public void testPrseIRIQuery() {
        String query = "SELECT ?s { WHERE ?s ?p ?o FILTER (?s=?subjectValue) ";

        query = SPARQLQueryUtil.parseIRIQuery(query, "subjectValue");

        assertEquals("SELECT ?s { WHERE ?s ?p ?o FILTER (?s=IRI(?subjectValue)) ", query);
    }
}
