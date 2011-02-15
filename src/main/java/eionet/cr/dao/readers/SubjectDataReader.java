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
package eionet.cr.dao.readers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.Hashes;
import eionet.cr.util.Util;
import eionet.cr.util.YesNoBoolean;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class SubjectDataReader extends ResultSetMixedReader<SubjectDTO>{

	/** */
	private Map<Long,SubjectDTO> subjectsMap;
	
	/** */
	private SubjectDTO currentSubject = null;
	private String currentPredicate = null;
	private Collection<ObjectDTO> currentObjects = null;
	private Collection<Long> predicateHashes = null;

	/**
	 * 
	 * @param subjectsMap
	 */
	public SubjectDataReader(Map<Long,SubjectDTO> subjectsMap){
		
		this.subjectsMap = subjectsMap;
	}
	
	/**
	 * 
	 * @param subjectUris
	 */
	public SubjectDataReader(List<String> subjectUris){
		
		subjectsMap = new LinkedHashMap<Long,SubjectDTO>();
		for (String subjectUri : subjectUris){
			Long subjectHash = Long.valueOf(Hashes.spoHash(subjectUri));
			subjectsMap.put(subjectHash, null);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.util.sql.ResultSetBaseReader#readRow(java.sql.ResultSet)
	 */
	public void readRow(ResultSet rs) throws SQLException, ResultSetReaderException {
		
		long subjectHash = rs.getLong("SUBJECT_HASH");
		boolean newSubject = currentSubject==null || subjectHash!=currentSubject.getUriHash();
		if (newSubject){
			currentSubject = new SubjectDTO(rs.getString("SUBJECT_URI"), YesNoBoolean.parse(rs.getString("ANON_SUBJ")));
			currentSubject.setUriHash(subjectHash);
			currentSubject.setLastModifiedTime(new Date(rs.getLong("SUBJECT_MODIFIED")));
			addNewSubject(subjectHash, currentSubject);
		}
		
		String predicateUri = rs.getString("PREDICATE_URI");
		boolean newPredicate = newSubject || currentPredicate==null || !currentPredicate.equals(predicateUri);
		if (newPredicate){
			currentPredicate = predicateUri;
			currentObjects = new ArrayList<ObjectDTO>();
			currentSubject.getPredicates().put(predicateUri, currentObjects);
		}
		
		addPredicateHash(rs.getLong("PREDICATE_HASH"));
		
		ObjectDTO object = new ObjectDTO(rs.getString("OBJECT"),
											rs.getString("OBJ_LANG"),
											YesNoBoolean.parse(rs.getString("LIT_OBJ")),
											YesNoBoolean.parse(rs.getString("ANON_OBJ")));
		object.setHash(rs.getLong("OBJECT_HASH"));
		object.setSourceUri(rs.getString("SOURCE_URI"));
		object.setSourceHash(rs.getLong("SOURCE"));
		object.setDerivSourceUri(rs.getString("DERIV_SOURCE_URI"));
		object.setDerivSourceHash(rs.getLong("OBJ_DERIV_SOURCE"));
		object.setSourceObjectHash(rs.getLong("OBJ_SOURCE_OBJECT"));
		
		currentObjects.add(object);
	}
	
	/**
	 * 
	 * @param subjectHash
	 * @param subjectDTO
	 */
	protected void addNewSubject(long subjectHash, SubjectDTO subjectDTO){
		subjectsMap.put(Long.valueOf(subjectHash), currentSubject);
	}
	
	/**
	 * 
	 * @return
	 */
	public void addPredicateHash(Long predicateHash){
		
		if (predicateHashes==null){
			this.predicateHashes = new ArrayList<Long>();
		}
		if(!predicateHashes.contains(predicateHash)){
			predicateHashes.add(predicateHash);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.util.sesame.SPARQLResultSetReader#readRow(org.openrdf.query.BindingSet)
	 */
	@Override
	public void readRow(BindingSet bindingSet) {
		
		Value subjectValue = bindingSet.getValue("s");
		String subjectUri = subjectValue.stringValue();
		
		boolean isAnonSubject = subjectValue instanceof BNode;
		if (isAnonSubject && blankNodeUriPrefix!=null){			
			subjectUri = blankNodeUriPrefix + subjectUri;
		}
		long subjectHash = Hashes.spoHash(subjectUri);
		
		boolean newSubject = currentSubject==null || subjectHash!=currentSubject.getUriHash();
		if (newSubject){
			currentSubject = new SubjectDTO(subjectUri, isAnonSubject);
			currentSubject.setUriHash(subjectHash);
			addNewSubject(subjectHash, currentSubject);
		}
		
		String predicateUri = bindingSet.getValue("p").stringValue();
		boolean newPredicate = newSubject || currentPredicate==null || !currentPredicate.equals(predicateUri);
		if (newPredicate){
			currentPredicate = predicateUri;
			currentObjects = new ArrayList<ObjectDTO>();
			currentSubject.getPredicates().put(predicateUri, currentObjects);
		}
		
		addPredicateHash(Long.valueOf(Hashes.spoHash(predicateUri)));
		
		Value objectValue = bindingSet.getValue("o");
		boolean isLiteral = objectValue instanceof Literal;
		String objectLang = isLiteral ? ((Literal)objectValue).getLanguage() : null;
		
		ObjectDTO object = new ObjectDTO(objectValue.stringValue(),
				objectLang==null ? "" : objectLang,
				isLiteral,
				objectValue instanceof BNode);
		
		object.setHash(Hashes.spoHash(objectValue.stringValue()));
		
		String sourceUri = bindingSet.getValue("g").stringValue();
		long sourceHash = Hashes.spoHash(sourceUri);
		
		object.setSourceUri(sourceUri);
		object.setSourceHash(sourceHash);
		object.setDerivSourceUri(sourceUri);
		object.setDerivSourceHash(sourceHash);
		
		// TODO: what about object's source object
		// object.setSourceObjectHash(rs.getLong("OBJ_SOURCE_OBJECT"));
		
		currentObjects.add(object);
	}

	/**
	 * 
	 */
	public String getPredicateHashesCommaSeparated() {
		return Util.toCSV(predicateHashes);
	}

	/** 
	 * @see eionet.cr.util.sql.ResultSetListReader#getResultList()
	 * {@inheritDoc}
	 */
	@Override
	public List<SubjectDTO> getResultList() {
		return new LinkedList<SubjectDTO>( subjectsMap.values());
	}

	/**
	 * @return the subjectsMap
	 */
	public Map<Long, SubjectDTO> getSubjectsMap() {
		return subjectsMap;
	}
}
