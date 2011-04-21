package eionet.cr.dao.readers;

import eionet.cr.common.CRException;

/**
 *
 * @author jaanus
 *
 */
public class ResultSetReaderException extends CRException {

    /**
     *
     */
    public ResultSetReaderException() {
        super();
    }

    /**
     *
     * @param message
     */
    public ResultSetReaderException(String message) {
        super(message);
    }

    /**
     *
     * @param message
     * @param cause
     */
    public ResultSetReaderException(String message, Throwable cause) {
        super(message, cause);
    }
}
