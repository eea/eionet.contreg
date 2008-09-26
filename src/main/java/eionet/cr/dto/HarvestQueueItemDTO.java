package eionet.cr.dto;

import java.io.Serializable;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class HarvestQueueItemDTO implements Serializable{
	
	/** */
	private String url;
	private String priority;
	private java.util.Date timeAdded;
	private String pushedContent;

	/**
	 * 
	 */
	public HarvestQueueItemDTO(){
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
	 * @return the priority
	 */
	public String getPriority() {
		return priority;
	}

	/**
	 * @param priority the priority to set
	 */
	public void setPriority(String priority) {
		this.priority = priority;
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
		return pushedContent!=null && pushedContent.length()>0;
	}
}
