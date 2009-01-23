package eionet.cr.search.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.PredicateDTO;
import eionet.cr.dto.SubjectDTOTemp;
import eionet.cr.util.YesNoBoolean;
import eionet.cr.util.sql.ResultSetBaseReader;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class SubjectsDataReader extends ResultSetBaseReader{

	/** */
	private LinkedHashMap<String,SubjectDTOTemp> subjectsMap;
	
	/** */
	private SubjectDTOTemp currentSubject = null;
	private String currentPredicate = null;
	private Collection<ObjectDTO> currentObjects = null;
	
	/**
	 * 
	 * @param subjectsMap
	 */
	public SubjectsDataReader(LinkedHashMap<String,SubjectDTOTemp> subjectsMap){
		this.subjectsMap = subjectsMap;
	}
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.util.sql.ResultSetBaseReader#readRow(java.sql.ResultSet)
	 */
	public void readRow(ResultSet rs) throws SQLException {
		
		String subjectUri = rs.getString("SUBJECT_URI");
		boolean newSubject = currentSubject==null || !currentSubject.getUri().equals(subjectUri);
		if (newSubject){
			String subjectHash = rs.getString("SUBJECT");
			currentSubject = new SubjectDTOTemp(subjectUri, YesNoBoolean.parse(rs.getString("ANON_SUBJ")));
			subjectsMap.put(subjectHash, currentSubject);
		}

		String predicateUri = rs.getString("PREDICATE_URI");
		boolean newPredicate = newSubject || currentPredicate==null || !currentPredicate.equals(predicateUri);
		if (newPredicate){
			currentPredicate = predicateUri;
			currentObjects = new ArrayList<ObjectDTO>();
			currentSubject.put(new PredicateDTO(predicateUri), currentObjects);
		}
		
		ObjectDTO object = new ObjectDTO(rs.getString("OBJECT"),
											rs.getString("OBJ_LANG"),
											YesNoBoolean.parse(rs.getString("LIT_OBJ")),
											YesNoBoolean.parse(rs.getString("ANON_OBJ")));
		object.setDerivSource(rs.getString("OBJ_DERIV_SOURCE"));
		
		currentObjects.add(object);
	}
}
