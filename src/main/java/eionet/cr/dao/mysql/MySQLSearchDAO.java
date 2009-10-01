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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
import eionet.cr.dao.ISearchDao;
import eionet.cr.dto.RawTripleDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.search.SearchException;
import eionet.cr.search.util.SearchExpression;
import eionet.cr.search.util.SimpleSearchDataReader;
import eionet.cr.search.util.SortOrder;
import eionet.cr.search.util.SubjectDataReader;
import eionet.cr.util.Hashes;
import eionet.cr.util.PageRequest;
import eionet.cr.util.Pair;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.URIUtil;
import eionet.cr.util.Util;
import eionet.cr.util.sql.PairReader;
import eionet.cr.util.sql.ResultSetListReader;
import eionet.cr.util.sql.SingleObjectReader;
import eionet.cr.web.util.columns.SubjectLastModifiedColumn;

/**
 * 
 * Mysql implementation of {@link ISearchDao}.
 * 
 * @author Aleksandr Ivanov
 * <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public class MySQLSearchDAO extends MySQLBaseDAO implements ISearchDao {

	MySQLSearchDAO() {
		//reduced visibility
	}
	
	/** 
	 * @see eionet.cr.dao.ISearchDao#performSpatialSourcesSearch()
	 * {@inheritDoc}
	 */
	public List<String> performSpatialSourcesSearch() throws DAOException {
		String sql = "select distinct URI from SPO,RESOURCE where " +
				"PREDICATE= ? and OBJECT_HASH= ? and SOURCE=RESOURCE.URI_HASH";
		List<Long> params = new LinkedList<Long>();
		params.add(Hashes.spoHash(Predicates.RDF_TYPE));
		params.add(Hashes.spoHash(Subjects.WGS_POINT));
		return executeQuery(sql, params, new SingleObjectReader<String>());
	}
	
	/** 
	 * @see eionet.cr.dao.ISearchDao#performSimpleSearch(eionet.cr.search.util.SearchExpression, int, eionet.cr.util.SortingRequest)
	 * {@inheritDoc}
	 */
	public Pair<Integer, List<SubjectDTO>> performSimpleSearch(
				SearchExpression expression,
				PageRequest pageRequest,
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
				.append((pageRequest.getPageNumber() -1) * pageRequest.getItemsPerPage())
				.append(',')
				.append(pageRequest.getItemsPerPage());
		Pair<List<Pair<Long,Long>>,Integer> result = executeQueryWithRowCount(
				selectQuery.toString(),
				searchParams,
				new PairReader<Long,Long>());
		Map<String, SubjectDTO> temp = new LinkedHashMap<String, SubjectDTO>();
		if (result != null && !result.getId().isEmpty()) {

			Map<String, Long> hitSources = new HashMap<String, Long>();
			for (Pair<Long,Long> subjectHash : result.getId()) {
				temp.put(subjectHash.getId() + "", null);
				hitSources.put(subjectHash.getId() + "", subjectHash.getValue());
			}
			SubjectDataReader reader = new SimpleSearchDataReader(temp, hitSources);
			executeQuery(getDataInitQuery(temp.keySet()), null, reader);
		}
		logger.debug("subject data select query took " + (System.currentTimeMillis()-time) + " ms");
			                                         
		return new Pair<Integer, List<SubjectDTO>>(result.getValue(), new LinkedList<SubjectDTO>(temp.values()));
	}
	
	private String getDataInitQuery(Collection<String> subjectHashes) {
		StringBuffer buf = new StringBuffer().
		append("select distinct ").
			append("SUBJECT as SUBJECT_HASH, SUBJ_RESOURCE.URI as SUBJECT_URI, SUBJ_RESOURCE.LASTMODIFIED_TIME as SUBJECT_MODIFIED, ").
			append("PREDICATE as PREDICATE_HASH, PRED_RESOURCE.URI as PREDICATE_URI, ").
			append("OBJECT, OBJECT_HASH, ANON_SUBJ, ANON_OBJ, LIT_OBJ, OBJ_LANG, OBJ_SOURCE_OBJECT, OBJ_DERIV_SOURCE, SOURCE, ").
			append("SRC_RESOURCE.URI as SOURCE_URI, DSRC_RESOURCE.URI as DERIV_SOURCE_URI ").
		append("from SPO ").
			append("left join RESOURCE as SUBJ_RESOURCE on (SUBJECT=SUBJ_RESOURCE.URI_HASH) ").
			append("left join RESOURCE as PRED_RESOURCE on (PREDICATE=PRED_RESOURCE.URI_HASH) ").
			append("left join RESOURCE as SRC_RESOURCE on (SOURCE=SRC_RESOURCE.URI_HASH) ").
			append("left join RESOURCE as DSRC_RESOURCE on (OBJ_DERIV_SOURCE=DSRC_RESOURCE.URI_HASH) ").
		append("where ").
			append("SUBJECT in (").append(Util.toCSV(subjectHashes)).append(") ").  
		append("order by ").
			append("SUBJECT, PREDICATE, OBJECT");
		return buf.toString();
	}

	/** 
	 * @see eionet.cr.dao.ISearchDao#performCustomSearch(java.util.Map, java.util.Set, int, eionet.cr.util.SortingRequest)
	 * {@inheritDoc}
	 */
	public Pair<Integer, List<SubjectDTO>> performCustomSearch(
			Map<String, String> criterias,
			Set<String> literalPredicates,
			PageRequest pageRequest,
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
		if (pageRequest.getItemsPerPage() > 0) {
			sb.append(" LIMIT ")
					.append( (pageRequest.getPageNumber() -1) * pageRequest.getItemsPerPage())
					.append(',')
					.append(pageRequest.getItemsPerPage());
		}
		logger.debug(sb.toString());
		Pair<List<Long>,Integer> subjectHashes = executeQueryWithRowCount(sb.toString(), parameters, new SingleObjectReader<Long>());
		if(subjectHashes == null || subjectHashes.getValue() == 0) {
			return new Pair<Integer,List<SubjectDTO>>(0, new LinkedList<SubjectDTO>());
		}
		Map<String, SubjectDTO> temp = new LinkedHashMap<String, SubjectDTO>();
		for (Long hash : subjectHashes.getId()) {
			temp.put(hash + "", null);
		}
		
		List<SubjectDTO> subjects = executeQuery(getDataInitQuery(temp.keySet()), null, new SubjectDataReader(temp));
		return new Pair<Integer,List<SubjectDTO>>(subjectHashes.getValue(), subjects);
	}

	/** 
	 * @see eionet.cr.dao.ISearchDao#isAllowLiteralSearch(java.lang.String)
	 * {@inheritDoc}
	 */
	public boolean isAllowLiteralSearch(String predicateUri) throws SearchException{
		//sanity checks
		if (StringUtils.isBlank(predicateUri)) {
			return false;
		}
		String allowLiteralSearchQuery = "select distinct OBJECT from SPO where SUBJECT=? and PREDICATE=? and LIT_OBJ='N' and ANON_OBJ='N'";
		
		try {
			ArrayList<Object> values = new ArrayList<Object>();
			values.add(Long.valueOf(Hashes.spoHash(predicateUri)));
			values.add(Long.valueOf((Hashes.spoHash(Predicates.RDFS_RANGE))));
			
			List<String> resultList = executeQuery(allowLiteralSearchQuery, values, new SingleObjectReader<String>());
			if (resultList == null || resultList.isEmpty()) {
				return true; // if not rdfs:domain specified at all, then lets allow literal search
			}
			
			for (String result : resultList){
				if (Subjects.RDFS_LITERAL.equals(result)){
					return true; // rdfs:Literal is present in the specified rdfs:domain
				}
			}
			
			return false;
		}
		catch(DAOException exception) {
			throw new SearchException();
		}
	}

	/** 
	 * @see eionet.cr.dao.ISearchDao#getSampleTriples(java.lang.String, int)
	 * {@inheritDoc}
	 */
	public Pair<Integer, List<RawTripleDTO>> getSampleTriples(String url, int limit)
			throws DAOException {
		StringBuffer buf = new StringBuffer().
		append("select distinct sql_calc_found_rows ").
			append("SUBJ_RESOURCE.URI as SUBJECT_URI, ").
			append("PRED_RESOURCE.URI as PREDICATE_URI, ").
			append("OBJECT, ").
			append("DSRC_RESOURCE.URI as DERIV_SOURCE_URI ").
		append("from SPO ").
			append("left join RESOURCE as SUBJ_RESOURCE on (SUBJECT=SUBJ_RESOURCE.URI_HASH) ").
			append("left join RESOURCE as PRED_RESOURCE on (PREDICATE=PRED_RESOURCE.URI_HASH) ").
			append("left join RESOURCE as SRC_RESOURCE on (SOURCE=SRC_RESOURCE.URI_HASH) ").
			append("left join RESOURCE as DSRC_RESOURCE on (OBJ_DERIV_SOURCE=DSRC_RESOURCE.URI_HASH) ").
		append("where ").
			append("SPO.SOURCE = ? LIMIT 0, ?");   
		List<Object> params = new LinkedList<Object>();
		params.add(Hashes.spoHash(url));
		params.add(limit);
		
		Pair<List<RawTripleDTO>, Integer> result = executeQueryWithRowCount(buf.toString(), params, new ResultSetListReader<RawTripleDTO>() {

			private List<RawTripleDTO> resultList = new LinkedList<RawTripleDTO>();
			
			@Override
			public List<RawTripleDTO> getResultList() {
				return resultList;
			}

			@Override
			public void readRow(ResultSet rs) throws SQLException {
				resultList.add(
						new RawTripleDTO(
								rs.getString("SUBJECT_URI"),
								rs.getString("PREDICATE_URI"),
								rs.getString("OBJECT"),
								rs.getString("DERIV_SOURCE_URI")));
			}
		});
		
		
		return new Pair<Integer, List<RawTripleDTO>>(result.getValue(), result.getId());
	}

}
