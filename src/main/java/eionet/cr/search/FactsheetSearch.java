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
import eionet.cr.search.util.SubProperties;
import eionet.cr.search.util.SubPropertiesReader;
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
	private SubProperties subProperties = new SubProperties();
	
	/**
	 * 
	 * @param uri
	 */
	public FactsheetSearch(String uri) {
		super(uri);
	}

	/**
	 * 
	 * @param uriHash
	 */
	public FactsheetSearch(long uriHash) {
		super(Long.valueOf(uriHash));
	}

	/**
	 * 
	 * @return
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
	protected void collectSubProperties(Connection conn, SubjectDataReader subjectDataReader) throws SQLException {
		
		if (StringUtils.isBlank(subjectDataReader.getPredicateHashesCommaSeparated()))
			return;
		
		StringBuffer sqlBuf = new StringBuffer("select distinct SPO.OBJECT as PREDICATE, RESOURCE.URI as SUB_PROPERTY").
		append(" from SPO, RESOURCE").
		append(" where SPO.OBJECT_HASH in (").append(subjectDataReader.getPredicateHashesCommaSeparated()).append(")").
		append(" and SPO.PREDICATE=").append(Hashes.spoHash(Predicates.RDFS_SUBPROPERTY_OF)).
		append(" and SPO.LIT_OBJ='N' and SPO.ANON_OBJ='N'").
		append(" and SPO.SUBJECT=RESOURCE.URI_HASH");
		
		SQLUtil.executeQuery(sqlBuf.toString(), new SubPropertiesReader(subProperties), conn);
	}

	/**
	 * @return the subProperties
	 */
	public SubProperties getSubProperties() {
		return subProperties;
	}
}
