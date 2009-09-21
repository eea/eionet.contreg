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
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.cr.web.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.search.util.SortOrder;
import eionet.cr.util.Hashes;
import eionet.cr.util.Util;
import eionet.cr.web.action.AbstractActionBean;
import eionet.cr.web.security.CRUser;
import eionet.cr.web.util.columns.SearchResultColumn;

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

	/**
	 * 
	 * @param userName
	 * @param aclName
	 * @param permission
	 * @return
	 */
	public static boolean hasPermission(java.lang.String userName, java.lang.String aclName, java.lang.String permission){
		return CRUser.hasPermission(userName, aclName, permission);
	}

	/**
	 * Returns a string that is constructed by concatenating the given bean request's getRequestURI() + "?" +
	 * the given bean request's getQueryString(), and replacing the sort predicate with the given one.
	 * The present sort order is replaced by the opposite.
	 * 
	 * @param request
	 * @param sortP
	 * @param sortO
	 * @return
	 */
	public static String sortUrl(AbstractActionBean actionBean, SearchResultColumn column){
		
		HttpServletRequest request = actionBean.getContext().getRequest();
		StringBuffer buf = new StringBuffer(actionBean.getUrlBinding());
		buf.append("?");
		if (StringUtils.isBlank(column.getActionRequestParameter())) {
			buf.append(
					StringEscapeUtils.escapeHtml(
							request.getQueryString()));
		} else {
			buf.append(column.getActionRequestParameter());
		}
		String sortParamValue = column.getSortParamValue();
		if (sortParamValue==null)
			sortParamValue = "";
		
		String curValue = request.getParameter("sortP");
		if (curValue!=null){
			buf = new StringBuffer(
					StringUtils.replace(buf.toString(), "sortP=" + Util.urlEncode(curValue), "sortP=" + Util.urlEncode(sortParamValue)));
		}
		else
			buf.append("&amp;sortP=").append(Util.urlEncode(sortParamValue)); 

		curValue = request.getParameter("sortO");
		if (curValue!=null)
			buf = new StringBuffer(StringUtils.replace(buf.toString(), "sortO=" + curValue, "sortO=" + SortOrder.oppositeSortOrder(curValue)));
		else
			buf.append("&amp;sortO=").append(SortOrder.oppositeSortOrder(curValue)); 

		String result = buf.toString();
		return result.startsWith("/") ? result.substring(1) : result;
	}
	
	/**
	 * Finds the label for the given predicate in the given predicate-label map.
	 * If there is no match, then looks for the last occurrence of '#' or '/' or ':' in the predicate.
	 * If such an occurrence is found, returns everything after that occurrence.
	 * Otherwise returns the predicate as it was given.
	 * 
	 * @param predicateLabels
	 * @param predicate
	 * @return
	 */
	public static String getPredicateLabel(Map predicateLabels, String predicate){

		Object o = predicateLabels.get(predicate);
		String label = o==null ? null : o.toString();
		if (StringUtils.isBlank(label)){
			int last = Math.max(Math.max(predicate.lastIndexOf('#'), predicate.lastIndexOf('/')), predicate.lastIndexOf(':'));
			if (last>=0){
				label = predicate.substring(last+1);
			}
		}
		
		return StringUtils.isBlank(label) ? predicate : label;
	}
	
	/**
	 * 
	 * @param subjectDTO
	 * @param predicates
	 * @param object
	 * @return
	 */
	public static boolean subjectHasPredicateObject(SubjectDTO subjectDTO, Set predicates, String object){
		
		boolean result = false;
		
		if (predicates==null)
			return result;
		
		for (Iterator i=predicates.iterator(); i.hasNext();){
			if (subjectDTO.hasPredicateObject(i.next().toString(), object)){
				result = true;
				break;
			}
		}
			
		return result;
	}
	
	/**
	 * 
	 * @param objects
	 * @param findObjectHash
	 * @return
	 */
	public static boolean isSourceToAny(long objectHash, Collection objects){
		
		boolean result = false;
		for (Iterator i=objects.iterator(); i.hasNext();){
			ObjectDTO objectDTO = (ObjectDTO)i.next();
			if (objectHash == objectDTO.getSourceObjectHash()){
				result = true;
				break;
			}
		}
		
		return result;
	}
	
	/**
	 * Returns a color for the given source by supplying the source's hash to the
	 * <code>Colors.colorByModulus(long)</code>.
	 * 
	 * @param source
	 * @return
	 */
	public static String colorForSource(String source){
		
		return Colors.toKML(Colors.colorByModulus(Hashes.spoHash(source==null ? "" : source)), false);
	}
	
	/**
	 * 
	 * @param s
	 * @return
	 */
	public static String urlEncode(String s){
		
		return Util.urlEncode(s);
	}
	
	/**
	 * 
	 * @param column
	 * @param subjectDTO
	 * @return
	 */
	public static String format(SearchResultColumn column, Object object){
		return column.format(object);
	}
	
	/**
	 * 
	 * @param s
	 * @return
	 */
	public static long spoHash(String s){
		return Hashes.spoHash(s);
	}
	
	/**
	 * 
	 * @param subject
	 * @param predicate
	 * @return
	 */
	public static String getObjectLiteral(SubjectDTO subject, String predicate){
		
		if (subject==null)
			return "";
		
		ObjectDTO object = subject.getObject(predicate, ObjectDTO.Type.LITERAL);
		return object==null ? "" : object.getValue();
	}
}
