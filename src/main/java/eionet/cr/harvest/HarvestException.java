package eionet.cr.harvest;

import eionet.cr.common.CRException;

/**
 * 
 * @author heinljab
 *
 */
public class HarvestException extends CRException{

	/**
	 * 
	 */
	public HarvestException(){
		super();
	}
	
	/**
	 * 
	 * @param message
	 */
	public HarvestException(String message){
		super(message);
	}

	/**
	 * 
	 * @param message
	 * @param cause
	 */
	public HarvestException(String message, Throwable cause){
		super(message, cause);
	}
}
