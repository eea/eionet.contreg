/*
 * The contents of this file are subjectUri to the Mozilla Public
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

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class RawTripleDTO {

	/** */
	private String subjectUri;
	private String predicateUri;
	private String object;
	private String objectDerivSourceUri;

	/** */
	private long subjectHash;
	private long predicateHash;
	private long objectHash;
	private long objectDerivSourceHash;
	
	/**
	 * @return the subjectUri
	 */
	public String getSubjectUri() {
		return subjectUri;
	}
	/**
	 * @param subjectUri the subjectUri to set
	 */
	public void setSubjectUri(String subject) {
		this.subjectUri = subject;
	}
	/**
	 * @return the predicateUri
	 */
	public String getPredicateUri() {
		return predicateUri;
	}
	/**
	 * @param predicateUri the predicateUri to set
	 */
	public void setPredicateUri(String predicate) {
		this.predicateUri = predicate;
	}
	/**
	 * @return the object
	 */
	public String getObject() {
		return object;
	}
	/**
	 * @param object the object to set
	 */
	public void setObject(String object) {
		this.object = object;
	}

	/**
	 * @return the objectDerivSourceUri
	 */
	public String getObjectDerivSourceUri() {
		return objectDerivSourceUri;
	}

	/**
	 * @param objectDerivSourceUri the objectDerivSourceUri to set
	 */
	public void setObjectDerivSourceUri(String objectDerivSource) {
		this.objectDerivSourceUri = objectDerivSource;
	}
	/**
	 * @return the subjectHash
	 */
	public long getSubjectHash() {
		return subjectHash;
	}
	/**
	 * @param subjectHash the subjectHash to set
	 */
	public void setSubjectHash(long subjectHash) {
		this.subjectHash = subjectHash;
	}
	/**
	 * @return the predicateHash
	 */
	public long getPredicateHash() {
		return predicateHash;
	}
	/**
	 * @param predicateHash the predicateHash to set
	 */
	public void setPredicateHash(long predicateHash) {
		this.predicateHash = predicateHash;
	}
	/**
	 * @return the objectHash
	 */
	public long getObjectHash() {
		return objectHash;
	}
	/**
	 * @param objectHash the objectHash to set
	 */
	public void setObjectHash(long objectHash) {
		this.objectHash = objectHash;
	}
	/**
	 * @return the objectDerivSourceHash
	 */
	public long getObjectDerivSourceHash() {
		return objectDerivSourceHash;
	}
	/**
	 * @param objectDerivSourceHash the objectDerivSourceHash to set
	 */
	public void setObjectDerivSourceHash(long objectDerivSourceHash) {
		this.objectDerivSourceHash = objectDerivSourceHash;
	}
}
