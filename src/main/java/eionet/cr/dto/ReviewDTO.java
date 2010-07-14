package eionet.cr.dto;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * 
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 *
 */

public class ReviewDTO  implements Serializable {
	
	private String reviewSubjectUri;
	private String title;
	private String objectUrl;
	private String reviewContent;
	private int reviewID;
	private List<String> attachments;
	
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
	public String getReviewSubjectUri() {
		return reviewSubjectUri;
	}
	public void setReviewSubjectUri(String reviewSubjectUri) {
		this.reviewSubjectUri = reviewSubjectUri;
	}
	
	public String getReviewSubjectHtmlFormatted() {
		return StringEscapeUtils.escapeHtml(reviewSubjectUri);
	}
	public int getReviewID() {
		return reviewID;
	}
	public void setReviewID(int reviewID) {
		this.reviewID = reviewID;
	}
	public List<String> getAttachments() {
		return attachments;
	}
	public void setAttachments(List<String> attachments) {
		this.attachments = attachments;
	}
	
}
