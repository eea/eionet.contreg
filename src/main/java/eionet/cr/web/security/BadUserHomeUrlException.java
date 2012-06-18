package eionet.cr.web.security;

/**
 * 
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 * 
 */

public class BadUserHomeUrlException extends Exception {

    public BadUserHomeUrlException(String message) {
        super(message);
    }

    public BadUserHomeUrlException() {
        super();
    }

}
