package eionet.cr.harvest.util;

import java.util.ArrayList;
import java.util.HashSet;
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
	private HashSet<String> distinctPropertyIds = null;
	private int countLiteralProperties = 0;

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
		addDistinctPropertyId(property.getId());
		if (property.isLiteral())
			countLiteralProperties++;
	}

	/**
	 * @return the properties
	 */
	public List<RDFResourceProperty> getProperties() {
		return properties;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isEncodingScheme(){
		
		return distinctPropertyIds!=null &&
			distinctPropertyIds.contains("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") &&
			distinctPropertyIds.contains("http://www.w3.org/2000/01/rdf-schema#label");
	}
	
	/**
	 * 
	 * @param id
	 */
	private void addDistinctPropertyId(String id){
		
		if (distinctPropertyIds==null)
			distinctPropertyIds = new HashSet<String>();
		distinctPropertyIds.add(id);
	}

	/**
	 * @return the countLitProperties
	 */
	public int getCountLiteralProperties() {
		return countLiteralProperties;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getCountTotalProperties(){
		return properties==null ? 0 : properties.size();
	}
}
