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
package eionet.cr.web.util;

import org.apache.commons.lang.StringUtils;

import eionet.cr.dto.ObjectDTO;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public final class FactsheetObjectId {

    /** */
    private static final String SEPARATOR = "|";

    /**
     * Hide utility class constructor.
     */
    private FactsheetObjectId() {
        // Just an empty private constructor to avoid instantiating this utility class.
    }

    /**
     *
     * @param object
     * @return
     */
    public static String format(ObjectDTO object) {

        if (object == null) {
            throw new IllegalArgumentException("Supplied object must not be null");
        }

        return new StringBuilder().append(object.getHash()).append(SEPARATOR).append(object.getSourceHash()).append(SEPARATOR)
        .append(object.getDerivSourceHash()).append(SEPARATOR).append(object.getSourceObjectHash()).toString();
    }

    /**
     *
     * @param s
     * @return
     */
    public static ObjectDTO parse(String s) {

        if (StringUtils.isBlank(s)) {
            throw new IllegalArgumentException("Supplied string must not be blank");
        }

        String[] parts = StringUtils.split(s, SEPARATOR);
        if (parts.length != 4) {
            throw new IllegalArgumentException("Supplied string has wrong format");
        }

        long[] hashes = new long[4];
        for (int i = 0; i < parts.length; i++) {
            try {
                hashes[i] = Long.parseLong(parts[i]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Supplied string has wrong format");
            }
        }

        return ObjectDTO.create(hashes[0], hashes[1], hashes[2], hashes[3]);
    }
}
