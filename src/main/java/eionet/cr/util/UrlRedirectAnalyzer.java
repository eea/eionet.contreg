/**
 *
 */
package eionet.cr.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

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
     * @param urlToAnlayze
     * @param urlConnection
     * @return UrlRedirectionInfo
     * @throws MalformedURLException
     */
    public static UrlRedirectionInfo analyzeUrlRedirection(String urlToAnlayze) {

        UrlRedirectionInfo result = new UrlRedirectionInfo();
        result.setSourceURL(urlToAnlayze);

        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(StringUtils.substringBefore(urlToAnlayze, "#"));
            urlConnection = (HttpURLConnection)URLUtil.replaceURLSpaces(url).openConnection();
            urlConnection.setRequestProperty("Accept", "application/rdf+xml, text/xml, */*;q=0.6");
            urlConnection.setRequestProperty("User-Agent", URLUtil.userAgentHeader());
            urlConnection.setInstanceFollowRedirects(false);

            int responseCode = urlConnection.getResponseCode();
            if (isRedirectionResponseCode(responseCode)) {

                String redirectLocation = urlConnection.getHeaderField("Location");
                String fullRedirectUrl = constructFullUrl(urlToAnlayze, redirectLocation);

                result.setRedirected(true);
                result.setTargetURL(fullRedirectUrl);
            }

        } catch (IOException e) {
            logger.warn("Ignoring this URL redirection analyze exception: " + e.toString());
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {}
        }

        return result;
    }

    /**
     *
     * @param baseUrl
     * @param relativeUrl
     * @return String
     * @throws MalformedURLException
     */
    public static String constructFullUrl(String baseUrl, String relativeUrl)
                                                                    throws MalformedURLException {
        if (relativeUrl == null) {
            return baseUrl;
        } else {
            return new URL (new URL(baseUrl), relativeUrl).toString();
        }
    }

    public static boolean isRedirectionResponseCode(int responseCode) {
        if ((responseCode == 301) || (responseCode == 302) || (responseCode == 303) || (responseCode == 307)) {
            return true;
        } else {
            return false;
        }
    }

    public static void main(String[] args) throws URISyntaxException, MalformedURLException {

        URL url = new URL("http://foo.com/hello world");
        URI uri = url.toURI();
        url = uri.toURL();
        System.out.println(uri.toString());
    }
}
