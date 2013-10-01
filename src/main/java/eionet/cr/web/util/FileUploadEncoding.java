package eionet.cr.web.util;

import org.apache.log4j.Logger;

/**
 *
 * Container of all supported encodings for uploading files
 *
 * @author Jaak
 */
public final class FileUploadEncoding extends OrderedProperties {

    /**
     * File containing mimetypes in properties format.
     */
    private static final String PROPERTIES_FILE = "fileUploadEncodings.properties";

    /** */
    private static final Logger LOGGER = Logger.getLogger(FileUploadEncoding.class);


    private FileUploadEncoding() {
        super(PROPERTIES_FILE, LOGGER);
    }

    /**
     * Public accessor for the singleton instance.
     *
     * @return The singleton instance.
     */
    public static FileUploadEncoding getInstance() {
        return FileUploadEncodingHolder.INSTANCE;
    }

    /**
     * {@link FileUploadEncoding} is loaded on the first execution of {@link FileUploadEncoding#getInstance()}.
     * or the first access to {@link #INSTANCE}, not before.
     *
     * See singleton pattern by Bill Hugh at http://en.wikipedia.org/wiki/Singleton_pattern#The_solution_of_Bill_Pugh.
     */
    private static class FileUploadEncodingHolder {
        public static final FileUploadEncoding INSTANCE = new FileUploadEncoding();
    }

}
