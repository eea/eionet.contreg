package eionet.cr.harvest.util;

import java.util.ArrayList;
import java.util.List;

import eionet.cr.index.IndexException;

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
	 * @param id
	 * @throws NullPointerException if the given id is null
	 */
	public RDFResource(String id){
		if (id==null)
			throw new NullPointerException();
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
	 * @throws NullPointerException if the given property is null
	 */
	public void addProperty(RDFResourceProperty property){
		
		if (property==null)
			throw new NullPointerException();
		
		if (properties==null)
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
