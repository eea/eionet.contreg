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
package eionet.cr.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.jsp.PageContext;

import org.quartz.CronExpression;

import eionet.cr.common.CRRuntimeException;


/**
 * 
 * @author heinljab
 *
 */
public class Util {

	/**
	 * 
	 * @param t
	 * @return
	 */
	public static String getStackTrace(Throwable t){
		
		StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        t.printStackTrace(pw);
        return sw.getBuffer().toString();
	}

	/**
	 * 
	 * @param t
	 * @return
	 */
	public static String getStackTraceForHTML(Throwable t){
		
		return processStackTraceForHTML(getStackTrace(t));
	}
	
	/**
	 * 
	 * @param stackTrace
	 * @return
	 */
	public static String processStackTraceForHTML(String stackTrace){
		
		if (stackTrace==null || stackTrace.trim().length()==0)
			return stackTrace;
		
		StringBuffer buf = new StringBuffer();
		String[] stackFrames = getStackFrames(stackTrace);
		for (int i=0; stackFrames!=null && i<stackFrames.length; i++){
			buf.append(stackFrames[i].replaceFirst("\t", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;")).append("<br/>");
		}
		
		return buf.length()>0 ? buf.toString() : stackTrace;
	}
	
	/**
	 * 
	 * @param stackTrace
	 * @return
	 */
	public static String[] getStackFrames(String stackTrace){
        StringTokenizer frames = new StringTokenizer(stackTrace, System.getProperty("line.separator"));
        List list = new LinkedList();
        for(; frames.hasMoreTokens(); list.add(frames.nextToken()));
        return (String[])list.toArray(new String[list.size()]);
    }
	
	/**
	 * 
	 * @param array
	 * @param separator
	 * @return
	 */
	public static String arrayToString(Object[] array, String separator){
		
		if (array==null)
			return null;
		
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<array.length; i++){
			if (i>0)
				buf.append(separator);
			buf.append(array[i].toString());
		}
		return buf.toString();
	}
	
	/**
	 * 
	 * @param date
	 * @param datePattern
	 * @return
	 */
    public static String dateToString(java.util.Date date, String datePattern){

    	if (date==null)
    		return null;
    	
    	SimpleDateFormat formatter = datePattern==null ? new SimpleDateFormat() : new SimpleDateFormat(datePattern);
    	return formatter.format(date);
	}

    /**
     * 
     * @param str
     * @param datePattern
     * @return
     */
    public static java.util.Date stringToDate(String str, String datePattern){

    	if (str==null || str.trim().length()==0)
    		return null;
    	
    	SimpleDateFormat formatter = new SimpleDateFormat(datePattern);
    	try {
			return formatter.parse(str);
		}
    	catch (ParseException e){
    		throw new CRRuntimeException("Failed to convert the given string to java.util.Date: " + e.toString(), e);
		}
	}

    /**
     * 
     * @return
     */
    public static long currentTimeSeconds(){
    	return (long)(System.currentTimeMillis() / (long)1000);
    }

    /**
     * 
     * @param milliSeconds
     * @return
     */
    public static long getSeconds(long milliSeconds){
    	return (long)(milliSeconds/(long)1000);
    }
    
    /**
     * 
     * @param str
     * @return
     */
    public static boolean isNullOrEmpty(String str){
    	return str == null || str.length() == 0 || str.trim().length() == 0;
    }
    
    /**
     * Returns true if the given string has any whitespace in it, including the leading and trailing whitespace.
     * @param s
     * @return
     */
    public static boolean hasWhiteSpace(String s){
    	
    	if (s==null || s.length()!=s.trim().length())
    		return true;
    	else{
	    	StringTokenizer st = new StringTokenizer(s);
	    	int count = 0;
	    	for (; st.hasMoreTokens() && count<2; count++)
	    		st.nextToken();
	    	return count>1;
    	}
    }
    
    /**
     * 
     * @param array
     * @return
     */
    public static Object getFirst(Object[] array){
    	return array!=null && array.length>0 ? array[0] : null;
    }

    /**
     * 
     * @param array
     * @return
     */
    public static String getFirst(String[] array){
    	return array!=null && array.length>0 ? array[0] : null;
    }

    /**
     * 
     * @param array
     * @return
     */
    public static String[] pruneUrls(String[] array){
    	
    	if (array==null || array.length==0)
    		return array;
    	
    	ArrayList<String> list = new ArrayList<String>();
    	for (int i=0; i<array.length; i++){
    		if (!URLUtil.isURL(array[i]))
    			list.add(array[i]);
    	}
    	
    	if (list.isEmpty())
    		return array;
    	else{
	    	String[] result = new String[list.size()];
	    	for (int i=0; i<list.size(); i++){
	    		result[i] = list.get(i);
	    	}
	    	return result;
    	}
    }
    
    /**
     * 
     * @param pageContext
     * @param objectClass
     * @return
     */
    public static Object findInAnyScope(PageContext pageContext, Class objectClass){
    	
    	if (pageContext==null || objectClass==null)
    		return null;
 
    	int[] scopes = {PageContext.APPLICATION_SCOPE, PageContext.PAGE_SCOPE, PageContext.REQUEST_SCOPE, PageContext.SESSION_SCOPE};
		for (int i=0; i<scopes.length; i++){
			Enumeration attrs = pageContext.getAttributeNamesInScope(scopes[i]);
			while (attrs!=null && attrs.hasMoreElements()){
				String name = (String)attrs.nextElement();
				Object o = pageContext.getAttribute(name, scopes[i]);
				if (o!=null && objectClass.isInstance(o)){
					return o;
				}
			}
		}
		
		return null;
    }
    
    /**
     * Convenience method for URL-encoding the given string.
     * @param s
     * @return
     */
    public static String urlEncode(String s){
    	try {
			return URLEncoder.encode(s, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			throw new CRRuntimeException(e.toString(), e);
		}
    }

    /**
     * Convenience method for URL-decoding the given string.
     * @param s
     * @return
     */
    public static String urlDecode(String s){
    	try {
			return URLDecoder.decode(s, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			throw new CRRuntimeException(e.toString(), e);
		}
    }

	/**
	 * 
	 * @param o
	 * @return
	 */
	public static Object[] toArray(Object o){
		if (o==null)
			return null;
		else{
			Object[] oo = new Object[1];
			oo[0] = o;
			return oo;
		}
	}
	
	/**
	 * 
	 * @param expression
	 */
	public static boolean isValidQuartzCronExpression(String expression){
		
		if (Util.isNullOrEmpty(expression))
			return false;
		else
			return CronExpression.isValidExpression(expression);
	}
	
	/**
	 * 
	 * @param coll
	 * @return
	 */
	public static String toCSV(Collection coll){
		
		StringBuffer buf = new StringBuffer();
		if (coll!=null){
			for (Iterator it = coll.iterator(); it.hasNext();){
				
				if (buf.length()>0){
					buf.append(",");
				}
				buf.append(it.next());
			}
		}
		return buf.toString();
	}
	
	/**
	 * 
	 * @param s
	 * @return
	 */
	public static Double toDouble(String s){
		
		if (s==null || s.trim().length()==0)
			return null;
		else{
			try{
				return Double.valueOf(s);
			}
			catch (NumberFormatException nfe){
				return null;
			}
		}
	}
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args){
		System.out.println(toDouble("-0,1"));
	}
}
