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

package eionet.cr.web.action.admin.postHarvest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Jaanus Heinlaid
 */
public final class PostHarvestScriptParser {

    /** */
    public static final int TEST_RESULTS_LIMIT = 500;

    /** */
    public static final String HARVESTED_SOURCE_VARIABLE = "harvestedSource";
    public static final String ASSOCIATED_TYPE_VARIABLE = "thisType";

    /**
     * Hide utility class constructor.
     */
    private PostHarvestScriptParser() {
        // Just an empty private constructor to avoid instantiating this utility class.
    }

    /**
     *
     * @param script
     * @param harvestedSource
     * @param associatedType
     * @return
     */
    public static String parseForExecution(String script, String harvestedSource, String associatedType) {

        if (StringUtils.isBlank(script)) {
            return script;
        }

        if (!StringUtils.isBlank(harvestedSource)) {
            script = StringUtils.replace(script, "?" + HARVESTED_SOURCE_VARIABLE, "<" + harvestedSource + ">");
        }

        if (!StringUtils.isBlank(associatedType)) {
            script = StringUtils.replace(script, "?" + ASSOCIATED_TYPE_VARIABLE, "<" + associatedType + ">");
        }

        return script;
    }

    /**
     *
     * @param script
     * @param graphUri
     * @return
     */
    public static String deriveSelect(String script, String graphUri) {

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
            if (!containsToken(result, "FROM")) {
                result =
                        substringBeforeToken(result, "WHERE") + "FROM <" + graphUri + "> WHERE"
                                + substringAfterToken(result, "WHERE");
            }
        } else {
            result = result + " FROM <" + graphUri + "> WHERE {?s ?p ?o}";
        }

        if (containsToken(result, "LIMIT")) {
            result = substringBeforeToken(result, "LIMIT") + "LIMIT " + TEST_RESULTS_LIMIT;
        } else {
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
     * @param script
     * @param harvestedSource
     * @param associatedType
     * @return
     * @throws ScriptParseException
     */
    public static String deriveConstruct(String script, String harvestedSource, String associatedType) throws ScriptParseException {

        if (script == null || StringUtils.isBlank(script)) {
            return script;
        }

        String result = script;
        if (containsToken(script, "MODIFY")) {
            result = deriveConstructFromModify(script);
        } else if (containsToken(script, "DELETE")) {
            result = deriveConstructFromDelete(script);
        } else if (containsToken(script, "INSERT")) {
            result = deriveConstructFromInsert(script);
        }

        if (result != null && !StringUtils.isBlank(harvestedSource)) {
            result = replaceToken(result, "?" + HARVESTED_SOURCE_VARIABLE, "<" + harvestedSource + ">");
        }

        if (result != null && !StringUtils.isBlank(associatedType)) {
            result = replaceToken(result, "?" + ASSOCIATED_TYPE_VARIABLE, "<" + associatedType + ">");
        }

        return result == null ? null : result.trim();
    }

    /**
     *
     * @param script
     * @return
     * @throws ScriptParseException
     */
    private static String deriveConstructFromModify(String script) throws ScriptParseException {

        String datasetClause = null;
        String constructTemplate = null;
        String wherePattern = null;

        // Ensure this is a valid MODIFY statement.

        if (!containsToken(script, "DELETE")) {
            throw new ScriptParseException("Expecting MODIFY statement to contain DELETE!");
        } else if (!containsToken(substringAfterToken(script, "DELETE"), "INSERT")) {
            throw new ScriptParseException("Expecting MODIFY statement to contain DELETE and INSERT!");
        }

        // Prepare some helpful variables.
        String afterInsert = substringAfterToken(script, "INSERT");
        boolean datasetSpecified = containsToken(afterInsert, "FROM");
        boolean wherePatternSpecified = containsToken(afterInsert, "WHERE");

        // Extract the CONSTRUCT template, and dataset part if specified.
        if (datasetSpecified) {

            constructTemplate = substringBeforeToken(afterInsert, "FROM");
            if (wherePatternSpecified) {
                datasetClause = substringBetweenTokens(afterInsert, "FROM", "WHERE");
            } else {
                datasetClause = substringAfterToken(afterInsert, "FROM");
            }
        } else if (wherePatternSpecified) {
            constructTemplate = substringBeforeToken(afterInsert, "WHERE");
        } else {
            constructTemplate = afterInsert;
        }

        // Ensure CONSTRUCT template is not blank and starts-ends with curly braces.
        if (constructTemplate == null || !startsEndsWith(constructTemplate.trim(), "{", "}")) {
            throw new ScriptParseException("Could not detect CONSTRUCT template!");
        }

        // Extract WHERE pattern if present.
        if (wherePatternSpecified) {
            wherePattern = substringAfterToken(afterInsert, "WHERE");
            if (StringUtils.isBlank(wherePattern) || !wherePattern.trim().startsWith("{")) {
                throw new ScriptParseException("Could not detect WHERE pattern!");
            }
        }

        // Build the resulting CONSTRUCT query.

        String result = substringBeforeToken(script, "MODIFY");
        result += "CONSTRUCT";
        result += ensureWhitespaceStartEnd(constructTemplate);

        if (!StringUtils.isBlank(datasetClause)) {
            result += "FROM" + ensureWhitespaceStart(datasetClause);
        }

        result = ensureWhitespaceEnd(result);
        if (!StringUtils.isBlank(wherePattern)) {
            result += "WHERE" + ensureWhitespaceStart(wherePattern);
        } else {
            result += "WHERE {?s ?p ?o}";
        }

        result = result.trim();
        result += " LIMIT " + TEST_RESULTS_LIMIT;

        return result;
    }

    /**
     *
     * @param script
     * @return
     * @throws ScriptParseException
     */
    private static String deriveConstructFromInsert(String script) throws ScriptParseException {

        String constructTemplate = null;
        String datasetClause = null;
        String wherePattern = null;
        String graphAndTemplate = null;

        boolean datasetSpecified = containsToken(script, "FROM");
        boolean wherePatternSpecified = containsToken(script, "WHERE");

        // Extract the graph-and-template section, and the dataset section if present.

        if (datasetSpecified) {
            graphAndTemplate = substringBetweenTokens(script, "INSERT", "FROM");
            if (wherePatternSpecified) {
                datasetClause = substringBetweenTokens(script, "FROM", "WHERE");
            } else {
                datasetClause = substringAfterToken(script, "FROM");
            }
        } else if (wherePatternSpecified) {
            graphAndTemplate = substringBetweenTokens(script, "INSERT", "WHERE");
        } else {
            graphAndTemplate = substringAfterToken(script, "INSERT");
        }

        // Extract construct template, make sure it's not blank and starts-ends with curly braces.

        int i = graphAndTemplate.indexOf("{");
        if (i != -1) {
            constructTemplate = graphAndTemplate.substring(i);
        }
        if (constructTemplate == null || !startsEndsWith(constructTemplate.trim(), "{", "}")) {
            throw new ScriptParseException("Could not detect CONSTRUCT template!");
        }

        // Extract WHERE pattern if present
        if (wherePatternSpecified) {
            wherePattern = substringAfterToken(script, "WHERE");
            if (StringUtils.isBlank(wherePattern) || !wherePattern.trim().startsWith("{")) {
                throw new ScriptParseException("Could not detect WHERE pattern!");
            }
        }

        String result = substringBeforeToken(script, "INSERT");
        result += "CONSTRUCT";
        result += ensureWhitespaceStartEnd(constructTemplate);

        if (!StringUtils.isBlank(datasetClause)) {
            result += "FROM" + ensureWhitespaceStart(datasetClause);
        }

        result = ensureWhitespaceEnd(result);
        if (!StringUtils.isBlank(wherePattern)) {
            result += "WHERE" + ensureWhitespaceStart(wherePattern);
        } else {
            result += "WHERE {?s ?p ?o}";
        }

        result = result.trim();
        result += " LIMIT " + TEST_RESULTS_LIMIT;

        return result;
    }

    /**
     *
     * @param script
     * @return
     * @throws ScriptParseException
     */
    private static String deriveConstructFromDelete(String script) throws ScriptParseException {

        String constructTemplate = null;
        String datasetClause = null;
        String wherePattern = null;

        boolean wherePatternSpecified = containsToken(script, "WHERE");
        // Extract the CONSTRUCT template, and the dataset section if present.

        String afterDelete = substringAfterToken(script, "DELETE");
        int i = afterDelete.indexOf("{");
        if (i != -1) {
            String afterGraph = afterDelete.substring(i);
            if (containsToken(afterGraph, "FROM")) {
                constructTemplate = substringBeforeToken(afterGraph, "FROM");
                if (wherePatternSpecified) {
                    datasetClause = substringBetweenTokens(afterGraph, "FROM", "WHERE");
                } else {
                    datasetClause = substringAfterToken(afterGraph, "FROM");
                }
            } else if (containsToken(afterGraph, "WHERE")) {
                constructTemplate = substringBeforeToken(afterGraph, "WHERE");
            } else {
                constructTemplate = afterGraph;
            }
        }

        // Ensure CONSTRUCT template is not blank and starts-ends with curly braces
        if (constructTemplate == null || !startsEndsWith(constructTemplate.trim(), "{", "}")) {
            throw new ScriptParseException("Could not detect CONSTRUCT template!");
        }

        // Extract WHERE pattern if present
        if (wherePatternSpecified) {
            wherePattern = substringAfterToken(script, "WHERE");
            if (StringUtils.isBlank(wherePattern) || !wherePattern.trim().startsWith("{")) {
                throw new ScriptParseException("Could not detect WHERE pattern!");
            }
        }

        // Build the resulting CONSTRUCT query.

        String result = substringBeforeToken(script, "DELETE");
        result += "CONSTRUCT";
        result += ensureWhitespaceStartEnd(constructTemplate);

        if (!StringUtils.isBlank(datasetClause)) {
            result += "FROM" + ensureWhitespaceStart(datasetClause);
        }

        result = ensureWhitespaceEnd(result);
        if (!StringUtils.isBlank(wherePattern)) {
            result += "WHERE" + ensureWhitespaceStart(wherePattern);
        } else {
            result += "WHERE {?s ?p ?o}";
        }

        result = result.trim();
        result += " LIMIT " + TEST_RESULTS_LIMIT;

        return result;
    }

    /**
     *
     * @param str
     * @param token
     * @return
     */
    public static boolean containsToken(String str, String token) {

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
     * Returns the substring that is between the given tokens (tokens themselves excluded). If neither tokens are present in the
     * string, the string itself is returned. If token1 is present in the string, but token2 is not, then the substring after token1
     * is returned. Similarly, if token2 is present in the string, but token1 is not, then the substring before token1 is returned.
     * If neither tokens are present in the string, the string is returned as it is.
     *
     * If the given string is blank (i.e. null or empty after trimmed), the given string is returned as it is. If token1 is not
     * blank, but token2 is, the method returns substring after token1. If token2 is not blank, but token1 is, the method returns
     * substring before token1. If both tokens are blank, the string is returned as it is.
     *
     * @param str The given string.
     * @param token1 token1
     * @param token2 token2
     * @return See method description above.
     */
    private static String substringBetweenTokens(String str, String token1, String token2) {

        if (StringUtils.isBlank(str) || (StringUtils.isBlank(token1) && StringUtils.isBlank(token2))) {
            return str;
        }

        if (!StringUtils.isBlank(token1) && StringUtils.isBlank(token2)) {
            return substringAfterToken(str, token1);
        } else if (StringUtils.isBlank(token1) && !StringUtils.isBlank(token2)) {
            return substringBeforeToken(str, token2);
        } else {
            String afterToken1 = substringAfterToken(str, token1);
            return substringBeforeToken(afterToken1, token2);
        }
    }

    /**
     *
     * @param str
     * @param tokenToReplace
     * @param replacement
     * @return
     */
    private static String replaceToken(String str, String tokenToReplace, String replacement) {

        if (str == null || str.trim().length() == 0 || tokenToReplace == null || tokenToReplace.trim().length() == 0) {
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

        StringBuilder buf = new StringBuilder();
        for (String token : originalTokens) {

            if (token.equalsIgnoreCase(tokenToReplace)) {
                buf.append(replacement);
            } else {
                buf.append(token);
            }
        }
        return buf.toString();
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
     * @param start
     * @param end
     * @return
     */
    private static boolean startsEndsWith(String str, String start, String end) {

        if (str == null) {
            return false;
        }

        return str.startsWith(start) && str.endsWith(end);
    }

    /**
     *
     * @param str
     * @return
     */
    private static String ensureWhitespaceStart(String str) {

        if (str == null) {
            return null;
        } else if (str.length() == 0 || !Character.isWhitespace(str.charAt(0))) {
            return " " + str;
        } else {
            return str;
        }
    }

    /**
     *
     * @param str
     * @return
     */
    private static String ensureWhitespaceEnd(String str) {

        if (str == null) {
            return null;
        } else if (str.length() == 0 || !Character.isWhitespace(str.charAt(str.length() - 1))) {
            return str + " ";
        } else {
            return str;
        }
    }

    /**
     *
     * @param str
     * @return
     */
    private static String ensureWhitespaceStartEnd(String str) {

        if (str == null) {
            return null;
        } else if (str.length() == 0) {
            return " ";
        } else {
            String result = str;
            if (!Character.isWhitespace(result.charAt(0))) {
                result = " " + result;
            }
            if (!Character.isWhitespace(result.charAt(result.length() - 1))) {
                result = result + " ";
            }
            return result;
        }
    }
}
