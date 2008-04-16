package eionet.cr.common;

/**
 * @author altnyris
 *
 */
public class CRException extends Exception {
	
	public CRException() {
    }

	/**
	 * @param msg the detail message.
	 */
    public CRException(String msg) {
        super(msg);
    }
    
    /**
	 * 
	 * @param message
	 * @param cause
	 */
	public CRException(String msg, Throwable cause){
		super(msg, cause);
	}

}
