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

import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import eionet.cr.common.Namespace;
import eionet.cr.config.GeneralConfig;
import eionet.cr.util.Bindings;

/**
 * Utility methods for building SPARQL queries.
 *
 * @author Enriko Käsper
 */
public final class SPARQLQueryUtil {

    /** Chars not allowed in IRI. */
    private static final char[] BAD_CHARS = { ' ', '{', '}', '<', '>', '"', '|', '\\', '^', '`' };

    /**
     * Inference definition in SPARQL.
     */
    public static final String INFERENCE_DEF = "DEFINE input:inference";

    /**
     * Hide utility class constructor.
     */
    private SPARQLQueryUtil() {
        // Just an empty private constructor to avoid instantiating this utility class.
    }

    /**
     * Constructs inference definition.
     *
     * @return
     */
    public static StringBuilder getCrInferenceDefinition() {

        StringBuilder strBuilder = new StringBuilder();
        if (GeneralConfig.isUseInferencing()) {
            strBuilder.append(INFERENCE_DEF).append("'").append(GeneralConfig.getProperty(GeneralConfig.VIRTUOSO_CR_RULESET_NAME))
            .append("' ");
        }
        return strBuilder;
    }

    /**
     * Construct the prefixes definitions.
     *
     * @param namespaces
     * @return
     */
    public static StringBuilder getPrefixes(Namespace... namespaces) {
        StringBuilder strBuilder = new StringBuilder();

        if (namespaces != null) {
            for (Namespace ns : namespaces) {
                strBuilder.append("PREFIX ").append(ns.getPrefix()).append(": <").append(ns.getUri()).append("> ");
            }
        }
        return strBuilder;
    }

    /**
     * Build SPARQL query heading with prefixes and inference definitions.
     *
     * @param useCrInference add CRInference rule definition.
     * @param namespaces add namespace prefixes
     * @return start of SPARQL query
     */
    public static StringBuilder getSparqlQueryHeader(boolean useCrInference, Namespace... namespaces) {
        StringBuilder strBuilder = new StringBuilder();

        if (useCrInference) {
            strBuilder.append(getCrInferenceDefinition());
        }
        if (namespaces != null) {
            strBuilder.append(getPrefixes(namespaces));
        }
        return strBuilder;
    }

    /**
     * Builds a comma-separated String of SPARQL aliases the given parameter values. Fills bindings with correct values. Example:
     * urisToCSV(List{<http://uri1.notexist.com>, <http://uri2.notexist.com>}, "subjectValue")= ?subjectValue1,subjectValue2.
     * bindings are filled: subjectValue1=http://uri1.notexist.com, subjectValue2=http://uri2.notexist.com
     *
     * @param uriList uris to be used as SPARQL parameters
     * @param variableAliasName prefix for the value alias in the SPARQL query
     * @param bindings SPARQL bindings
     * @return comma separated String for Sparql
     */
    public static String urisToCSV(Collection<String> uriList, String variableAliasName, Bindings bindings) {

        StringBuilder strBuilder = new StringBuilder();
        if (uriList != null) {
            int i = 1;
            for (String uri : uriList) {
                String alias = variableAliasName + i;
                if (strBuilder.length() > 0) {
                    strBuilder.append(",");
                }
                // if URI contains spaces - use IRI() function that makes an IRI from URI and seems to work with spaces as well
                if (isIRI(uri)) {
                    strBuilder.append("?" + alias);
                } else {
                    strBuilder.append("IRI(?" + alias + ")");
                }

                if (bindings != null) {
                    bindings.setIRI(alias, uri);
                }
                i++;
            }
        }
        return strBuilder.toString();
    }

    /**
     * Builds a comma-separated String of SPARQL aliases the given parameter values.
     *
     * @param uriList uris to be used as SPARQL parameters
     * @return comma separated String for Sparql
     */
    public static String urisToCSV(Collection<String> uriList) {
        StringBuffer sb = new StringBuffer();
        if (uriList != null) {
            int idx = 0;
            for (String uri : uriList) {
                if (idx != 0) {
                    sb.append(",");
                }
                sb.append("<").append(uri).append(">");
                idx++;
            }
        }
        return sb.toString();
    }

    /**
     * Returns CR inference rule definition.
     *
     * @return SPARQL inference rule definition to be used at the bginning of SPARQL sentences.
     */
    public static String getCrInferenceDefinitionStr() {
        return getCrInferenceDefinition().toString();
    }

    /**
     * Order by clause used in many SPARQL sentences. Composes ORDER BY clause by the optional field (mostly rdfs:label) if label
     * not specified takes the last part of the URI.
     *
     * @param aliasName alias field name in the sparql
     * @param sortOrder valid sort order for the SPARQL (asc or desc)
     * @return SPARQL fragment.
     */
    public static String getOrderByClause(final String aliasName, final String sortOrder) {
        return "ORDER BY " + sortOrder + "(bif:either( bif:isnull(?" + aliasName + ") , "
        + "(bif:lcase(bif:subseq (bif:replace (?s, '/', '#'), bif:strrchr (bif:replace (?s, '/', '#'), '#')+1))) , "
        + "bif:lcase(?" + aliasName + ")))";
    }

    /**
     * Determines if it is valid IRI (for SPARQL).
     *
     * @param str given URI
     * @return true if the URI is valid IRI
     */
    public static boolean isIRI(String str) {

        if (StringUtils.containsAny(str, BAD_CHARS)) {
            return false;
        }
        return true;
    }

    /**
     * Changes SPARQL query to use IRI function for this parameter value. ?subject -> IRI(?subject)
     *
     * @param query SPARQL query
     * @param paramName parameter to be replaced
     * @return
     */
    public static String parseIRIQuery(String query, String paramName) {
        String tmpQuery = query;
        tmpQuery = StringUtils.replace(tmpQuery, "?" + paramName, "IRI(?" + paramName + ")");

        return tmpQuery;
    }
}
