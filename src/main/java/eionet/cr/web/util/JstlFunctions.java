package eionet.cr.web.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.search.util.PredicateLabels;
import eionet.cr.search.util.SortOrder;
import eionet.cr.util.Util;
import eionet.cr.web.action.AbstractActionBean;
import eionet.cr.web.security.CRUser;

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
	 * Gets the collection of objects matching to the given predicate in the given subject.
	 * Formats the given collection to comma-separated string and returns it.
	 * Only distinct objects and only literal ones are selected (unless there is not a single literal
	 * in which case the non-literals are returned.
	 * 
	 * @param subjectDTO
	 * @param predicateUri
	 * @return
	 */
	public static String formatPredicateObjects(SubjectDTO subjectDTO, String predicateUri){
		
		if (subjectDTO==null || subjectDTO.getPredicateCount()==0 || predicateUri==null)
			return "";
		
		Collection<ObjectDTO> objects = subjectDTO.getObjects(predicateUri);
		if (objects==null || objects.isEmpty())
			return "";
		
		LinkedHashSet<ObjectDTO> distinctObjects = new LinkedHashSet<ObjectDTO>(objects);		
		StringBuffer bufLiterals = new StringBuffer();
		StringBuffer bufNonLiterals = new StringBuffer();
		
		for (Iterator<ObjectDTO> iter = distinctObjects.iterator(); iter.hasNext();){			
			ObjectDTO objectDTO = iter.next();
			if (objectDTO.isLiteral())
				append(bufLiterals, objectDTO.toString());
			else
				append(bufNonLiterals, objectDTO.toString());
		}
		
		return bufLiterals.length()>0 ? bufLiterals.toString() : bufNonLiterals.toString();
	}
	
	/**
	 * 
	 * @param buf
	 * @param s
	 */
	private static void append(StringBuffer buf, String s){
		if (s.trim().length()>0){
			if (buf.length()>0){
				buf.append(", ");
			}
			buf.append(s);
		}
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
	public static String sortUrl(AbstractActionBean actionBean, String sortPredicate){
		
		HttpServletRequest request = actionBean.getContext().getRequest();
		StringBuffer buf = new StringBuffer(actionBean.getUrlBinding());
		buf.append("?").append(request.getQueryString());
		
		String curValue = request.getParameter("sortP");
		if (curValue!=null){
			buf = new StringBuffer(
					StringUtils.replace(buf.toString(), "sortP=" + Util.urlEncode(curValue), "sortP=" + Util.urlEncode(sortPredicate)));
		}
		else
			buf.append("&sortP=").append(Util.urlEncode(sortPredicate)); 

		curValue = request.getParameter("sortO");
		if (curValue!=null)
			buf = new StringBuffer(StringUtils.replace(buf.toString(), "sortO=" + curValue, "sortO=" + oppositeSortOrder(curValue)));
		else
			buf.append("&sortO=").append(oppositeSortOrder(curValue)); 

		String result = buf.toString();
		return result.startsWith("/") ? result.substring(1) : result;
	}
	
	/**
	 * 
	 * @param order
	 * @return
	 */
	private static String oppositeSortOrder(String order){
		if (StringUtils.isBlank(order))
			return SortOrder.ASCENDING.toString();
		else
			return SortOrder.parse(order).toOpposite().toString();
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
	public static boolean isSourceToAny(String objectHash, Collection objects){
		
		boolean result = false;
		if (objectHash!=null){
			for (Iterator i=objects.iterator(); i.hasNext();){
				ObjectDTO objectDTO = (ObjectDTO)i.next();
				if (objectHash.equals(objectDTO.getSourceObject())){
					result = true;
					break;
				}
			}
		}
		
		return result;
	}
}
