/**
 *
 */
package eionet.cr.dto;

import java.io.Serializable;

/**
 * @author altnyris
 *
 */
public class HarvestSourceDTO implements Serializable {
	private String identifier;
	private String pullUrl;
	private String type;
	private String emails;
	private String weekday;
	private int hour;
	private int period;
	
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public String getPullUrl() {
		return pullUrl;
	}
	public void setPullUrl(String pullUrl) {
		this.pullUrl = pullUrl;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getEmails() {
		return emails;
	}
	public void setEmails(String emails) {
		this.emails = emails;
	}
	public String getWeekday() {
		return weekday;
	}
	public void setWeekday(String weekday) {
		this.weekday = weekday;
	}
	public int getHour() {
		return hour;
	}
	public void setHour(int hour) {
		this.hour = hour;
	}
	public int getPeriod() {
		return period;
	}
	public void setPeriod(int period) {
		this.period = period;
	}
}
