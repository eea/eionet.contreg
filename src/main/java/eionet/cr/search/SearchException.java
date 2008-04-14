package eionet.cr.search;

import eionet.cr.util.CRException;

/**
 * 
 * @author heinljab
 *
 */
public class SearchException extends CRException{

	/**
	 * 
	 */
	public SearchException(){
		super();
	}
	
	/**
	 * 
	 * @param message
	 */
	public SearchException(String message){
		super(message);
	}

	/**
	 * 
	 * @param message
	 * @param cause
	 */
	public SearchException(String message, Throwable cause){
		super(message, cause);
	}

}
