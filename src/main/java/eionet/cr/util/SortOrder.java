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
package eionet.cr.util;

import org.apache.commons.lang.StringUtils;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public enum SortOrder {

    ASCENDING("asc"), DESCENDING("desc");

    /** */
    private String s;

    /**
     *
     * @param s
     */
    SortOrder(String s) {
        this.s = s;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Enum#toString()
     */
    public String toString() {
        return s;
    }

    /**
     *
     * @return
     */
    public String toSQL() {
        return s;
    }

    /**
     *
     * @return
     */
    public SortOrder toOpposite() {
        if (this.equals(ASCENDING))
            return DESCENDING;
        else
            return ASCENDING;
    }

    /**
     *
     * @param order
     * @return
     */
    public static String oppositeSortOrder(String order) {
        if (StringUtils.isBlank(order))
            return ASCENDING.toString();
        else
            return parse(order).toOpposite().toString();
    }

    /**
     *
     * @param s
     * @return
     */
    public static SortOrder parse(String s) {

        if (s == null)
            return null;
        else if (s.equals(ASCENDING.toString()))
            return ASCENDING;
        else if (s.equals(DESCENDING.toString()))
            return DESCENDING;
        else
            throw new IllegalArgumentException("Unknown sort order: " + s);
    }
}
