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

/**
 *
 * @author Jaanus Heinlaid
 */
public class PostHarvestScriptParser {

    /**
     *
     * @param script
     * @return
     */
    public static String parseForTest(String script, String graphUri) {

        String trimmedScript = script.trim();
        StringTokenizer st = new StringTokenizer(trimmedScript, " \t\n\r\f", true);
        ArrayList<String> originalTokens = new ArrayList<String>();
        ArrayList<String> upperCaseTokens = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            originalTokens.add(token);
            upperCaseTokens.add(token.toUpperCase());
        }

        String result = trimmedScript;
        int insertIndex = -1;
        int tokensSize = originalTokens.size();
        int deleteIndex = upperCaseTokens.indexOf("DELETE");

        if (deleteIndex >= 0) {

            String beforeDelete = tokensToString(originalTokens.subList(0, deleteIndex));
            String afterDelete = "";
            if (deleteIndex != tokensSize - 1) {
                afterDelete = tokensToString(originalTokens.subList(deleteIndex + 1, tokensSize));
            }

            insertIndex = upperCaseTokens.indexOf("INSERT");
            if (insertIndex == -1) {
                result = beforeDelete + " SELECT " + afterDelete;
            } else if (insertIndex > deleteIndex) {
                String afterInsert = "";
                if (insertIndex != tokensSize - 1) {
                    afterInsert = tokensToString(originalTokens.subList(insertIndex + 1, tokensSize));
                }
                result = beforeDelete + " SELECT " + afterInsert;
            }
        } else {
            insertIndex = upperCaseTokens.indexOf("INSERT");
            if (insertIndex >= 0) {

                String beforeInsert = tokensToString(originalTokens.subList(0, insertIndex));
                String afterInsert = "";
                if (insertIndex != tokensSize - 1) {
                    afterInsert = tokensToString(originalTokens.subList(insertIndex + 1, tokensSize));
                }
                result = beforeInsert + " SELECT " + afterInsert;
            }
        }

        int whereIndex = upperCaseTokens.indexOf("WHERE");
        if (whereIndex >= 0) {
            result = replaceToken(result, "WHERE", "FROM <" + graphUri + "> WHERE");
        } else {
            result = result + " FROM <" + graphUri + "> WHERE {?s ?p ?o}";
        }

        return result.trim();
    }

    /**
     *
     * @param script
     * @param graphUri
     * @return
     */
    public static String parseForExecution(String script, String graphUri) {

        String trimmedScript = script.trim();
        StringTokenizer st = new StringTokenizer(trimmedScript, " \t\n\r\f", true);
        ArrayList<String> originalTokens = new ArrayList<String>();
        ArrayList<String> upperCaseTokens = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            originalTokens.add(token);
            upperCaseTokens.add(token.toUpperCase());
        }

        String result = trimmedScript;
        int insertIndex = -1;
        int tokensSize = originalTokens.size();
        int deleteIndex = upperCaseTokens.indexOf("DELETE");

        if (deleteIndex >= 0) {

            String beforeDelete = tokensToString(originalTokens.subList(0, deleteIndex));
            String afterDelete = "";
            if (deleteIndex != tokensSize - 1) {
                afterDelete = tokensToString(originalTokens.subList(deleteIndex + 1, tokensSize));
            }

            insertIndex = upperCaseTokens.indexOf("INSERT");
            if (insertIndex == -1) {
                result = beforeDelete + " DELETE FROM <" + graphUri + "> " + afterDelete;
            } else if (insertIndex > deleteIndex) {
                result = beforeDelete + " MODIFY <" + graphUri + "> DELETE " + afterDelete;
            }
        } else {
            insertIndex = upperCaseTokens.indexOf("INSERT");
            if (insertIndex >= 0) {

                String beforeInsert = tokensToString(originalTokens.subList(0, insertIndex));
                String afterInsert = "";
                if (insertIndex != tokensSize - 1) {
                    afterInsert = tokensToString(originalTokens.subList(insertIndex + 1, tokensSize));
                }

                result = beforeInsert + " INSERT INTO <" + graphUri + "> " + afterInsert;
            }
        }

        return result.trim();
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

    /**
     *
     * @param str
     * @param token
     * @param replacement
     * @return
     */
    private static String replaceToken(String str, String token, String replacement) {

        if (str == null || str.trim().length() == 0) {
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

        int tokensSize = originalTokens.size();
        int tokenIndex = upperCaseTokens.indexOf(token.toUpperCase());
        if (tokenIndex >= 0) {

            String beforeToken = tokensToString(originalTokens.subList(0, tokenIndex));
            String afterToken = "";
            if (tokenIndex < tokensSize - 1) {
                afterToken = tokensToString(originalTokens.subList(tokenIndex + 1, tokensSize));
            }

            return beforeToken + replacement + afterToken;
        } else {
            return str;
        }
    }

    public static void main(String[] args) {

        String[] ss = new String[3];
        ss[0] = "delete {1} where {2}";
        ss[1] = "insert {1} where {2}";
        ss[2] = "delete {1} insert {2} where {3}";
        String graph = "http://www.neti.ee";

        for (int i = 0; i < ss.length; i++) {
            System.out.println(parseForExecution(ss[i], graph));
            System.out.println(parseForTest(ss[i], graph));
            System.out.println("---------------------------------");
        }
    }
}
