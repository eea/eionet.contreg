package eionet.cr.dto;

/**
 * 
 * @author heinljab
 *
 */
public class HarvestDTO implements java.io.Serializable{
	
	private Integer harvestId;
	private Integer harvestSourceId;
	private String harvestType;
	private String user;
	private String status;
	private java.util.Date datetimeStarted;
	private java.util.Date datetimeFinished;
	private Integer totalResources;
	private Integer encodingSchemes;
	private Integer totalStatements;
	private Integer litObjStatements;
	private String messages;

	/**
	 * 
	 */
	public HarvestDTO(){
	}

	/**
	 * @return the harvestId
	 */
	public Integer getHarvestId() {
		return harvestId;
	}

	/**
	 * @param harvestId the harvestId to set
	 */
	public void setHarvestId(Integer harvestId) {
		this.harvestId = harvestId;
	}

	/**
	 * @return the harvestSourceId
	 */
	public Integer getHarvestSourceId() {
		return harvestSourceId;
	}

	/**
	 * @param harvestSourceId the harvestSourceId to set
	 */
	public void setHarvestSourceId(Integer harvestSourceId) {
		this.harvestSourceId = harvestSourceId;
	}

	/**
	 * @return the harvestType
	 */
	public String getHarvestType() {
		return harvestType;
	}

	/**
	 * @param harvestType the harvestType to set
	 */
	public void setHarvestType(String harvestType) {
		this.harvestType = harvestType;
	}

	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the datetimeStarted
	 */
	public java.util.Date getDatetimeStarted() {
		return datetimeStarted;
	}

	/**
	 * @param datetimeStarted the datetimeStarted to set
	 */
	public void setDatetimeStarted(java.util.Date datetimeStarted) {
		this.datetimeStarted = datetimeStarted;
	}

	/**
	 * @return the datetimeFinished
	 */
	public java.util.Date getDatetimeFinished() {
		return datetimeFinished;
	}

	/**
	 * @param datetimeFinished the datetimeFinished to set
	 */
	public void setDatetimeFinished(java.util.Date datetimeFinished) {
		this.datetimeFinished = datetimeFinished;
	}

	/**
	 * @return the totalStatements
	 */
	public Integer getTotalStatements() {
		return totalStatements;
	}

	/**
	 * @param totalStatements the totalStatements to set
	 */
	public void setTotalStatements(Integer totalStatements) {
		this.totalStatements = totalStatements;
	}

	/**
	 * @return the litObjStatements
	 */
	public Integer getLitObjStatements() {
		return litObjStatements;
	}

	/**
	 * @param litObjStatements the litObjStatements to set
	 */
	public void setLitObjStatements(Integer litObjStatements) {
		this.litObjStatements = litObjStatements;
	}

	/**
	 * @return the totalResources
	 */
	public Integer getTotalResources() {
		return totalResources;
	}

	/**
	 * @param totalResources the totalResources to set
	 */
	public void setTotalResources(Integer totalResources) {
		this.totalResources = totalResources;
	}

	/**
	 * @return the encodingSchemes
	 */
	public Integer getEncodingSchemes() {
		return encodingSchemes;
	}

	/**
	 * @param encodingSchemes the encodingSchemes to set
	 */
	public void setEncodingSchemes(Integer encodingSchemes) {
		this.encodingSchemes = encodingSchemes;
	}

	/**
	 * @return the messages
	 */
	public String getMessages() {
		return messages;
	}

	/**
	 * @param messages the messages to set
	 */
	public void setMessages(String messages) {
		this.messages = messages;
	}

}
