package eionet.cr.web.sparqlClient.helpers;

/**
 * A result-value object for any subject/predicate/object to be used in CR's SPARQL endpoint query results.
 *
 * @author jaanus
 */
public class ResultValue {

    /** The string-value as it came from triple-store. */
    private String value;

    /** True if the value is to be treated as a literal. */
    private boolean isLiteral;

    /** True, if the value is to be treated as an anonymous resource. */
    private boolean isAnonymous;

    /**
     * Simple constructor for the {@link #value} and {@link #isLiteral()} fields.
     *
     * @param value
     * @param isLiteral
     */
    public ResultValue(String value, boolean isLiteral) {

        this.value = value;
        this.isLiteral = isLiteral;
    }

    /**
     * Simple constructor for the {@link #value}, {@link #isLiteral()} and {@link #isAnonymous} fields.
     *
     * @param value
     * @param isLiteral
     * @param isAnonymous
     */
    public ResultValue(String value, boolean isLiteral, boolean isAnonymous) {

        this.value = value;
        this.isLiteral = isLiteral;
        this.isAnonymous = isAnonymous;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return value;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @return the isLiteral
     */
    public boolean isLiteral() {
        return isLiteral;
    }

    /**
     * @return the isAnonymous
     */
    public boolean isAnonymous() {
        return isAnonymous;
    }
}
