package eionet.cr.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import java.lang.reflect.InvocationTargetException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.jsp.PageContext;

import eionet.cr.common.CRRuntimeException;


/**
 * 
 * @author heinljab
 *
 */
public class Util {

	/**
	 * 
	 * @param s
	 * @return
	 */
	public static String md5digest(String s){
		return Util.digest(s, "md5");
	}
	
	/**
	 * 
	 * @param src
	 * @param algorithm
	 * @return
	 */
	public static String digest(String src, String algorithm){
        
        byte[] srcBytes = src.getBytes();
        byte[] dstBytes = new byte[16];
        
        MessageDigest md;
        try{
        	md = MessageDigest.getInstance(algorithm);
        }
        catch (GeneralSecurityException e){
        	throw new CRRuntimeException(e.toString(), e);
        }
        md.update(srcBytes);
        dstBytes = md.digest();
        md.reset();
        
        StringBuffer buf = new StringBuffer();
        for (int i=0; i<dstBytes.length; i++){
            Byte byteWrapper = new Byte(dstBytes[i]);
            int k = byteWrapper.intValue();
            String s = Integer.toHexString(byteWrapper.intValue());
            if (s.length() == 1) s = "0" + s;
            buf.append(s.substring(s.length() - 2));
        }
        
        return buf.toString();
    }
	
	/**
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isURL(String s){
		try{
			URL url = new URL(s);
			return true;
		}
		catch (MalformedURLException e){
			return false;
		}
	}

	/**
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isURI(String s){
		try{
			URI uri = new URI(s);
			return true;
		}
		catch (URISyntaxException e){
			return false;
		}
	}

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
    public static String escapeForLuceneQuery(String str){
		
        StringBuffer buf = new StringBuffer();
        for(int i=0; i<str.length(); i++){
        	
        	char c = str.charAt(i);
            switch(c){
            
	            case 43: // '+'
	            	buf.append("\\+");
	                break;
	            case 45: // '-'
	                buf.append("\\-");
	                break;
	            case 33: // '!'
	                buf.append("\\!");
	                break;
	            case 40: // '('
	                buf.append("\\(");
	                break;
	            case 41: // ')'
	                buf.append("\\)");
	                break;
	            case 123: // '{'
	                buf.append("\\{");
	                break;
	            case 125: // '}'
	                buf.append("\\}");
	                break;
	            case 91: // '['
	                buf.append("\\[");
	                break;
	            case 93: // ']'
	                buf.append("\\]");
	                break;
	            case 94: // '^'
	                buf.append("\\^");
	                break;
	            case 34: // '"'
	                buf.append("\\\"");
	                break;
	            case 126: // '~'
	                buf.append("\\~");
	                break;
	            case 42: // '*'
	                buf.append("\\*");
	                break;
	            case 63: // '?'
	                buf.append("\\?");
	                break;
	            case 58: // ':'
	                buf.append("\\:");
	                break;
	            case 92: // '\'
	                buf.append("\\\\");
	                break;
	            default:
	            	buf.append(c);
	                break;
            }
        }

        return buf.toString();
    }
    
    /**
     * 
     * @param s
     * @return
     */
    public static boolean isNullOrEmpty(String s){
    	return s==null || s.length()==0;
    }
    
    /**
     * 
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
     * @param scheme
     */
    public static boolean isCommonURIScheme(String scheme){
    	
    	if (scheme==null || scheme.length()!=scheme.trim().length() || scheme.length()==0)
    		return false;
    	else if (scheme.equals("dns"))
    		return true;
    	else if (scheme.equals("file"))
    		return true;
    	else if (scheme.equals("ftp"))
    		return true;
    	else if (scheme.equals("sftp"))
    		return true;
    	else if (scheme.equals("tftp"))
    		return true;
    	else if (scheme.equals("go"))
    		return true;
    	else if (scheme.equals("gopher"))
    		return true;
    	else if (scheme.equals("http"))
    		return true;
    	else if (scheme.equals("https"))
    		return true;
    	else if (scheme.equals("ldap"))
    		return true;
    	else if (scheme.equals("mailto"))
    		return true;
    	else if (scheme.equals("news"))
    		return true;
    	else if (scheme.equals("shttp"))
    		return true;
    	else if (scheme.equals("skype"))
    		return true;
    	else if (scheme.equals("snmp"))
    		return true;
    	else if (scheme.equals("telnet"))
    		return true;
    	else if (scheme.equals("urn"))
    		return true;
    	else if (scheme.equals("news"))
    		return true;
    	else
    		return false;
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
    		if (!Util.isURL(array[i]))
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
     * 
     * @param args
     */
    public static void main(String[] args){
    }
}
