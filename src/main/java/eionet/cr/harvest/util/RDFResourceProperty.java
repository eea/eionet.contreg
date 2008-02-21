package eionet.cr.harvest.util;

import eionet.cr.util.Util;

/**
 * 
 * @author heinljab
 *
 */
public class RDFResourceProperty {

	/** */
	private String id = null;
	private String value = null;
	
	/** */
	private boolean isLiteral = false;
	private boolean isAnonymous = false;
	
	/** */
	private boolean isValueURL = false;
	
	/**
	 * 
	 * @param id
	 * @param value
	 * @throws NullPointerException if the given id or value is null
	 */
	public RDFResourceProperty(String id, String value, boolean isLiteral, boolean isAnonymous){
		
		if (id==null || value==null)
			throw new NullPointerException();
		
		this.id = id;
		this.value = value;
		this.isLiteral = isLiteral;
		this.isAnonymous = isAnonymous;
		this.isValueURL = Util.isURL(value);
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @return the isLiteral
	 */
	public boolean isLiteral() {
		return isLiteral;
	}

	/**
	 * @return the isAnonymous
	 */
	public boolean isAnonymous() {
		return isAnonymous;
	}

	/**
	 * @return the isValueURL
	 */
	public boolean isValueURL() {
		return isValueURL;
	}
}
