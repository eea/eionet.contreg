package eionet.cr.util;

public class ConnectionError {

    public enum ErrType {
        PERMANENT, TEMPORARY
    };

    private ErrType type;
    private int code;
    private String message;

    public ConnectionError(ErrType type, int code, String message) {
        this.type = type;
        this.code = code;
        this.message = message;
    }

    public ErrType getType() {
        return type;
    }

    public void setType(ErrType type) {
        this.type = type;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
