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
import eionet.cr.config.GeneralConfig;

/**
 * Utility methods for building SPARQL queries.
 * @author Enriko Käsper
 */
public class SPARQLQueryUtil {

    /**
     * Inference definition in SPARQL.
     */
    public static final String inferenceDef = "DEFINE input:inference";


    /**
     * Constructs inference definition.
     * @return
     */
    public static StringBuilder getCrInferenceDefinition() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(inferenceDef).append("'").append(
                GeneralConfig
                        .getProperty(GeneralConfig.VIRTUOSO_CR_RULESET_NAME))
                .append("' ");
        return strBuilder;
    }

    /**
     * Construct the prefixes definitions.
     * @param namespaces
     * @return
     */
    public static StringBuilder getPrefixes(Namespace... namespaces){
        StringBuilder strBuilder = new StringBuilder();

        if(namespaces != null){
            for (Namespace ns : namespaces){
                strBuilder.append("PREFIX ").append(ns.getPrefix()).append(": <").append(ns.getUri()).append("> ");
            }
        }
        return strBuilder;
    }

    /**
     * Build SPARQL query heading with prefixes and inference definitions.
     * @param useCrInference add CRInference rule definition.
     * @param namespaces add namespace prefixes
     * @return start of SPARQL query
     */
    public static StringBuilder getSparqlQueryHeader(boolean useCrInference, Namespace... namespaces){
        StringBuilder strBuilder = new StringBuilder();

        if(useCrInference){
            strBuilder.append(getCrInferenceDefinition());
        }
        if(namespaces != null){
            strBuilder.append(getPrefixes(namespaces));
        }
        return strBuilder;
    }

}
