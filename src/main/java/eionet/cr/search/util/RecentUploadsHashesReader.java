package eionet.cr.search.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

public class RecentUploadsHashesReader extends SubjectHashesReader {

	/** */
	private Map<String,Date> firstSeenTimes;

	/**
	 * 
	 * @param firstSeenTimes
	 */
	public RecentUploadsHashesReader(Map<String,Date> firstSeenTimes){
		
		super();
		
		if (firstSeenTimes==null)
			throw new IllegalArgumentException();
		this.firstSeenTimes = firstSeenTimes;
	}
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.search.util.SubjectHashesReader#readRow(java.sql.ResultSet)
	 */
	public void readRow(ResultSet rs) throws SQLException{
		super.readRow(rs);
		firstSeenTimes.put(rs.getString("SUBJECT_HASH"), new Date(rs.getLong("FIRSTSEEN_TIME")));
	}
}
