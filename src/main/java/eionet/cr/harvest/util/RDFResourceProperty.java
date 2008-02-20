package eionet.cr.harvest.util;

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
	
	/**
	 * 
	 * @param id
	 * @param value
	 */
	public RDFResourceProperty(String id, String value, boolean isAnonymous, boolean isLiteral){
		this.id = id;
		this.value = value;
		this.isLiteral = isLiteral;
		this.isAnonymous = isAnonymous;
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
}
