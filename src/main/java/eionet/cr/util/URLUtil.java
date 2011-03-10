/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.cr.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;

import eionet.cr.config.GeneralConfig;
import eionet.cr.harvest.PullHarvest;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class URLUtil {

    /**
     *
     * @param s
     * @return
     */
    public static boolean isURL(String s){

        if (s==null || s.trim().length()==0)
            return false;

        try{
            URL url = new URL(s);
            return true;
        }
        catch (MalformedURLException e){
            return false;
        }
    }

    /**
     *
     * @return
     */
    public static String userAgentHeader(){

        String ua = GeneralConfig.getRequiredProperty(GeneralConfig.APPLICATION_USERAGENT);
        Object[] args = new String[1];
        args[0] = GeneralConfig.getRequiredProperty(GeneralConfig.APPLICATION_VERSION);
        return MessageFormat.format(ua, args);
    }

    /**
     *
     * @param timestamp
     * @return
     */
    public static Boolean isModifiedSince(String urlString, long timestamp){

        if (!URLUtil.isURL(urlString))
            return false;

        if (timestamp==0)
            return true;

        InputStream inputStream = null;
        try{
            URL url = new URL(StringUtils.substringBefore(urlString, "#"));
            URLConnection urlConnection = replaceURLSpaces(url).openConnection();
            urlConnection.setRequestProperty("User-Agent", userAgentHeader());
            urlConnection.setIfModifiedSince(timestamp);
            inputStream = urlConnection.getInputStream();

            int responseCode = ((HttpURLConnection)urlConnection).getResponseCode();
            if (responseCode==HttpURLConnection.HTTP_NOT_MODIFIED){
                return Boolean.FALSE;
            }
            else if (responseCode==HttpURLConnection.HTTP_OK){
                return Boolean.TRUE;
            }
            else{
                return null;
            }
        }
        catch (IOException ioe){
            return null;
        }
        finally{
            if (inputStream!=null){
                try{ inputStream.close(); } catch (IOException ioe){}
            }
        }
    }

    /**
     *
     * @param uri
     * @return
     */
    public static boolean isNotExisting(String urlStr){

        int responseCode = -1;
        IOException ioe = null;
        InputStream inputStream = null;
        try{
            URL url = new URL(StringUtils.substringBefore(urlStr, "#"));
            URLConnection urlConnection = replaceURLSpaces(url).openConnection();
            inputStream = urlConnection.getInputStream();
            responseCode = ((HttpURLConnection)urlConnection).getResponseCode();
        }
        catch (IOException e){
            ioe = e;
        }
        finally{
            if (inputStream!=null){
                try{inputStream.close();}catch (IOException e){}
            }
        }

        return    ioe instanceof MalformedURLException
               || ioe instanceof UnknownHostException
               || (responseCode>=400 && responseCode<=499)
               || responseCode==501
               || responseCode==505;
    }


    /**
     *
     * @param uri
     * @param dflt
     * @return
     */
    public static String extractUrlHost(String uri){

        if (URLUtil.isURL(uri)){
            String host = "";
            URL url;
            try {
                url = new URL(StringUtils.substringBefore(uri, "#"));
                host = uri.substring(0, uri.indexOf(url.getPath()));

            } catch (Exception ex){

            }
            return host;
        }else {
            return null;
        }
    }

    /**
     *
     * @param url
     * @return
     * @throws MalformedURLException
     */
    public static URL replaceURLSpaces(URL url) throws MalformedURLException{

        return url==null ? null : new URL(StringUtils.replace(url.toString(), " ", "%20"));
    }
}
