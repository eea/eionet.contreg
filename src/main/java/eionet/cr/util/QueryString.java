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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletRequest;

/**
 * Class that manages query parameters as a set. Extends HashMap. A parameter can have more than one value.
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public final class QueryString extends HashMap<String, Set<String>> {

    /**
     *
     */
    private QueryString() {
        super();
    }

    /**
     * Build the hashmap content from the parameters in the request.
     *
     * @param request HTTP Request
     * @param excludeParameters
     */
    private QueryString(ServletRequest request) {

        super();

        Enumeration e = request.getParameterNames();
        while (e != null && e.hasMoreElements()) {
            String parName = e.nextElement().toString();
            String[] parValues = request.getParameterValues(parName);
            for (int i = 0; i < parValues.length; i++) {
                addParameterValue(parName, parValues[i]);
            }
        }
    }

    /**
     * Add parameter value.
     *
     * @param parName
     * @param parValue
     */
    public QueryString addParameterValue(String parName, String parValue) {

        Set<String> values = get(parName);
        if (values == null) {
            values = new HashSet<String>();
            put(parName, values);
        }
        values.add(parValue);

        return this;
    }

    /**
     * Removes a value from a parameter. If it was the last value, then remove the parameter.
     *
     * @param parName
     * @param parValue
     */
    public QueryString removeParameterValue(String parName, String parValue) {

        Set<String> values = get(parName);
        if (values != null) {
            values.remove(parValue);
            if (values.isEmpty()) {
                values = null;
                remove(parName);
            }
        }

        return this;
    }

    /**
     * Remove all values for a parameter.
     *
     * @param parName
     */
    public QueryString removeParameter(String parName) {
        remove(parName);
        return this;
    }

    /**
     * Remove all values for several parameters.
     *
     * @param parName
     */
    public QueryString removeParameters(String[] parNames) {

        if (parNames != null) {
            for (int i = 0; i < parNames.length; i++) {
                remove(parNames[i]);
            }
        }

        return this;
    }

    /**
     * Remove all values for a parameter and set a new value.
     *
     * @param parName
     */
    public QueryString setParameterValue(String parName, String parValue) {
        remove(parName);
        return addParameterValue(parName, parValue);
    }

    /**
     * Constructs the query string from the parameters in the hashmap. The string doesn't start with '?' and '&amp;'-signs are HTML
     * escaped.
     *
     * @return the query string
     */
    public String toURLFormat() {

        StringBuffer buf = new StringBuffer();
        for (Iterator<String> keys = keySet().iterator(); keys.hasNext();) {
            String parName = keys.next();
            Set<String> parValueSet = get(parName);
            for (Iterator<String> parValues = parValueSet.iterator(); parValues.hasNext();) {
                buf.append(buf.length() > 0 ? "&amp;" : "").append(parName).append("=").append(Util.urlEncode(parValues.next()));
            }
        }

        return buf.toString();
    }

    /**
     *
     * @param request
     * @return
     */
    public static QueryString createQueryString(ServletRequest request) {
        return new QueryString(request);
    }

    /**
     *
     * @return
     */
    public static QueryString createQueryString() {
        return new QueryString();
    }
}
