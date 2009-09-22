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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eionet.cr.dto.SubjectDTO;
import eionet.cr.search.util.SearchExpression;
import eionet.cr.search.util.SimpleSearchDataReader;
import eionet.cr.search.util.SimpleSearchHashesReader;
import eionet.cr.search.util.SubjectDataReader;
import eionet.cr.search.util.SubjectHashesReader;
import eionet.cr.util.Hashes;
import eionet.cr.web.util.columns.SubjectLastModifiedColumn;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 * 
 *	@deprecated - use {@link HelperDao#} performSimpleSearch method instead.
 */
@Deprecated
public class SimpleSearch extends AbstractSubjectSearch{

	/** */
	private SearchExpression searchExpression;
	
	/** */
	private Map<String,Long> hitSources = new HashMap<String,Long>();
	
	/**
	 * 
	 * @param searchExpression
	 */
	public SimpleSearch(SearchExpression searchExpression){
		this.searchExpression = searchExpression;
	}

	/**
	 * 
	 * @param str
	 */
	public SimpleSearch(String searchExpression){
		this(new SearchExpression(searchExpression));
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.search.AbstractSubjectSearch#getSubjectSelectSQL(java.util.List)
	 */
	protected String getSubjectSelectSQL(List inParameters){
		
		if (searchExpression==null || searchExpression.isEmpty())
			return null;
		
		StringBuffer sqlBuf = new StringBuffer("select SPO.SUBJECT as SUBJECT_HASH, SPO.SOURCE as HIT_SOURCE from SPO ");
		if (sortPredicate!=null){
			
			if (sortPredicate.equals(SubjectLastModifiedColumn.class.getSimpleName())){
				sqlBuf.append("left join RESOURCE on (SPO.SUBJECT=RESOURCE.URI_HASH) ");
			}
			else{
				sqlBuf.append("left join SPO as ORDERING on (SPO.SUBJECT=ORDERING.SUBJECT and ORDERING.PREDICATE=?) ");
				inParameters.add(Long.valueOf(Hashes.spoHash(sortPredicate)));
			}
		}
		
		if (searchExpression.isUri() || searchExpression.isHash()){
			
			sqlBuf.append(" where SPO.OBJECT_HASH=?");
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
		
		if (sortPredicate!=null){
			if (sortPredicate.equals(SubjectLastModifiedColumn.class.getSimpleName())){
				sqlBuf.append(" order by RESOURCE.LASTMODIFIED_TIME ").append(sortOrder==null ? sortOrder.ASCENDING.toSQL() : sortOrder.toSQL());
			}
			else{
				sqlBuf.append(" order by ORDERING.OBJECT ").append(sortOrder==null ? sortOrder.ASCENDING.toSQL() : sortOrder.toSQL());
			}
		}
		
		return sqlBuf.toString();
	}

	/**
	 * @param searchExpression the searchExpression to set
	 */
	public void setSearchExpression(SearchExpression searchExpression) {
		this.searchExpression = searchExpression;
	}

	/**
	 * 
	 * @param string
	 */
	public void setSearchExpression(String string) {
		this.searchExpression = new SearchExpression(string);
	}
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.search.AbstractSubjectSearch#createSubjectHashesReader()
	 */
	protected SubjectHashesReader createSubjectHashesReader(){
		return new SimpleSearchHashesReader(hitSources);
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.search.AbstractSubjectSearch#createSubjectDataReader(java.util.Map)
	 */
	protected SubjectDataReader createSubjectDataReader(Map<String, SubjectDTO> subjectsMap){
		return new SimpleSearchDataReader(subjectsMap, hitSources);
	}
}
