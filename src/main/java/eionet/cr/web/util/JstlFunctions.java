package eionet.cr.web.util;

import java.util.StringTokenizer;

import eionet.cr.util.Util;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class JstlFunctions {

	/**
	 * Parses the given string with a whitespace tokenizer and looks up the first
	 * token whose length exceeds <tt>cutAtLength</tt>. If such a token is found, returns
	 * the given string's <code>substring(0, i + cutAtLength) + "..."</code>, where <code>i</code>
	 * is the start index of the found token.
	 * If no tokens are found that exceed the length of <tt>cutAtLength</tt>, then this method
	 * simply return the given string.
	 * 
	 * @return
	 */
	public static java.lang.String cutAtFirstLongToken(java.lang.String str, int cutAtLength){
		
		if (str==null)
			return "";
		
		String firstLongToken = null;
		StringTokenizer st = new StringTokenizer(str);
		while (st.hasMoreTokens()){
			String token = st.nextToken();
			if (token.length()>cutAtLength){
				firstLongToken = token;
				break;
			}
		}
		
		if (firstLongToken!=null){
			int i = str.indexOf(firstLongToken);
			StringBuffer buf = new StringBuffer(str.substring(0, i+cutAtLength));
			return buf.append("...").toString();
		}
		else
			return str;
	}
	
	/**
	 * Checks if the given string (after being trimmed first) contains any whitespace. If yes, returns
	 * the given string surrounded by quotes. Otherwise returns the given string.
	 * If the given string is <code>null</code>, returns null.
	 * 
	 * @param str
	 * @return
	 */
	public static java.lang.String addQuotesIfWhitespaceInside(java.lang.String str){
		
		if (str==null || str.trim().length()==0)
			return str;
		
		if (!Util.hasWhiteSpace(str.trim()))
			return str;
		else
			return "\"" + str + "\"";
	}
}
