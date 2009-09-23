package eionet.cr.dao.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.HelperDao;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.search.SearchException;
import eionet.cr.search.util.SearchExpression;
import eionet.cr.search.util.SimpleSearchDataReader;
import eionet.cr.search.util.SortOrder;
import eionet.cr.search.util.SubjectDataReader;
import eionet.cr.util.Hashes;
import eionet.cr.util.Pair;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.URIUtil;
import eionet.cr.util.Util;
import eionet.cr.util.YesNoBoolean;
import eionet.cr.util.sql.PairReader;
import eionet.cr.util.sql.SQLUtil;
import eionet.cr.util.sql.SingleObjectReader;
import eionet.cr.web.util.columns.SubjectLastModifiedColumn;


/**
 *	Mysql implementation of {@link HelperDao}.
 * 
 * @author Aleksandr Ivanov
 * <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public class MySQLHelperDao extends MySQLBaseDAO implements HelperDao {
	
	/**
	 * 
	 */
	MySQLHelperDao() {
		//reducing visibility
	}

	/** 
	 * @see eionet.cr.dao.HelperDao#performSpatialSourcesSearch()
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
	 * @see eionet.cr.dao.HelperDao#performSimpleSearch(eionet.cr.search.util.SearchExpression, int, eionet.cr.util.SortingRequest)
	 * {@inheritDoc}
	 */
	public Pair<Integer, List<SubjectDTO>> performSimpleSearch(
				SearchExpression expression,
				int pageNumber,
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
		
		selectQuery.append(" LIMIT ").append( (pageNumber -1) * 15).append(',').append(15);
		Pair<List<Pair<Long,Long>>,Integer> result = executeQueryWithRowCount(selectQuery.toString(), searchParams, new PairReader<Long,Long>());
		Map<String, SubjectDTO> temp = new LinkedHashMap<String, SubjectDTO>();
		if (result != null && !result.getId().isEmpty()) {

			Map<String, Long> hitSources = new HashMap<String, Long>();
			for (Pair<Long,Long> subjectHash : result.getId()) {
				temp.put(subjectHash.getId() + "", null);
				hitSources.put(subjectHash.getId() + "", subjectHash.getValue());
			}
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
				append("SUBJECT in (").append(Util.toCSV(temp.keySet())).append(") ").  
			append("order by ").
				append("SUBJECT, PREDICATE, OBJECT");
			SubjectDataReader reader = new SimpleSearchDataReader(temp, hitSources);
			SQLUtil.executeQuery(buf.toString(), reader, getConnection());
		}
		logger.debug("subject data select query took " + (System.currentTimeMillis()-time) + " ms");
			                                         
		return new Pair<Integer, List<SubjectDTO>>(result.getValue(), new LinkedList<SubjectDTO>(temp.values()));
	}
	
	/** 
	 * @see eionet.cr.dao.HelperDao#getRecentlyDiscoveredFiles()
	 * {@inheritDoc}
	 */
	public List<Pair<String, String>> getRecentlyDiscoveredFiles(int limit) throws DAOException {
		
		/* Get the hashes and URIs of recent subjects of type=cr:file
		 * (we need URIs, because we might need to derive labels from them).
		 */
		
		String sql = "SELECT DISTINCT RESOURCE.URI_HASH, RESOURCE.URI FROM RESOURCE INNER JOIN SPO ON RESOURCE.URI_HASH = SPO.SUBJECT" +
				" WHERE SPO.PREDICATE= ? AND OBJECT_HASH= ? ORDER BY FIRSTSEEN_TIME DESC LIMIT ?;";
		List<Long> params = new LinkedList<Long>();
		params.add(Hashes.spoHash(Predicates.RDF_TYPE));
		params.add( Hashes.spoHash(Subjects.CR_FILE));
		params.add(new Long(limit));
		
		Map<String,String> labelMap = new LinkedHashMap<String,String>();
		Map<String,String> uriMap = new LinkedHashMap<String,String>();
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try{
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, Hashes.spoHash(Predicates.RDF_TYPE));
			pstmt.setLong(2, Hashes.spoHash(Subjects.CR_FILE));
			pstmt.setLong(3, limit);
			rs = pstmt.executeQuery();
			while (rs.next()){
				uriMap.put(rs.getString(1), rs.getString(2));
				labelMap.put(rs.getString(1), "");
			}

			/* if any subjects were found, let's find their labels */
			
			if (!labelMap.isEmpty()){
				
				sql = "SELECT DISTINCT SPO.SUBJECT as id, SPO.OBJECT as value FROM SPO WHERE SPO.PREDICATE=? " +
						"AND SPO.SUBJECT IN (" + Util.toCSV(labelMap.keySet()) + ")";				
				pstmt = conn.prepareStatement(sql);
				pstmt.setLong(1, Hashes.spoHash(Predicates.RDFS_LABEL));
				rs = pstmt.executeQuery();
				while (rs.next()){
					labelMap.put(rs.getString(1), rs.getString(2));
				}
			}
		}
		catch (SQLException e){
			throw new DAOException(e.toString(), e);
		}
		finally{
			SQLUtil.close(rs);
			SQLUtil.close(pstmt);
			SQLUtil.close(conn);
		}
		
		/* Loop through labels and if a label was not found for a particular subject,
		 * then derive the label from the subject's URI.
		 */
		ArrayList<Pair<String, String>> result = new ArrayList<Pair<String, String>>();
		for (String uriHash:labelMap.keySet()){
			if (StringUtils.isBlank(labelMap.get(uriHash))){
				result.add(new Pair(uriHash, URIUtil.deriveLabel(uriMap.get(uriHash))));
			}
			else{
				result.add(new Pair(uriHash, labelMap.get(uriHash)));
			}
		}
		
		return result;
	}

	/** */
	private static final String sqlQuery = "select distinct OBJECT from SPO where PREDICATE=? and LIT_OBJ='Y' order by OBJECT asc";

	/** 
	 * @see eionet.cr.dao.HelperDao#getPicklistForPredicate(java.lang.String)
	 * {@inheritDoc}
	 */
	public Collection<String> getPicklistForPredicate(String predicateUri) throws SearchException {
		if (StringUtils.isBlank(predicateUri)) {
			return Collections.emptyList();
		}
		try {
			List<String> resultList = executeQuery(
					sqlQuery,
					Collections.singletonList((Object)Hashes.spoHash(predicateUri)),
					new SingleObjectReader<String>());
			return resultList;
		}
		catch (DAOException e){
			throw new SearchException(e.toString(), e);
		}
	}

	/** */
	private static final String allowLiteralSearchQuery = "select distinct OBJECT from SPO where SUBJECT=? and PREDICATE=? and LIT_OBJ='N' and ANON_OBJ='N'";
	
	/** 
	 * @see eionet.cr.dao.HelperDao#isAllowLiteralSearch(java.lang.String)
	 * {@inheritDoc}
	 */
	public boolean isAllowLiteralSearch(String predicateUri) throws SearchException{
		
		//sanity checks
		if (StringUtils.isBlank(predicateUri)) {
			return false;
		}
		
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

	/** */
	private static final String tripleInsertSQL = "insert high_priority into SPO (SUBJECT, PREDICATE, OBJECT, OBJECT_HASH, OBJECT_DOUBLE," +
			" ANON_SUBJ, ANON_OBJ, LIT_OBJ, OBJ_LANG, OBJ_DERIV_SOURCE, OBJ_DERIV_SOURCE_GEN_TIME, OBJ_SOURCE_OBJECT, SOURCE," +
			" GEN_TIME) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.SpoHelperDao#addTriples(eionet.cr.dto.SubjectDTO)
	 */
	public void addTriples(SubjectDTO subjectDTO) throws DAOException{
		
		if (subjectDTO==null || subjectDTO.getPredicateCount()==0)
			return;
		
		long firstSeenTime = System.currentTimeMillis();
			
		Connection conn = null;
		PreparedStatement pstmt = null;
		try{
			conn = getConnection();
			pstmt = conn.prepareStatement(tripleInsertSQL);

			boolean doExecuteBatch = false;
			long subjectHash = subjectDTO.getUriHash();
			for (String predicateUri:subjectDTO.getPredicateUris()){
				
				Collection<ObjectDTO> objects = subjectDTO.getObjects(predicateUri);
				if (objects!=null && !objects.isEmpty()){
					
					long predicateHash = Hashes.spoHash(predicateUri);
					for (ObjectDTO object:objects){
						
						pstmt.setLong(   1, subjectHash);
						pstmt.setLong(   2, predicateHash);
						pstmt.setString( 3, object.getValue());
						pstmt.setLong(   4, object.getHash());
						pstmt.setObject( 5, Util.toDouble(object.getValue()));
						pstmt.setString( 6, YesNoBoolean.format(subjectDTO.isAnonymous()));
						pstmt.setString( 7, YesNoBoolean.format(object.isAnonymous()));
						pstmt.setString( 8, YesNoBoolean.format(object.isLiteral()));
						pstmt.setString( 9, StringUtils.trimToEmpty(object.getLanguage()));
						pstmt.setLong(  10, object.getDerivSourceUri()==null ? 0 : Hashes.spoHash(object.getDerivSourceUri()));
						pstmt.setLong(  11, object.getDerivSourceGenTime());
						pstmt.setLong(  12, object.getSourceObjectHash());
						pstmt.setLong(  13, Hashes.spoHash(object.getSourceUri()));
						pstmt.setLong(  14, System.currentTimeMillis());
						
						pstmt.addBatch();
						if (doExecuteBatch==false){
							doExecuteBatch = true;
						}
					}
				}
			}
			
			if (doExecuteBatch==true){
				
				// insert triples
				pstmt.executeBatch();
			}
		}
		catch (SQLException e){
			throw new DAOException(e.toString(), e);
		}
		finally{
			SQLUtil.close(pstmt);
			SQLUtil.close(conn);
		}
	}

	/** */
	private static final String deleteTriplesSQL = "delete from SPO where SUBJECT=? and PREDICATE=?" +
			" and OBJECT_HASH=? and SOURCE=? and OBJ_DERIV_SOURCE=? and OBJ_SOURCE_OBJECT=?";
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HelperDao#deleteTriples(eionet.cr.dto.SubjectDTO)
	 */
	public void deleteTriples(SubjectDTO subject) throws DAOException{
		
		if (subject==null || subject.getPredicateCount()==0){
			return;
		}
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		boolean executeBatch = false;
		try{
			conn = getConnection();
			pstmt = conn.prepareStatement(deleteTriplesSQL);
			Map<String,Collection<ObjectDTO>> predicates = subject.getPredicates();
			for (String predicate:predicates.keySet()){
				
				Collection<ObjectDTO> objects = subject.getObjects(predicate);
				if (objects!=null && !objects.isEmpty()){
					
					for (ObjectDTO object:objects){
						
						pstmt.setLong(1, subject.getUriHash());
						pstmt.setLong(2, Long.parseLong(predicate));
						pstmt.setLong(3, object.getHash());
						pstmt.setLong(4, object.getSourceHash());
						pstmt.setLong(5, object.getDerivSourceHash());
						pstmt.setLong(6, object.getSourceObjectHash());
						pstmt.addBatch();
						
						if (executeBatch==false){
							executeBatch = true;
						}
					}
				}
			}
			
			if (executeBatch==true){
				pstmt.executeBatch();
			}
		}
		catch (NumberFormatException e){
			throw new IllegalArgumentException("Expected the predicates to be in hash format");
		}
		catch (SQLException e){
			throw new DAOException(e.toString(), e);
		}
		finally{
			SQLUtil.close(conn);
		}
	}

	/** */
	public static final String insertResourceSQL = "insert high_priority ignore into RESOURCE" +
			" (URI, URI_HASH, FIRSTSEEN_SOURCE, FIRSTSEEN_TIME) values (?, ?, ?, ?)";
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.SpoHelperDao#addResource(java.lang.String, java.lang.String)
	 */
	public void addResource(String uri, String firstSeenSourceUri) throws DAOException {
		
		ArrayList values = new ArrayList();
		values.add(uri);
		values.add(Long.valueOf(Hashes.spoHash(uri)));
		if (StringUtils.isBlank(firstSeenSourceUri)){
			values.add(Long.valueOf(0));
			values.add(Long.valueOf(0));
		}
		else{
			values.add(Long.valueOf(Hashes.spoHash(firstSeenSourceUri)));
			values.add(Long.valueOf(System.currentTimeMillis()));
		}
		
		Connection conn = null;
		try{
			conn = getConnection();
			SQLUtil.executeUpdate(insertResourceSQL, values, conn);
		}
		catch (SQLException e){
			throw new DAOException(e.toString(), e);
		}
		finally{
			SQLUtil.close(conn);
		}
	}

	/** */
	private static final String getDCPropertiesSQL = "select distinct SUBJECT from SPO" +
			" where SOURCE=" + Hashes.spoHash(Subjects.DUBLIN_CORE_SOURCE_URL) +
			" and PREDICATE=" + Hashes.spoHash(Predicates.RDF_TYPE) +
			" and OBJECT_HASH=" + Hashes.spoHash(Subjects.RDF_PROPERTY);
	
	/**
	 * 
	 * @param subjectTypes
	 * @return
	 * @throws DAOException
	 */
	public HashMap<String,String> getAddibleProperties(Collection<String> subjectTypes) throws DAOException {
		
		HashMap<String,String> result = new HashMap<String,String>();
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try{
			/* get the DublinCore properties */
			
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(getDCPropertiesSQL);			
			List<String> dcPropertiesHashes = new ArrayList<String>();
			while (rs.next()){
				dcPropertiesHashes.add(rs.getString(1));
			}
			SQLUtil.close(rs);

			if (!dcPropertiesHashes.isEmpty()){
				result.putAll(getSubjectLabels(dcPropertiesHashes, conn));
			}
			
			/* get the properties for given subject types */
			
			if (subjectTypes!=null && !subjectTypes.isEmpty()){
				
				StringBuilder buf = new StringBuilder("select distinct SUBJECT from SPO where PREDICATE=").
				append(Hashes.spoHash(Predicates.RDFS_DOMAIN)).append(" and OBJECT_HASH in (").
				append(Util.toCSV(subjectTypes)).append(")");
				
				rs = stmt.executeQuery(buf.toString());			
				List<String> otherPropertiesHashes = new ArrayList<String>();
				while (rs.next()){
					otherPropertiesHashes.add(rs.getString(1));
				}
				
				if (!otherPropertiesHashes.isEmpty()){
					result.putAll(getSubjectLabels(otherPropertiesHashes, conn));
				}
			}
		}
		catch (SQLException e){
			throw new DAOException(e.toString(), e);
		}
		finally{
			SQLUtil.close(rs);
			SQLUtil.close(stmt);
			SQLUtil.close(conn);
		}
		
		return result;
	}
	
	/**
	 * 
	 * @param subjectHashes
	 * @return
	 * @throws SQLException 
	 */
	public HashMap<String,String> getSubjectLabels(Collection<String> subjectHashes, Connection conn) throws SQLException{
		
		HashMap<String,String> result = new HashMap<String,String>();
		boolean closeConnection = false;
		
		Statement stmt = null;
		ResultSet rs = null;
		try{
			if (conn==null){
				conn = getConnection();
				closeConnection = true;
			}
			stmt = conn.createStatement();
			
			StringBuilder buf =
				new StringBuilder("select distinct RESOURCE.URI as SUBJECT_URI, OBJECT as SUBJECT_LABEL").
				append(" from SPO inner join RESOURCE on SPO.SUBJECT=RESOURCE.URI_HASH where SUBJECT in (").
				append(Util.toCSV(subjectHashes)).append(") and PREDICATE=").
				append(Hashes.spoHash(Predicates.RDFS_LABEL)).append(" and LIT_OBJ='Y'");
			
			rs = stmt.executeQuery(buf.toString());
			while (rs.next()){
				result.put(rs.getString("SUBJECT_URI"), rs.getString("SUBJECT_LABEL"));
			}
		}
		finally{
			SQLUtil.close(rs);
			SQLUtil.close(stmt);
			if (closeConnection){
				SQLUtil.close(conn);
			}
		}
		
		return result;
	}
}
