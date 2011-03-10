package eionet.cr.util;

/**
 *
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 *
 */
public class UrlRedirectionInfo {

    /** */
    private String sourceURL = "";
    private String targetURL = "";
    private int responseCode = 0;
    private boolean redirected = false;

    /**
     * @return the sourceURL
     */
    public String getSourceURL() {
        return sourceURL;
    }
    /**
     * @param sourceURL the sourceURL to set
     */
    public void setSourceURL(String sourceURL) {
        this.sourceURL = sourceURL;
    }
    /**
     * @return the targetURL
     */
    public String getTargetURL() {
        return targetURL;
    }
    /**
     * @param targetURL the targetURL to set
     */
    public void setTargetURL(String targetURL) {
        this.targetURL = targetURL;
    }
    /**
     * @return the responseCode
     */
    public int getResponseCode() {
        return responseCode;
    }
    /**
     * @param responseCode the responseCode to set
     */
    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }
    /**
     * @return the redirected
     */
    public boolean isRedirected() {
        return redirected;
    }
    /**
     * @param redirected the redirected to set
     */
    public void setRedirected(boolean redirected) {
        this.redirected = redirected;
    }

}
