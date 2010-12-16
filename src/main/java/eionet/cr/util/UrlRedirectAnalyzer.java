/**
 * 
 */
package eionet.cr.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * 
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 *
 */

public class UrlRedirectAnalyzer {
	
	/** */
	public static Logger logger = Logger.getLogger(UrlRedirectAnalyzer.class);

	/**
	 * 
	 */
	public static UrlRedirectionInfo analyzeUrlRedirection(String urlToAnlayze){
		
		UrlRedirectionInfo result = new UrlRedirectionInfo();
		result.setSourceURL(urlToAnlayze);
		
		HttpURLConnection urlConnection = null;
		InputStream inputStream = null;
		try {
			URL url = new URL(StringUtils.substringBefore(urlToAnlayze, "#"));
			
			urlConnection = (HttpURLConnection)url.openConnection();
			urlConnection.setRequestProperty("Accept", "application/rdf+xml, text/xml, */*;q=0.6");
			urlConnection.setRequestProperty("User-Agent", URLUtil.userAgentHeader());
			urlConnection.setInstanceFollowRedirects(false);
			
			inputStream = urlConnection.getInputStream();
			int responseCode = urlConnection.getResponseCode();

			if (isRedirectionResponseCode(responseCode)){
				
				String redirectLocation = urlConnection.getHeaderField("Location");
				String fullRedirectUrl = constructFullUrl(urlToAnlayze, redirectLocation);
				
				result.setRedirected(true);
				result.setTargetURL(fullRedirectUrl);
			}
			
		}
		catch (IOException e) {
			logger.warn("Ignoring this URL redirection analyze exception: " + e.getMessage());
		}
		finally{
			try{
				if (inputStream!=null){ 
					inputStream.close();
				}
			}
			catch (IOException e){}
		}
		
		return result;
	}
	
	/**
	 * 
	 * @param baseUrl
	 * @param relativeUrl
	 * @return
	 * @throws MalformedURLException
	 */
	public static String constructFullUrl(String baseUrl, String relativeUrl)
																	throws MalformedURLException {
		if (relativeUrl==null){
			return baseUrl;
		}
		else{
			return new URL (new URL(baseUrl), relativeUrl).toString();
		}
	}
	
	public static boolean isRedirectionResponseCode(int responseCode){
		if ((responseCode == 301)||(responseCode == 302)||(responseCode == 303)||(responseCode == 307)){
			return true;
		} else {
			return false;
		}
	}
	
	public static void main(String[] args) throws MalformedURLException{
		
		URL url = new URL(new URL("http://seis-basis.jrc.ec.europa.eu/preview/search_bynation.cfm?id_nation=13"), "");
		System.out.println(url.toString());
	}
	
}
