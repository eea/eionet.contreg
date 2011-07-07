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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eionet.cr.config.GeneralConfig;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class URLUtil {

    /** */
    private static final Logger LOGGER = Logger.getLogger(URLUtil.class);

    /**
     *
     * @param s
     * @return boolean
     */
    public static boolean isURL(String s) {

        if (s == null || s.trim().length() == 0)
            return false;

        try {
            URL url = new URL(s);
            return url != null;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    /**
     * Create the User-agent header value from config file.
     *
     * @return the User-agent value.
     */
    public static String userAgentHeader() {

        String ua = GeneralConfig.getRequiredProperty(GeneralConfig.APPLICATION_USERAGENT);
        Object[] args = new String[1];
        args[0] = GeneralConfig.getRequiredProperty(GeneralConfig.APPLICATION_VERSION);
        return MessageFormat.format(ua, args);
    }

    /**
     * Connect to the URL and check if it is modified since the timestamp argument. Returns null, if it cannot be determined for
     * sure.
     *
     * @param urlString
     * @param timestamp
     * @return true if it is modified.
     */
    public static Boolean isModifiedSince(String urlString, long timestamp) {

        if (!URLUtil.isURL(urlString)) {
            return Boolean.FALSE;
        }

        if (timestamp == 0) {
            return Boolean.TRUE;
        }

        URLConnection urlConnection = null;
        try {
            URL url = new URL(StringUtils.substringBefore(urlString, "#"));
            urlConnection = replaceURLSpaces(url).openConnection();
            urlConnection.setRequestProperty("Connection", "close");
            urlConnection.setRequestProperty("User-Agent", userAgentHeader());
            urlConnection.setIfModifiedSince(timestamp);

            int responseCode = ((HttpURLConnection) urlConnection).getResponseCode();
            System.out.println(responseCode);
            if (responseCode == HttpURLConnection.HTTP_NOT_MODIFIED) {
                return Boolean.FALSE;
            } else if (responseCode == HttpURLConnection.HTTP_OK) {
                return Boolean.TRUE;
            } else {
                // Return null to indicate if it's unclear whether the source has
                // been modified or not.
                return null;
            }
        } catch (IOException ioe) {
            return null;
        } finally {
            URLUtil.disconnect(urlConnection);
        }
    }

    /**
     * Connect to the URL and check if it exists at the remote end. Local ids are removed (the part after the '#') before
     * connecting.
     *
     * @param urlStr
     *            the URL to check.
     * @return true is the URL does <b>NOT</b> exist.
     */
    public static boolean isNotExisting(String urlStr) {

        int responseCode = -1;
        IOException ioe = null;
        URLConnection urlConnection = null;
        try {
            URL url = new URL(StringUtils.substringBefore(urlStr, "#"));
            urlConnection = replaceURLSpaces(url).openConnection();
            urlConnection.setRequestProperty("Connection", "close");
            responseCode = ((HttpURLConnection) urlConnection).getResponseCode();
        } catch (IOException e) {
            ioe = e;
        } finally {
            URLUtil.disconnect(urlConnection);
        }

        return ioe instanceof MalformedURLException || ioe instanceof UnknownHostException
        || (responseCode >= 400 && responseCode <= 499) || responseCode == HttpURLConnection.HTTP_NOT_IMPLEMENTED
        || responseCode == HttpURLConnection.HTTP_VERSION;
    }

    /**
     * A cross-the-stream-to-get-water replacement for {@link java.net.URL#getHost()}.
     *
     * @param uri
     * @param dflt
     * @return String
     */
    public static String extractUrlHost(String uri) {

        if (URLUtil.isURL(uri)) {
            String host = "";
            URL url;
            try {
                url = new URL(StringUtils.substringBefore(uri, "#"));
                host = uri.substring(0, uri.indexOf(url.getPath()));

            } catch (Exception ex) {

            }
            return host;
        } else {
            return null;
        }
    }

    /**
     *
     * @param url
     * @return URL
     * @throws MalformedURLException
     */
    public static URL replaceURLSpaces(URL url) throws MalformedURLException {

        return url == null ? null : new URL(replaceURLSpaces(url.toString()));
    }

    /**
     *
     * @param url
     * @return URL
     * @throws MalformedURLException
     */
    public static String replaceURLSpaces(String url) throws MalformedURLException {

        return url == null ? null : StringUtils.replace(url.toString(), " ", "%20");
    }

    /**
     *
     * @param urlConnection
     */
    public static void disconnect(URLConnection urlConnection) {

        if (urlConnection != null && urlConnection instanceof HttpURLConnection) {
            try {
                ((HttpURLConnection) urlConnection).disconnect();
            } catch (Exception e) {
                LOGGER.warn("Error when disconnection from " + urlConnection.getURL() + ": " + e.toString());
            }
        }
    }
}
