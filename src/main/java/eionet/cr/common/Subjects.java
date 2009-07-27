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
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.cr.common;

public interface Subjects {

	public static final String RSS_ITEM_CLASS = "http://purl.org/rss/1.0/Item";
	public static final String ROD_OBLIGATION_CLASS = "http://rod.eionet.europa.eu/schema.rdf#Obligation";
	public static final String ROD_INSTRUMENT_CLASS = "http://rod.eionet.europa.eu/schema.rdf#Instrument";
	public static final String ROD_LOCALITY_CLASS = "http://rod.eionet.europa.eu/schema.rdf#Locality";
	public static final String ROD_ISSUE_CLASS = "http://rod.eionet.europa.eu/schema.rdf#Issue";
	public static final String ROD_DELIVERY_CLASS = "http://rod.eionet.europa.eu/schema.rdf#Delivery";
	
	public static final String FULL_REPORT_CLASS = "http://reports.eea.europa.eu/reports_rdf#FullReport";
	public static final String DCTYPE_DATASET_CLASS = "http://purl.org/dc/dcmitype/Dataset";
	
	public static final String QAW_RESOURCE_CLASS = "http://qaw.eionet.europa.eu/schema.rdf#QawResource";
	public static final String QA_REPORT_CLASS = "http://qaw.eionet.europa.eu/schema.rdf#QaReport";
	
	public static final String RDF_RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#RDF";
	
	public static final String ANON_ID_PREFIX = "http://cr.eionet.europa.eu/anonymous/";

	public static final String RDFS_LITERAL = "http://www.w3.org/2000/01/rdf-schema#Literal";
	public static final String RDFS_CLASS = "http://www.w3.org/2000/01/rdf-schema#Class";
	
	public static final String RDF_PROPERTY = "http://www.w3.org/1999/02/22-rdf-syntax-ns#Property";
	
	public static final String CR_FILE = "http://cr.eionet.europa.eu/ontologies/contreg.rdf#File";
	
	public static final String WGS_POINT = "http://www.w3.org/2003/01/geo/wgs84_pos#Point";
	public static final String WGS_SPATIAL_THING = "http://www.w3.org/2003/01/geo/wgs84_pos#SpatialThing";
}
