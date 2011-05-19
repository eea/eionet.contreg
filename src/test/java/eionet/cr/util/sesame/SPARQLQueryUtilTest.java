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

import eionet.cr.common.Namespace;
import junit.framework.TestCase;

/**
 * Test SPARQLQueryUtil methods.
 *
 * @author Enriko Käsper
 */
public class SPARQLQueryUtilTest extends TestCase {

    private static final String CR_INFERENCE_DEF = "DEFINE input:inference'CRInferenceRule' ";
    private static final String CR_NAMESPACE_DEF = "PREFIX cr: <http://cr.eionet.europa.eu/ontologies/contreg.rdf#> ";
    private static final String RDF_NAMESPACE_DEF = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ";
    private static final String RDFS_NAMESPACE_DEF = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> ";

    /**
     * Test getCrInferenceDefinition method.
     */
    public static void testCrInference() {

        assertEquals(CR_INFERENCE_DEF, SPARQLQueryUtil.getCrInferenceDefinition().toString());
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
        assertEquals(CR_INFERENCE_DEF.concat(CR_NAMESPACE_DEF), SPARQLQueryUtil.getSparqlQueryHeader(true, Namespace.CR).toString());
        assertEquals(CR_NAMESPACE_DEF, SPARQLQueryUtil.getSparqlQueryHeader(false, Namespace.CR).toString());
        assertEquals(CR_INFERENCE_DEF.concat(CR_NAMESPACE_DEF).concat(RDF_NAMESPACE_DEF).concat(RDFS_NAMESPACE_DEF),
                SPARQLQueryUtil.getSparqlQueryHeader(true, Namespace.CR, Namespace.RDF, Namespace.RDFS).toString());
        assertEquals(CR_NAMESPACE_DEF.concat(RDF_NAMESPACE_DEF).concat(RDFS_NAMESPACE_DEF),
                SPARQLQueryUtil.getSparqlQueryHeader(false, Namespace.CR, Namespace.RDF, Namespace.RDFS).toString());
    }
}