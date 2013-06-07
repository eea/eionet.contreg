package eionet.cr.web.util;

import org.apache.commons.lang.StringUtils;

/**
 * An enumeration of useful namespaces suggested for SPARQL queries written by users in form inputs.
 * 
 * @author jaanus
 */
public enum UsefulNamespace {

    /** The list of namespaces. */
    RDF("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"),
    RDFS("rdfs", "http://www.w3.org/2000/01/rdf-schema#"),
    XSD("xsd", "http://www.w3.org/2001/XMLSchema#"),
    OWL("owl", "http://www.w3.org/2002/07/owl#"),
    DC("dc", "http://purl.org/dc/elements/1.1/"),
    DCTERMS("dcterms", "http://purl.org/dc/terms/"),
    FOAF("foaf", "http://xmlns.com/foaf/0.1/"),
    GEO("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#"),
    CR("cr", "http://cr.eionet.europa.eu/ontologies/contreg.rdf#"),
    ROD("rod", "http://rod.eionet.europa.eu/schema.rdf#"),
    SKOS("skos", "http://www.w3.org/2004/02/skos/core#");

    /** Namespace prefix. */
    private String prefix;

    /** Namespace URI. */
    private String uri;

    /**
     * Constructor by the given prefix and URI.
     * @param prefix The prefix.
     * @param uri The URI.
     */
    UsefulNamespace(String prefix, String uri) {

        if (StringUtils.isBlank(prefix) || StringUtils.isBlank(uri)) {
            throw new IllegalArgumentException("The namespace prefix and URI must not be blank!");
        }

        this.prefix = prefix;
        this.uri = uri;
    }

    /**
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }
}
