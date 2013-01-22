package eionet.cr.staging.imp;

/**
 *
 * @author jaanus
 *
 */
public class ImportException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 6407250754180318093L;

    /**
     *
     */
    public ImportException(){
        super();
    }

    /**
     *
     * @param message
     */
    public ImportException(String message){
        super(message);
    }

    /**
     *
     * @param throwable
     */
    public ImportException(Throwable throwable){
        super(throwable);
    }

    /**
     *
     * @param message
     * @param throwable
     */
    public ImportException(String message, Throwable throwable){
        super(message, throwable);
    }
}
