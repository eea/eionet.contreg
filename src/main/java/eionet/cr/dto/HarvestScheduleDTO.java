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
public class HarvestScheduleDTO implements Serializable {
	private Integer harvestSourceId;
	private String weekday;
	private Integer hour;
	private Integer period;
	private Date nextHarvest;
	
	/**
	 * 
	 */
	public HarvestScheduleDTO(){
	}
	
	public Integer getHarvestSourceId() {
		return harvestSourceId;
	}

	public void setHarvestSourceId(Integer harvestSourceId) {
		this.harvestSourceId = harvestSourceId;
	}

	public String getWeekday() {
		return weekday;
	}

	public void setWeekday(String weekday) {
		this.weekday = weekday;
	}

	public Integer getHour() {
		return hour;
	}

	public void setHour(Integer hour) {
		this.hour = hour;
	}

	public Integer getPeriod() {
		return period;
	}

	public void setPeriod(Integer period) {
		this.period = period;
	}

	public Date getNextHarvest() {
		return nextHarvest;
	}

	public void setNextHarvest(Date nextHarvest) {
		this.nextHarvest = nextHarvest;
	}
	
}
