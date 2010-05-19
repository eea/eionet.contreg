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
package eionet.cr.dao.postgre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
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
import eionet.cr.dao.HelperDAO;
import eionet.cr.dao.readers.DataflowPicklistReader;
import eionet.cr.dao.readers.PredicateLabelsReader;
import eionet.cr.dao.readers.RawTripleDTOReader;
import eionet.cr.dao.readers.SubPropertiesReader;
import eionet.cr.dao.readers.UriHashesReader;
import eionet.cr.dao.util.PredicateLabels;
import eionet.cr.dao.util.SubProperties;
import eionet.cr.dao.util.UriLabelPair;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.RawTripleDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.harvest.statistics.dto.HarvestUrgencyScoreDTO;
import eionet.cr.harvest.statistics.dto.HarvestedUrlCountDTO;
import eionet.cr.util.Hashes;
import eionet.cr.util.Pair;
import eionet.cr.util.URIUtil;
import eionet.cr.util.Util;
import eionet.cr.util.YesNoBoolean;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.util.sql.PairReader;
import eionet.cr.util.sql.SQLUtil;
import eionet.cr.util.sql.SingleObjectReader;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class PostgreSQLHelperDAO extends PostgreSQLBaseDAO implements HelperDAO{

	/** */
	public static final String insertResourceSQL = "insert into RESOURCE" +
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
	private static final String tripleInsertSQL = "insert into SPO (SUBJECT, PREDICATE, OBJECT," +
			" OBJECT_HASH, OBJECT_DOUBLE, ANON_SUBJ, ANON_OBJ, LIT_OBJ, OBJ_LANG," +
			" OBJ_DERIV_SOURCE, OBJ_DERIV_SOURCE_GEN_TIME, OBJ_SOURCE_OBJECT, SOURCE," +
			" GEN_TIME) values (?, ?, ?, ?, ?," +
			" cast(? as ynboolean), cast(? as ynboolean), cast(? as ynboolean), ?, ?, ?, ?, ?, ?)";
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
						pstmt.setLong(  10, object.getDerivSourceUri()==null ?
								0 : Hashes.spoHash(object.getDerivSourceUri()));
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
				conn.setAutoCommit(false);
				pstmt.executeBatch();
				conn.commit();
			}
		}
		catch (SQLException e){
			SQLUtil.rollback(conn);
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
	 * @see eionet.cr.dao.HelperDAO#deleteTriples(eionet.cr.dto.SubjectDTO)
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
	public HashMap<String,String> getAddibleProperties(Collection<String> subjectTypes)
																		throws DAOException {
		
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
				
				StringBuilder buf = new StringBuilder().
				append("select distinct SUBJECT from SPO where PREDICATE=").
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
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	private HashMap<String,String> getSubjectLabels(Collection<String> subjectHashes, Connection conn) throws SQLException{
		
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
			
			StringBuilder buf = new StringBuilder().
			append("select distinct RESOURCE.URI as SUBJECT_URI, OBJECT as SUBJECT_LABEL from").
			append(" SPO inner join RESOURCE on SPO.SUBJECT=RESOURCE.URI_HASH where SUBJECT in (").
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

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getLatestFiles(int)
	 */
	public List<Pair<String, String>> getLatestFiles(int limit) throws DAOException {
		
		// Get the hashes and URIs of recent subjects of type=cr:file
		// (we need URIs, because we might need to derive labels from them).
		
		String sql = "SELECT RESOURCE.URI_HASH, RESOURCE.URI, FIRSTSEEN_TIME" +
				" FROM RESOURCE" +
				" WHERE URI_HASH in" +
				" (select SUBJECT from SPO" +
				" where SPO.PREDICATE=? AND OBJECT_HASH=?)" +
				" ORDER BY FIRSTSEEN_TIME DESC LIMIT ?";
		
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
			pstmt.setInt(3, Math.max(1, limit));
			
			rs = pstmt.executeQuery();
			while (rs.next()){
				uriMap.put(rs.getString(1), rs.getString(2));
				labelMap.put(rs.getString(1), "");
			}

			/* if any subjects were found, let's find their labels */
			if (!labelMap.isEmpty()){
				
				sql = "SELECT SUBJECT,OBJECT FROM SPO WHERE SPO.PREDICATE=? " +
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
	private static final String sqlPicklist = "select distinct OBJECT from SPO" +
			" where PREDICATE=? and LIT_OBJ='Y' order by OBJECT asc";
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getPicklistForPredicate(java.lang.String)
	 */
	public Collection<String> getPicklistForPredicate(String predicateUri) throws DAOException {
		
		if (StringUtils.isBlank(predicateUri)) {
			return Collections.emptyList();
		}
		
		List<String> resultList = executeQuery(
				sqlPicklist,
				Collections.singletonList((Object)Hashes.spoHash(predicateUri)),
				new SingleObjectReader<String>());
		return resultList;
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#isAllowLiteralSearch(java.lang.String)
	 */
	public boolean isAllowLiteralSearch(String predicateUri) throws DAOException {
		return false;
	}

	/** */
	private static final String getPredicatesUsedForType_SQL =
		"select distinct PREDICATE from SPO where SUBJECT in " +
		"(select distinct SUBJECT from SPO where PREDICATE=" +
		Hashes.spoHash(Predicates.RDF_TYPE) + " and OBJECT_HASH=? )";
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getPredicatesUsedForType(java.lang.String)
	 */
	public List<SubjectDTO> getPredicatesUsedForType(String typeUri) throws DAOException{
		
		ArrayList<Object> values = new ArrayList<Object>();
		values.add(Long.valueOf(Hashes.spoHash(typeUri)));
		
		long startTime = System.currentTimeMillis();

		List<Long> predicateUris = executeQuery(
				getPredicatesUsedForType_SQL, values, new SingleObjectReader<Long>());
		logger.trace("usedPredicates query took " + Util.durationSince(startTime));
		
		if (predicateUris==null || predicateUris.isEmpty()){
			return new ArrayList<SubjectDTO>();
		}
		else{
			// get the SubjectDTO objects of the found predicates
			Map<Long,SubjectDTO> subjectsMap = new HashMap<Long,SubjectDTO>();
			for (Long hash : predicateUris) {
				subjectsMap.put(hash, null);
			}

			// get the subjects data
			getSubjectsData(subjectsMap);
			
			// since a used predicate may not appear as a subject in SPO,
			// there might unfound SubjectDTO objects 
			HashSet<Long> unfoundSubjects = new HashSet<Long>();
			for (Entry<Long,SubjectDTO> entry : subjectsMap.entrySet()){
				if (entry.getValue()==null){
					unfoundSubjects.add(entry.getKey());
				}
			}
			
			// if there were indeed any unfound SubjectDTO objects, find URIs for those predicates
			// and create dummy SubjectDTO objects from those URIs
			if (!unfoundSubjects.isEmpty()){
				Map<Long,String> resourceUris = getResourceUris(unfoundSubjects);
				for (Entry<Long,SubjectDTO> entry : subjectsMap.entrySet()){
					if (entry.getValue()==null){
						String uri = resourceUris.get(entry.getKey());
						if (!StringUtils.isBlank(uri)){
							unfoundSubjects.remove(entry.getKey());
							entry.setValue(new SubjectDTO(uri, false));
						}
					}
				}
			}
			
			// clean the subjectsMap of unfound subjects 
			for (Long hash : unfoundSubjects){
				subjectsMap.remove(hash);
			}
			
			return new LinkedList<SubjectDTO>( subjectsMap.values());
		}
	}

	/**
	 * 
	 * @param resourceHashes
	 * @return
	 * @throws DAOException 
	 */
	private Map<Long,String> getResourceUris(HashSet<Long> resourceHashes) throws DAOException{
		
		StringBuffer buf = new StringBuffer().
		append("select URI_HASH, URI from RESOURCE where URI_HASH in (").
		append(Util.toCSV(resourceHashes)).append(")");
		
		HashMap<Long,String> result = new HashMap<Long,String>();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try{
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(buf.toString());
			while (rs.next()){
				String uri = rs.getString("URI");
				if (!StringUtils.isBlank(uri)){
					result.put(Long.valueOf(rs.getLong("URI_HASH")), uri);
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

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getSampleTriples(java.lang.String, int)
	 */
	public List<RawTripleDTO> getSampleTriples(String url, int limit)
																	throws DAOException {
		
		StringBuffer buf = new StringBuffer("select * from SPO where SOURCE=").
		append(Hashes.spoHash(url)).
		append(" limit ").append(Math.max(1, limit));
		
		RawTripleDTOReader reader = new RawTripleDTOReader();
		List<RawTripleDTO> triples = executeQuery(buf.toString(), new LinkedList<Object>(), reader);
		
		if (!triples.isEmpty() && !reader.getDistinctHashes().isEmpty()){
			
			buf = new StringBuffer().
			append("select URI_HASH, URI from RESOURCE where URI_HASH in (").
			append(Util.toCSV(reader.getDistinctHashes())).append(")");
			
			HashMap<String,String> map = new HashMap<String, String>();
			executeQuery(buf.toString(), new UriHashesReader(map));
			
			if (!map.isEmpty()){				
				for (RawTripleDTO dto : triples){					
					dto.setSubject(map.get(dto.getSubject()));
					dto.setPredicate(map.get(dto.getPredicate()));
					dto.setObjectDerivSource(map.get(dto.getObjectDerivSource()));
				}
			}
		}
		
		return triples;
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getSpatialSources()
	 */
	public List<String> getSpatialSources() throws DAOException {
		
		String sql = "select distinct URI from SPO,RESOURCE where " +
		"PREDICATE= ? and OBJECT_HASH= ? and SOURCE=RESOURCE.URI_HASH";
		
		List<Long> params = new LinkedList<Long>();
		params.add(Hashes.spoHash(Predicates.RDF_TYPE));
		params.add(Hashes.spoHash(Subjects.WGS_POINT));
		
		return executeQuery(sql, params, new SingleObjectReader<String>());
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getSubject(java.lang.String)
	 */
	public SubjectDTO getSubject(Long subjectHash) throws DAOException {
		
		if (subjectHash==null){
			return null;
		}
		
		Map<Long,SubjectDTO> map = new LinkedHashMap<Long, SubjectDTO>();
		map.put(subjectHash, null);
		
		List<SubjectDTO> subjects = getSubjectsData(map);
		return subjects==null || subjects.isEmpty() ? null : subjects.get(0);
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getPredicateLabels(java.util.Set)
	 */
	public PredicateLabels getPredicateLabels(Set<Long> subjectHashes) throws DAOException {
		
		PredicateLabels predLabels = new PredicateLabels();
		if (subjectHashes!=null && !subjectHashes.isEmpty()){
			
			StringBuffer sqlBuf = new StringBuffer().
			append("select RESOURCE.URI as PREDICATE_URI, SPO.OBJECT as LABEL, SPO.OBJ_LANG as LANG").
			append(" from SPO, RESOURCE").
			append(" where SPO.SUBJECT in (").append(Util.toCSV(subjectHashes)).append(")").
			append(" and SPO.PREDICATE=").append(Hashes.spoHash(Predicates.RDFS_LABEL)).
			append(" and SPO.LIT_OBJ='Y'").
			append(" and SPO.SUBJECT=RESOURCE.URI_HASH");
			
			executeQuery(sqlBuf.toString(), new PredicateLabelsReader(predLabels));
		}
		
		return predLabels;
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getSubProperties(java.util.Set)
	 */
	public SubProperties getSubProperties(Set<Long> subjectHashes) throws DAOException {
		
		SubProperties subProperties = new SubProperties();
		if (subjectHashes!=null && !subjectHashes.isEmpty()){
			
			StringBuffer sqlBuf = new StringBuffer().
			append("select distinct SPO.OBJECT as PREDICATE, RESOURCE.URI as SUB_PROPERTY").
			append(" from SPO, RESOURCE").
			append(" where SPO.OBJECT_HASH in (").append(Util.toCSV(subjectHashes)).append(")").
			append(" and SPO.PREDICATE=").append(Hashes.spoHash(Predicates.RDFS_SUBPROPERTY_OF)).
			append(" and SPO.LIT_OBJ='N' and SPO.ANON_OBJ='N'").
			append(" and SPO.SUBJECT=RESOURCE.URI_HASH");

			executeQuery(sqlBuf.toString(), new SubPropertiesReader(subProperties));
		}
		
		return subProperties;
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getLatestSubjects(int)
	 */
	public Collection<SubjectDTO> getLatestSubjects(String rdfType, int limit) throws DAOException {
		
		// validate arguments
		if (StringUtils.isBlank(rdfType))
			throw new IllegalArgumentException("rdfType must not be blank!");
		if (limit<=0)
			throw new IllegalArgumentException("limit must be greater than 0!");

		// build SQL query
//		StringBuffer sqlBuf = new StringBuffer().
//		append("select SPO.SUBJECT as ").append(PairReader.LEFTCOL).
//		append(", RESOURCE.FIRSTSEEN_TIME as ").append(PairReader.RIGHTCOL).
//		append(" from SPO, RESOURCE").
//		append(" where SPO.PREDICATE=").append(Hashes.spoHash(Predicates.RDF_TYPE)).
//		append(" and SPO.OBJECT_HASH=").append(Hashes.spoHash(rdfType)).
//		append(" and SPO.SUBJECT=RESOURCE.URI_HASH").
//		append(" order by RESOURCE.FIRSTSEEN_TIME desc").
//		append(" limit ").append(limit);
		
		StringBuffer sqlBuf = new StringBuffer().
		append("select RESOURCE.URI_HASH as ").append(PairReader.LEFTCOL).
		append(", RESOURCE.FIRSTSEEN_TIME as ").append(PairReader.RIGHTCOL).
		append(" from RESOURCE where URI_HASH in (select SUBJECT from SPO where ").
		append(" SPO.PREDICATE=").append(Hashes.spoHash(Predicates.RDF_TYPE)).
		append(" and SPO.OBJECT_HASH=").append(Hashes.spoHash(rdfType)).
		append(" ) order by RESOURCE.FIRSTSEEN_TIME desc limit ").append(Math.max(1, limit));

		
		// execute SQL query
		PairReader<Long,Long> pairReader = new PairReader<Long,Long>();
		
		long startTime = System.currentTimeMillis();
		logger.trace("Recent uploads search, executing subject finder query: " + sqlBuf.toString());

		executeQuery(sqlBuf.toString(), pairReader);
		List<Pair<Long,Long>> pairList = pairReader.getResultList();
		
		Collection<SubjectDTO> result = new LinkedList<SubjectDTO>();
		
		// if result list not empty, get the subjects data and set their first-seen times		
		if (pairList!=null && !pairList.isEmpty()){
			
			// create helper objects
			Map<Long,SubjectDTO> subjectsMap = new LinkedHashMap<Long, SubjectDTO>();
			Map<Long,Date> firstSeenTimes = new HashMap<Long, Date>(); 
			for (Pair<Long,Long> p : pairList){
				subjectsMap.put(p.getLeft(), null);
				firstSeenTimes.put(p.getLeft(), new Date(p.getRight()));
			}
			
			// get subjects data
			
			logger.trace("Recent uploads search, getting the data of the found subjects");
			
			getSubjectsData(subjectsMap);
			
			// set firstseen-times of found subjects
			for (SubjectDTO subject : subjectsMap.values()){
				subject.setFirstSeenTime(
						firstSeenTimes.get(new Long(subject.getUriHash())));
			}
			
			result = subjectsMap.values();
		}
		
		logger.debug("Recent uploads search, total query time " + Util.durationSince(startTime));

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getSubjectsNewerThan(java.util.Date, int)
	 */
	public List<SubjectDTO> getSubjectsNewerThan(Date timestamp, int limit) throws DAOException {
		
		// validate arguments
		if (timestamp==null || timestamp.after(new Date()))
			throw new IllegalArgumentException("timestamp must not be null or after current time!");
		if (limit<=0)
			throw new IllegalArgumentException("limit must be greater than 0!");

		// build SQL query
		StringBuffer sqlBuf = new StringBuffer().
		append("select SPO.SUBJECT as SUBJECT_HASH from SPO, RESOURCE").
		append(" where SPO.PREDICATE=? and SPO.OBJECT_HASH=? and SPO.SUBJECT=RESOURCE.URI_HASH ").
		append(" and RESOURCE.FIRSTSEEN_TIME>?").
		append(" order by RESOURCE.FIRSTSEEN_TIME desc").
		append(" limit ").append(limit);
		
		ArrayList<Object> inParameters = new ArrayList<Object>();
		inParameters.add(Hashes.spoHash(Predicates.RDF_TYPE));
		inParameters.add(Hashes.spoHash(Subjects.CR_FILE));
		inParameters.add(Long.valueOf(timestamp.getTime()));
		
		// execute SQL query
		SingleObjectReader<Long> reader = new SingleObjectReader<Long>();
		executeQuery(sqlBuf.toString(), inParameters, reader);
		List<Long> resultList = reader.getResultList();
		
		// if result list null or empty, return
		if (resultList==null || resultList.isEmpty()){
			return new LinkedList<SubjectDTO>();
		}
		
		// create helper objects
		Map<Long,SubjectDTO> subjectsMap = new HashMap<Long, SubjectDTO>();
		for (Long subjectHash : resultList){
			subjectsMap.put(subjectHash, null);
		}
		
		// get subjects data
		List<SubjectDTO> result = getSubjectsData(subjectsMap);
		return result;
	}

	/** */
	private static final String getSubjectSchemaUriSQL =
		"select OBJECT from SPO where SUBJECT=? and PREDICATE="
		+ Hashes.spoHash(Predicates.CR_SCHEMA) + " limit 1";
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getSubjectSchemaUri(java.lang.String)
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

	/** */
	private static final String dataflowPicklistSQL = new StringBuffer().
		append("select distinct ").
			append("INSTRUMENT_TITLE.OBJECT as INSTRUMENT_TITLE, ").
			append("OBLIGATION_TITLE.OBJECT as OBLIGATION_TITLE, ").
			append("OBLIGATION_URI.URI as OBLIGATION_URI ").
		append("from ").
			append("SPO as OBLIGATION_TITLE ").
			append("left join RESOURCE as OBLIGATION_URI on OBLIGATION_TITLE.SUBJECT=OBLIGATION_URI.URI_HASH ").
			append("left join SPO as OBLIGATION_INSTR on OBLIGATION_TITLE.SUBJECT=OBLIGATION_INSTR.SUBJECT ").
			append("left join SPO as INSTRUMENT_TITLE on OBLIGATION_INSTR.OBJECT_HASH=INSTRUMENT_TITLE.SUBJECT ").			
		append("where ").
			append("OBLIGATION_TITLE.PREDICATE=").append(Hashes.spoHash(Predicates.DC_TITLE)).
			append(" and OBLIGATION_TITLE.LIT_OBJ='Y' and OBLIGATION_INSTR.PREDICATE=").
			append(Hashes.spoHash(Predicates.ROD_INSTRUMENT_PROPERTY)).
			append(" and OBLIGATION_INSTR.LIT_OBJ='N' and OBLIGATION_INSTR.ANON_OBJ='N'").
			append(" and INSTRUMENT_TITLE.PREDICATE=").append(Hashes.spoHash(Predicates.DCTERMS_ALTERNATIVE)).
			append(" and INSTRUMENT_TITLE.LIT_OBJ='Y' ").
		append("order by ").
			append("INSTRUMENT_TITLE.OBJECT, OBLIGATION_TITLE.OBJECT ").toString();
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getDataflowSearchPicklist()
	 */
	public HashMap<String, ArrayList<UriLabelPair>> getDataflowSearchPicklist()
	                                                                    throws DAOException {
		
		DataflowPicklistReader reader = new DataflowPicklistReader();
		executeQuery(dataflowPicklistSQL, reader);
		return reader.getResultMap();
	}

	private static final String getDistinctOrderedTypesSQL =
		"";
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getDistinctOrderedTypes()
	 */
	public ArrayList<Pair<String, String>> getDistinctOrderedTypes() throws DAOException {
		
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getSubjectCountInSource(long)
	 */
	public int getSubjectCountInSource(long sourceHash) throws DAOException {
		
		Connection conn = null;
		try{
			conn = getConnection();
			Object o = SQLUtil.executeSingleReturnValueQuery(
					"select count(distinct SUBJECT) from SPO where SOURCE=" + sourceHash, conn);
			return (o==null || StringUtils.isBlank(o.toString()))
					? 0 : Integer.parseInt(o.toString());
		}
		catch (SQLException e){
			throw new DAOException(e.toString(), e);
		}
		finally{
			SQLUtil.close(conn);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getLatestHarvestedURLs()
	 */
	public Pair <Integer, List <HarvestedUrlCountDTO>> getLatestHarvestedURLs(int days) throws DAOException {

		StringBuffer buf = new StringBuffer().
		append("SELECT DATE(LAST_HARVEST) AS HARVESTDAY, COUNT(HARVEST_SOURCE_ID) AS HARVESTS").
		append(" FROM HARVEST_SOURCE WHERE LAST_HARVEST IS NOT NULL").
		append(" AND LAST_HARVEST + INTERVAL '"+days+" days' > current_date").
		append(" GROUP BY DATE(LAST_HARVEST);");

		List <HarvestedUrlCountDTO> result = new ArrayList();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try{
			SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd");
			
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(buf.toString());
			while (rs.next()){
				HarvestedUrlCountDTO resultRow = new HarvestedUrlCountDTO();
				try {
					resultRow.setHarvestDay(sdf.parse(rs.getString("HARVESTDAY")));
				} catch (ParseException ex){
					throw new DAOException(ex.toString(), ex);
				}
				resultRow.setHarvestCount(rs.getLong("HARVESTS"));
				result.add(resultRow);
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
		
		return new Pair<Integer, List<HarvestedUrlCountDTO>>(result.size(), result);
	}
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getUrgencyOfComingHarvests()
	 */
	public Pair <Integer, List <HarvestUrgencyScoreDTO>> getUrgencyOfComingHarvests(int amount) throws DAOException {

		
		
		StringBuffer buf = new StringBuffer().	
		append("SELECT url, last_harvest, interval_minutes, ").
		append(" EXTRACT (EPOCH FROM NOW()-(coalesce(last_harvest,").
		append(" (time_created - interval_minutes * interval '1 minute') ").
		append(" )))/(interval_minutes * 60) AS urgency ").
		append(" FROM HARVEST_SOURCE ").
		append(" WHERE interval_minutes > 0 ").
		append(" ORDER BY urgency DESC ").
		append(" LIMIT "+amount+" ");

		List <HarvestUrgencyScoreDTO> result = new ArrayList();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try{
			SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd hh:mm:ss");
			
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(buf.toString());
			while (rs.next()){
				HarvestUrgencyScoreDTO resultRow = new HarvestUrgencyScoreDTO();
				resultRow.setUrl(rs.getString("url"));
				try {
					resultRow.setLastHarvest(sdf.parse(rs.getString("last_harvest")+""));
				} catch (ParseException ex){
					resultRow.setLastHarvest(null);
					//throw new DAOException(ex.toString(), ex);
				}
				resultRow.setIntervalMinutes(rs.getLong("interval_minutes"));
				resultRow.setUrgency(rs.getDouble("urgency"));
				result.add(resultRow);
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
		
		return new Pair<Integer, List<HarvestUrgencyScoreDTO>>(result.size(), result);
		
	}
	
	
	public boolean isUrlInHarvestSource(String url) throws DAOException
	{
		StringBuffer buf = new StringBuffer().	
		append("SELECT url FROM harvest_source WHERE url='"+url+"' ");
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		boolean returnValue = false;
		
		try{
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(buf.toString());
			if (rs.next()){
				returnValue = true;
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
		
		return returnValue;
	}
	
}
