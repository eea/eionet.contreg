package eionet.cr.web.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class ResourcePropertyDTO implements Comparable{

	/** */
	private String uri;
	private String label;
	private List<ResourcePropertyValueDTO> values;

	/**
	 * 
	 * @param valueDTO
	 */
	public void addValue(ResourcePropertyValueDTO valueDTO){
		
		if (valueDTO==null)
			return;
		
		if (values==null)
			values = new ArrayList<ResourcePropertyValueDTO>();
		values.add(valueDTO);

	}
	
	/**
	 * @return the propertyLabel
	 */
	public String getLabel() {
		return label;
	}
	/**
	 * @param propertyLabel the propertyLabel to set
	 */
	public void setLabel(String propertyLabel) {
		this.label = propertyLabel;
	}
	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		return label.compareTo(((ResourcePropertyDTO)o).getLabel());
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
		
		HashMap map = new HashMap();
		map.put("uri", uri);
		map.put("label", label);
		map.put("values", values);
		return map.toString();
	}
	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}
	/**
	 * @param uri the uri to set
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

	/**
	 * @return the values
	 */
	public List<ResourcePropertyValueDTO> getValues() {
		return values;
	}
}
