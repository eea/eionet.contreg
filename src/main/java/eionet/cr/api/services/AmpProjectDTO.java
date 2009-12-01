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
import eionet.cr.common.Subjects;
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
	
	@Element(required = false, name = "dc:identifier")
	private String identifier;
	
	@ElementList(inline = true, name = "rdf:type")
	private List<AmpProjectTypeDTO> type;

	@ElementList(inline = true, name="amp:eeaproject")
	private List<AmpEeaprojectDTO> eeaproject;

	@Element(required = false, name="amp:code")
	private String csiCode;

	@Element(required = false)
	private String mpsCode;
	
	@Element(required = false, name = "dc:title")
	private String title;
	
	@Element (required = false, name = "rdfs:label")
	private String label;

	@Element (required = false, name = "rdfs:comment")
	private String rdfsComment;

	@Element (required = false, name = "dc:description")
	private String description;
	
	@Element (required = false, name = "dc:language")
	private String language;
	
	@Element (required = false, name = "dc:date")
	private String date;
	
	@Element (required = false, name = "cr:contentLastModified")
	private String lastModifiedDate;

	@Element (required = false, name = "amp:forCode")
	private String forCode;
	
	@Element (required = false, name = "amp:forYear")
	private String forYear;

	/**
	 * 
	 */
	public AmpProjectDTO() {
		//blank
	}
	
	/**
	 * @param subject amp:Output subject
	 */
	public AmpProjectDTO(SubjectDTO subject) {
		
		// URI and dc:identifier
		uri = subject.getUri();
		identifier = subject.getObjectValue(Predicates.DC_IDENTIFIER);
		
		// rdf:type
		type = new LinkedList<AmpProjectTypeDTO>();
		Collection<ObjectDTO> types = subject.getObjects(Predicates.RDF_TYPE, ObjectDTO.Type.RESOURCE);
		if (types != null && !types.isEmpty()) {
			for(ObjectDTO temp : types){
				String value = temp.getValue();
				// skip the generic amp:Output type
				if (value!=null && !value.equals(Subjects.AMP_OUTPUT)){
					type.add(new AmpProjectTypeDTO(value));
				}
			}
		}

		// amp:eeaproject
		eeaproject = new LinkedList<AmpEeaprojectDTO>();
		Collection<ObjectDTO> eeaprojects = subject.getObjects(Predicates.AMP_ONTOLOGY_EEAPROJECT, ObjectDTO.Type.RESOURCE);
		if (eeaprojects != null && !eeaprojects.isEmpty()) {
			for(ObjectDTO temp : eeaprojects){
				eeaproject.add(new AmpEeaprojectDTO(temp.getValue()));
			}
		}
		
		// amp stuff
		csiCode = subject.getObjectValue(Predicates.AMP_ONTOLOGY_CODE);
		forCode = subject.getObjectValue(Predicates.AMP_ONTOLOGY_FORCODE);
		forYear = subject.getObjectValue(Predicates.AMP_ONTOLOGY_FORYEAR);
		
		// derive rdfs:label
		String label = URIUtil.deriveLabel(subject.getObjectValue(Predicates.RDFS_LABEL));
		this.label = StringUtils.isBlank(label) ? "No label" : label;
		
		// rdfs:comment
		rdfsComment = subject.getObjectValue(Predicates.RDFS_COMMENT);
			
		// dc:description and dc:language
		description = subject.getObjectValue(Predicates.DC_DESCRIPTION);
		language = subject.getObjectValue(Predicates.DC_LANGUAGE);
		
		// derive dc:date
		String date = subject.getObjectValue(Predicates.DC_DATE);
		if (StringUtils.isBlank(date) && subject.getFirstSeenTime() != null) {
			date = subject.getFirstSeenTime().toString();
		}
		this.date = date;
		
		// cr:contentLastModified
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

	/**
	 * @return the rdfsComment
	 */
	public String getRdfsComment() {
		return rdfsComment;
	}

	/**
	 * @param rdfsComment the rdfsComment to set
	 */
	public void setRdfsComment(String rdfsComment) {
		this.rdfsComment = rdfsComment;
	}

	/**
	 * @return the forCode
	 */
	public String getForCode() {
		return forCode;
	}

	/**
	 * @param forCode the forCode to set
	 */
	public void setForCode(String forCode) {
		this.forCode = forCode;
	}

	/**
	 * @return the forYear
	 */
	public String getForYear() {
		return forYear;
	}

	/**
	 * @param forYear the forYear to set
	 */
	public void setForYear(String forYear) {
		this.forYear = forYear;
	}

}
