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
package eionet.cr.api.services;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import eionet.cr.common.Predicates;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.URIUtil;

/**
 * Used for serialization of subjectDTO (amp:Output type) to xml.
 * 
 * @author Aleksandr Ivanov
 * <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
@Root(name = "rdf:Description")
public class AmpProjectDTO implements Serializable {
	
	/**
	 * serial.
	 */
	private static final long serialVersionUID = 1L;
	
	@Attribute(name = "rdf:about", required = false)
	private String uri;
	@Element(required = false)
	private String identifier;
	@ElementList(name = "rdf:type", inline = true)
	private List<AmpProjectTypeDTO> type;
	@Element(required = false, name="code")
	private String csiCode;
	@Element(required = false)
	private String mpsCode;
	@Element(required = false)
	private String title;
	@Element (required = false)
	private String label;
	@Element (required = false)
	private String description;
	@Element (required = false)
	private String language;
	@Element (name = "startingDate", required = false)
	private String date;
	@Element (required = false)
	private String lastModifiedDate;

	public AmpProjectDTO() {
		//blank
	}
	
	/**
	 * @param subject amp:Output subject
	 */
	public AmpProjectDTO(SubjectDTO subject) {
		uri = subject.getUri();
		identifier = subject.getObjectValue(Predicates.DC_IDENTIFIER);
		Collection<ObjectDTO> types = subject.getObjects(Predicates.RDF_TYPE, ObjectDTO.Type.RESOURCE);
		if (types != null && !types.isEmpty()) {
			type = new LinkedList<AmpProjectTypeDTO>();
			for(ObjectDTO temp : types) {
				type.add(new AmpProjectTypeDTO(temp.getValue()));
			}
		}
		
		csiCode = subject.getObjectValue(Predicates.AMP_ONTOLOGY_CODE);
		title = subject.getObjectValue(Predicates.DC_TITLE);
		
		//derive label
		String label = URIUtil.deriveLabel(subject.getObjectValue(Predicates.RDFS_LABEL));
		this.label = StringUtils.isBlank(label) ? "No label" : label;
		
		description = subject.getObjectValue(Predicates.DC_DESCRIPTION);
		language = subject.getObjectValue(Predicates.DC_LANGUAGE);
		
		//derive date
		String date = subject.getObjectValue(Predicates.DC_DATE);
		if (StringUtils.isBlank(date) && subject.getFirstSeenTime() != null) {
			date = subject.getFirstSeenTime().toString();
		}
		this.date = date;
		lastModifiedDate = subject.getObjectValue(Predicates.CR_LAST_MODIFIED);
	}
	/**
	 * @return the id
	 */
	public String getIdentifier() {
		return identifier;
	}
	/**
	 * @param id the id to set
	 */
	public void setIdentifier(String id) {
		this.identifier = id;
	}

	/**
	 * @return the code
	 */
	public String getCsiCode() {
		return csiCode;
	}
	/**
	 * @param code the code to set
	 */
	public void setCsiCode(String code) {
		this.csiCode = code;
	}
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the language
	 */
	public String getLanguage() {
		return language;
	}
	/**
	 * @param language the language to set
	 */
	public void setLanguage(String language) {
		this.language = language;
	}
	/**
	 * @return the lastModifiedDate
	 */
	public String getLastModifiedDate() {
		return lastModifiedDate;
	}
	/**
	 * @param lastModifiedDate the lastModifiedDate to set
	 */
	public void setLastModifiedDate(String lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}
	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}
	/**
	 * @param uri the uri to set
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

	/**
	 * @return the mpsCode
	 */
	public String getMpsCode() {
		return mpsCode;
	}

	/**
	 * @param mpsCode the mpsCode to set
	 */
	public void setMpsCode(String mpsCode) {
		this.mpsCode = mpsCode;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the date
	 */
	public String getDate() {
		return date;
	}

	/**
	 * @param date the date to set
	 */
	public void setDate(String date) {
		this.date = date;
	}

}
