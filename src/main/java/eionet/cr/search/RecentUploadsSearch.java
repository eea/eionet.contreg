package eionet.cr.search;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import eionet.cr.common.Predicates;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.search.util.RecentUploadsDataReader;
import eionet.cr.search.util.RecentUploadsHashesReader;
import eionet.cr.search.util.SubjectDataReader;
import eionet.cr.search.util.SubjectHashesReader;
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
	private Map<String,Date> firstSeenTimes = new HashMap<String,Date>();
	
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
		
		StringBuffer sqlBuf = new StringBuffer().
		append("select sql_calc_found_rows distinct SPO.SUBJECT as SUBJECT_HASH, RESOURCE.FIRSTSEEN_TIME as FIRSTSEEN_TIME from SPO, RESOURCE").
		append(" where SPO.PREDICATE=").append(Hashes.spoHash(Predicates.RDF_TYPE)).
		append(" and SPO.OBJECT_HASH=?").
		append(" and SPO.SUBJECT=RESOURCE.URI_HASH").
		append(" order by RESOURCE.FIRSTSEEN_TIME desc").
		append(" limit ").append(maxResults);
		
		inParameters.add(Long.valueOf(Hashes.spoHash(subjectType)));
		
		return sqlBuf.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.search.AbstractSubjectSearch#createSubjectHashesReader()
	 */
	protected SubjectHashesReader createSubjectHashesReader(){
		return new RecentUploadsHashesReader(firstSeenTimes);
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.search.AbstractSubjectSearch#createSubjectDataReader(java.util.Map)
	 */
	protected SubjectDataReader createSubjectDataReader(Map<String, SubjectDTO> subjectsMap){
		return new RecentUploadsDataReader(subjectsMap, firstSeenTimes);
	}
}
