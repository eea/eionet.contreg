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
	private boolean isHash = false;
	
	/**
	 * 
	 * @param s
	 */
	public SearchExpression(String s){
		
		expression = s==null ? "" : s.trim();
		isUri = URIUtil.isSchemedURI(expression);
		
		try{
			Long.parseLong(expression);
			isHash = true;
		}
		catch (NumberFormatException nfe){}
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isEmpty(){
		return expression==null || expression.length()==0;
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

	/**
	 * @return the isHash
	 */
	public boolean isHash() {
		return isHash;
	}
}
