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
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        jaanus
 */

package eionet.cr.staging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;


import eionet.cr.common.CRRuntimeException;
import eionet.cr.config.GeneralConfig;
import eionet.cr.util.URLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A thread that downloads from a given URL a file that will be available for creating staging databases.
 *
 * @author jaanus
 */
public class FileDownloader extends Thread {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(FileDownloader.class);

    /** */
    public static final File FILES_DIR = getFilesDir();

    /** */
    public static final String FILE_SUFFIX = ".downloading";

    /** */
    private String url;

    /** */
    private String newFileName;

    /**
     * Constructs a new instance of this class, that will be used to download from the given URL. The downloaded file will be
     * renamed to the one given in the method's second input, unless it is null or blank. The file will be saved into
     * {@link #FILES_DIR}.
     *
     * @param url The given URL.
     * @param newFileName The name that the file will be renamed to.
     */
    public FileDownloader(String url, String newFileName) {
        super();
        this.url = url;
        this.newFileName = newFileName;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        try {
            File file = execute();
            // Remove the SUFFIX from the file name, now that it's downloaded.
            file.renameTo(new File(FILES_DIR, StringUtils.substringBeforeLast(file.getName(), FILE_SUFFIX)));
        } catch (IOException e) {
            LOGGER.error("Failed to download from the given URL: " + url, e);
        }
    }

    /**
     * The thread's execution body called by {@link #run()}.
     *
     * @return The downloaded file's location.
     * @throws IOException In case any sort of IO error happened.
     *
     */
    private File execute() throws IOException {

        URLConnection connection = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            connection = new URL(url).openConnection();
            String fileName = ensureUniqueFileName(getFileName(connection)) + FILE_SUFFIX;
            File file = new File(FILES_DIR, fileName);
            outputStream = new FileOutputStream(file);
            inputStream = connection.getInputStream();
            IOUtils.copy(inputStream, outputStream);
            return file;
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
            URLUtil.disconnect(connection);
        }
    }

    /**
     * Derives a name for the file to be downloaded from the given {@link URLConnection}.
     *
     * @param connection The given {@link URLConnection}.
     * @return The derived file name.
     */
    private String getFileName(URLConnection connection) {

        // If file name already given, just return it.
        if (StringUtils.isNotBlank(newFileName)) {
            return newFileName;
        }

        // Attempt detection from the response's "Content-Disposition" header.
        String contentDisposition = connection.getHeaderField("Content-Disposition");
        if (StringUtils.isNotBlank(contentDisposition)) {
            String s = StringUtils.substringAfter(contentDisposition, "filename");
            if (StringUtils.isNotBlank(s)) {
                s = StringUtils.substringAfter(s, "=");
                if (StringUtils.isNotBlank(s)) {
                    s = StringUtils.substringAfter(s, "\"");
                    if (StringUtils.isNotBlank(s)) {
                        s = StringUtils.substringBefore(s, "\"");
                        if (StringUtils.isNotBlank(s)) {
                            return s.trim();
                        }
                    }
                }
            }
        }

        // Attempt detection from the response's "Content-Location" header.
        String contentLocation = connection.getHeaderField("Content-Location");
        if (StringUtils.isNotBlank(contentLocation)) {
            String s = new File(contentLocation).getName();
            if (StringUtils.isNotBlank(s)) {
                return s.trim();
            }
        }

        // Attempt detection from the URL itself.

        String s = StringUtils.substringAfterLast(connection.getURL().toString(), "#");
        if (StringUtils.isBlank(s)) {
            s = StringUtils.substringAfterLast(connection.getURL().toString(), "/");
        }

        if (StringUtils.isNotBlank(s)) {
            // Remove all characters that are not one of these: a latin letter, a digit, a minus, a dot, an underscore.
            s = s.replaceAll("[^a-zA-Z0-9-._]+", "");
        }

        // If still no success, then just generate a hash from the URL.
        return StringUtils.isBlank(s) ? DigestUtils.md5Hex(connection.getURL().toString()) : s;
    }

    /**
     * Returns a form of the given file name that will surely be unique in the folder where the files are downloaded to.
     *
     * @param fileName The file name for which the unique form is returned.
     * @return The unique file name.
     */
    public static String ensureUniqueFileName(String fileName) {

        if (!fileExists(fileName)) {
            return fileName;
        }

        for (int i = 1; i <= 100; i++) {
            String newFileName = fileName + "." + i;
            if (!fileExists(newFileName)) {
                return newFileName;
            }
        }

        throw new CRRuntimeException("Unable generate unique file name for [" + fileName + "] in " + FILES_DIR);
    }

    /**
     * Returns true if the given file already exists in the folder where the files are downloaded to. Takes into account the file
     * suffixes added for uniqueness!
     *
     * @param fileName The file name to check.
     * @return As indicated above.
     */
    private static boolean fileExists(String fileName) {
        return new File(FILES_DIR, fileName).exists() || new File(FILES_DIR, fileName + FILE_SUFFIX).exists();
    }

    /**
     * Returns java.io.File pointing to the directory where the available files should be kept in.
     * @return The java.io.File.
     */
    static final File getFilesDir() {

        File file = new File(GeneralConfig.getRequiredProperty(GeneralConfig.STAGING_FILES_DIR));
        if (!file.exists() || !file.isDirectory()) {
            String userHome = System.getProperty("user.home");
            if (StringUtils.isNotBlank(userHome)) {
                file = new File(userHome);
                if (!file.exists() || !file.isDirectory()) {
                    file = new File(".");
                }
            }
        }

        return file;
    }
}
