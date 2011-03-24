package eionet.cr.util;

public class ObjectLabelPair extends Pair<String, String> implements Comparable<ObjectLabelPair> {

    private static final long serialVersionUID = 8436701057926603287L;
    
    private String comparableLabel;

    public ObjectLabelPair(String left, String right) {
        super(left, right);
        
        if (!URIUtil.isSchemedURI(right)) {
            this.comparableLabel = right;
        }
        else {
            this.comparableLabel = URIUtil.extractURILabel(right, right);
        }
        
    }

    @Override
    public int compareTo(ObjectLabelPair other) {
        //return getRight().compareTo(other.getRight());
        return comparableLabel.compareTo(other.getComparableLabel());
    }

    public String getComparableLabel() {
        return comparableLabel;
    }
  
}
