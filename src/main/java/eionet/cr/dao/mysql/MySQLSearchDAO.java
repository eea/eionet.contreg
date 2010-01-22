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
 * Aleksandr Ivanov, Tieto Eesti
 */
package eionet.cr.dao.mysql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.SearchDAO;
import eionet.cr.dto.RawTripleDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.search.SearchException;
import eionet.cr.search.util.SearchExpression;
import eionet.cr.search.util.SimpleSearchDataReader;
import eionet.cr.search.util.SortOrder;
import eionet.cr.search.util.SubjectDataReader;
import eionet.cr.util.Hashes;
import eionet.cr.util.PagingRequest;
import eionet.cr.util.Pair;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.URIUtil;
import eionet.cr.util.Util;
import eionet.cr.util.sql.PairReader;
import eionet.cr.util.sql.ResultSetListReader;
import eionet.cr.util.sql.SQLUtil;
import eionet.cr.util.sql.SingleObjectReader;
import eionet.cr.web.util.columns.ReferringPredicatesColumn;
import eionet.cr.web.util.columns.SubjectLastModifiedColumn;

/**
 * 
 * Mysql implementation of {@link SearchDAO}.
 * 
 * @author Aleksandr Ivanov
 * <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public class MySQLSearchDAO extends MySQLBaseDAO implements SearchDAO {

	MySQLSearchDAO() {
		//reduced visibility
	}
	
	/** 
	 * @see eionet.cr.dao.SearchDAO#freetextSearch(eionet.cr.search.util.SearchExpression, int, eionet.cr.util.SortingRequest)
	 * {@inheritDoc}
	 */
	public Pair<Integer, List<SubjectDTO>> freetextSearch(
				SearchExpression expression,
				PagingRequest pagingRequest,
				SortingRequest sortingRequest) throws DAOException, SQLException {
		
		long time = System.currentTimeMillis();
		List<Object> searchParams = new LinkedList<Object>();
		StringBuffer selectQuery = new StringBuffer();
		selectQuery.append("select sql_calc_found_rows SPO.SUBJECT as id, IF(SPO.OBJ_DERIV_SOURCE <> 0, SPO.OBJ_DERIV_SOURCE, SPO.SOURCE) as value from SPO ");
		if (sortingRequest  != null && sortingRequest.getSortingColumnName() != null) {
			if (sortingRequest.getSortingColumnName().equals(SubjectLastModifiedColumn.class.getSimpleName())){
				selectQuery.append("left join RESOURCE on (SPO.SUBJECT=RESOURCE.URI_HASH) ");
			}
			else{
				selectQuery.append("left join SPO as ORDERING on (SPO.SUBJECT=ORDERING.SUBJECT and ORDERING.PREDICATE=?) ");
				searchParams.add(Long.valueOf(Hashes.spoHash(sortingRequest.getSortingColumnName())));
			}
		}
		if (expression.isUri() || expression.isHash()){
			selectQuery.append(" where SPO.OBJECT_HASH=?");
			searchParams.add(
					expression.isHash() 
							? expression.toString()
							: Hashes.spoHash(expression.toString()));
				
		} else{
			selectQuery.append(" where match(SPO.OBJECT) against (? in boolean mode)");
			searchParams.add(expression.toString());
		}		
		selectQuery.append(" GROUP BY SPO.SUBJECT "); 
		
		if (sortingRequest !=null && sortingRequest.getSortingColumnName() != null){
			if (sortingRequest.getSortingColumnName().equals(SubjectLastModifiedColumn.class.getSimpleName())){
				selectQuery.append(" order by RESOURCE.LASTMODIFIED_TIME ").append(
						sortingRequest.getSortOrder() == null 
								? SortOrder.ASCENDING.toSQL() 
								: sortingRequest.getSortOrder().toSQL());
			} else{
				selectQuery.append(" order by ORDERING.OBJECT ").append(
						sortingRequest.getSortOrder() == null 
								? SortOrder.ASCENDING.toSQL() 
								: sortingRequest.getSortOrder().toSQL());
			}
		}
		
		selectQuery.append(" LIMIT ")
				.append((pagingRequest.getPageNumber() -1) * pagingRequest.getItemsPerPage())
				.append(',')
				.append(pagingRequest.getItemsPerPage());
		Pair<List<Pair<Long,Long>>,Integer> result = executeQueryWithRowCount(
				selectQuery.toString(),
				searchParams,
				new PairReader<Long,Long>());
		Map<Long,SubjectDTO> temp = new LinkedHashMap<Long,SubjectDTO>();
		if (result != null && !result.getLeft().isEmpty()) {

			Map<Long,Long> hitSources = new HashMap<Long,Long>();
			for (Pair<Long,Long> subjectHash : result.getLeft()) {
				temp.put(subjectHash.getLeft(), null);
				hitSources.put(subjectHash.getLeft(),subjectHash.getRight());
			}
			SubjectDataReader reader = new SimpleSearchDataReader(temp, hitSources);
			executeQuery(getSubjectsDataQuery(temp.keySet()), null, reader);
		}
		logger.debug("subject data select query took " + (System.currentTimeMillis()-time) + " ms");
			                                         
		return new Pair<Integer, List<SubjectDTO>>(result.getRight(), new LinkedList<SubjectDTO>(temp.values()));
	}
	
	/** 
	 * @see eionet.cr.dao.SearchDAO#filteredSearch(java.util.Map, java.util.Set, int, eionet.cr.util.SortingRequest)
	 * {@inheritDoc}
	 */
	public Pair<Integer, List<SubjectDTO>> filteredSearch(
			Map<String, String> criterias,
			Set<String> literalPredicates,
			PagingRequest pagingRequest,
			SortingRequest sortingRequest)
			throws DAOException {
		
		StringBuffer sb = new StringBuffer();
		List<Object> parameters = new LinkedList<Object>();
		sb.append("select distinct sql_calc_found_rows SPO1.SUBJECT as SUBJECT_HASH from SPO as SPO1 ");
		
		//check if sorting has been requested
		if (sortingRequest != null && sortingRequest.getSortingColumnName() != null) {
			if (sortingRequest.getSortingColumnName().equals(SubjectLastModifiedColumn.class.getSimpleName())){
				sb.append(" left join RESOURCE on (SPO1.SUBJECT=RESOURCE.URI_HASH) ");
			}
			else{
				sb.append(" left join SPO as ORDERING on (SPO1.SUBJECT=ORDERING.SUBJECT and ORDERING.PREDICATE=?) ");
				parameters.add(Long.valueOf(Hashes.spoHash(sortingRequest.getSortingColumnName())));
			}
		}
		StringBuffer whereClause = new StringBuffer();
		
		//building up where and from clause
		int index = 1;
		for(Entry<String,String> criteria : criterias.entrySet()) {
			String spoCurr = "SPO" + index++;
			whereClause.append(whereClause.length() > 0 ? " and " : "");
			whereClause.append(spoCurr).append(".PREDICATE=? and ");
			parameters.add(Long.valueOf(Hashes.spoHash(criteria.getKey())));
			
			if ( (criteria.getValue().startsWith("\"") && criteria.getValue().endsWith("\"")) 
					|| URIUtil.isSchemedURI(criteria.getValue())
					|| !(literalPredicates != null && literalPredicates.contains(criteria.getKey()))){
				whereClause.append(spoCurr).append(".OBJECT_HASH=?");
				parameters.add(Long.valueOf(Hashes.spoHash(StringUtils.strip(criteria.getValue(), "\""))));
			} else{
				whereClause.append("match(").append(spoCurr).append(".OBJECT) against (?)");
				parameters.add(criteria.getValue());
			}
		}
		//packing it all together into sql select
		for (int i = 2; i < index; i++) {
			sb.append(" inner join SPO as SPO")
					.append(i)
					.append(" on SPO1.SUBJECT = SPO")
					.append(i)
					.append(".SUBJECT ");
		}
		if (whereClause.length() > 0) {
			sb.append(" where ");
			sb.append(whereClause);
		}
		if (sortingRequest != null && sortingRequest.getSortingColumnName() != null){
			if (sortingRequest.getSortingColumnName().equals(SubjectLastModifiedColumn.class.getSimpleName())){
				sb.append(" order by RESOURCE.LASTMODIFIED_TIME ").append(sortingRequest.getSortOrder().toSQL());
			}
			else{
				sb.append(" order by ORDERING.OBJECT ").append(sortingRequest.getSortOrder().toSQL());
			}
		}
		//iff pageRequest.itemsPerPage == 0 - we want to deliver all results.
		if (pagingRequest.getItemsPerPage() > 0) {
			sb.append(" LIMIT ")
					.append( (pagingRequest.getPageNumber() -1) * pagingRequest.getItemsPerPage())
					.append(',')
					.append(pagingRequest.getItemsPerPage());
		}
		logger.debug(sb.toString());
		Pair<List<Long>,Integer> subjectHashes = executeQueryWithRowCount(sb.toString(), parameters, new SingleObjectReader<Long>());
		if(subjectHashes == null || subjectHashes.getRight() == 0) {
			return new Pair<Integer,List<SubjectDTO>>(0, new LinkedList<SubjectDTO>());
		}
		Map<Long,SubjectDTO> temp = new LinkedHashMap<Long, SubjectDTO>();
		for (Long hash : subjectHashes.getLeft()) {
			temp.put(hash, null);
		}
		
		List<SubjectDTO> subjects = executeQuery(getSubjectsDataQuery(temp.keySet()), null, new SubjectDataReader(temp));
		return new Pair<Integer,List<SubjectDTO>>(subjectHashes.getRight(), subjects);
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.SearchDAO#referenceSearch(java.lang.Long, eionet.cr.util.PagingRequest, eionet.cr.util.SortingRequest)
	 */
	public Pair<Integer, List<SubjectDTO>> referenceSearch(Long subjectHash,
			PagingRequest pagingRequest, SortingRequest sortingRequest)
			throws DAOException {
		
		if (subjectHash==null){
			return null;
		}

		String sortPredicate = sortingRequest!=null ? sortingRequest.getSortingColumnName() : null;
		SortOrder sortOrder  = sortingRequest!=null ? sortingRequest.getSortOrder() : null;
		boolean doSort = !StringUtils.isBlank(sortPredicate);
		
		// build the "select from" part
		StringBuffer sqlBuf = new StringBuffer().
		append("select sql_calc_found_rows SPO.SUBJECT as SUBJECT_HASH from SPO");
		if (doSort){
			
			if (sortPredicate.equals(ReferringPredicatesColumn.class.getSimpleName())){
				sqlBuf.append(" left join SPO as ORDERING on ").
				append("(SPO.PREDICATE=ORDERING.SUBJECT and ORDERING.PREDICATE=").
				append(Hashes.spoHash(Predicates.RDFS_LABEL)).append(")");
			}
			else{
				sqlBuf.append(" left join SPO as ORDERING on ").
				append("(SPO.SUBJECT=ORDERING.SUBJECT and ORDERING.PREDICATE=").
				append(Hashes.spoHash(sortPredicate)).append(")");
			}
		}
		
		// build the "where" part
		sqlBuf.append(" where SPO.LIT_OBJ='N' and SPO.OBJECT_HASH=").append(subjectHash);
		
		// build the "order by" part
		if (doSort){
			sqlBuf.append(" order by ORDERING.OBJECT ").
			append(sortOrder==null ? sortOrder.ASCENDING.toSQL() : sortOrder.toSQL());
		}

		// build the "limit" part
		if (pagingRequest!=null && pagingRequest.getItemsPerPage()>0){
			
			sqlBuf.append(" limit ");
			if (pagingRequest.getPageNumber()>0){
				sqlBuf.append(pagingRequest.getPageNumber());
			}
			sqlBuf.append(",").append(pagingRequest.getItemsPerPage());
		}
		
		Pair<List<Long>,Integer> pair = executeQueryWithRowCount(sqlBuf.toString(),
				new SingleObjectReader<Long>());
		if (pair==null || pair.getRight()==0){
			return new Pair<Integer, List<SubjectDTO>>(0, new LinkedList<SubjectDTO>());
		}
		else{
			Map<Long,SubjectDTO> temp = new LinkedHashMap<Long, SubjectDTO>();
			for (Long hash : pair.getLeft()) {
				temp.put(hash, null);
			}
			List<SubjectDTO> subjects = executeQuery(
					getSubjectsDataQuery(temp.keySet()), null, new SubjectDataReader(temp));
			return new Pair<Integer,List<SubjectDTO>>(pair.getRight(), subjects);
		}
	}
}
