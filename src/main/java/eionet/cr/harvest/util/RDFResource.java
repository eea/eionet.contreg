package eionet.cr.harvest.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author heinljab
 *
 */
public class RDFResource {

	/** */
	private String id = null;
	private List<RDFResourceProperty> properties = null;

	/**
	 * 
	 * @param id
	 */
	public RDFResource(String id){
		this.id = id;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * 
	 * @param property
	 */
	public void addProperty(RDFResourceProperty property){
		if (properties!=null)
			properties = new ArrayList<RDFResourceProperty>();
		properties.add(property);
	}

	/**
	 * @return the properties
	 */
	public List<RDFResourceProperty> getProperties() {
		return properties;
	}
}
