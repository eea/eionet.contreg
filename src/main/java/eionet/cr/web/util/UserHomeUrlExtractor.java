package eionet.cr.web.util;

import eionet.cr.web.security.BadUserHomeUrlException;

/**
 * 
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 *
 */

public class UserHomeUrlExtractor {

	public static String extractUserNameFromHomeUrl(String url) throws BadUserHomeUrlException{
		// Just to be sure that there is something before "/home/".
		url = "#"+url;
		String userNamePart;
		String userName;
		try {
			userNamePart = url.split("/home/")[1];
			userName = userNamePart.split("/")[0];
		} catch (Exception ex){
			throw new BadUserHomeUrlException("Not properly typed user home directory URL");
		}
		
		return userName;
		
	}
	
	public static String extractSectionFromHomeUrl(String url) throws BadUserHomeUrlException{
		
		String userName = extractUserNameFromHomeUrl(url);
		String [] urlParts = new String[0];
		String section = "";
		try {
			urlParts = url.split(userName+"/");
		} catch (Exception ex){
		}
		
		if (urlParts.length > 1){
			section = urlParts[1].split("/")[0];
		}
		return section;
		
	}
	
}
