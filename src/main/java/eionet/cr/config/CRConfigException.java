package eionet.cr.config;

import eionet.cr.util.CRRuntimeException;

/**
 * 
 * @author heinljab
 *
 */
public class CRConfigException extends CRRuntimeException{

	/**
	 * 
	 */
	public CRConfigException(){
		super();
	}
	
	/**
	 * 
	 * @param message
	 */
	public CRConfigException(String message){
		super(message);
	}

	/**
	 * 
	 * @param message
	 * @param cause
	 */
	public CRConfigException(String message, Throwable cause){
		super(message, cause);
	}
}
