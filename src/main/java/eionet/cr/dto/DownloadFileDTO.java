package eionet.cr.dto;

import java.io.InputStream;

public class DownloadFileDTO {

    private String contentType;
    private InputStream inputStream;
    private boolean fileFound;

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        fileFound = true;
        this.contentType = contentType;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public boolean isFileFound() {
        return fileFound;
    }

    public void setFileFound(boolean fileFound) {
        this.fileFound = fileFound;
    }

}
