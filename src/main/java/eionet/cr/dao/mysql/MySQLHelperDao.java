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
import eionet.cr.util.Hashes;
import eionet.cr.util.Pair;
import eionet.cr.util.URIUtil;
import eionet.cr.util.Util;
import eionet.cr.util.YesNoBoolean;
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
	private static final String tripleInsertSQL = "insert into SPO (SUBJECT, PREDICATE, OBJECT, OBJECT_HASH, OBJECT_DOUBLE," +
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
	public static final String insertResourceSQL = "insert ignore into RESOURCE" +
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

	/** */
	private static final String getSubjectSchemaUriSQL = "select OBJECT from SPO where SUBJECT=? and PREDICATE="
		+ Hashes.spoHash(Predicates.CR_SCHEMA) + " limit 1";

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HelperDao#getSubjectSchemaUri(java.lang.String)
	 */
	public String getSubjectSchemaUri(String subjectUri) throws DAOException {
		
		if (StringUtils.isBlank(subjectUri))
			return null;
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try{
			conn = getConnection();
			stmt = conn.prepareStatement(getSubjectSchemaUriSQL);
			stmt.setLong(1, Hashes.spoHash(subjectUri));
			rs = stmt.executeQuery();
			return rs.next() ? rs.getString(1) : null;
		}
		catch (SQLException e){
			throw new DAOException(e.toString(), e);
		}
		finally{
			SQLUtil.close(rs);
			SQLUtil.close(stmt);
			SQLUtil.close(conn);
		}
	}
}
