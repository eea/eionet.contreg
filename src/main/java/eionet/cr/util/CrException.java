package eionet.cr.util;

/**
 * @author altnyris
 *
 */
public class CrException extends Exception {
	
	public CrException() {
    }

	/**
	 * @param msg the detail message.
	 */
    public CrException(String msg) {
        super(msg);
    }
    
    /**
	 * 
	 * @param message
	 * @param cause
	 */
	public CrException(String msg, Throwable cause){
		super(msg, cause);
	}

}
