package eionet.cr.filestore;

import java.io.IOException;

/**
 *
 * @author jaanus
 *
 */
public class FileAlreadyExistsException extends IOException {

    /**
     *
     */
    public FileAlreadyExistsException() {
        super();
    }

    /**
     * @param message
     */
    public FileAlreadyExistsException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public FileAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
