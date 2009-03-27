package eionet.cr.search;

import java.util.List;

import eionet.cr.util.Hashes;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class SourceContentsSearch extends AbstractSubjectSearch {
	
	/** */
	private String sourceUrl;
	private long sourceUrlHash;

	/**
	 * 
	 * @param sourceUrl
	 */
	public SourceContentsSearch(String sourceUrl){
		
		this.sourceUrl = sourceUrl;
		if (this.sourceUrl!=null)
			this.sourceUrlHash = Hashes.spoHash(this.sourceUrl);
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.search.AbstractSubjectSearch#getSubjectSelectSQL(java.util.List)
	 */
	protected String getSubjectSelectSQL(List inParameters){
		
		if (this.sourceUrl==null)
			return null;
		
		StringBuffer sqlBuf = new StringBuffer("select sql_calc_found_rows distinct SPO.SUBJECT as SUBJECT_HASH from SPO ");
		if (sortPredicate!=null){
			sqlBuf.append("left join SPO as ORDERING on (SPO.SUBJECT=ORDERING.SUBJECT and ORDERING.PREDICATE=?) ");
			inParameters.add(Long.valueOf(Hashes.spoHash(sortPredicate)));
		}
		sqlBuf.append(" where SPO.SOURCE=?");
		inParameters.add(Long.valueOf(sourceUrlHash));
		
		if (sortPredicate!=null)
			sqlBuf.append(" order by ORDERING.OBJECT ").append(sortOrder==null ? sortOrder.ASCENDING.toSQL() : sortOrder.toSQL());

		if (pageLength>0){
			sqlBuf.append(" limit ");
			if (pageNumber>0){
				sqlBuf.append("?,");
				inParameters.add(new Integer((pageNumber-1)*pageLength));
			}
			sqlBuf.append(pageLength);
		}
		
		return sqlBuf.toString();
	}
}
