package eionet.cr.harvest.statistics.dto;

import java.util.Date;

/**
 * 
 * @author <a href="mailto:jaak.kapten@tietoenator.com">Jaak Kapten</a>
 *
 */

public class HarvestUrgencyScoreDTO {
	private String url;
	private Date lastHarvest;
	private long intervalMinutes;
	private double urgency;
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Date getLastHarvest() {
		return lastHarvest;
	}
	public void setLastHarvest(Date lastHarvest) {
		this.lastHarvest = lastHarvest;
	}
	public long getIntervalMinutes() {
		return intervalMinutes;
	}
	public void setIntervalMinutes(long intervalMinutes) {
		this.intervalMinutes = intervalMinutes;
	}
	public double getUrgency() {
		return urgency;
	}
	public void setUrgency(double urgency) {
		this.urgency = urgency;
	}

}
