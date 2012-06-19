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
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.cr.harvest.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public final class MediaTypeToDcmiTypeConverter {

    /**
     * File containing mimetypes in properties format.
     */
    private static final String MAPPINGS_FILENAME = "mediaTypeToDcmiType.xml";

    /** */
    private static final Logger LOGGER = Logger.getLogger(MediaTypeToDcmiTypeConverter.class);

    /** */
    private static LinkedHashMap<String, String> mappings;

    /** */
    private static Object initializationLock = new Object();

    /**
     * Hide utility class constructor.
     */
    private MediaTypeToDcmiTypeConverter() {
        // Just an empty private constructor to avoid instantiating this utility class.
    }

    /**
     * Loads the mimetypes from the file and puts them into mimeToRdfMap.
     */
    private static void initialize() {

        mappings = new LinkedHashMap<String, String>();

        InputStream inputStream = null;
        Properties properties = new Properties();
        try {
            inputStream = MediaTypeToDcmiTypeConverter.class.getClassLoader().getResourceAsStream(MAPPINGS_FILENAME);
            properties.loadFromXML(inputStream);
        } catch (IOException e) {
            LOGGER.error("Failed to load XML-formatted properties from " + MAPPINGS_FILENAME, e);
        } finally {
            if (inputStream != null) {
                IOUtils.closeQuietly(inputStream);
            }
        }

        if (!properties.isEmpty()) {

            for (Map.Entry entry : properties.entrySet()) {

                String rdfType = entry.getKey().toString();
                String[] mediaTypes = entry.getValue().toString().split("\\s+");

                if (!StringUtils.isBlank(rdfType) && mediaTypes != null && mediaTypes.length > 0) {

                    for (int i = 0; i < mediaTypes.length; i++) {

                        if (!StringUtils.isBlank(mediaTypes[i])) {

                            mappings.put(mediaTypes[i].trim(), rdfType.trim());
                        }
                    }
                }
            }
        }
    }

    /**
     * Looks up the rdf:type from mimeToRdfMap.
     *
     * @param mimeType the media type to look up
     * @return String containing the rdf:type
     */
    public static String getDcmiTypeFor(String mimeType) {

        if (mappings == null) {

            synchronized (initializationLock) {
                if (mappings == null) {
                    initialize();
                }
            }
        }

        // Try to find exact match first.
        // If no exact match found, loop through all entries
        // and do starts-with matching for each.

        String result = mappings.get(mimeType);
        if (StringUtils.isBlank(result)) {

            for (Map.Entry<String, String> entry : mappings.entrySet()) {

                if (mimeType.startsWith(entry.getKey())) {
                    result = entry.getValue();
                }
            }
        }

        return StringUtils.isBlank(result) ? null : result.trim();
    }
}
