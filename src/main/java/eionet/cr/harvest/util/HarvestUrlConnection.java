package eionet.cr.harvest.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

import org.apache.commons.lang.StringUtils;

import eionet.cr.util.UrlRedirectAnalyzer;
import eionet.cr.util.UrlRedirectionInfo;
import eionet.cr.util.URLUtil;

/**
 * 
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 *
 */

public class HarvestUrlConnection {

	private URL url;
	private HttpURLConnection urlConnection;
	private URLConnection generalConnection;

	private int responseCode = 0;

	private UrlRedirectionInfo redirectionInfo = new UrlRedirectionInfo();
	
	InputStream inputStream = null;
	private String contentType = null;
	private boolean sourceAvailable = false;
	private String sourceNotExistMessage = "";
	private boolean fileConnection = false;
	private boolean httpConnection = false;

	public static HarvestUrlConnection getConnection(String sourceUrlString) throws IOException, MalformedURLException{
		
		HarvestUrlConnection returnConnection = new HarvestUrlConnection();
		returnConnection.url = new URL(StringUtils.substringBefore(sourceUrlString, "#"));
		returnConnection.redirectionInfo.setSourceURL(sourceUrlString); 
		
		if (returnConnection.url.getProtocol().equals("http")||returnConnection.url.getProtocol().equals("https")){
			returnConnection.httpConnection = true;
			returnConnection.urlConnection = (HttpURLConnection)returnConnection.url.openConnection();
			returnConnection.urlConnection.setRequestProperty("Accept", "application/rdf+xml, text/xml, */*;q=0.6");
			returnConnection.urlConnection.setRequestProperty("User-Agent", URLUtil.userAgentHeader());
			returnConnection.urlConnection.setInstanceFollowRedirects(false);
		} else {
			returnConnection.fileConnection = true;
			returnConnection.generalConnection = (URLConnection)returnConnection.url.openConnection();
			returnConnection.generalConnection.setRequestProperty("Accept", "application/rdf+xml, text/xml, */*;q=0.6");
			returnConnection.generalConnection.setRequestProperty("User-Agent", URLUtil.userAgentHeader());
		}
		
		
		return returnConnection;
	}
	
	private boolean checkUrlRedirected(){
		// Checking redirection conditions.
		/*
		 * 301 Moved Permanently
		 * 302 Found
		 * 303 See Other
		 * 307 Temporary Redirect
		 */
		try {
			
			if (UrlRedirectAnalyzer.isCodeRedirectionResponseCode(responseCode)){
				redirectionInfo.setTargetURL(urlConnection.getHeaderField("Location"));
				redirectionInfo.setResponseCode(responseCode);
				redirectionInfo.setRedirected(true);
				return true;
			}
			else {
				return false;
			}
		} catch (Exception ex){
			System.out.println(ex.getMessage());
			return false;
		}
	}
	
	public boolean openInputStream() throws Exception{
		// open connection stream
		try {
			sourceAvailable = Boolean.FALSE;
			if (httpConnection){
				inputStream = urlConnection.getInputStream();
				responseCode = urlConnection.getResponseCode();
				checkUrlRedirected();
			} else if(fileConnection){
				inputStream = generalConnection.getInputStream();
			}
			sourceAvailable = Boolean.TRUE;
		} catch (Exception e){
			
			if (e!=null && e instanceof UnknownHostException){
				sourceNotExistMessage = "IP address of the host could not be determined";
			}
			else if (urlConnection instanceof HttpURLConnection){
				responseCode = urlConnection.getResponseCode();
				if ((responseCode>=400 && responseCode<=499) || responseCode==501 || responseCode==505){
					sourceNotExistMessage = "Got HTTP response code " + responseCode;
				}
			}
			
			System.out.println(e.getMessage()+" - "+sourceNotExistMessage);
		}
		return sourceAvailable;
	}

	public URLConnection getConnection(){
		if (fileConnection){
			return (URLConnection) generalConnection;
		}
		if (httpConnection){
			return (URLConnection) urlConnection;
		}
		return null;
	}
	
	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public HttpURLConnection getUrlConnection() {
		return urlConnection;
	}

	public void setUrlConnection(HttpURLConnection urlConnection) {
		this.urlConnection = urlConnection;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}
	
	public boolean isSourceAvailable() {
		return sourceAvailable;
	}

	public void setSourceAvailable(boolean sourceAvailable) {
		this.sourceAvailable = sourceAvailable;
	}

	public String getSourceNotExistMessage() {
		return sourceNotExistMessage;
	}

	public void setSourceNotExistMessage(String sourceNotExistMessage) {
		this.sourceNotExistMessage = sourceNotExistMessage;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public URLConnection getGeneralConnection() {
		return generalConnection;
	}

	public void setGeneralConnection(URLConnection generalConnection) {
		this.generalConnection = generalConnection;
	}

	public boolean isFileConnection() {
		return fileConnection;
	}

	public void setFileConnection(boolean fileConnection) {
		this.fileConnection = fileConnection;
	}

	public boolean isHttpConnection() {
		return httpConnection;
	}

	public void setHttpConnection(boolean httpConnection) {
		this.httpConnection = httpConnection;
	}

	public UrlRedirectionInfo getRedirectionInfo() {
		return redirectionInfo;
	}

	public void setRedirectionInfo(UrlRedirectionInfo redirectionInfo) {
		this.redirectionInfo = redirectionInfo;
	}

}
