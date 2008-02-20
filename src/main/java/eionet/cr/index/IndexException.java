package eionet.cr.index;

/**
 * 
 * @author heinljab
 *
 */
public class IndexException extends Exception{

	/**
	 * 
	 */
	public IndexException(){
		super();
	}
	
	/**
	 * 
	 * @param message
	 */
	public IndexException(String message){
		super(message);
	}

	/**
	 * 
	 * @param message
	 * @param cause
	 */
	public IndexException(String message, Throwable cause){
		super(message, cause);
	}

}
