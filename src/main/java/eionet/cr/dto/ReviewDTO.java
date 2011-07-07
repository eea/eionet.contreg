package eionet.cr.dto;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;

/**
 *
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 *
 */

public class ReviewDTO implements Serializable {

    /** */
    private String reviewSubjectUri;
    private String title;
    private String objectUrl;
    private String reviewContent;
    private int reviewID;
    private List<String> attachments;
    private String reviewContentType;

    /**
     * @return
     */
    public String getReviewSubjectUri() {
        return reviewSubjectUri;
    }

    /**
     * @param reviewSubjectUri
     */
    public void setReviewSubjectUri(String reviewSubjectUri) {

        int reviewID = -1;
        int i = reviewSubjectUri.lastIndexOf('/');
        if (i != -1) {
            try {
                reviewID = Integer.parseInt(reviewSubjectUri.substring(i + 1));
            } catch (IndexOutOfBoundsException e) {
            } catch (NumberFormatException e) {
            }
        }

        if (reviewID == -1) {
            throw new IllegalArgumentException("Malformed review URI: " + reviewSubjectUri);
        } else {
            this.reviewSubjectUri = reviewSubjectUri;
            this.reviewID = reviewID;
        }
    }

    /**
     * @return
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return
     */
    public String getObjectUrl() {
        return objectUrl;
    }

    /**
     * @return
     */
    public String getObjectUrlHTML() {
        return StringEscapeUtils.escapeHtml(objectUrl);
    }

    /**
     * @param objectUrl
     */
    public void setObjectUrl(String objectUrl) {
        this.objectUrl = objectUrl;
    }

    /**
     * @return
     */
    public String getReviewContent() {
        return reviewContent;
    }

    /**
     * @param reviewContent
     */
    public void setReviewContent(String reviewContent) {
        this.reviewContent = reviewContent;
    }

    /**
     * @return
     */
    public String getReviewSubjectHtmlFormatted() {
        return StringEscapeUtils.escapeHtml(reviewSubjectUri);
    }

    /**
     * @return
     */
    public int getReviewID() {
        return reviewID;
    }

    /**
     * @return
     */
    public List<String> getAttachments() {
        return attachments;
    }

    /**
     * @param attachments
     */
    public void setAttachments(List<String> attachments) {
        this.attachments = attachments;
    }

    /**
     * @return
     */
    public String getReviewContentType() {
        return reviewContentType;
    }

    /**
     * @param reviewContentType
     */
    public void setReviewContentType(String reviewContentType) {
        this.reviewContentType = reviewContentType;
    }

}
