package eionet.cr.util.sql;

/**
 * 
 * @author heinljab
 *
 */
public class DataSourceException extends Exception {

	/**
	 * 
	 */
	public DataSourceException(){
		super();
	}
	
	/**
	 * 
	 * @param message
	 */
	public DataSourceException(String message){
		super(message);
	}

	/**
	 * 
	 * @param message
	 * @param cause
	 */
	public DataSourceException(String message, Throwable cause){
		super(message, cause);
	}
}
