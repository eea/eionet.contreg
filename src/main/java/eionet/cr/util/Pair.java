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
 * Aleksandr Ivanov, Tieto Eesti
 */
package eionet.cr.util;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author Aleksandr Ivanov
 * <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public class Pair<T,T1> implements Serializable {

	/**
	 * serial.
	 */
	private static final long serialVersionUID = 1L;
	
	
	private T id;
	private T1 value;

	/**
	 * @param id
	 * @param value
	 */
	public Pair(T id, T1 value) {
		this.id = id;
		this.value = value;
	}
	
	/**
	 * @return the id
	 */
	public T getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(T id) {
		this.id = id;
	}
	/**
	 * @return the value
	 */
	public T1 getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(T1 value) {
		this.value = value;
	}

	/** 
	 * @see java.lang.Object#hashCode()
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	/** 
	 * @see java.lang.Object#equals(java.lang.Object)
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}
	
	

}
