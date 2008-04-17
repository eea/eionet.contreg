package eionet.cr.util;

import java.net.URLEncoder;

/**
 * Helper class that contains static helper methods for string manupulation.
 * 
 * @author gerasvad
 * 
 */
public class StringUtils {
	/**
	 * Checks whether input string is null or empty (empty string or string that
	 * contains only space symbols).
	 * 
	 * @param str
	 *            string to be checked
	 * @return true if string is null or empty.
	 */
	public static boolean isEmptyOrNull(String str) {
		return str == null || str.length() == 0 || str.trim().length() == 0;
	}

	/**
	 * makes string URL encoded in UTF-8 characyer encoding.
	 * 
	 * @param in string to encode
	 * @return encoded string
	 */
	public static String urlEncode(String in) {
		String result = in;

		if (in != null) {
			try {
				result = URLEncoder.encode(in, "UTF-8");
			} catch (Exception e) {
				// ignore
			}
		}

		return result;
	}
	
	public static String appendHttpIfRequired(String str) {
		return str != null && !str.toLowerCase().startsWith("http://") ? "http://" + str : str;
	}
	
	/**
	 * 
	 * @param obj1
	 * @param obj2
	 * @return
	 */
	public static boolean objectsEquals(Object obj1, Object obj2) {
		if (obj1 == null && obj2 == null) {
			return true;
		} else if (obj1 != null && obj2 != null) {
			return obj1.equals(obj2);
		} else {
			return false;
		}
	}
	
}
