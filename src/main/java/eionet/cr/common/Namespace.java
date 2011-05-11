/*
* The contents of this file are subject to the Mozilla Public
*
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
* Agency. Portions created by Tieto Eesti are Copyright
* (C) European Environment Agency. All Rights Reserved.
*
* Contributor(s):
* Jaanus Heinlaid, Tieto Eesti*/
package eionet.cr.common;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public enum Namespace {

    RDF("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf"),
    RDFS("http://www.w3.org/2000/01/rdf-schema#", "rdfs"),
    OWL("http://www.w3.org/2002/07/owl#", "owl"),
    DC("http://purl.org/dc/elements/1.1/", "dc"),
    CR("http://cr.eionet.europa.eu/ontologies/contreg.rdf#", "cr"),
    IMS("http://rdfdata.eionet.europa.eu/ims/ontology/", "ims"),
	AMP("http://rdfdata.eionet.europa.eu/amp/ontology/", "amp"),
	AMP_IGN("http://moonefish.eea.europa.eu:81/AMP-schema.rdf#", "ampign"), // Phased out
	ROD("http://rod.eionet.europa.eu/schema.rdf#", "rod"),
    ROD_OLD("http://rod.eionet.eu.int/schema.rdf#", "rodold"),
    EPER("http://rdfdata.eionet.europa.eu/eper/dataflow", "eper");

    /** */
    private final String uri;
    private final String prefix;

    /**
     *
     */
    private Namespace(String uri, String prefix) {
        this.uri = uri;
        this.prefix = prefix;
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
    }
}
