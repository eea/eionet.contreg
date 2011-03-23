package eionet.cr.util;

public class ObjectLabelPair extends Pair<String, String> implements Comparable<ObjectLabelPair> {

    private static final long serialVersionUID = 8436701057926603287L;

    public ObjectLabelPair(String left, String right) {
        super(left, right);
    }

    @Override
    public int compareTo(ObjectLabelPair other) {
        return getComparableStr().compareTo(other.getComparableStr());
    }

    /**
     * Label by what the string is sorted by
     * 
     * @return
     */
    public String getComparableStr() {
        if (!URLUtil.isURL(getRight())) {
            return getRight();
        }

        return URIUtil.extractURILabel(getRight(), getRight());
    }

}
