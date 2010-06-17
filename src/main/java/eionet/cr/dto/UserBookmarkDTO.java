package eionet.cr.dto;

import java.io.Serializable;

import org.apache.commons.lang.StringEscapeUtils;

public class UserBookmarkDTO implements Serializable {

	private String bookmarkUrl;

	public String getBookmarkUrl() {
		return bookmarkUrl;
	}

	public void setBookmarkUrl(String bookmarkUrl) {
		this.bookmarkUrl = bookmarkUrl;
	}

	public String getBookmarkUrlHtmlFormatted() {
		return StringEscapeUtils.escapeHtml(bookmarkUrl);
	}
	
	
	
}
