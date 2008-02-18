package eionet.cr.dao;

/**
 * 
 * @author heinljab
 *
 */
public class DAOException extends Exception {

	/**
	 * 
	 */
	public DAOException(){
		super();
	}
	
	/**
	 * 
	 * @param message
	 */
	public DAOException(String message){
		super(message);
	}

	/**
	 * 
	 * @param message
	 * @param cause
	 */
	public DAOException(String message, Throwable cause){
		super(message, cause);
	}
}
