package eionet.cr.common;

/**
 * 
 * @author <a href="mailto:jaak.kapten@tietoenator.com">Jaak Kapten</a>
 * 
 */

public class AccessException extends Exception {

    public AccessException() {

    }

    /**
     * 
     * @param message
     * @param cause
     */
    public AccessException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
