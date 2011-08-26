/**
 * 
 */
package eionet.cr.dto;

import java.io.Serializable;

/**
 * @author Risto Alt
 *
 */
public class DocumentationDTO implements Serializable {

    /**
     * serial
     */
    private static final long serialVersionUID = 1L;

    private String pageId;
    private String contentType;
    private byte[] content;

    public String getPageId() {
        return pageId;
    }
    public void setPageId(String pageId) {
        this.pageId = pageId;
    }
    public String getContentType() {
        return contentType;
    }
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    public byte[] getContent() {
        return content;
    }
    public void setContent(byte[] content) {
        this.content = content;
    }

}
