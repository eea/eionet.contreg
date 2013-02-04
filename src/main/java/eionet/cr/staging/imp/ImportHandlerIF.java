package eionet.cr.staging.imp;


/**
 * An base interface for staging database import handlers. Contains only these methods are expected to be common for handlers.
 *
 * @author jaanus
 */
public interface ImportHandlerIF {

    /**
     * Handle end of file.
     *
     * @throws ImportException the import exception
     */
    void endOfFile() throws ImportException;

    /**
     * Close resources.
     */
    void close();
}
