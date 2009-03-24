package eionet.cr.search;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import eionet.cr.common.Predicates;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.search.util.PredicateLabels;
import eionet.cr.search.util.PredicateLabelsReader;
import eionet.cr.search.util.SubjectDataReader;
import eionet.cr.util.Hashes;
import eionet.cr.util.sql.SQLUtil;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class FactsheetSearch extends UriSearch {

	/** */
	private PredicateLabels predicateLabels = new PredicateLabels();
	
	/**
	 * 
	 * @param uri
	 */
	public FactsheetSearch(String uri) {
		super(uri);
	}

	/**
	 * 
	 */
	public PredicateLabels getPredicateLabels(){
		return predicateLabels;
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.search.AbstractSubjectSearch#collectPredicateLabels(java.sql.Connection, eionet.cr.search.util.SubjectDataReader)
	 */
	protected void collectPredicateLabels(Connection conn, SubjectDataReader subjectDataReader) throws SQLException {

		if (StringUtils.isBlank(subjectDataReader.getPredicateHashesCommaSeparated()))
			return;
		
		StringBuffer sqlBuf = new StringBuffer("select RESOURCE.URI as PREDICATE_URI, SPO.OBJECT as LABEL, SPO.OBJ_LANG as LANG").
		append(" from SPO, RESOURCE").
		append(" where SPO.SUBJECT in (").append(subjectDataReader.getPredicateHashesCommaSeparated()).append(")").
		append(" and SPO.PREDICATE=").append(Hashes.spoHash(Predicates.RDFS_LABEL)).
		append(" and SPO.LIT_OBJ='Y'").
		append(" and SPO.SUBJECT=RESOURCE.URI_HASH");
		
		SQLUtil.executeQuery(sqlBuf.toString(), new PredicateLabelsReader(predicateLabels), conn);
	}
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.search.AbstractSubjectSearch#collectPredicateParents(java.sql.Connection, eionet.cr.search.util.SubjectDataReader)
	 */
	protected void collectPredicateParents(Connection conn, SubjectDataReader subjectDataReader) throws SQLException {
		
		if (StringUtils.isBlank(subjectDataReader.getPredicateHashesCommaSeparated()))
			return;
		
		StringBuffer sqlBuf = new StringBuffer("select distinct RESOURCE.URI as SUBJECT_URI, SPO.OBJECT as PARENT").
		append(" from SPO, RESOURCE").
		append(" where SPO.SUBJECT in (").append(subjectDataReader.getPredicateHashesCommaSeparated()).append(")").
		append(" and SPO.PREDICATE=8744745537220110927").
		append(" and SPO.LIT_OBJ='Y'").
		append(" and SPO.SUBJECT=RESOURCE.URI_HASH");
		
		SQLUtil.executeQuery(sqlBuf.toString(), new PredicateLabelsReader(predicateLabels), conn);
	}
}
