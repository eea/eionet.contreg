package eionet.cr.dto;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author jaanus
 *
 */
public class UserBookmarkDTO {

    /** */
    private String bookmarkUrl;

    /** */
    private String bookmarkLabel;

    /**
     *
     * @return
     */
    public String getBookmarkUrl() {
        return bookmarkUrl;
    }

    /**
     *
     * @param bookmarkUrl
     */
    public void setBookmarkUrl(String bookmarkUrl) {
        this.bookmarkUrl = bookmarkUrl;
    }

    /**
     *
     * @return
     */
    public String getBookmarkUrlHtmlFormatted() {
        return StringEscapeUtils.escapeHtml(bookmarkUrl);
    }

    public String getBookmarkLabel() {
        return bookmarkLabel;
    }

    public void setBookmarkLabel(String bookmarkLabel) {
        this.bookmarkLabel = bookmarkLabel;
    }

    /**
     *
     * @return
     */
    public String getBookmarkTitle() {
        String ret = StringEscapeUtils.escapeXml(getBookmarkLabel());
        if (StringUtils.isBlank(ret)) {
            ret = getBookmarkUrl();
        }
        return ret;
    }
}
