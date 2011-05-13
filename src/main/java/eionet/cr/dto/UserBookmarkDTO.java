package eionet.cr.dto;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * 
 * @author jaanus
 *
 */
public class UserBookmarkDTO{

    /** */
    private String bookmarkUrl;

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
}
