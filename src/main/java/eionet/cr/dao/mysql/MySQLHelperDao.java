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
import eionet.cr.util.Hashes;
import eionet.cr.util.Pair;
import eionet.cr.util.Util;
import eionet.cr.util.YesNoBoolean;
import eionet.cr.util.sql.PairReader;
import eionet.cr.util.sql.SQLUtil;
import eionet.cr.util.sql.SingleObjectReader;


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
	 * @see eionet.cr.dao.HelperDao#getRecentlyDiscoveredFiles()
	 * {@inheritDoc}
	 */
	public List<Pair<String, String>> getRecentlyDiscoveredFiles(int limit) throws DAOException {
		//workaround as mysql doesn't allow limit in subqueries
		//first let's select needed uri_hash
		String sql = "SELECT DISTINCT RESOURCE.URI_HASH FROM RESOURCE INNER JOIN SPO ON RESOURCE.URI_HASH = SPO.SUBJECT" +
				" WHERE SPO.PREDICATE= ? AND OBJECT_HASH= ? ORDER BY FIRSTSEEN_TIME DESC LIMIT ?;";
		List<Long> params = new LinkedList<Long>();
		params.add(Hashes.spoHash(Predicates.RDF_TYPE));
		params.add( Hashes.spoHash(Subjects.CR_FILE));
		params.add(new Long(limit));
		
		List<Long> result = executeQuery(sql, params, new SingleObjectReader<Long>());
		logger.debug(result);
		//now let's fetch labels
		sql = "SELECT DISTINCT SPO.SUBJECT as id, SPO.OBJECT as value FROM RESOURCE INNER JOIN SPO ON RESOURCE.URI_HASH = SPO.SUBJECT" +
				" WHERE SPO.PREDICATE= ? AND SPO.SUBJECT IN (" + Util.toCSV(result) + 
				") ORDER BY FIRSTSEEN_TIME DESC LIMIT ?;";
		logger.debug(sql);
		params.clear();
		params.add(Hashes.spoHash(Predicates.RDFS_LABEL));
		params.add(new Long(limit));
		List<Pair<String, String>> resultList = executeQuery(sql, params, new PairReader<String, String>());
		logger.debug(resultList);
		
		return resultList;
	}

	/** */
	private static final String sqlQuery = "select distinct OBJECT from SPO where PREDICATE=? and LIT_OBJ='Y' and ANON_OBJ='N' order by OBJECT asc";

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
