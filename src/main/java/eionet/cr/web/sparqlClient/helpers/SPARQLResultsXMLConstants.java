/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package eionet.cr.web.sparqlClient.helpers;

/**
 * Interface defining tags and attribute names that are used in SPARQL Results Documents. See <a
 * href="http://www.w3.org/TR/rdf-sparql-XMLres/">SPARQL Query Results XML Format</a> for the definition of this format.
 * 
 * @author Arjohn Kampman
 */
interface SPARQLResultsXMLConstants {

    String NAMESPACE = "http://www.w3.org/2005/sparql-results#";

    String ROOT_TAG = "sparql";

    String HEAD_TAG = "head";

    String VAR_TAG = "variable";

    String VAR_NAME_ATT = "name";

    String BOOLEAN_TAG = "boolean";

    String BOOLEAN_TRUE = "true";

    String BOOLEAN_FALSE = "false";

    String RESULT_SET_TAG = "results";

    String RESULT_TAG = "result";

    String BINDING_TAG = "binding";

    String BINDING_NAME_ATT = "name";

    String URI_TAG = "uri";

    String BNODE_TAG = "bnode";

    String LITERAL_TAG = "literal";

    String LITERAL_LANG_ATT = "xml:lang";

    String LITERAL_DATATYPE_ATT = "datatype";

    String UNBOUND_TAG = "unbound";
}
