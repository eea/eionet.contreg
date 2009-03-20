package eionet.cr.search;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import eionet.cr.common.Predicates;
import eionet.cr.util.Hashes;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class RecentUploadsSearch extends AbstractSubjectSearch {
	
	/** */
	private String subjectType;
	private int maxResults;
	
	/**
	 * 
	 * @param subjectType
	 */
	public RecentUploadsSearch(String subjectType, int maxResults){
		this.subjectType = subjectType;
		this.maxResults = maxResults;
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.search.AbstractSubjectSearch#getSubjectSelectSQL(java.util.List)
	 */
	protected String getSubjectSelectSQL(List inParameters) {
		
		if (StringUtils.isBlank(subjectType))
			return null;
		
		StringBuffer sqlBuf = new StringBuffer("select sql_calc_found_rows distinct SPO.SUBJECT from SPO, RESOURCE").
		append(" where SPO.PREDICATE=").append(Hashes.spoHash(Predicates.RDF_TYPE)).
		append(" and SPO.OBJECT_HASH=?").
		append(" and SPO.SUBJECT=RESOURCE.URI_HASH").
		append(" order by RESOURCE.FIRSTSEEN_TIME desc").
		append(" limit ").append(maxResults);
		
		inParameters.add(Long.valueOf(Hashes.spoHash(subjectType)));
		
		return sqlBuf.toString();
	}

}
