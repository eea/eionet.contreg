package eionet.cr.util;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.ArrayList;

import eionet.cr.common.CRRuntimeException;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class Hashes {
	
	/**
	 * 
	 * @param s
	 * @return
	 */
	public static long spoHash(String s){
		return Hashes.fnv64(s);
	}

	/**
	 * 
	 * @param s
	 * @return
	 */
	public static long fnv64(String s){
		
		long hash = 0xcbf29ce484222325L;
		if (s!=null && s.length()>0){
		    for (int i=0; i<s.length(); i++){
		    	hash ^= (long)s.charAt(i);
		    	hash += (hash << 1) + (hash << 4) + (hash << 5) + (hash << 7) + (hash << 8) + (hash << 40);
		    }
		}
		
		return hash;
	}

	/**
	 * 
	 * @param s
	 * @return
	 */
	public static String md5(String s){
		return Hashes.digest(s, "md5");
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
}
