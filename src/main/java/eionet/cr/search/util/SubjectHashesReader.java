package eionet.cr.search.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;

import eionet.cr.dto.RDFSubject;
import eionet.cr.util.sql.ResultSetBaseReader;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class SubjectHashesReader extends ResultSetBaseReader{
	
	/** */
	private LinkedHashMap<String,RDFSubject> resultMap = new LinkedHashMap<String,RDFSubject>();
	private StringBuffer subjectHashesCommaSeparated = new StringBuffer();
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.util.sql.ResultSetBaseReader#readRow(java.sql.ResultSet)
	 */
	public void readRow(ResultSet rs) throws SQLException{
		
		String subjectHash = rs.getString(1);
		if (subjectHashesCommaSeparated.length()>0){
			subjectHashesCommaSeparated.append(",");
		}
		subjectHashesCommaSeparated.append(subjectHash);
		resultMap.put(subjectHash, null);
	}

	/**
	 * @return the resultMap
	 */
	public LinkedHashMap<String,RDFSubject> getResultMap(){
		return resultMap;
	}

	/**
	 * @return the subjectHashesCommaSeparated
	 */
	public String getSubjectHashesCommaSeparated() {
		return subjectHashesCommaSeparated.toString();
	}

	/**
	 * 
	 * @return
	 */
	public int getResultCount(){
		return resultMap.size();
	}
}
