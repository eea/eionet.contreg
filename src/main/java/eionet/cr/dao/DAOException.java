package eionet.cr.dao;

import eionet.cr.common.CRException;

/**
 * 
 * @author heinljab
 *
 */
public class DAOException extends CRException {

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
