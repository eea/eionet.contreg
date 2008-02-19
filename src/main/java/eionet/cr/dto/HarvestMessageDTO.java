package eionet.cr.dto;

/**
 * 
 * @author heinljab
 *
 */
public class HarvestMessageDTO implements java.io.Serializable{

	/** */
	Integer harvestID = null;
	String type = null;
	String message = null;
	String stackTrace = null;

	/**
	 * 
	 */
	public HarvestMessageDTO(){
	}
	
	/**
	 * @return the harvestID
	 */
	public Integer getHarvestID() {
		return harvestID;
	}
	/**
	 * @param harvestID the harvestID to set
	 */
	public void setHarvestID(Integer harvestID) {
		this.harvestID = harvestID;
	}
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	/**
	 * @return the stackTrace
	 */
	public String getStackTrace() {
		return stackTrace;
	}
	/**
	 * @param stackTrace the stackTrace to set
	 */
	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}
}
