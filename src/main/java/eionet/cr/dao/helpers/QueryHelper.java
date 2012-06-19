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

    private static final String PREFIX = "PREFIX";
    private static final String SELECT = "SELECT";
    private static final String WHERE = "WHERE";
    private static final String FROM = "FROM";

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

        // On some cases the binding value alrady is surrounded by "'", so this removes the redundant apostrophes
        query = StringUtils.replace(query, "''", "'");

        return formatSparqlQuery(query);
    }

    private static String formatSparqlQuery(String query) {
        query = StringUtils.replace(query, PREFIX.toLowerCase(), PREFIX);
        query = StringUtils.replace(query, SELECT.toLowerCase(), SELECT);
        query = StringUtils.replace(query, WHERE.toLowerCase(), WHERE);
        query = StringUtils.replace(query, FROM.toLowerCase(), FROM);

        query = StringUtils.replace(query, " " + PREFIX, "\n" + PREFIX);
        query = StringUtils.replace(query, " " + SELECT, "\n" + SELECT);
        query = StringUtils.replace(query, " " + WHERE, "\n" + WHERE);
        query = StringUtils.replace(query, " " + FROM, "\n" + FROM);

        query = StringUtils.replace(query, "{", "{\n ");
        query = StringUtils.replace(query, "}", "\n}");
        query = StringUtils.replace(query, " .", ".\n");

        return query.trim();
    }

}
