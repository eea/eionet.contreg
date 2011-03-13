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
* Jaanus Heinlaid, Risto Alt*/
package eionet.cr.util.sql;

import java.text.ParseException;
import java.util.HashSet;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;

import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.dao.helpers.FreeTextSearchHelper.FilterType;
import eionet.cr.dao.util.SearchExpression;

/**
 *
 * @author risto
 *
 */
public class VirtuosoFullTextQuery {

    /** */
    private static final int MIN_WORD_LENGTH = 3;

    /** */
    protected static final String DEFAULT_BOOLEAN_OPERATOR = "&";

    /** */
    private static HashSet<String> booleanOperators;

    /** */
    static{
        booleanOperators = new HashSet<String>();
        booleanOperators.add("&");
        booleanOperators.add("|");
        booleanOperators.add("!");
        booleanOperators.add("AND");
        booleanOperators.add("OR");
        booleanOperators.add("NOT");
    }

    /** */
    private String query;
    private StringBuffer parsedQuery = new StringBuffer();
    private FilterType filterType;
    private String type;

    /** */
    private HashSet<String> phrases = new HashSet<String>();

    /**
     *
     * @param query
     * @throws ParseException
     */
    private VirtuosoFullTextQuery(String query, FilterType filterType) throws ParseException{
        this.query = query;
        this.filterType = filterType;
        parse();
    }
    
    /**
    * @param filterType
    * @param query
    * @return VirtuosoFullTextQuery
    * @throws ParseException
    */
    public static VirtuosoFullTextQuery parse(String query, FilterType filterType) throws ParseException{

        return new VirtuosoFullTextQuery(query, filterType);
    }

    /**
     * @param filterType
     * @param searchExpression
     * @return VirtuosoFullTextQuery
     * @throws ParseException
     */
    public static VirtuosoFullTextQuery parse(SearchExpression searchExpression, FilterType filterType) throws ParseException{

        return new VirtuosoFullTextQuery(searchExpression==null ? "" : searchExpression.toString(), filterType);
    }

    /**
     * @throws ParseException
     *
     */
    private void parse() throws ParseException{

        if (query==null){
            return;
        }

        query = query.trim();
        if (query.length()==0){
            return;
        }

        // parse phrases first

        int unclosedQuotes = -1;
        int len = query.length();
        for (int i=0; i<len; i++){
            if (query.charAt(i)=='"'){
                if (unclosedQuotes!=-1){
                    String phrase = query.substring(unclosedQuotes+1, i);
                    if (phrase.trim().length()>=MIN_WORD_LENGTH){
                        if (StringUtils.containsAny(phrase, " \t\r\n\f")){
                            phrases.add(query.substring(unclosedQuotes+1, i));
                        }
                    }
                    unclosedQuotes = -1;
                }
                else{
                    unclosedQuotes = i;
                }
            }
        }
        
        //remove phrases from expression
        if(phrases != null){
            for(String phrase : phrases){
                query = query.replaceAll("\""+phrase+"\"", "");
            }
        }

        // there must be no unclosed quotes left
        if (unclosedQuotes!=-1){
            throw new ParseException("Unclosed quotes at index " + unclosedQuotes, unclosedQuotes);
        }
        
        // parse filters
        addFilterParams(); 
        
        boolean isFirst = true;
        String prevToken = null;
        String prevBooleanOp = null;
        StringTokenizer st = new StringTokenizer(query, " \t\r\n\f\"");
        //remove any redundant logical operands and too short words from the beginning of query
        query = removeOperands(st);

        st = new StringTokenizer(query, " \t\r\n\f\"");
        while (st.hasMoreTokens()){

            String token = st.nextToken();
        
            // If this token is a PostgreSQL full-text query's boolean operator
            // then append it to the parsed query only if the previous token
            // was not null and wasn't already a boolean operator itself.
            //
            // However, if this token is NOT a a PostgreSQL full-text query's boolean operator,
            // then append it to the parsed query, but first make sure that the previous
            // token was a boolean operator. If it wasn't then append the default operator.


            if (isBooleanOperator(token)){
                //if AND and OR operands are mixed in query then add additional braces
                if(prevBooleanOp != null && (((token.equals("|") || token.equals("OR")) && (prevBooleanOp.equals("&") || prevBooleanOp.equals("AND"))) || 
                                           ((token.equals("&") || token.equals("AND")) && (prevBooleanOp.equals("|") || prevBooleanOp.equals("OR"))))){
                    if(parsedQuery != null && parsedQuery.length() > 0){
                        parsedQuery.insert(0, "{");
                        parsedQuery.append("}");
                    }
                    isFirst = false;
                }
                if (prevToken!=null && !isBooleanOperator(prevToken)){
                    if(token.equals("&") || token.equals("AND")){
                        if(isFirst){
                            parsedQuery = new StringBuffer();
                            parsedQuery.append("{?s ?p ?o .").append(type).append(" FILTER regex(?o, \"").append(prevToken).append("\", \"i\")}");
                            isFirst = false;
                        }
                    } else if(token.equals("|") || token.equals("OR")){
                        parsedQuery.append(" UNION ");
                    }
                }
                prevBooleanOp = token;
            }
            else{
                if (prevToken!=null && !isBooleanOperator(prevToken) || (isBooleanOperator(prevToken) && (prevToken.equals("&") || prevToken.equals("AND")))){
                    if(isFirst){
                        parsedQuery = new StringBuffer();
                        parsedQuery.append("{?s ?p ?o .").append(type).append(" FILTER regex(?o, \"").append(prevToken).append("\", \"i\")}");
                        isFirst = false;
                    }
                    parsedQuery.append("{?s ?p ?o .").append(type).append(" FILTER regex(?o, \"").append(token).append("\", \"i\")}");
                } else {
                    parsedQuery.append("{?s ?p ?o .").append(type).append(" FILTER bif:contains(?o, \"'").append(token).append("'\")}");
                }
            }

            prevToken = token;
        }
        
        //append phrases
        if(phrases != null && phrases.size() > 0){
            if(parsedQuery != null && parsedQuery.length() > 0){
                parsedQuery.insert(0, "{");
                parsedQuery.append("}");
            }
            
            for (String phrase : phrases){
                parsedQuery.append("{?s ?p ?o .").append(type).append(" FILTER regex(?o, \"").append(phrase).append("\", \"i\")}");
            }
        }
    }
    
    //remove any redundant logical operands and too short words from the beginning of query
    private String removeOperands(StringTokenizer st) {
        
        StringBuffer ret = new StringBuffer();
        boolean nonLogicalTokenFound = false;
        
        if(st != null){
            while (st.hasMoreTokens()){
                String token = st.nextToken();
                if(token.equals(" ") || isBooleanOperator(token)){
                    if(nonLogicalTokenFound){
                        ret.append(token).append(" ");
                    }
                } else if(!isBooleanOperator(token) && token.length() >= MIN_WORD_LENGTH){
                    ret.append(token).append(" ");
                    nonLogicalTokenFound = true;
                }
            }
        }
        
        return ret.toString();
    }
    
    private void addFilterParams(){

        StringBuffer buf = new StringBuffer();
        buf.append(" ");

        if (filterType != FilterType.ANY_OBJECT){
            
            buf.append("?s <").append(Predicates.RDF_TYPE).append("> ");
            if (filterType == FilterType.ANY_FILE){
                buf.append("<").append(Subjects.CR_FILE).append(">");
            } else if (filterType == FilterType.DATASETS){
                buf.append("<").append(Predicates.DC_MITYPE_DATASET).append(">");
            } else if (filterType == FilterType.IMAGES){
                buf.append("<").append(Predicates.DC_MITYPE_IMAGE).append(">");
            } else if (filterType == FilterType.TEXTS){
                buf.append("<").append(Predicates.DC_MITYPE_TEXT).append(">");
            }

            buf.append(" .");
        }
        type = buf.toString();
    }

    /**
     *
     * @param s
     * @return
     */
    private boolean isBooleanOperator(String s){
        return booleanOperators.contains(s);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString(){
        return new StringBuffer("parsedQuery=[").
        append(parsedQuery).append("], phrases=").append(phrases).toString();
    }

    public String getParsedQuery(){
        return parsedQuery.toString();
    }

    /**
     * @return the phrases
     */
    public HashSet<String> getPhrases() {
        return phrases;
    }

    /**
     *
     * @param args
     * @throws ParseException
     */
    public static void main(String[] args) throws ParseException{

		String s = "mina \"olen robert\" ja sina oled (paha | kuri)";
        //String s = "air | sina & mina";

        VirtuosoFullTextQuery query = VirtuosoFullTextQuery.parse(s, FilterType.ANY_OBJECT);

        System.out.println(">" + query.toString() + "<");
        System.out.println(query.getPhrases());

//		System.out.println(Arrays.binarySearch(booleanOperators, "AND"));

//		StringTokenizer st = new StringTokenizer("jaanus kala\"mees", " \"");
//		while (st.hasMoreTokens()){
//			System.out.println(st.nextToken());
//		}

//		PostgreSQLFullTextQuery query = PostgreSQLFullTextQuery.parse("\"jaanus \"");
//		System.out.println(query.phrases);

//		System.out.println(StringUtils.containsAny("jaanus", "\t\r\n "));

//		QueryParser parser = new QueryParser("", new StandardAnalyzer());
//		Query query = parser.parse("\"jaanus juhan\"");
//		System.out.println(query.getClass().getSimpleName() + ": " + query);
//
//		HashSet<Term> terms = new HashSet<Term>();
//		query.extractTerms(terms);
//		for (Term term : terms){
//			System.out.println(term);
//		}
    }

    public String getType() {
        return type;
    }


}
