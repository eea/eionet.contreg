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
    private boolean sourceAvailable = false;
    private String sourceNotExistMessage = "";
    private boolean fileConnection = false;
    private boolean httpConnection = false;

    /**
     * @param sourceUrlString
     * @return
     * @throws IOException
     * @throws MalformedURLException
     */
    public static HarvestUrlConnection getConnection(String sourceUrlString) throws IOException, MalformedURLException{

        HarvestUrlConnection result = new HarvestUrlConnection();
        result.url = new URL(StringUtils.substringBefore(sourceUrlString, "#"));

        result.redirectionInfo = UrlRedirectAnalyzer.analyzeUrlRedirection(sourceUrlString);

        URL normalizedURL = URLUtil.replaceURLSpaces(result.url);
        if (result.url.getProtocol().equals("http") || result.url.getProtocol().equals("https")){

            result.httpConnection = true;
            result.urlConnection = (HttpURLConnection)normalizedURL.openConnection();
            result.urlConnection.setRequestProperty("Accept", "application/rdf+xml, text/xml, */*;q=0.6");
            result.urlConnection.setRequestProperty("User-Agent", URLUtil.userAgentHeader());
            result.urlConnection.setInstanceFollowRedirects(false);
        }
        else {
            result.fileConnection = true;
            result.generalConnection = (URLConnection)normalizedURL.openConnection();
            result.generalConnection.setRequestProperty("Accept", "application/rdf+xml, text/xml, */*;q=0.6");
            result.generalConnection.setRequestProperty("User-Agent", URLUtil.userAgentHeader());
        }

        return result;
    }

    /**
     *
     * @return
     */
    private boolean checkUrlRedirected(){

        return redirectionInfo.isRedirected();
    }

    /**
     *
     * @return
     * @throws Exception
     */
    public boolean openInputStream() throws Exception{

        try {
            sourceAvailable = Boolean.FALSE;
            if (httpConnection){
                inputStream = urlConnection.getInputStream();
                responseCode = urlConnection.getResponseCode();
            }
            else if(fileConnection){
                inputStream = generalConnection.getInputStream();
            }

            sourceAvailable = Boolean.TRUE;
        }
        catch (Exception e){

            if (e!=null && e instanceof UnknownHostException){
                sourceNotExistMessage = "IP address of the host could not be determined";
            }
            else if (urlConnection instanceof HttpURLConnection){
                responseCode = urlConnection.getResponseCode();
                if ((responseCode>=400 && responseCode<=499) || responseCode==501 || responseCode==505){
                    sourceNotExistMessage = "Got HTTP response code " + responseCode;
                }

                //release TCP connection properly when HTTP connection fails
                if(e instanceof IOException){
                    try{
                        inputStream = ((HttpURLConnection)urlConnection).getErrorStream();
                    //  read the response body
                        if(inputStream!=null){
                            while (inputStream.read(new byte[1024]) >= 0) {}
                        }
                    //  inputstream is closed in VirtuosoPullHarvest final block
                    } catch(IOException ioe){
                        e.printStackTrace();
                    }
                }
            }

            System.out.println(e.getMessage()+" - "+sourceNotExistMessage);
        }

        return sourceAvailable;
    }

    /**
     *
     * @return
     */
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
