package eionet.cr.harvest;

import java.net.MalformedURLException;
import java.net.URL;

import eionet.cr.util.Util;

/**
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class DocumentAttribute {

	/** */
	public String name;
	public String value;
	public boolean isLiteral = true;
	public boolean isAnonymous = false;
	
	/**
	 * 
	 * @param name
	 * @param value
	 */
	public DocumentAttribute(String name, String value){
		this.name = name;
		this.value = value;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isValueURL(){
		
		if (value==null || value.trim().length()==0)
			return false;
		else
			return Util.isUrl(value);
	}
	
	/*
	 *  (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return (new StringBuffer(name)).append("=").append(value).toString();
	}
}
