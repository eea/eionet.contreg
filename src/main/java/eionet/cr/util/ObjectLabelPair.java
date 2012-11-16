package eionet.cr.util;

/**
 * Helper class for displaying objects in sorted order in picklists etc.
 *
 * @author kaido
 *
 */
public class ObjectLabelPair extends Pair<String, String> implements Comparable<ObjectLabelPair> {

    /** */
    private static final long serialVersionUID = 8436701057926603287L;

    /**
     * overrides superclass constructor.
     *
     * @param left object key
     * @param right label
     */
    public ObjectLabelPair(final String left, final String right) {
        super(left, right);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(final ObjectLabelPair other) {
        return getRight().compareTo(other.getRight());
    }

}
