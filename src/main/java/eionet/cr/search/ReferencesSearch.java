/*
* The contents of this file are subject to the Mozilla Public
* 
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
* Agency. Portions created by Tieto Eesti are Copyright
* (C) European Environment Agency. All Rights Reserved.
* 
* Contributor(s):
* Jaanus Heinlaid, Tieto Eesti*/
package eionet.cr.search;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import eionet.cr.common.Predicates;
import eionet.cr.search.util.PredicateLabels;
import eionet.cr.search.util.PredicateLabelsReader;
import eionet.cr.search.util.SearchExpression;
import eionet.cr.search.util.SubjectDataReader;
import eionet.cr.util.Hashes;
import eionet.cr.util.sql.SQLUtil;
import eionet.cr.web.util.columns.ReferringPredicatesColumn;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class ReferencesSearch extends AbstractSubjectSearch{

	/** */
	private PredicateLabels predicateLabels = new PredicateLabels();
	
	/** */
	private SearchExpression searchExpression;
	
	/**
	 * 
	 * @param searchExpression
	 */
	public ReferencesSearch(SearchExpression searchExpression) {
		this.searchExpression = searchExpression;
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.search.AbstractSubjectSearch#getSQL(java.util.List)
	 */
	protected String getSubjectSelectSQL(List inParameters){
		
		if (searchExpression==null || searchExpression.isEmpty())
			return null;
		
		StringBuffer sqlBuf = new StringBuffer("select SPO.SUBJECT as SUBJECT_HASH from SPO ");
		if (sortPredicate!=null){
			if (sortPredicate.equals(ReferringPredicatesColumn.class.getSimpleName())){
				sqlBuf.append("left join SPO as ORDERING on (SPO.PREDICATE=ORDERING.SUBJECT and ORDERING.PREDICATE=").
				append(Hashes.spoHash(Predicates.RDFS_LABEL)).append(") ");
			}
			else{
				sqlBuf.append("left join SPO as ORDERING on (SPO.SUBJECT=ORDERING.SUBJECT and ORDERING.PREDICATE=?) ");
				inParameters.add(Long.valueOf(Hashes.spoHash(sortPredicate)));
			}
		}
		
		if (searchExpression.isUri() || searchExpression.isHash()){
			
			sqlBuf.append(" where SPO.LIT_OBJ='N' and SPO.OBJECT_HASH=?");
			if (searchExpression.isHash()){
				inParameters.add(searchExpression.toString());
			}
			else{
				inParameters.add(Hashes.spoHash(searchExpression.toString()));
			}
				
		}
		else{
			sqlBuf.append(" where match(SPO.OBJECT) against (? in boolean mode)");
			inParameters.add(searchExpression.toString());
		}		
		
		if (sortPredicate!=null)
			sqlBuf.append(" order by ORDERING.OBJECT ").append(sortOrder==null ? sortOrder.ASCENDING.toSQL() : sortOrder.toSQL());
		
		return sqlBuf.toString();
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

	/**
	 * @return the predicateLabels
	 */
	public PredicateLabels getPredicateLabels() {
		return predicateLabels;
	}
}
