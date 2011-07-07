package eionet.cr.dto;

public class UserHistoryDTO {

    String url;
    String lastOperationTime;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLastOperation() {
        return lastOperationTime;
    }

    public void setLastOperation(String lastOperation) {
        this.lastOperationTime = lastOperation;
    }

}
