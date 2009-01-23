package eionet.cr.search.util;

import eionet.cr.util.URIUtil;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class SearchExpression {

	/** */
	private String expression;
	private boolean exactPhrase = false;
	private boolean isUri = false;
	
	/**
	 * 
	 * @param s
	 */
	public SearchExpression(String s){
		
		expression = s==null ? "" : s.trim();
		exactPhrase = expression.startsWith("\"") && expression.endsWith("\"");
		if (exactPhrase){
			expression = expression.substring(1, expression.length()-1);
		}
		
		isUri = URIUtil.isSchemedURI(expression);
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isEmpty(){
		return expression==null || expression.length()==0;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isExactPhrase(){
		return exactPhrase; 
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return expression;
	}

	/**
	 * @return the isUri
	 */
	public boolean isUri() {
		return isUri;
	}
}
