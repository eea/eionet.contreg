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

package eionet.cr.web.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Jaanus Heinlaid
 */
public class PostHarvestScriptParser {

    /** */
    public static final int TEST_RESULTS_LIMIT = 500;

    /**
     *
     * @param script
     * @param graphUri
     * @return
     */
    public static String parseForExecution(String script, String graphUri) {

        String result = script;
        if (containsToken(result, "DELETE")) {

            if (containsToken(result, "INSERT")) {
                result =
                    substringBeforeToken(result, "DELETE") + "MODIFY <" + graphUri + "> DELETE"
                    + substringAfterToken(result, "DELETE");
            } else {
                result =
                    substringBeforeToken(result, "DELETE") + "DELETE FROM <" + graphUri + ">"
                    + substringAfterToken(result, "DELETE");
            }
        } else if (containsToken(result, "INSERT")) {
            result =
                substringBeforeToken(result, "INSERT") + "INSERT INTO <" + graphUri + ">"
                + substringAfterToken(result, "INSERT");
        }

        return result.trim();
    }

    /**
     *
     * @param script
     * @param graphUri
     * @return
     */
    public static String parseForTest(String script, String graphUri) {

        String result = script;
        if (containsToken(result, "DELETE")) {

            if (containsToken(result, "INSERT")) {
                result = substringBeforeToken(result, "DELETE") + "SELECT" + substringAfterToken(result, "INSERT");
            } else {
                result = substringBeforeToken(result, "DELETE") + "SELECT" + substringAfterToken(result, "DELETE");
            }
        } else if (containsToken(result, "INSERT")) {
            result = substringBeforeToken(result, "INSERT") + "SELECT" + substringAfterToken(result, "INSERT");
        }

        if (containsToken(result, "WHERE")) {
            result =
                substringBeforeToken(result, "WHERE") + "FROM <" + graphUri + "> WHERE" + substringAfterToken(result, "WHERE");
        } else {
            result = result + " FROM <" + graphUri + "> WHERE {?s ?p ?o}";
        }

        if (containsToken(result, "LIMIT")) {
            result = substringBeforeToken(result, "LIMIT") + "LIMIT " + TEST_RESULTS_LIMIT;
        }
        else{
            result = result + " LIMIT " + TEST_RESULTS_LIMIT;
        }

        if (containsToken(result, "SELECT")) {

            String afterSelect = substringAfterToken(result, "SELECT");
            afterSelect = StringUtils.replaceOnce(afterSelect, "{", "");
            afterSelect = StringUtils.replaceOnce(afterSelect, "}", "");
            result = substringBeforeToken(result, "SELECT") + "SELECT" + afterSelect;
        }

        return result.trim();
    }

    /**
     *
     * @param str
     * @param token
     * @return
     */
    private static boolean containsToken(String str, String token) {

        if (str == null || str.trim().length() == 0 || token == null || token.trim().length() == 0) {
            return false;
        }

        StringTokenizer st = new StringTokenizer(str, " \t\n\r\f", true);
        ArrayList<String> upperCaseTokens = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            String nextToken = st.nextToken();
            upperCaseTokens.add(nextToken.toUpperCase());
        }

        return upperCaseTokens.contains(token.toUpperCase());
    }

    /**
     *
     * @param str
     * @param token
     * @return
     */
    private static String substringBeforeToken(String str, String token) {

        if (str == null || str.trim().length() == 0 || token == null || token.trim().length() == 0) {
            return str;
        }

        StringTokenizer st = new StringTokenizer(str, " \t\n\r\f", true);
        ArrayList<String> originalTokens = new ArrayList<String>();
        ArrayList<String> upperCaseTokens = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            String nextToken = st.nextToken();
            originalTokens.add(nextToken);
            upperCaseTokens.add(nextToken.toUpperCase());
        }

        int tokenIndex = upperCaseTokens.indexOf(token.toUpperCase());
        if (tokenIndex >= 0) {
            return tokensToString(originalTokens.subList(0, tokenIndex));
        } else {
            return str;
        }
    }

    /**
     *
     * @param str
     * @param token
     * @return
     */
    private static String substringAfterToken(String str, String token) {

        if (str == null || str.trim().length() == 0 || token == null || token.trim().length() == 0) {
            return str;
        }

        StringTokenizer st = new StringTokenizer(str, " \t\n\r\f", true);
        ArrayList<String> originalTokens = new ArrayList<String>();
        ArrayList<String> upperCaseTokens = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            String nextToken = st.nextToken();
            originalTokens.add(nextToken);
            upperCaseTokens.add(nextToken.toUpperCase());
        }

        int tokenIndex = upperCaseTokens.indexOf(token.toUpperCase());
        if (tokenIndex >= 0) {
            String afterToken = "";
            int tokensSize = originalTokens.size();
            if (tokenIndex < tokensSize - 1) {
                afterToken = tokensToString(originalTokens.subList(tokenIndex + 1, tokensSize));
            }

            return afterToken;
        } else {
            return str;
        }
    }

    /**
     *
     * @param tokens
     * @return
     */
    private static String tokensToString(Collection<String> tokens) {

        StringBuilder result = new StringBuilder();
        if (tokens != null) {
            for (String token : tokens) {
                result.append(token);
            }
        }
        return result.toString();
    }
}
