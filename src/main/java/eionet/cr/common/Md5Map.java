package eionet.cr.common;

import java.util.HashMap;
import java.util.HashSet;

import eionet.cr.util.Hashes;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class Md5Map extends HashMap<String,String>{
	
	/** */
	private static Md5Map instance = null;

	/**
	 * 
	 */
	private Md5Map(){
		super();
	}
	
	/**
	 * 
	 * @return
	 */
	public static Md5Map getInstance(){
		if (instance==null)
			instance = new Md5Map();
		return instance;
	}
	
	/**
	 * 
	 * @param value
	 */
	public static synchronized String addValue(String value){
		
		if (value==null)
			return null;
		
		String digest = Hashes.md5(value);
		getInstance().put(digest, value);
		return digest;
	}
	
	/**
	 * 
	 * @param md5key - the key in MD5 format
	 * @return
	 */
	public static String getValue(String md5key){
		if (md5key==null)
			return null;
		else{
			return getInstance().get(md5key);
		}
	}
	
	/**
	 * 
	 * @param md5key - the key in MD5 format
	 * @return
	 */
	public static boolean hasKey(String md5key){
		
		if (md5key==null)
			return false;
		else
			return getInstance().containsKey(md5key);
	}
}
