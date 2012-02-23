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
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Juhan Voolaid
 */

package eionet.cr.dao.helpers;

import org.apache.commons.lang.StringUtils;

import eionet.cr.util.Bindings;
import eionet.cr.util.URIUtil;

/**
 * Helper utility for getting formated query string.
 *
 * @author Juhan Voolaid
 */
public class QueryHelper {

    public static String getFormatedQuery(String query, Bindings bindings) {

        if (bindings != null) {

            if (!bindings.getBindings().isEmpty()) {
                for (String key : bindings.getBindings().keySet()) {
                    String value = bindings.getBindings().get(key).toString();
                    if (URIUtil.isSchemedURI(value)) {
                        value = "<" + value + ">";
                    } else {
                        value = "'" + value + "'";
                    }
                    query = StringUtils.replace(query, "?" + key, key);
                    query = StringUtils.replace(query, key, value);
                }
            }
        }

        return query;
    }
}
