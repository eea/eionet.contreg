package eionet.cr.search.util;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class UriLabelPair implements Comparable{

	/** */
	private String uri;
	private String label;
	
	/**
	 * 
	 * @param uri
	 * @param label
	 * @return
	 */
	public static UriLabelPair create(String uri, String label){
		
		UriLabelPair pair = new UriLabelPair();
		pair.setUri(uri);
		pair.setLabel(label);
		return pair;
	}
	
	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}
	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		
		UriLabelPair otherOption = (UriLabelPair)o; // this can be done even if o==null and it will throw ClassCastException if (o istanceof OptionDTO)==false
		if (otherOption==null)
			return 1;
		else if (label==null)
			return otherOption.getLabel()==null ? 0 : -1;
		else
			return label.compareTo(otherOption.getLabel());
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o){
		return compareTo(o)==0;
	}

	/**
	 * @param uri the uri to set
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return new StringBuffer().append(uri).append(" [").append(label).append("]").toString();
	}
}
