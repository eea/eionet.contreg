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
package eionet.cr.util.sql;

import java.text.ParseException;
import java.util.HashSet;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;

import eionet.cr.dao.util.SearchExpression;

/**
 * Full text queries.
 *
 * @author risto
 *
 */
public final class VirtuosoFullTextQuery {

    /** */
    private static final int MIN_WORD_LENGTH = 3;

    /** */
    protected static final String DEFAULT_BOOLEAN_OPERATOR = "and";

    /** */
    private static HashSet<String> booleanOperators;

    /** */
    static {
        booleanOperators = new HashSet<String>();
        booleanOperators.add("&");
        booleanOperators.add("|");
        booleanOperators.add("!");
        booleanOperators.add("AND");
        booleanOperators.add("OR");
        booleanOperators.add("NOT");
        booleanOperators.add("and");
        booleanOperators.add("or");
    }

    /** */
    private String query;
    private StringBuffer parsedQuery = new StringBuffer();;

    /** */
    private HashSet<String> phrases = new HashSet<String>();

    /**
     *
     * @param query
     * @throws ParseException
     */
    private VirtuosoFullTextQuery(String query) throws ParseException {
        this.query = query;
        parse();
    }

    /**
     *
     * @param query
     * @return VirtuosoFullTextQuery
     * @throws ParseException
     */
    public static VirtuosoFullTextQuery parse(String query) throws ParseException {

        return new VirtuosoFullTextQuery(query);
    }

    /**
     *
     * @param searchExpression
     * @return VirtuosoFullTextQuery
     * @throws ParseException
     */
    public static VirtuosoFullTextQuery parse(SearchExpression searchExpression) throws ParseException {

        return new VirtuosoFullTextQuery(searchExpression == null ? "" : searchExpression.toString());
    }

    /**
     * @throws ParseException
     *
     */
    private void parse() throws ParseException {

        if (query == null) {
            return;
        }

        query = query.trim();
        if (query.length() == 0) {
            return;
        }

        // parse phrases first

        int unclosedQuotes = -1;
        int len = query.length();
        for (int i = 0; i < len; i++) {
            if (query.charAt(i) == '"') {
                if (unclosedQuotes != -1) {
                    String phrase = query.substring(unclosedQuotes + 1, i);
                    if (phrase.trim().length() >= MIN_WORD_LENGTH) {
                        if (StringUtils.containsAny(phrase, " \t\r\n\f")) {
                            phrases.add(query.substring(unclosedQuotes + 1, i));
                        }
                    }
                    unclosedQuotes = -1;
                } else {
                    unclosedQuotes = i;
                }
            }
        }

        // remove phrases from expression
        if (phrases != null) {
            for (String phrase : phrases) {
                query = query.replaceAll("\"" + phrase + "\"", "");
            }
        }

        // there must be no unclosed quotes left
        if (unclosedQuotes != -1) {
            throw new ParseException("Unclosed quotes at index " + unclosedQuotes, unclosedQuotes);
        }

        String prevToken = null;
        StringTokenizer st = new StringTokenizer(query, " \t\r\n\f\"");
        // remove any redundant logical operands and too short words from the beginning of query
        query = removeOperands(st);
        st = new StringTokenizer(query, " \t\r\n\f\"");
        while (st.hasMoreTokens()) {

            String token = st.nextToken();

            if (token.equals("&"))
                token = "AND";
            if (token.equals("|"))
                token = "OR";

            // If this token is a VirtuosoSQL full-text query's boolean operator
            // then append it to the parsed query only if the previous token
            // was not null and wasn't already a boolean operator itself.
            //
            // However, if this token is NOT a a VirtuosoSQL full-text query's boolean operator,
            // then append it to the parsed query, but first make sure that the previous
            // token was a boolean operator. If it wasn't then append the default operator.

            boolean appendThisToken = false;

            if (isBooleanOperator(token)) {
                if (prevToken != null && !isBooleanOperator(prevToken)) {
                    appendThisToken = true;
                }
            } else {
                if (token.length() >= MIN_WORD_LENGTH) {

                    appendThisToken = true;
                    if (prevToken != null && !isBooleanOperator(prevToken)) {
                        parsedQuery.append(" ").append(DEFAULT_BOOLEAN_OPERATOR);
                    }
                }
            }

            if (appendThisToken) {

                if (parsedQuery.length() > 0) {
                    parsedQuery.append(" ");
                }

                if (isBooleanOperator(token))
                    parsedQuery.append(token);
                else
                    parsedQuery.append("'").append(token).append("'");

                prevToken = token;
            }
        }

        // add phrases to query
        if (phrases != null && phrases.size() > 0) {
            for (String phrase : phrases) {
                if (parsedQuery != null && parsedQuery.length() > 0)
                    parsedQuery.append(" and ");
                parsedQuery.append("'").append(phrase).append("'");
            }
        }
    }

    /**
     * Remove any redundant logical operands and too short words from the beginning of query.
     *
     * @param st
     * @return the cleaned query string
     */
    private String removeOperands(StringTokenizer st) {

        StringBuffer ret = new StringBuffer();
        boolean nonLogicalTokenFound = false;

        if (st != null) {
            while (st.hasMoreTokens()) {
                String token = st.nextToken();

                if (token.equals(" ") || isBooleanOperator(token)) {
                    if (nonLogicalTokenFound) {
                        ret.append(token).append(" ");
                    }
                } else if (!isBooleanOperator(token) && token.length() >= MIN_WORD_LENGTH) {
                    ret.append(token).append(" ");
                    nonLogicalTokenFound = true;
                }
            }
        }

        return ret.toString();
    }

    /**
     * Check if the string is a boolean operator.
     *
     * @param s
     * @return true if the string is in the list of boolean operators
     */
    private boolean isBooleanOperator(String s) {
        return booleanOperators.contains(s);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return new StringBuffer("parsedQuery=[").append(parsedQuery).append("], phrases=").append(phrases).toString();
    }

    public String getParsedQuery() {
        return parsedQuery.toString();
    }

    /**
     * @return the phrases
     */
    public HashSet<String> getPhrases() {
        return phrases;
    }
}
