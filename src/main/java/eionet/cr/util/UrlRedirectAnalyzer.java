/**
 * 
 */
package eionet.cr.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 *
 */

public class UrlRedirectAnalyzer {

	public static UrlRedirectionInfo analyzeUrlRedirection(String testUrl){
		UrlRedirectionInfo returnUrlRedirectionInfo = new UrlRedirectionInfo();
		returnUrlRedirectionInfo.setSourceURL(testUrl);
		HttpURLConnection connection = null;
		InputStream inputStream = null;
		try {
			URL url = new URL(StringUtils.substringBefore(testUrl, "#"));
			connection = (HttpURLConnection)url.openConnection();
			connection.setRequestProperty("Accept", "application/rdf+xml, text/xml, */*;q=0.6");
			connection.setRequestProperty("User-Agent", URLUtil.userAgentHeader());
			connection.setInstanceFollowRedirects(false);
			
			inputStream = connection.getInputStream();
			int responseCode = connection.getResponseCode();

			if (isCodeRedirectionResponseCode(responseCode)){
				returnUrlRedirectionInfo.setRedirected(true);
				returnUrlRedirectionInfo.setTargetURL(
						fixRelativeUrl(
						connection.getHeaderField("Location"), 
						responseCode, 
						testUrl)
						);
			}
			
		} catch (Exception ex){
		}
		finally{
			// close input stream
			try{ 
				if (connection != null && inputStream!=null) 
					inputStream.close(); 
			} catch (IOException e){}
		}
		
		return returnUrlRedirectionInfo;
	}
	
	public static String fixRelativeUrl(String relativeUrl, int responseCode, String sourceUrl){
		String fixedUrl = "";
		if (responseCode == 303 && !relativeUrl.isEmpty()){
			if (relativeUrl.startsWith("/")){
				 fixedUrl = URLUtil.extractUrlHost(sourceUrl)+relativeUrl;
			} else {
				fixedUrl = sourceUrl.substring(0, sourceUrl.lastIndexOf("/")) + "/" + relativeUrl;
			}
			return fixedUrl;
		} else {
			return relativeUrl;
		}
	}
	
	public static boolean isCodeRedirectionResponseCode(int responseCode){
		if ((responseCode == 301)||(responseCode == 302)||(responseCode == 303)||(responseCode == 307)){
			return true;
		} else {
			return false;
		}
	}
	
}
