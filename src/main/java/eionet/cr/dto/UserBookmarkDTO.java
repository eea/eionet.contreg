package eionet.cr.dto;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author jaanus
 *
 */
public class UserBookmarkDTO {

    /** Bookmark subject uri. */
    private String uri;

    /** Bookmark's target url, when type is bookmark. */
    private String bookmarkUrl;

    /** Label. */
    private String bookmarkLabel;

    /** Bookmark's query, when type is SPARQL bookmark. */
    private String query;

    /** Type uri. */
    private String type;

    /** Type label. */
    private String typeLabel;

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
     * @return the query
     */
    public String getQuery() {
        return query;
    }

    /**
     * @param query the query to set
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the typeLabel
     */
    public String getTypeLabel() {
        return typeLabel;
    }

    /**
     * @param typeLabel the typeLabel to set
     */
    public void setTypeLabel(String typeLabel) {
        this.typeLabel = typeLabel;
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * @param uri the uri to set
     */
    public void setUri(String uri) {
        this.uri = uri;
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
