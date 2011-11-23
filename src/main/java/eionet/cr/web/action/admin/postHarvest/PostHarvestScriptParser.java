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
public class PostHarvestScriptParser {

    /** */
    public static final int TEST_RESULTS_LIMIT = 500;

    /** */
    public static final String HARVESTED_SOURCE_VARIABLE = "harvestedSource";
    public static final String ASSOCIATED_TYPE_VARIABLE = "thisType";

    /**
     *
     * @param script
     * @param harvestedSource
     * @param associatedType
     * @return
     */
    public static String parseForExecution(String script, String harvestedSource, String associatedType){

        if (StringUtils.isBlank(script)){
            return script;
        }

        if (!StringUtils.isBlank(harvestedSource)){
            script = StringUtils.replace(script, "?" + HARVESTED_SOURCE_VARIABLE, "<" + harvestedSource + ">");
        }

        if (!StringUtils.isBlank(associatedType)){
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
     * @param harvestedSource TODO
     * @return
     * @throws ScriptParseException
     */
    public static String deriveConstruct(String script, String harvestedSource) throws ScriptParseException {

        if (script == null || StringUtils.isBlank(script)) {
            return script;
        }

        String result = script;
        if (containsToken(script, "MODIFY")) {
            result = deriveConstructFromModify(script, harvestedSource);
        } else if (containsToken(script, "DELETE")) {
            result = deriveConstructFromDelete(script, harvestedSource);
        } else if (containsToken(script, "INSERT")) {
            result = deriveConstructFromInsert(script, harvestedSource);
        }

        return result==null ? null : result.trim();
    }


    /**
     * @param script
     * @param harvestedSource TODO
     * @return
     * @throws ScriptParseException
     */
    private static String deriveConstructFromModify(String script, String harvestedSource) throws ScriptParseException {

        String datasetClause = null;
        String deleteTemplate = null;
        String insertTemplate = null;
        String wherePattern = null;

        if (!containsToken(script, "DELETE")){
            throw new ScriptParseException("Expecting MODIFY statement to contain DELETE!");
        }
        else if (!containsToken(substringAfterToken(script, "DELETE"), "INSERT")){
            throw new ScriptParseException("Expecting MODIFY statement to contain DELETE and INSERT!");
        }

        datasetClause = substringBetweenTokens(script, "MODIFY", "DELETE");
        deleteTemplate = substringBetweenTokens(script, "DELETE", "INSERT");

        String afterInsert = substringAfterToken(script, "INSERT");
        if (containsToken(afterInsert, "WHERE")){
            insertTemplate = substringBetweenTokens(script, "INSERT", "WHERE");
            wherePattern = substringAfterToken(afterInsert, "WHERE");
            if (StringUtils.isBlank(wherePattern) || !wherePattern.trim().startsWith("{")){
                throw new ScriptParseException("Could not detect WHERE pattern!");
            }
        }
        else{
            insertTemplate = afterInsert;
        }

        String deleteTemplTrimd = deleteTemplate==null ? null : deleteTemplate.trim();
        if (StringUtils.isEmpty(deleteTemplTrimd) || !(deleteTemplTrimd.startsWith("{") && deleteTemplTrimd.endsWith("}"))){
            throw new ScriptParseException("Could not detect DELETE template!");
        }

        String insertTemplTrimd = insertTemplate==null ? null : insertTemplate.trim();
        if (StringUtils.isEmpty(insertTemplTrimd) || !(insertTemplTrimd.startsWith("{") && insertTemplTrimd.endsWith("}"))){
            throw new ScriptParseException("Could not detect INSERT template!");
        }

        String result = substringBeforeToken(script, "MODIFY");
        result += "CONSTRUCT";
        result += insertTemplate;
        if (!Character.isWhitespace(result.charAt(result.length()-1))){
            result += " ";
        }

        if (!StringUtils.isBlank(datasetClause)){
            String[] graphs = StringUtils.split(datasetClause);
            for (int i = 0; i < graphs.length; i++) {
                String graph = StringUtils.replace(graphs[i], "?" + HARVESTED_SOURCE_VARIABLE, "<" + harvestedSource + ">");
                result += "FROM " + graph + " ";
            }
        }

        if (!StringUtils.isBlank(wherePattern)){
            result += "WHERE" + wherePattern;
        }
        else{
            result += "WHERE {?s ?p ?o}";
        }
        result = result.trim();
        result += " LIMIT 500";

        return result;
    }

    /**
     *
     * @param script
     * @param harvestedSource TODO
     * @return
     * @throws ScriptParseException
     */
    private static String deriveConstructFromInsert(String script, String harvestedSource) throws ScriptParseException {

        String insertTemplate = null;
        String datasetClause = null;
        String wherePattern = null;

        boolean datasetSpecified = containsToken(script, "INTO");
        boolean wherePatternSpecified = containsToken(script, "WHERE");
        if (datasetSpecified) {

            String datasetAndTemplate = null;
            if (wherePatternSpecified) {
                datasetAndTemplate = substringBetweenTokens(script, "INTO", "WHERE");
                wherePattern = substringAfterToken(script, "WHERE");
                if (StringUtils.isBlank(wherePattern) || !wherePattern.trim().startsWith("{")){
                    throw new ScriptParseException("Could not detect WHERE pattern!");
                }
            } else {
                datasetAndTemplate = substringAfterToken(script, "INTO");
            }

            int i = datasetAndTemplate.indexOf("{");
            if (i != -1) {
                datasetClause = datasetAndTemplate.substring(0, i);
                insertTemplate = datasetAndTemplate.substring(i);
            }
        } else {
            if (wherePatternSpecified){
                insertTemplate =  substringBetweenTokens(script, "INSERT", "WHERE");
                wherePattern = substringAfterToken(script, "WHERE");
                if (StringUtils.isBlank(wherePattern) || !wherePattern.trim().startsWith("{")){
                    throw new ScriptParseException("Could not detect WHERE pattern!");
                }
            }
            else{
                insertTemplate = substringAfterToken(script, "INSERT");
            }
        }

        String insertTemplTrimd = insertTemplate==null ? null : insertTemplate.trim();
        if (StringUtils.isEmpty(insertTemplTrimd) || !(insertTemplTrimd.startsWith("{") && insertTemplTrimd.endsWith("}"))){
            throw new ScriptParseException("Could not detect INSERT template!");
        }

        String result = substringBeforeToken(script, "INSERT");
        result += "CONSTRUCT";
        if (!Character.isWhitespace(insertTemplate.charAt(0))){
            result += " ";
        }
        result += insertTemplate;
        if (!Character.isWhitespace(result.charAt(result.length()-1))){
            result += " ";
        }

        if (!StringUtils.isBlank(datasetClause)){
            datasetClause = StringUtils.replace(datasetClause, "?" + HARVESTED_SOURCE_VARIABLE, "<" + harvestedSource + ">");
            result += "FROM" + replaceToken(datasetClause, "INTO", "FROM");
        }

        if (!StringUtils.isBlank(wherePattern)){
            result += "WHERE" + wherePattern;
        }
        else{
            result += "WHERE {?s ?p ?o}";
        }
        result = result.trim();
        result += " LIMIT 500";

        return result;
    }

    /**
     *
     * @param script
     * @param harvestedSource TODO
     * @return
     * @throws ScriptParseException
     */
    private static String deriveConstructFromDelete(String script, String harvestedSource) throws ScriptParseException {

        String deleteTemplate = null;
        String datasetClause = null;
        String wherePattern = null;

        boolean datasetSpecified = containsToken(script, "FROM");
        boolean wherePatternSpecified = containsToken(script, "WHERE");
        if (datasetSpecified) {

            String datasetAndTemplate = null;
            if (wherePatternSpecified) {
                datasetAndTemplate = substringBetweenTokens(script, "FROM", "WHERE");
                wherePattern = substringAfterToken(script, "WHERE");
                if (StringUtils.isBlank(wherePattern) || !wherePattern.trim().startsWith("{")){
                    throw new ScriptParseException("Could not detect WHERE pattern!");
                }
            } else {
                datasetAndTemplate = substringAfterToken(script, "FROM");
            }

            int i = datasetAndTemplate.indexOf("{");
            if (i != -1) {
                datasetClause = datasetAndTemplate.substring(0, i);
                deleteTemplate = datasetAndTemplate.substring(i);
            }
        } else {
            if (wherePatternSpecified){
                deleteTemplate =  substringBetweenTokens(script, "DELETE", "WHERE");
                wherePattern = substringAfterToken(script, "WHERE");
                if (StringUtils.isBlank(wherePattern) || !wherePattern.trim().startsWith("{")){
                    throw new ScriptParseException("Could not detect WHERE pattern!");
                }
            }
            else{
                deleteTemplate = substringAfterToken(script, "DELETE");
            }
        }

        String deleteTemplTrimd = deleteTemplate==null ? null : deleteTemplate.trim();
        if (StringUtils.isEmpty(deleteTemplTrimd) || !(deleteTemplTrimd.startsWith("{") && deleteTemplTrimd.endsWith("}"))){
            throw new ScriptParseException("Could not detect DELETE template!");
        }

        String result = substringBeforeToken(script, "DELETE");
        result += "CONSTRUCT";
        if (!Character.isWhitespace(deleteTemplate.charAt(0))){
            result += " ";
        }
        result += deleteTemplate;
        if (!Character.isWhitespace(result.charAt(result.length()-1))){
            result += " ";
        }


        if (!StringUtils.isBlank(datasetClause)){
            datasetClause = StringUtils.replace(datasetClause, "?" + HARVESTED_SOURCE_VARIABLE, "<" + harvestedSource + ">");
            result += "FROM" + datasetClause;
        }

        if (!StringUtils.isBlank(wherePattern)){
            result += "WHERE" + wherePattern;
        }
        else{
            result += "WHERE {?s ?p ?o}";
        }
        result = result.trim();
        result += " LIMIT 500";

        return result;
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
     * @param str
     * @param token1
     * @param token2
     * @return
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
    private static String replaceToken(String str, String tokenToReplace, String replacement){

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
        for (String token : originalTokens){

            if (token.equalsIgnoreCase(tokenToReplace)){
                buf.append(replacement);
            }
            else{
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
     * @param args
     */
    public static void main(String[] args) {

        String s = "    jaanus risto enriko ander marek juhan";
        System.out.println("_" + s.replaceAll("^\\s+", "") + "_");
        String ss = "kk jaanus risto jaanus ander marek jaanus ...";
        System.out.println("_" + replaceToken(ss, "jaaNus", "LIINA") + "_");

        //        String s = "PREFIX dc: <http://uuu.ee> insert into <http://kk.ee> {?s ?p ?o}";
        //        System.out.println("_" + substringBetweenTokens(s, "into", "where") + "_");
        //        System.out.println("_" + substringAfterToken(s, "where") + "_");
    }

}