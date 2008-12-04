package eionet.cr.util;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class YesNoBoolean {
	
	/** */
	public static final String YES = "Y";
	public static final String NO = "N";

	/**
	 * 
	 * @param b
	 * @return
	 */
	public static String format(boolean b){
		return b==true ? YES : NO;
	}
	
	/**
	 * 
	 * @param s
	 * @return
	 */
	public static boolean parse(String s){
		
		if (s.equals(YES))
			return true;
		else if (s.equals(NO))
			return false;
		else
			throw new IllegalArgumentException(s);
	}
}
