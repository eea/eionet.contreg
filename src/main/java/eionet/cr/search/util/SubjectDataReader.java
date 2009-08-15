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
package eionet.cr.search.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.YesNoBoolean;
import eionet.cr.util.sql.ResultSetBaseReader;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class SubjectDataReader extends ResultSetBaseReader{

	/** */
	private Map<String,SubjectDTO> subjectsMap;
	
	/** */
	private SubjectDTO currentSubject = null;
	private String currentPredicate = null;
	private Collection<ObjectDTO> currentObjects = null;
	private StringBuffer predicateHashesCommaSeparated = new StringBuffer();
	
	/**
	 * 
	 * @param subjectsMap
	 */
	public SubjectDataReader(Map<String,SubjectDTO> subjectsMap){
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
			addNewSubject(String.valueOf(subjectHash), currentSubject);
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
		object.setDerivSourceUri(rs.getString("DERIV_SOURCE_URI"));
		object.setSourceObjectHash(rs.getLong("OBJ_SOURCE_OBJECT"));
		
		currentObjects.add(object);
	}
	
	/**
	 * 
	 * @param subjectHash
	 * @param subjectDTO
	 */
	protected void addNewSubject(String subjectHash, SubjectDTO subjectDTO){
		subjectsMap.put(subjectHash, currentSubject);
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
}
