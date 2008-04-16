package eionet.cr.web.util.display;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class ResourcePropertyDTO implements Comparable{

	/** */
	private String propertyLabel;
	private String valueLabel;
	private String valueUrl;
	/**
	 * @return the propertyLabel
	 */
	public String getPropertyLabel() {
		return propertyLabel;
	}
	/**
	 * @param propertyLabel the propertyLabel to set
	 */
	public void setPropertyLabel(String propertyLabel) {
		this.propertyLabel = propertyLabel;
	}
	/**
	 * @return the valueLabel
	 */
	public String getValueLabel() {
		return valueLabel;
	}
	/**
	 * @param valueLabel the valueLabel to set
	 */
	public void setValueLabel(String valueLabel) {
		this.valueLabel = valueLabel;
	}
	/**
	 * @return the valueUrl
	 */
	public String getValueUrl() {
		return valueUrl;
	}
	/**
	 * @param valueUrl the valueUrl to set
	 */
	public void setValueUrl(String valueUrl) {
		this.valueUrl = valueUrl;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		return propertyLabel.compareTo(((ResourcePropertyDTO)o).getPropertyLabel());
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o){
		return compareTo(o)==0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		StringBuffer buf = new StringBuffer(propertyLabel);
		return buf.append("|").append(valueUrl).append("|").append(valueLabel).toString();
	}
}
