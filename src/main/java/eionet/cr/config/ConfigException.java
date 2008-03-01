package eionet.cr.config;

import eionet.cr.util.CRRuntimeException;

/**
 * 
 * @author heinljab
 *
 */
public class ConfigException extends CRRuntimeException{

	/**
	 * 
	 */
	public ConfigException(){
		super();
	}
	
	/**
	 * 
	 * @param message
	 */
	public ConfigException(String message){
		super(message);
	}

	/**
	 * 
	 * @param message
	 * @param cause
	 */
	public ConfigException(String message, Throwable cause){
		super(message, cause);
	}
}
