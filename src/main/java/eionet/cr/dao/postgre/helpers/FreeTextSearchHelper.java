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
package eionet.cr.dao.postgre.helpers;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import eionet.cr.search.util.SearchExpression;
import eionet.cr.search.util.SortOrder;
import eionet.cr.util.Hashes;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.util.sql.PairReader;
import eionet.cr.web.util.columns.SubjectLastModifiedColumn;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class FreeTextSearchHelper {
	
	/** */
	private SearchExpression expression;
	private PagingRequest pagingRequest;
	private String sortPredicate;
	private String sortOrder;
	
	/**
	 * 
	 * @param expression
	 * @param pagingRequest
	 * @param sortingRequest
	 */
	private FreeTextSearchHelper(SearchExpression expression,
			PagingRequest pagingRequest,
			SortingRequest sortingRequest){
		
		this.expression = expression;
		this.pagingRequest = pagingRequest;
		if (sortingRequest!=null){
			this.sortPredicate = sortingRequest.getSortingColumnName();
			this.sortOrder = sortingRequest.getSortOrder() == null  ? SortOrder.ASCENDING.toSQL() 
					: sortingRequest.getSortOrder().toSQL();
		}
	}
	
	/**
	 * 
	 * @param expression
	 * @param pagingRequest
	 * @param sortingRequest
	 * @return
	 */
	public static FreeTextSearchHelper create(SearchExpression expression,
			PagingRequest pagingRequest,
			SortingRequest sortingRequest){
		return new FreeTextSearchHelper(expression, pagingRequest, sortingRequest);
	}

	/**
	 * 
	 * @param inParams
	 * @return
	 */
	public String getQuery(List<Object> inParams){
		
		String query = StringUtils.isBlank(sortPredicate) ?
				getUnorderedQuery(inParams) : getOrderedQuery(inParams);
		
		if (pagingRequest!=null){
			return query;
		}
		else{
			return new StringBuffer(query).
			append(" limit ").append(pagingRequest.getItemsPerPage()).
			append(" offset ").append(pagingRequest.getOffset()).
			toString();
		}
	}

	/**
	 * 
	 * @param inParams
	 * @return
	 */
	private String getUnorderedQuery(List<Object> inParams){
		
		StringBuffer buf = new StringBuffer().
		append("select distinct SPO.SUBJECT as ").append(PairReader.LEFTCOL).append(", ").
		append("(CASE WHEN SPO.OBJ_DERIV_SOURCE<>0 THEN SPO.OBJ_DERIV_SOURCE ELSE SPO.SOURCE END)").
		append(" as ").append(PairReader.RIGHTCOL).append(" from SPO ");
		
		if (expression.isUri() || expression.isHash()){
			buf.append(" where SPO.OBJECT_HASH=?");
			inParams.add(expression.isHash() ?
					expression.toString() : Long.valueOf(Hashes.spoHash(expression.toString())));
		}
		else{
			buf.append(" where to_tsvector('simple', SPO.OBJECT) @@ to_tsquery('simple', ?)").
			append(" and SPO.LIT_OBJ='Y'");
			inParams.add(expression.toString());
		}
		
		return buf.toString();
	}

	/**
	 * 
	 * @param inParams
	 * @return
	 */
	private String getOrderedQuery(List<Object> inParams){

		StringBuffer subSelect = new StringBuffer().
		append("select distinct on (").append(PairReader.LEFTCOL).append(")").
		append(" SPO.SUBJECT as ").append(PairReader.LEFTCOL).append(", ").
		append("(CASE WHEN SPO.OBJ_DERIV_SOURCE<>0 THEN SPO.OBJ_DERIV_SOURCE ELSE SPO.SOURCE END)").
		append(" as ").append(PairReader.RIGHTCOL).append(", ").
		append(" ORDERING.OBJECT as OBJECT_ORDERED_BY").
		append(" from SPO");
		
		if (sortPredicate.equals(SubjectLastModifiedColumn.class.getSimpleName())){
			
			subSelect.append(" left join RESOURCE on (SPO.SUBJECT=RESOURCE.URI_HASH) ");
		}
		else{
			subSelect.append(" left join SPO as ORDERING on").
			append(" (SPO.SUBJECT=ORDERING.SUBJECT and ORDERING.PREDICATE=").
			append(Hashes.spoHash(sortPredicate)).append(")");
		}
		
		if (expression.isUri() || expression.isHash()){
			subSelect.append(" where SPO.OBJECT_HASH=?");
			inParams.add(expression.isHash() ?
					expression.toString() : Long.valueOf(Hashes.spoHash(expression.toString())));
		}
		else{
			subSelect.append(" where to_tsvector('simple', SPO.OBJECT) @@ to_tsquery('simple', ?)").
			append(" and SPO.LIT_OBJ='Y'");
			inParams.add(expression.toString());
		}

		StringBuffer buf = new StringBuffer().
		append("select * from (").append(subSelect).append(") as FOO order by OBJECT_ORDERED_BY");
		
		return buf.toString();
	}

	/**
	 * 
	 * @param inParams
	 * @return
	 */
	public String getCountQuery(List<Object> inParams){

		StringBuffer buf = new StringBuffer("select count(distinct SPO.SUBJECT) from SPO");
		
		if (expression.isUri() || expression.isHash()){
			buf.append(" where SPO.OBJECT_HASH=?");
			inParams.add(expression.isHash() ?
					expression.toString() : Long.valueOf(Hashes.spoHash(expression.toString())));
		}
		else{
			buf.
			append(" where to_tsvector('simple', SPO.OBJECT) @@ to_tsquery('simple', ?)").
			append(" and SPO.LIT_OBJ='Y'");
			inParams.add(expression.toString());
		}
		
		return buf.toString();
	}
}
