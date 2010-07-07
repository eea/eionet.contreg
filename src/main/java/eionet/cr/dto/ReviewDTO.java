package eionet.cr.dto;

import java.io.Serializable;

/**
 * 
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 *
 */

public class ReviewDTO  implements Serializable {
	private String title;
	private String objectUrl;
	private String reviewContent;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getObjectUrl() {
		return objectUrl;
	}
	public void setObjectUrl(String objectUrl) {
		this.objectUrl = objectUrl;
	}
	public String getReviewContent() {
		return reviewContent;
	}
	public void setReviewContent(String reviewContent) {
		this.reviewContent = reviewContent;
	}
	
}
