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
package eionet.cr.dao.util;

import eionet.cr.util.URIUtil;
import eionet.cr.util.URLUtil;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class SearchExpression {

    /** */
    private String expression;

    /** */
    private boolean isUri = false;
    private boolean isHash = false;

    /**
     *
     * @param s
     */
    public SearchExpression(String s) {

        expression = s == null ? "" : s.trim();

        try {
            Long.parseLong(expression);
            isHash = true;
        } catch (NumberFormatException nfe) {
            // Ignore deliberately.
        }

        // Escape spaces
        String escapedExpression = URLUtil.escapeIRI(expression);

        isUri = URIUtil.isSchemedURI(escapedExpression);
        if (isUri) {
            expression = escapedExpression;
        }
    }

    /**
     *
     * @return boolean
     */
    public boolean isEmpty() {
        return expression.length() == 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return expression;
    }

    /**
     * @return the isUri
     */
    public boolean isUri() {
        return isUri;
    }

    /**
     * @return the isHash
     */
    public boolean isHash() {
        return isHash;
    }
}
