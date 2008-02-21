package eionet.cr.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;

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
}
