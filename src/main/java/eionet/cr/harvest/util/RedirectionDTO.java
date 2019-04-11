package eionet.cr.harvest.util;

/**
 * A simple POJO representing a redirection:
 * from where to where and what's the redirection code (ie HTTP response code.)
 */
public class RedirectionDTO {

    private String fromUrl;
    private String toUrl;
    private int code;

    public RedirectionDTO(String fromUrl, String toUrl, int code) {
        this.fromUrl = fromUrl;
        this.toUrl = toUrl;
        this.code = code;
    }

    public String getFromUrl() {
        return fromUrl;
    }

    public void setFromUrl(String fromUrl) {
        this.fromUrl = fromUrl;
    }

    public String getToUrl() {
        return toUrl;
    }

    public void setToUrl(String toUrl) {
        this.toUrl = toUrl;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return String.format("%s -> (%d) %s", fromUrl, code, toUrl);
    }
}