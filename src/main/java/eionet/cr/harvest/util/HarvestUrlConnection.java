package eionet.cr.harvest.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eionet.cr.util.ConnectionError;
import eionet.cr.util.ConnectionError.ErrType;
import eionet.cr.util.URLUtil;

/**
 * Class to open a connection to a remote source or local file. Doesn't have a close method. You have to know how the class works to
 * close the connection via the inputStream field.
 *
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 *
 */
public class HarvestUrlConnection {

    /** */
    private static final Logger LOGGER = Logger.getLogger(HarvestUrlConnection.class);

    private URL url;
    private HttpURLConnection urlConnection;
    private URLConnection generalConnection;

    private ConnectionError error;

    private int responseCode = 0;

    InputStream inputStream = null;
    private boolean sourceAvailable = false;
    private String sourceNotExistMessage = "";
    private boolean fileConnection = false;
    private boolean httpConnection = false;

    /**
     * Behaves almost as a constructor, but isn't called HarvestUrlConnection.
     *
     * @param sourceUrlString
     * @return HarvestUrlConnection
     * @throws IOException
     */
    public static HarvestUrlConnection getConnection(String sourceUrlString) throws IOException {

        HarvestUrlConnection result = new HarvestUrlConnection();
        result.url = new URL(StringUtils.substringBefore(sourceUrlString, "#"));

        URL normalizedURL = URLUtil.replaceURLSpaces(result.url);
        if (result.url.getProtocol().equals("http") || result.url.getProtocol().equals("https")) {
            result.httpConnection = true;
            result.urlConnection = (HttpURLConnection) normalizedURL.openConnection();
            result.urlConnection.setRequestProperty("Accept", "application/rdf+xml, text/xml, */*;q=0.6");
            result.urlConnection.setRequestProperty("User-Agent", URLUtil.userAgentHeader());
            result.urlConnection.setRequestProperty("Connection", "close");
            result.urlConnection.setInstanceFollowRedirects(true);
        } else {
            result.fileConnection = true;
            result.generalConnection = normalizedURL.openConnection();
            result.generalConnection.setRequestProperty("Accept", "application/rdf+xml, text/xml, */*;q=0.6");
            result.generalConnection.setRequestProperty("User-Agent", URLUtil.userAgentHeader());
            result.urlConnection.setRequestProperty("Connection", "close");
        }

        return result;
    }

    /**
     * Opens the source and stores the file descriptor in inputStream.
     *
     * @return true if the source is available.
     * @throws Exception
     */
    public boolean openInputStream() throws Exception {

        try {
            sourceAvailable = Boolean.FALSE;
            if (httpConnection) {
                inputStream = urlConnection.getInputStream();
                responseCode = urlConnection.getResponseCode();
            } else if (fileConnection) {
                inputStream = generalConnection.getInputStream();
            }

            sourceAvailable = Boolean.TRUE;
        } catch (Exception e) {
            if (e instanceof UnknownHostException) {
                sourceNotExistMessage = "IP address of the host could not be determined";
                error = new ConnectionError(ErrType.TEMPORARY, 000, sourceNotExistMessage);
            } else if (httpConnection) {
                responseCode = urlConnection.getResponseCode();
                if (responseCode == 400 || (responseCode >= 402 && responseCode <= 407)
                        || (responseCode >= 409 && responseCode <= 417) || responseCode == 501 || responseCode == 505) {
                    sourceNotExistMessage = "Permanent error: Got HTTP response code " + responseCode;
                    error = new ConnectionError(ErrType.PERMANENT, responseCode, sourceNotExistMessage);
                } else if (responseCode == 401 || responseCode == 408 || responseCode == 500
                        || (responseCode >= 502 && responseCode <= 504)) {
                    sourceNotExistMessage = "Temporary error: Got HTTP response code " + responseCode;
                    error = new ConnectionError(ErrType.TEMPORARY, responseCode, sourceNotExistMessage);
                }

                // release TCP connection properly when HTTP connection fails
                if (e instanceof IOException) {
                    if (error == null) {
                        error = new ConnectionError(ErrType.TEMPORARY, 000, "Temporary error: \n " + e.getMessage());
                    }
                    try {
                        inputStream = (urlConnection).getErrorStream();
                        // read the response body - use a new buffer at every read.
                        // Hope the GC will run before we run out of memory.
                        if (inputStream != null) {
                            while (inputStream.read(new byte[1024]) >= 0) {
                            }
                        }
                        // inputstream is closed in VirtuosoPullHarvest final block
                    } catch (IOException ioe) {
                        e.printStackTrace();
                    }
                }
            }

            System.out.println(e.getMessage() + " - " + sourceNotExistMessage);
        }
        return sourceAvailable;
    }

    /**
     * Returns the connection. Connections can be stored in two variables: {@link #generalConnection} and {@link #urlConnection}. It
     * has been decided to use two fields even though one type (HttpURLConnection) is a subclass of the other (URLConnection). Uses
     * another two fields to decide which field is the one to use. No relation to {@link #getConnection(String)}. Just a confusing
     * name clash.
     *
     * @return the connection
     */
    public URLConnection getConnection() {
        if (fileConnection) {
            return generalConnection;
        }
        if (httpConnection) {
            return urlConnection;
        }
        return null;
    }

    /**
     * Closes inputStream
     */
    public void close() {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to close HTTP connection stream: " + e.toString());
        }
        URLUtil.disconnect(urlConnection);
        URLUtil.disconnect(generalConnection);
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

    public ConnectionError getError() {
        return error;
    }

}
