package eionet.cr.search.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

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
		
		resultSet.add(rs.getString("SUBJECT_HASH"));
	}

	/**
	 * @return the resultMap
	 */
	public LinkedHashMap<String,SubjectDTO> getPageMap(int pageNumber, int pageLength){
		
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
	public int getTotalResultCount(){
		return resultSet.size();
	}
}
