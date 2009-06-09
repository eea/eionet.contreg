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
