package eionet.cr.util;

/**
 * 
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 *
 */

public class UrlRedirectionInfo {

	private String sourceURL = "";
	private String targetURL = "";
	private int responseCode = 0;
	private boolean Redirected = false;
	
	
	public String getSourceURL() {
		return sourceURL;
	}
	public void setSourceURL(String sourceURL) {
		this.sourceURL = sourceURL;
	}
	public String getTargetURL() {
		return targetURL;
	}
	public void setTargetURL(String targetURL) {
		this.targetURL = targetURL;
	}
	public int getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}
	public boolean isRedirected() {
		return Redirected;
	}
	public void setRedirected(boolean redirected) {
		Redirected = redirected;
	}
}
