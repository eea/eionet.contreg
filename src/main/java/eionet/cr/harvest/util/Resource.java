package eionet.cr.harvest.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author heinljab
 *
 */
public class Resource {

	/** */
	private String id = null;
	private List<ResourceProperty> properties = null;

	/**
	 * 
	 * @param id
	 */
	public Resource(String id){
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
	public void addProperty(ResourceProperty property){
		if (properties!=null)
			properties = new ArrayList<ResourceProperty>();
		properties.add(property);
	}

	/**
	 * @return the properties
	 */
	public List<ResourceProperty> getProperties() {
		return properties;
	}
}
