package eionet.cr.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

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
	 * @throws GeneralSecurityException 
	 */
	public static String md5digest(String s) throws GeneralSecurityException{
		return Util.digest(s, "md5");
	}
	
	/**
	 * 
	 * @param src
	 * @param algorithm
	 * @return
	 * @throws GeneralSecurityException
	 */
	public static String digest(String src, String algorithm) throws GeneralSecurityException {
        
        byte[] srcBytes = src.getBytes();
        byte[] dstBytes = new byte[16];
        
        MessageDigest md = MessageDigest.getInstance(algorithm);
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
}
