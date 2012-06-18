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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * File operation utilities.
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 */
public final class FileUtil {
    /** To prevent public instancing. */
    private FileUtil() {
    }

    /**
     * class internal logger.
     */
    private static Log logger = LogFactory.getLog(FileUtil.class);

    /**
     * Input stream buffer size.
     */
    private static final int INPUTSTREAM_BUFFERSIZE = 1024;

    /**
     * Downloads the contents of the given URL into the given file and closes the file.
     * 
     * @param urlString downloadable url
     * @param toFile output file location
     * @throws IOException if input of output fails
     */
    public static void downloadUrlToFile(final String urlString, final File toFile) throws IOException {

        URLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            URL url = new URL(urlString == null ? urlString : StringUtils.replace(urlString, " ", "%20"));
            urlConnection = url.openConnection();
            urlConnection.setRequestProperty("Connection", "close");
            inputStream = urlConnection.getInputStream();
            FileUtil.streamToFile(inputStream, toFile);
        } finally {
            IOUtils.closeQuietly(inputStream);
            URLUtil.disconnect(urlConnection);
        }
    }

    /**
     * Writes the content of the given InputStream into the given file and closes the file. The caller is responsible for closing
     * the InputStream!
     * 
     * @param inputStream InputSream
     * @param toFile File output file
     * @return total bytes count
     * @throws IOException if streaming fails
     */
    public static int streamToFile(final InputStream inputStream, final File toFile) throws IOException {

        FileOutputStream outputStream = null;
        try {

            String enc = "UTF-8";
            UnicodeInputStream uin = new UnicodeInputStream(inputStream, enc);
            enc = uin.getEncoding();

            int i = -1;
            int totalBytes = 0;
            byte[] bytes = new byte[INPUTSTREAM_BUFFERSIZE];
            outputStream = new FileOutputStream(toFile);
            while ((i = uin.read(bytes, 0, bytes.length)) != -1) {
                outputStream.write(bytes, 0, i);
                totalBytes = totalBytes + i;
            }

            return totalBytes;
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
    }
}
