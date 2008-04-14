package eionet.cr.search.util;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import eionet.cr.util.Identifiers;
import eionet.cr.util.Util;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class SimpleSearchExpression{

	/** */
	private String expression = null;
	private Boolean isUrlSearch = null;
	
	/**
	 * 
	 * @param queryStr
	 */
	public SimpleSearchExpression(String expression){
		
		this.expression = expression;
	}
	
	/**
	 * 
	 * @return
	 */
	public String toLuceneQueryString(){
		
		if (isUrlSearch())
			return processQueryForURLSearch(expression);
		else	
			return expression;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return expression;
	}

	/**
	 * @return the analyzerName
	 */
	public Analyzer getAnalyzer() {
		if (isUrlSearch())
			return new KeywordAnalyzer();
		else
			return new StandardAnalyzer();
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isUrlSearch(){
		
		if (isUrlSearch==null){
			
			isUrlSearch = new Boolean(false);
			if (expression.trim().length()>0){
				try {
					URI uri = new URI(expression.trim());
					isUrlSearch = new Boolean(Util.isCommonURIScheme(uri.getScheme()));
				}
				catch (URISyntaxException e){
				}
			}
		}
		
		return isUrlSearch.booleanValue();
	}
	
	/**
	 * 
	 * @param query
	 * @return
	 */
	private static String processQueryForURLSearch(String query){
		
		StringBuffer buf = new StringBuffer(Identifiers.DOC_ID);
		buf.append(":").append(Util.escapeForLuceneQuery(query.trim()));
		return buf.toString();
	}
}
