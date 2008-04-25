package eionet.cr.util;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class URLUtil {

	/**
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isURL(String s){
		
		if (s==null || s.trim().length()==0)
			return false;
		
		try{
			URL url = new URL(s);
			return true;
		}
		catch (MalformedURLException e){
			return false;
		}
	}
}
