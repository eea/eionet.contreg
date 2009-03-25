package eionet.cr.search.util;

import java.util.Date;
import java.util.Map;

import eionet.cr.dto.SubjectDTO;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class RecentUploadsDataReader extends SubjectDataReader {

	/** */
	private Map<String,Date> firstSeenTimes;
	
	/**
	 * 
	 * @param subjectsMap
	 */
	public RecentUploadsDataReader(Map<String,SubjectDTO> subjectsMap, Map<String,Date> firstSeenTimes){
		
		super(subjectsMap);
		
		if (firstSeenTimes==null)
			throw new IllegalArgumentException();
		this.firstSeenTimes = firstSeenTimes;
	}
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.search.util.SubjectDataReader#addNewSubject(java.lang.String, eionet.cr.dto.SubjectDTO)
	 */
	protected void addNewSubject(String subjectHash, SubjectDTO subjectDTO){
		
		super.addNewSubject(subjectHash, subjectDTO);
		subjectDTO.setFirstSeenTime(firstSeenTimes.get(subjectHash));
	}
}
