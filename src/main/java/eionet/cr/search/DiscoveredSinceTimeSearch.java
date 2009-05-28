package eionet.cr.search;

import java.util.Date;
import java.util.List;

import eionet.cr.common.Predicates;
import eionet.cr.util.Hashes;
import eionet.cr.util.pagination.Pagination;

public class DiscoveredSinceTimeSearch extends AbstractSubjectSearch {
	
	/** */
	private static final int DEFAULT_LIMIT = 300;
	
	/** */
	private Date time;
	
	/**
	 * 
	 * @param time
	 */
	public DiscoveredSinceTimeSearch(Date time){
		this.time = time;
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.search.AbstractSubjectSearch#getSubjectSelectSQL(java.util.List)
	 */
	protected String getSubjectSelectSQL(List inParameters) {
		
		if (time==null)
			return null;
		
		StringBuffer sqlBuf = new StringBuffer().
		append("select SPO.SUBJECT as SUBJECT_HASH from SPO, RESOURCE").
		append(" where SPO.SUBJECT=RESOURCE.URI_HASH ").
		append(" and RESOURCE.FIRSTSEEN_TIME>?").
		append(" order by RESOURCE.FIRSTSEEN_TIME desc");
		
		inParameters.add(Long.valueOf(time.getTime()));
		
		if (pageLength>0){
			sqlBuf.append(" limit ");
			if (pageNumber>0){
				sqlBuf.append("?,");
				inParameters.add(new Integer((pageNumber-1)*pageLength));
			}
			sqlBuf.append(pageLength);
		}
		else
			sqlBuf.append(" limit ").append(DEFAULT_LIMIT);
		
		return sqlBuf.toString();
	}
}
