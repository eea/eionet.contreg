package eionet.cr.staging.msaccess;

/**
 *
 * @author jaanus
 *
 */
public class ConversionException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 6407250754180318093L;

    /**
     *
     */
    public ConversionException(){
        super();
    }

    /**
     *
     * @param message
     */
    public ConversionException(String message){
        super(message);
    }

    /**
     *
     * @param throwable
     */
    public ConversionException(Throwable throwable){
        super(throwable);
    }

    /**
     *
     * @param message
     * @param throwable
     */
    public ConversionException(String message, Throwable throwable){
        super(message, throwable);
    }
}
