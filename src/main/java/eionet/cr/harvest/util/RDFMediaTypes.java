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
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Jaanus Heinlaid
 */

package eionet.cr.harvest.util;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.openrdf.rio.RDFFormat;

/**
 *
 * @author Jaanus Heinlaid
 */
public final class RDFMediaTypes {

    /** */
    private static final LinkedHashMap<String, RDFFormat> MAPPINGS = init();

    /**
     * Hide utility class constructor.
     */
    private RDFMediaTypes() {
        // Just an empty private constructor to avoid instantiating this utility class.
    }

    /**
     * @return
     */
    private static LinkedHashMap<String, RDFFormat> init() {

        LinkedHashMap<String, RDFFormat> result = new LinkedHashMap<String, RDFFormat>();
        result.put("application/rdf+xml", RDFFormat.RDFXML);
        result.put("text/turtle", RDFFormat.TURTLE);
        result.put("text/n3", RDFFormat.N3);
        result.put("application/x-turtle", RDFFormat.TURTLE);
        result.put("text/rdf+n3", RDFFormat.N3);
        return result;
    }

    /**
     *
     * @param httpResponseContentType
     * @return
     */
    public static RDFFormat toRdfFormat(String httpResponseContentType) {

        if (httpResponseContentType != null) {
            String lowerCase = httpResponseContentType.toLowerCase();
            for (Map.Entry<String, RDFFormat> entry : MAPPINGS.entrySet()) {

                if (lowerCase.startsWith(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }

        return null;
    }

    /**
     *
     * @return
     */
    public static Collection<String> collection() {

        return Collections.unmodifiableSet(MAPPINGS.keySet());
    }
}
