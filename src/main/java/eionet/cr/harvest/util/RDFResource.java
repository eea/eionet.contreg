package eionet.cr.harvest.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import eionet.cr.index.IndexException;
import eionet.cr.util.Identifiers;

/**
 * 
 * @author heinljab
 *
 */
public class RDFResource {

	/** */
	private String id = null;
	private String sourceId = null;
	
	/** */
	private List<RDFResourceProperty> properties = null;
	private HashSet<String> distinctPropertyIds = null;
	private int countLiteralProperties = 0;
	
	/** */
	private String firstSeenTimestamp = null;

	/**
	 * @param id
	 * @throws NullPointerException if the given id is null
	 */
	public RDFResource(String id, String sourceId){
		if (id==null || sourceId==null)
			throw new NullPointerException();
		this.id = id;
		this.sourceId = sourceId;
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
			distinctPropertyIds.contains(Identifiers.RDFS_LABEL);
		//FIXME distinctPropertyIds.contains(Identifiers.RDF_TYPE) &&
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isSubPropertyOf(){
		return distinctPropertyIds!=null && distinctPropertyIds.contains(Identifiers.RDFS_SUB_PROPERTY_OF);
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

	/**
	 * @return the sourceId
	 */
	public String getSourceId() {
		return sourceId;
	}

	/**
	 * 
	 * @return
	 */
	public String getFirstSeenTimestamp() {
		return firstSeenTimestamp;
	}

	/**
	 * 
	 * @param firstSeenTime
	 */
	public void setFirstSeenTimestamp(String firstSeenTime) {
		this.firstSeenTimestamp = firstSeenTime;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		
		StringBuffer buf = new StringBuffer(id==null ? "null" : id);
		if (properties!=null && !properties.isEmpty())
			buf.append(", ").append(properties.toString());
		return buf.toString();
	}
}
