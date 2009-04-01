/**
 *
 */
package eionet.cr.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * @author altnyris
 *
 */
public class HarvestSourceDTO implements Serializable {
	
	/** */
	public static final int COUNT_UNAVAIL_THRESHOLD = 5;
	public static final int DEFAULT_REFERRALS_INTERVAL = 60480;
	
	/** */
	private Integer sourceId;
	private String name;
	private String url;
	private String type;
	private String emails;
	private Date timeCreated;
	private String creator;
	private Integer statements;
	private Integer resources;
	private Integer countUnavail;
	private Date lastHarvest;
	private Integer intervalMinutes;
	
	/**
	 * 
	 */
	public HarvestSourceDTO(){
	}

	/**
	 * @return the sourceId
	 */
	public Integer getSourceId() {
		return sourceId;
	}

	/**
	 * @param sourceId the sourceId to set
	 */
	public void setSourceId(Integer sourceId) {
		this.sourceId = sourceId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
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
	 * @return the emails
	 */
	public String getEmails() {
		return emails;
	}

	/**
	 * @param emails the emails to set
	 */
	public void setEmails(String emails) {
		this.emails = emails;
	}

	/**
	 * @return the timeCreated
	 */
	public Date getTimeCreated() {
		return timeCreated;
	}

	/**
	 * @param timeCreated the timeCreated to set
	 */
	public void setTimeCreated(Date timeCreated) {
		this.timeCreated = timeCreated;
	}

	/**
	 * @return the creator
	 */
	public String getCreator() {
		return creator;
	}

	/**
	 * @param creator the creator to set
	 */
	public void setCreator(String creator) {
		this.creator = creator;
	}

	/**
	 * @return the statements
	 */
	public Integer getStatements() {
		return statements;
	}

	/**
	 * @param statements the statements to set
	 */
	public void setStatements(Integer statements) {
		this.statements = statements;
	}

	/**
	 * @return the resources
	 */
	public Integer getResources() {
		return resources;
	}

	/**
	 * @param resources the resources to set
	 */
	public void setResources(Integer resources) {
		this.resources = resources;
	}

	/**
	 * @return the countUnavail
	 */
	public Integer getCountUnavail() {
		return countUnavail;
	}

	/**
	 * @param countUnavail the countUnavail to set
	 */
	public void setCountUnavail(Integer countUnavail) {
		this.countUnavail = countUnavail;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isUnavailable(){
		
		return countUnavail!=null && countUnavail.intValue()>=COUNT_UNAVAIL_THRESHOLD;
	}

	/**
	 */
	public Integer getIntervalMinutes() {
		return intervalMinutes;
	}

	/**
	 */
	public void setIntervalMinutes(Integer intervalMinutes) {
		this.intervalMinutes = intervalMinutes;
	}

	/**
	 * @return the lastHarvest
	 */
	public Date getLastHarvest() {
		return lastHarvest;
	}

	/**
	 * @param lastHarvest the lastHarvest to set
	 */
	public void setLastHarvest(Date lastHarvest) {
		this.lastHarvest = lastHarvest;
	}
}
