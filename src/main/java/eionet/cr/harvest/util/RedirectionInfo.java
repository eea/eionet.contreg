package eionet.cr.harvest.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eionet.cr.util.URLUtil;

/**
 *
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 *
 */
public class RedirectionInfo {

    /** */
    public static Logger LOGGER = Logger.getLogger(RedirectionInfo.class);

    /** */
    private String sourceURL;
    private String targetURL;

    /**
     *
     */
    private RedirectionInfo(String sourceUrl){

        this.sourceURL = sourceUrl;

        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(StringUtils.substringBefore(sourceUrl, "#"));
            urlConnection = (HttpURLConnection) URLUtil.replaceURLSpaces(url).openConnection();
            urlConnection.setRequestProperty("Connection", "close");
            urlConnection.setRequestProperty("Accept", "application/rdf+xml, text/xml, */*;q=0.6");
            urlConnection.setRequestProperty("User-Agent", URLUtil.userAgentHeader());
            urlConnection.setInstanceFollowRedirects(false);

            int responseCode = urlConnection.getResponseCode();
            if (isRedirectionResponseCode(responseCode)) {

                String redirectLocation = urlConnection.getHeaderField("Location");
                String fullRedirectUrl = constructFullUrl(sourceUrl, redirectLocation);
                this.targetURL = fullRedirectUrl;
            }
        } catch (IOException e) {
            LOGGER.warn("Ignoring this URL redirection analyze exception: " + e.toString());
        } finally {
            URLUtil.disconnect(urlConnection);
        }
    }

    /**
     *
     * @param url
     * @return
     */
    public static RedirectionInfo parse(String url){

        return new RedirectionInfo(url);
    }

    /**
     * @return the sourceURL
     */
    public String getSourceURL() {
        return sourceURL;
    }

    /**
     * @return the targetURL
     */
    public String getTargetURL() {
        return targetURL;
    }

    /**
     * @return the redirected
     */
    public boolean isRedirected() {
        return targetURL!=null && !targetURL.isEmpty();
    }

    /**
     *
     * @param responseCode
     * @return
     */
    private static boolean isRedirectionResponseCode(int responseCode) {

        return responseCode == 301 || responseCode == 302 || responseCode == 303 || responseCode == 307;
    }

    /**
     *
     * @param baseUrl
     * @param relativeUrl
     * @return
     * @throws MalformedURLException
     */
    protected static String constructFullUrl(String baseUrl, String relativeUrl) throws MalformedURLException {
        if (relativeUrl == null) {
            return baseUrl;
        } else {
            return new URL(new URL(baseUrl), relativeUrl).toString();
        }
    }
}
