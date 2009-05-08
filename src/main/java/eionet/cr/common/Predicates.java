package eionet.cr.common;

public interface Predicates {

	public static final String DC_TITLE = "http://purl.org/dc/elements/1.1/title";
	public static final String DC_DATE = "http://purl.org/dc/elements/1.1/date";
	public static final String DC_COVERAGE = "http://purl.org/dc/elements/1.1/coverage";
	public static final String DC_IDENTIFIER = "http://purl.org/dc/elements/1.1/identifier";
	public static final String DC_SOURCE = "http://purl.org/dc/elements/1.1/source";
	public static final String DC_SUBJECT = "http://purl.org/dc/elements/1.1/subject";
	public static final String DC_CREATOR = "http://purl.org/dc/elements/1.1/creator";
	public static final String DC_DESCRIPTION = "http://purl.org/dc/elements/1.1/description";
	public static final String DC_PUBLISHER = "http://purl.org/dc/elements/1.1/publisher";
	public static final String DC_CONTRIBUTOR = "http://purl.org/dc/elements/1.1/contributor";
	public static final String DC_RELATION = "http://purl.org/dc/elements/1.1/relation";
	public static final String DC_LANGUAGE = "http://purl.org/dc/elements/1.1/language";
	
	public static final String DCTERMS_ALTERNATIVE = "http://purl.org/dc/terms/alternative";
	
	public static final String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
	public static final String RDFS_LABEL = "http://www.w3.org/2000/01/rdf-schema#label";
	public static final String RDFS_DOMAIN = "http://www.w3.org/2000/01/rdf-schema#domain";
	public static final String RDFS_RANGE = "http://www.w3.org/2000/01/rdf-schema#range";
	
	public static final String SKOS_PREF_LABEL = "http://www.w3.org/2004/02/skos/core#prefLabel";
		
	public static final String ROD_OBLIGATION_PROPERTY = "http://rod.eionet.eu.int/schema.rdf#obligation";
	public static final String ROD_INSTRUMENT_PROPERTY = "http://rod.eionet.eu.int/schema.rdf#instrument";
	public static final String ROD_LOCALITY_PROPERTY = "http://rod.eionet.eu.int/schema.rdf#locality";
	public static final String ROD_ISSUE_PROPERTY = "http://rod.eionet.eu.int/schema.rdf#issue";
	public static final String RDFS_SUBPROPERTY_OF = "http://www.w3.org/2000/01/rdf-schema#subPropertyOf";
	public static final String RDFS_SUBCLASS_OF = "http://www.w3.org/2000/01/rdf-schema#subClassOf";

	public static final String CR_FIRSTSEEN_TIME = "urn:eionet:contreg:firstseen-time";
	public static final String CR_URL = "urn:eionet:contreg:url";
}
