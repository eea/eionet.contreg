package eionet.cr.harvest.util;

import com.hp.hpl.jena.rdf.arp.ALiteral;
import com.hp.hpl.jena.rdf.arp.AResource;

/**
 * 
 * @author heinljab
 *
 */
public class WrappedARPObject {
	
	/** */
	private Object wrappedObject = null;
	private String wrappedObjectType = null;
	private String stringValue = null;

	/**
	 * 
	 * @param litObject
	 */
	public WrappedARPObject(ALiteral literalObject){
		this.wrappedObject = literalObject;
		this.wrappedObjectType = ALiteral.class.getName();
		this.stringValue = literalObject.toString();
	}

	/**
	 * 
	 * @param resourceObject
	 */
	public WrappedARPObject(AResource resourceObject, String resourceID){
		this.wrappedObject = resourceObject;
		this.wrappedObjectType = AResource.class.getName();
		this.stringValue = resourceID;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isLiteral(){
		return wrappedObjectType.equals(ALiteral.class.getName());
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isAnonymous(){
		return wrappedObjectType.equals(ALiteral.class.getName()) ? false : ((AResource)wrappedObject).isAnonymous();
	}

	/**
	 * @return the stringValue
	 */
	public String getStringValue() {
		return stringValue;
	}
}
