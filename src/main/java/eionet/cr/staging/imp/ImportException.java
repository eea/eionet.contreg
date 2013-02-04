package eionet.cr.staging.imp;

/**
 * The Class ImportException.
 *
 * @author jaanus
 */
public class ImportException extends Exception {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 6407250754180318093L;

    /**
     * Instantiates a new import exception.
     */
    public ImportException() {
        super();
    }

    /**
     * Instantiates a new import exception.
     *
     * @param message the message
     */
    public ImportException(String message) {
        super(message);
    }

    /**
     * Instantiates a new import exception.
     *
     * @param throwable the throwable
     */
    public ImportException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new import exception.
     *
     * @param message the message
     * @param throwable the throwable
     */
    public ImportException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
