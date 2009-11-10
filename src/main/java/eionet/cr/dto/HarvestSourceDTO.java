/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti
 */
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
	
	/**
	 * serial.
	 */
	private static final long serialVersionUID = 1L;
	/** */
	public static final int COUNT_UNAVAIL_THRESHOLD = 5;
	public static final int DEFAULT_REFERRALS_INTERVAL = 60480;
	
	/** */
	private Integer sourceId;
	private String url;
	private String emails;
	private boolean trackedFile;
	private Date timeCreated;
	private Integer statements;
	private Integer resources;
	private Integer countUnavail;
	private Date lastHarvest;
	private boolean lastHarvestFailed;
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
	
	/**
	 * @return the trackedFile
	 */
	public boolean isTrackedFile() {
		return trackedFile;
	}

	/**
	 * @param trackedFile the trackedFile to set
	 */
	public void setTrackedFile(boolean trackedFile) {
		this.trackedFile = trackedFile;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return new StringBuffer().append("Harvest source ").append(url).toString();
	}

	/**
	 * @return the lastHarvestFailed
	 */
	public boolean isLastHarvestFailed() {
		return lastHarvestFailed;
	}

	/**
	 * @param lastHarvestFailed the lastHarvestFailed to set
	 */
	public void setLastHarvestFailed(boolean lastHarvestFailed) {
		this.lastHarvestFailed = lastHarvestFailed;
	}

	
}
