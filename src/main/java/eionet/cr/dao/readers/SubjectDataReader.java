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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.YesNoBoolean;
import eionet.cr.util.sql.ResultSetListReader;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class SubjectDataReader extends ResultSetListReader<SubjectDTO>{

	/** */
	private Map<Long,SubjectDTO> subjectsMap;
	
	/** */
	private SubjectDTO currentSubject = null;
	private String currentPredicate = null;
	private Collection<ObjectDTO> currentObjects = null;
	private StringBuffer predicateHashesCommaSeparated = new StringBuffer();
	
	/**
	 * 
	 * @param subjectsMap
	 */
	public SubjectDataReader(Map<Long,SubjectDTO> subjectsMap){
		this.subjectsMap = subjectsMap;
	}
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.util.sql.ResultSetBaseReader#readRow(java.sql.ResultSet)
	 */
	public void readRow(ResultSet rs) throws SQLException {
		
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
		
		addPredicateHash(rs.getString("PREDICATE_HASH"));
		
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
	protected void addPredicateHash(String predicateHash){
		
		if (predicateHashesCommaSeparated.length()>0){
			predicateHashesCommaSeparated.append(",");
		}
		predicateHashesCommaSeparated.append(predicateHash);
	}

	/**
	 * 
	 */
	public String getPredicateHashesCommaSeparated() {
		return predicateHashesCommaSeparated.toString();
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
