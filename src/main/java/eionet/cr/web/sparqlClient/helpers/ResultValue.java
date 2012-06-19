package eionet.cr.web.sparqlClient.helpers;

public class ResultValue {

    /** */
    private String value;
    private boolean isLiteral;

    /**
     *
     * @param value
     * @param isLiteral
     */
    public ResultValue(String value, boolean isLiteral) {

        this.value = value;
        this.isLiteral = isLiteral;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return the isLiteral
     */
    public boolean isLiteral() {
        return isLiteral;
    }

    /**
     * @param isLiteral the isLiteral to set
     */
    public void setLiteral(boolean isLiteral) {
        this.isLiteral = isLiteral;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return value;
    }
}
