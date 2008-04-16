package eionet.cr.index;

import eionet.cr.common.CRException;

/**
 * 
 * @author heinljab
 *
 */
public class IndexException extends CRException{

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
