package eionet.cr.harvest;

import eionet.cr.common.CRRuntimeException;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class LoadException extends CRRuntimeException{

	/**
	 * 
	 */
	public LoadException(){
		super();
	}
	
	/**
	 * 
	 * @param message
	 */
	public LoadException(String message){
		super(message);
	}

	/**
	 * 
	 * @param message
	 * @param cause
	 */
	public LoadException(String message, Throwable cause){
		super(message, cause);
	}
}
