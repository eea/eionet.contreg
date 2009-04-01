package eionet.cr.dto;

import java.io.Serializable;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class UrgentHarvestQueueItemDTO implements Serializable{
	
	/** */
	private String url;
	private java.util.Date timeAdded;
	private String pushedContent;

	/**
	 * 
	 */
	public UrgentHarvestQueueItemDTO(){
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
	 * @return the timeAdded
	 */
	public java.util.Date getTimeAdded() {
		return timeAdded;
	}

	/**
	 * @param timeAdded the timeAdded to set
	 */
	public void setTimeAdded(java.util.Date timeAdded) {
		this.timeAdded = timeAdded;
	}

	/**
	 * @return the pushedContent
	 */
	public String getPushedContent() {
		return pushedContent;
	}

	/**
	 * @param pushedContent the pushedContent to set
	 */
	public void setPushedContent(String pushedContent) {
		this.pushedContent = pushedContent;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isPushHarvest(){
		return pushedContent!=null;
	}
}
