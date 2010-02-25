/*
* The contents of this file are subject to the Mozilla Public
* 
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
* Agency. Portions created by Tieto Eesti are Copyright
* (C) European Environment Agency. All Rights Reserved.
* 
* Contributor(s):
* Jaanus Heinlaid, Tieto Eesti*/
package eionet.cr.dao.readers;

import java.util.Map;

import eionet.cr.dto.SubjectDTO;

public class FreeTextSearchDataReader extends SubjectDataReader {

	/** */
	private Map<Long,Long> hitSources;
	
	/**
	 * 
	 * @param subjectsMap
	 */
	public FreeTextSearchDataReader(Map<Long,SubjectDTO> subjectsMap, Map<Long,Long> hitSources){
		
		super(subjectsMap);
		this.hitSources = hitSources;
	}
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.search.util.SubjectDataReader#addNewSubject(java.lang.String, eionet.cr.dto.SubjectDTO)
	 */
	protected void addNewSubject(long subjectHash, SubjectDTO subjectDTO){
		
		super.addNewSubject(Long.valueOf(subjectHash), subjectDTO);
		
		if (hitSources!=null){
			subjectDTO.setHitSource(Long.valueOf(hitSources.get(Long.valueOf(subjectHash))));
		}
	}
}
