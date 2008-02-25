package eionet.cr.config;

/**
 * 
 * @author heinljab
 *
 */
public class ConfigException extends RuntimeException{

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
