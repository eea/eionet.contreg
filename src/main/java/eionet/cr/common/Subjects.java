package eionet.cr.common;

public interface Subjects {

	public static final String RSS_ITEM_CLASS = "http://purl.org/rss/1.0/Item";
	public static final String ROD_OBLIGATION_CLASS = "http://rod.eionet.eu.int/schema.rdf#Obligation";
	public static final String ROD_INSTRUMENT_CLASS = "http://rod.eionet.eu.int/schema.rdf#Instrument";
	public static final String ROD_LOCALITY_CLASS = "http://rod.eionet.eu.int/schema.rdf#Locality";
	public static final String ROD_ISSUE_CLASS = "http://rod.eionet.eu.int/schema.rdf#Issue";
	public static final String ROD_DELIVERY_CLASS = "http://rod.eionet.eu.int/schema.rdf#Delivery";
	
	public static final String FULL_REPORT_CLASS = "http://reports.eea.europa.eu/reports_rdf#FullReport";
	public static final String DCTYPE_DATASET_CLASS = "http://purl.org/dc/dcmitype/Dataset";
	public static final String QAW_RESOURCE_CLASS = "http://qaw.eionet.europa.eu/schema.rdf#QawResource";
	public static final String QA_REPORT_CLASS = "http://qaw.eionet.europa.eu/schema.rdf#QaReport";
	
	public static final String RDF_RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#RDF";
	
	public static final String ANON_ID_PREFIX = "http://cr.eionet.europa.eu/anonymous/";

	public static final String RDFS_LITERAL = "http://www.w3.org/2000/01/rdf-schema#Literal";
	public static final String RDFS_CLASS = "http://www.w3.org/2000/01/rdf-schema#Class";
}
