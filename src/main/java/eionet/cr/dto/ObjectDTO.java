/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.cr.dto;

import eionet.cr.util.Hashes;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class ObjectDTO {
	
	/** */
	public enum Type{LITERAL, RESOURCE;}

	/** */
	private String value;
	private String language;
	private boolean literal;
	private boolean anonymous;
	private String source;
	private String derivSource;
	private String sourceObject;
	
	/**
	 * 
	 * @param value
	 * @param language
	 * @param literal
	 * @param anonymous
	 */
	public ObjectDTO(String value, String language, boolean literal, boolean anonymous){
		
		this.value = value;
		this.language = language;
		this.literal = literal;
		this.anonymous = anonymous;
	}
	
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	/**
	 * @return the language
	 */
	public String getLanguage() {
		return language;
	}
	/**
	 * @return the literal
	 */
	public boolean isLiteral() {
		return literal;
	}
	/**
	 * @return the anonymous
	 */
	public boolean isAnonymous() {
		return anonymous;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return getValue();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other){
		
		if (this==other)
			return true;
		
		if (!(other instanceof ObjectDTO))
			return false;
		
		
		String otherValue = ((ObjectDTO)other).getValue();
		return getValue()==null ? otherValue==null : getValue().equals(otherValue);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode(){
		return getValue()==null ? 0 : getValue().hashCode();
	}

	/**
	 * 
	 * @return
	 */
	public String getValueHash(){
		return getValue()==null ? null : String.valueOf(Hashes.spoHash(getValue()));
	}

	/**
	 * @return the derivSource
	 */
	public String getDerivSource() {
		return derivSource;
	}

	/**
	 * @param derivSource the derivSource to set
	 */
	public void setDerivSource(String derivSource) {
		this.derivSource = derivSource;
	}

	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * 
	 * @return
	 */
	public String getSourceSmart() {
		if (derivSource!=null && derivSource.trim().length()>0)
			return derivSource;
		else if (source!=null && source.trim().length()>0)
			return source;
		else
			return null;
	}

	/**
	 * @return the sourceObject
	 */
	public String getSourceObject() {
		return sourceObject;
	}

	/**
	 * @param sourceObject the sourceObject to set
	 */
	public void setSourceObject(String derivObject) {
		this.sourceObject = derivObject;
	}
	
	/**
	 * 
	 * @return
	 */
	public long getSourceObjectLong(){
		return this.sourceObject==null ? 0 : Long.parseLong(this.sourceObject);
	}
}
