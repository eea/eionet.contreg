package eionet.cr.harvest;

/**
 * 
 * @author heinljab
 *
 */
public class HarvestException extends Exception{

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
