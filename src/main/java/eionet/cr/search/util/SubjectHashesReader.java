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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.pagination.Pagination;
import eionet.cr.util.sql.ResultSetBaseReader;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class SubjectHashesReader extends ResultSetBaseReader{
	
	/** */
	private LinkedHashSet<String> resultSet = new LinkedHashSet<String>();
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.util.sql.ResultSetBaseReader#readRow(java.sql.ResultSet)
	 */
	public void readRow(ResultSet rs) throws SQLException{
		
		String subjectHash = rs.getString("SUBJECT_HASH");
		resultSet.add(subjectHash);
	}
	
	/**
	 * 
	 * @param pageNumber
	 * @param pageLength
	 * @return
	 */
	public LinkedHashMap<String,SubjectDTO> getResultMap(){
		
		LinkedHashMap<String, SubjectDTO> result = new LinkedHashMap<String, SubjectDTO>();
		for (Iterator<String> it=resultSet.iterator(); it.hasNext();){
			result.put(it.next(), (SubjectDTO)null);					
		}
		
		return result;
	}

	/**
	 * @return the resultMap
	 */
	public LinkedHashMap<String,SubjectDTO> getResultMap(int pageNumber, int pageLength){
		
		LinkedHashMap<String,SubjectDTO> pageMap = new LinkedHashMap<String,SubjectDTO>();
		if (resultSet.size()>0){
		
			/* calculate first and last index of the requested page */
			
			int firstIndexInclusive = 0;
			int lastIndexExclusive = resultSet.size();
			if (pageLength>0){ // if page length <=0 assume all records are wanted
			
				if (pageNumber>0){
					firstIndexInclusive = (pageNumber-1) * pageLength;
					firstIndexInclusive = Math.min(firstIndexInclusive, resultSet.size()-1);
					firstIndexInclusive = Math.max(firstIndexInclusive, 0);
					
					lastIndexExclusive = firstIndexInclusive + pageLength;
					lastIndexExclusive = Math.min(lastIndexExclusive, resultSet.size());
				}
				else{
					lastIndexExclusive = pageLength;
				}
			}
			
			/* populate page map */
			
			int i = 0;
			for (Iterator<String> it=resultSet.iterator(); it.hasNext() && i<lastIndexExclusive; i++){
				
				String subjectHash = it.next();
				if (i>=firstIndexInclusive){
					pageMap.put(subjectHash, (SubjectDTO)null);					
				}
			}
		}
		
		return pageMap;
	}

	/**
	 * 
	 * @return
	 */
	public int getResultSetSize(){
		return resultSet.size();
	}
}
