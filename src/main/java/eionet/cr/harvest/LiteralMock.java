package eionet.cr.harvest;

import com.hp.hpl.jena.rdf.arp.ALiteral;
import com.hp.hpl.jena.rdf.arp.AResource;

/**
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class LiteralMock implements ALiteral{
	
	/** */
	AResource resource = null;
	String resourceID = null;
	
	/**
	 * 
	 * @param object
	 */
	public LiteralMock(AResource resource, String resourceID){
		this.resource = resource;
		this.resourceID = resourceID;
	}

	/**
	 * 
	 */
	public boolean isWellFormedXML() {
		throw new RuntimeException("Unimplemented method");
	}

	/**
	 * 
	 */
	public String getParseType() {
		throw new RuntimeException("Unimplemented method");
	}

	/**
	 * 
	 */
	public String getDatatypeURI() {
		throw new RuntimeException("Unimplemented method");
	}

	/**
	 * 
	 */
	public String getLang() {
		throw new RuntimeException("Unimplemented method");
	}
	
	/**
	 * 
	 * @return
	 */
	public AResource getResource(){
		return resource;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return resourceID;
	}
}