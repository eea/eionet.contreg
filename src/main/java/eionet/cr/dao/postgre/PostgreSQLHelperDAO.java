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
import eionet.cr.dao.readers.SampleTriplesReader;
import eionet.cr.dao.readers.SubPropertiesReader;
import eionet.cr.dao.readers.SubjectDataReader;
import eionet.cr.dao.readers.UriHashesReader;
import eionet.cr.dao.util.PredicateLabels;
import eionet.cr.dao.util.SubProperties;
import eionet.cr.dao.util.UriLabelPair;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.ReviewDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.dto.TripleDTO;
import eionet.cr.dto.UserBookmarkDTO;
import eionet.cr.dto.UserHistoryDTO;
import eionet.cr.harvest.statistics.dto.HarvestUrgencyScoreDTO;
import eionet.cr.harvest.statistics.dto.HarvestedUrlCountDTO;
import eionet.cr.util.Hashes;
import eionet.cr.util.Pair;
import eionet.cr.util.URIUtil;
import eionet.cr.util.URLUtil;
import eionet.cr.util.Util;
import eionet.cr.util.YesNoBoolean;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.util.sql.PairReader;
import eionet.cr.util.sql.SQLUtil;
import eionet.cr.util.sql.SingleObjectReader;
import eionet.cr.web.security.CRUser;

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
	private static String sqlDeleteTriples = "delete from SPO where SUBJECT=? and PREDICATE=? and OBJECT_HASH=?";

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#deleteTriples2(java.util.Collection)
	 */
	public void deleteTriples(Collection<TripleDTO> triples) throws DAOException{
		
		if (triples==null || triples.isEmpty()){
			return;
		}
		
		Statement stmt = null;
		Connection conn = null;
		try{
			conn = getConnection();
			stmt = conn.createStatement();
			
			for (TripleDTO triple : triples){

				String sql = StringUtils.replace(
						sqlDeleteTriples, "?", String.valueOf(triple.getSubjectHash()), 1);
				sql = StringUtils.replace(sql, "?", String.valueOf(triple.getPredicateHash()), 1);
				sql = StringUtils.replace(sql, "?", String.valueOf(triple.getObjectHash()), 1);

				StringBuilder bld = new StringBuilder(sql);
				if (triple.getSourceHash()!=null){
					bld.append(" and SOURCE=").append(triple.getSourceHash());
				}
				if (triple.getObjectDerivSourceHash()!=null){
					bld.append(" and OBJ_DERIV_SOURCE=").append(triple.getObjectDerivSourceHash());
				}
				if (triple.getObjectSourceObjectHash()!=null){
					bld.append(" and OBJ_SOURCE_OBJECT=").append(triple.getObjectSourceObjectHash());
				}

				stmt.addBatch(bld.toString());
			}
			
			stmt.executeBatch();
		}
		catch (SQLException e){
			throw new DAOException(e.toString(), e);
		}
		finally{
			SQLUtil.close(stmt);
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
				String uri = uriMap.get(uriHash);
				result.add(new Pair(uriHash, URIUtil.extractURILabel(uri,uri)));
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
	/** */
	private static final String getPredicatesUsedForTypeCache_SQL =
		"select distinct PREDICATE from cache_SPO_TYPE_PREDICATE where OBJECT_HASH=? ";
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getPredicatesUsedForType(java.lang.String)
	 */
	public List<SubjectDTO> getPredicatesUsedForType(String typeUri) throws DAOException{
		
		ArrayList<Object> values = new ArrayList<Object>();
		values.add(Long.valueOf(Hashes.spoHash(typeUri)));
		
		long startTime = System.currentTimeMillis();
		List<Long> predicateUris = null;
		boolean useCache = true;
		try{
			predicateUris = executeQuery(
				getPredicatesUsedForTypeCache_SQL, values, new SingleObjectReader<Long>());
		}
		catch(DAOException e){
			logger.warn("Cache tables are not created yet. Continue with spo table");
			predicateUris = executeQuery(
				getPredicatesUsedForType_SQL, values, new SingleObjectReader<Long>());
			useCache = false;
		}
		logger.trace("usedPredicatesForType query took " + Util.durationSince(startTime));
		
		if (predicateUris==null || predicateUris.isEmpty()){
			return new ArrayList<SubjectDTO>();
		}
		else{
			// get the SubjectDTO objects of the found predicates
			Map<Long,SubjectDTO> subjectsMap = new HashMap<Long,SubjectDTO>();
			for (Long hash : predicateUris) {
				subjectsMap.put(hash, null);
			}

			//restrict the query with specified predicates. For types we are interested only in labels 
			SubjectDataReader reader = new SubjectDataReader(subjectsMap);
			reader.addPredicateHash(Hashes.spoHash(Predicates.RDFS_LABEL));
			
			// get the subjects data
			if(useCache){ 	
				getSubjectsData(reader, StringUtils.replace(getPredicatesUsedForTypeCache_SQL, "?", String.valueOf(Hashes.spoHash(typeUri))));
			}
			else{
				getSubjectsData(reader);
			}
			
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
		append(" SELECT DATE(LAST_HARVEST) AS HARVESTDAY, COUNT(HARVEST_SOURCE_ID) AS HARVESTS").
		append(" FROM HARVEST_SOURCE WHERE LAST_HARVEST IS NOT NULL").
		append(" AND LAST_HARVEST + INTERVAL '"+days+" days' > current_date").
		append(" GROUP BY DATE(LAST_HARVEST) ORDER BY HARVESTDAY DESC ;");

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
	
	/** */
	private static final String getUpdateTypeData_SQL =
		"delete from cache_SPO_TYPE; " +
		"insert into cache_SPO_TYPE " +
			"select distinct SUBJECT from SPO where PREDICATE=" +
			Hashes.spoHash(Predicates.RDF_TYPE) +
			" and OBJECT_HASH=" + Hashes.spoHash(Subjects.RDFS_CLASS) + ";" +
		"delete from cache_SPO_TYPE_SUBJECT; " + 
		"insert into cache_SPO_TYPE_SUBJECT " +
			"select distinct object_hash, subject from SPO where PREDICATE=" +
			Hashes.spoHash(Predicates.RDF_TYPE) + " and OBJECT_HASH in (select subject from cache_SPO_TYPE);"; 
	/** */
	private static final String getUpdateTypePredicateData_SQL =
		"create table temp_SPO_TYPE_PREDICATE as " +
			"select distinct ct.object_hash, spo.predicate from SPO, cache_SPO_TYPE_SUBJECT as ct where " +
			"spo.subject=ct.subject; " + 
		"drop table cache_SPO_TYPE_PREDICATE; " +
		"alter table temp_SPO_TYPE_PREDICATE rename to cache_SPO_TYPE_PREDICATE; " +
		"vacuum analyze temp_SPO_TYPE_PREDICATE; ";

	
	
	
	/**
	 * 
	 */
	public void updateTypeDataCache() throws DAOException {
		
		long startTime = System.currentTimeMillis();
		logger.trace("updateTypeDataCache query is: " + getUpdateTypeData_SQL);		
		execute(getUpdateTypeData_SQL, null);		
		logger.debug("updateTypeDataCache query took " + Util.durationSince(startTime));

		//startTime = System.currentTimeMillis();
		//logger.trace("updateTypeDataPredicatesCache query is: " + getUpdateTypePredicateData_SQL);
		//execute(getUpdateTypePredicateData_SQL, null);		
		//logger.debug("updateTypeDataPredicatesCache query took " + Util.durationSince(startTime));
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#registerUserUrl(eionet.cr.web.security.CRUser, java.lang.String, boolean)
	 */
	public void registerUserUrl(CRUser user, String url, boolean isBookmark) throws DAOException {
		
		// input arguments sanity checking
		
		if (user==null || StringUtils.isBlank(user.getUserName()))
			throw new IllegalArgumentException("user must not be null and must have user name");
		if (URLUtil.isURL(url)==false)
			throw new IllegalArgumentException("url must not be null and must be valid URL");
		
		// get the subject that the user wants to register
		SubjectDTO registeredSubject = getSubject(Hashes.spoHash(url));

		// if subject did not exist or it isn't registered in user's registrations yet,
		// then add the necessary triples
		if (registeredSubject==null || !registeredSubject.isRegisteredBy(user)){
			
			boolean subjectFirstSeen = registeredSubject==null;
			
			// add the rdf:type=cr:File triple into user's registrations
			
			registeredSubject = new SubjectDTO(url, registeredSubject==null ? false : registeredSubject.isAnonymous());
			ObjectDTO objectDTO = new ObjectDTO(Subjects.CR_FILE, false);
			objectDTO.setSourceUri(user.getRegistrationsUri());
			registeredSubject.addObject(Predicates.RDF_TYPE, objectDTO);
			
			addTriples(registeredSubject);
			
			// if this is the first time this subject is seen, store it in RESOURCE
			if (subjectFirstSeen){
				addResource(url, user.getRegistrationsUri());
			}

			// let the user registrations' URI be stored in RESOURCE
			addResource(user.getRegistrationsUri(), user.getRegistrationsUri());

			// add the URL into user's history
			
			SubjectDTO userHomeItemSubject = new SubjectDTO(user.getHomeItemUri(url), false);
			objectDTO = new ObjectDTO(Util.dateToString(new Date(), "yyyy-MM-dd'T'HH:mm:ss"), true);
			objectDTO.setSourceUri(user.getHistoryUri());
			userHomeItemSubject.addObject(Predicates.CR_SAVETIME, objectDTO);

			objectDTO = new ObjectDTO(url, false);
			objectDTO.setSourceUri(user.getHistoryUri());
			userHomeItemSubject.addObject(Predicates.CR_HISTORY, objectDTO);

			// add the URL also into user's bookmarks if requested
			
			if (isBookmark){
				objectDTO = new ObjectDTO(url, false);
				objectDTO.setSourceUri(user.getBookmarksUri());
				userHomeItemSubject.addObject(Predicates.CR_BOOKMARK, objectDTO);
			}
			
			// store the history and bookmark triples
			addTriples(userHomeItemSubject);

			// let the user home item subject URI be stored in RESOURCE
			addResource(userHomeItemSubject.getUri(), user.getBookmarksUri());
			
			// store Predicates.CR_SAVETIME, Predicates.CR_HISTORY and user.getHistoryUri()
			// in RESOURCE table
			addResource(Predicates.CR_SAVETIME, user.getHistoryUri());
			addResource(Predicates.CR_HISTORY, user.getHistoryUri());
			addResource(user.getHistoryUri(), user.getHistoryUri());
			
			// store Predicates.CR_BOOKMARK and user.getBookmarksUri() in RESOURCE table
			if (isBookmark){				
				addResource(Predicates.CR_BOOKMARK, user.getRegistrationsUri());
				addResource(user.getBookmarksUri(), user.getBookmarksUri());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#addUserBookmark(eionet.cr.web.security.CRUser, java.lang.String)
	 */
	public void addUserBookmark(CRUser user, String url) throws DAOException {
		
		if (user==null || StringUtils.isBlank(user.getUserName()))
			throw new IllegalArgumentException("user must not be null and must have user name");
		if (URLUtil.isURL(url)==false)
			throw new IllegalArgumentException("url must not be null and must be valid URL");

		SubjectDTO userHomeItemSubject = new SubjectDTO(user.getHomeItemUri(url), false);
		ObjectDTO objectDTO = new ObjectDTO(url, false);
		objectDTO.setSourceUri(user.getBookmarksUri());
		userHomeItemSubject.addObject(Predicates.CR_BOOKMARK, objectDTO);
		
		addTriples(userHomeItemSubject);

		// let the user home item subject URI be stored in RESOURCE
		addResource(userHomeItemSubject.getUri(), user.getBookmarksUri());
		
		// let the bookmark predicate be stored in RESOURCE
		addResource(Predicates.CR_BOOKMARK, user.getRegistrationsUri());
		
		// store the user's bookmarks URI into RESOURCE table
		addResource(user.getBookmarksUri(), user.getBookmarksUri());
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#deleteUserBookmark(eionet.cr.web.security.CRUser, java.lang.String)
	 */
	public void deleteUserBookmark(CRUser user, String url) throws DAOException {

		if (user==null || StringUtils.isBlank(user.getUserName()))
			throw new IllegalArgumentException("user must not be null and must have user name");
		if (URLUtil.isURL(url)==false)
			throw new IllegalArgumentException("url must not be null and must be valid URL");

		TripleDTO triple = new TripleDTO(Long.parseLong(user.getHomeItemUri(url)),
				Hashes.spoHash(Predicates.CR_BOOKMARK), Hashes.spoHash(url));
		triple.setSourceHash(Long.valueOf(Hashes.spoHash(user.getBookmarksUri())));
		
		deleteTriples(Collections.singletonList(triple));
	}
	
	@Override
	public List<UserBookmarkDTO> getUserBookmarks(CRUser user) throws DAOException{

		String dbQuery = "select distinct OBJECT " +
				"from SPO where LIT_OBJ='N' and " +
				"PREDICATE="+Hashes.spoHash(Predicates.CR_BOOKMARK)+" and " +
				"SOURCE="+Hashes.spoHash(CRUser.bookmarksUri(user.getUserName()))+"" +
				" ORDER BY OBJECT ASC"; 
		
		List<UserBookmarkDTO> returnBookmarks = new ArrayList<UserBookmarkDTO>();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try{
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(dbQuery);
			while (rs.next()){
				UserBookmarkDTO bookmark = new UserBookmarkDTO();
				bookmark.setBookmarkUrl(rs.getString("object"));
				returnBookmarks.add(bookmark);
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

		return returnBookmarks;
	}
	
	@Override
	public boolean isUrlUserBookmark(CRUser user, String url) throws DAOException{
		String dbQuery = "select count(*) as cnt from SPO " +
				"where OBJECT_HASH=" + Hashes.spoHash(url) + " and " +
				"LIT_OBJ='N' and " +
				"PREDICATE="+Hashes.spoHash(Predicates.CR_BOOKMARK)+ " and " +
				"SOURCE="+ Hashes.spoHash(CRUser.bookmarksUri(user.getUserName())) + "";
		
		int count = 0;
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try{
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(dbQuery);
			while (rs.next()){
				count = Integer.parseInt(rs.getString("cnt")); 
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
		if (count == 0){
			return false;
		} else {
			return true;
		}
	}

	/** */
	private static final String sqlDeleteUserSaveTime =
		"delete from SPO where SUBJECT=? and PREDICATE=? and SOURCE=?";
	private static final String sqlIsUrlInHistory =
		"select count(*) from SPO where SUBJECT=? and PREDICATE=? and OBJECT_HASH=? and SOURCE=? " +
		"and LIT_OBJ='N'";

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#updateUserSaveTime(eionet.cr.web.security.CRUser, java.lang.String)
	 */
	public void updateUserHistory(CRUser user, String url) throws DAOException {
		
		Connection conn = null;
		try{
			conn = getConnection();

			// if URL not yet in user history, add it there
			
			List values = new ArrayList();
			values.add(new Long(Hashes.spoHash(user.getHomeItemUri(url))));
			values.add(new Long(Hashes.spoHash(Predicates.CR_HISTORY)));
			values.add(new Long(Hashes.spoHash(url)));
			values.add(new Long(Hashes.spoHash(user.getHistoryUri())));
			
			Object o = SQLUtil.executeSingleReturnValueQuery(sqlIsUrlInHistory, values, conn);
			if (o==null || o.toString().equals("0")){
				
				SubjectDTO userHomeItemSubject = new SubjectDTO(user.getHomeItemUri(url), false);
				ObjectDTO objectDTO = new ObjectDTO(url, false);
				objectDTO.setSourceUri(user.getHistoryUri());
				userHomeItemSubject.addObject(Predicates.CR_HISTORY, objectDTO);
				
				addTriples(userHomeItemSubject);
			}
			
			// delete the previous save-time first

			values = new ArrayList();
			values.add(new Long(Hashes.spoHash(user.getHomeItemUri(url))));
			values.add(new Long(Hashes.spoHash(Predicates.CR_SAVETIME)));
			values.add(new Long(Hashes.spoHash(user.getHistoryUri())));
			
			SQLUtil.executeUpdate(sqlDeleteUserSaveTime, values, conn);
		}
		catch (SQLException e){
			throw new DAOException(e.toString(), e);
		}
		finally{
			SQLUtil.close(conn);
		}

		// now add new save-time
		
		SubjectDTO userHomeItemSubject = new SubjectDTO(user.getHomeItemUri(url), false);
		ObjectDTO objectDTO = new ObjectDTO(
				Util.dateToString(new Date(), "yyyy-MM-dd'T'HH:mm:ss"), true);
		objectDTO.setSourceUri(user.getHistoryUri());
		userHomeItemSubject.addObject(Predicates.CR_SAVETIME, objectDTO);
		
		addTriples(userHomeItemSubject);
		
		addResource(Predicates.CR_SAVETIME, user.getHistoryUri());
		addResource(Predicates.CR_HISTORY, user.getHistoryUri());
		addResource(user.getHistoryUri(), user.getHistoryUri());
	}
	
	/*
	 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
	 */
	@Override
	public List<UserHistoryDTO> getUserHistory(CRUser user) throws DAOException{
		
		String dbQuery = "select " +
				"SPOHIST.OBJECT as URL, " +
				"SPOSAVE.OBJECT as LAST_UPDATE " +
				"from SPO as SPOHIST, SPO as SPOSAVE " +
				"where " +
				"SPOHIST.PREDICATE=" + Hashes.spoHash(Predicates.CR_HISTORY) +
				"and SPOSAVE.PREDICATE=" + Hashes.spoHash(Predicates.CR_SAVETIME) +
				"and SPOHIST.SUBJECT=SPOSAVE.SUBJECT " +
				"and SPOHIST.SOURCE=" + Hashes.spoHash(user.getHistoryUri()) +
				"and SPOSAVE.SOURCE=" + Hashes.spoHash(user.getHistoryUri()) +
				"order by " +
				"SPOSAVE.GEN_TIME desc";


		List<UserHistoryDTO> returnHistory = new ArrayList<UserHistoryDTO>();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try{
			SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd'T'hh:mm:ss");
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(dbQuery);

			while (rs.next()){
				UserHistoryDTO historyItem = new UserHistoryDTO();
				historyItem.setUrl(rs.getString("URL"));
				historyItem.setLastOperation(rs.getString("LAST_UPDATE"));
				returnHistory.add(historyItem);
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
		
		return returnHistory;
	}
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getTriplesFor(java.lang.String, eionet.cr.util.pagination.PagingRequest)
	 */
	public List<TripleDTO> getSampleTriplesInSource(String sourceUrl, PagingRequest pagingRequest) throws DAOException{
		
		StringBuffer buf = new StringBuffer("select SUBJECT,PREDICATE,OBJECT,OBJ_DERIV_SOURCE").
		append(" from SPO where SOURCE=").append(Hashes.spoHash(sourceUrl));
		
		if (pagingRequest!=null){
			buf.append(" limit ").append(pagingRequest.getItemsPerPage()).
			append(" offset ").append(pagingRequest.getOffset()).
			toString();			
		}
		
		SampleTriplesReader reader = new SampleTriplesReader();
		List<TripleDTO> triples = executeQuery(buf.toString(), new LinkedList<Object>(), reader);
		
		if (!triples.isEmpty() && !reader.getDistinctHashes().isEmpty()){
			
			buf = new StringBuffer().
			append("select URI_HASH, URI from RESOURCE where URI_HASH in (").
			append(Util.toCSV(reader.getDistinctHashes())).append(")");
			
			HashMap<Long,String> urisByHashes = new HashMap<Long, String>();
			executeQuery(buf.toString(), new UriHashesReader(urisByHashes));
			
			if (!urisByHashes.isEmpty()){
				
				for (TripleDTO tripleDTO : triples){
					
					tripleDTO.setSubjectUri(urisByHashes.get(tripleDTO.getSubjectHash()));
					tripleDTO.setPredicateUri(urisByHashes.get(tripleDTO.getPredicateHash()));
					tripleDTO.setObjectDerivSourceUri(
							urisByHashes.get(tripleDTO.getObjectDerivSourceHash()));
				}
			}
		}
		
		return triples;
	}
	
	@Override
	public int getLastReviewId(CRUser user) throws DAOException{
		String subjectUrl = user.getHomeUri();
		
		SubjectDTO subject = new SubjectDTO(subjectUrl, false);
		
		String dbQuery = "select OBJECT as lastid from SPO " +
		"where "+
		"PREDICATE="+Hashes.spoHash(Predicates.CR_USER_REVIEW_LAST_NUMBER)+ " and " +
		"SUBJECT="+ Hashes.spoHash(subjectUrl) + "";

		int lastid = 0;
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try{
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(dbQuery);
			while (rs.next()){
				lastid = Integer.parseInt(rs.getString("lastid")); 
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

		return lastid;
	}

	@Override
	public int generateNewReviewId(CRUser user) throws DAOException{
		
		int currentLastId = getLastReviewId(user);
		// Deleting from the database the old value and creating a new one.
		
		String deleteQuery = "DELETE FROM spo WHERE "+
		"PREDICATE="+Hashes.spoHash(Predicates.CR_USER_REVIEW_LAST_NUMBER)+ " and " +
		"SUBJECT="+ Hashes.spoHash(user.getHomeUri()) + "";
		Connection conn = null;
		Statement stmt = null;
		try{
			conn = getConnection();
			stmt = conn.createStatement();
			stmt.execute(deleteQuery);
		}
		catch (SQLException e){
			throw new DAOException(e.toString(), e);
		}
		finally{
			SQLUtil.close(stmt);
			SQLUtil.close(conn);
		}
		
		// Generating new ID
		int newId = currentLastId + 1;
		
		SubjectDTO newValue = new SubjectDTO(user.getHomeUri(), false);
		ObjectDTO objectDTO = new ObjectDTO(String.valueOf(newId), true);
		objectDTO.setSourceUri(user.getHomeUri());
		
		newValue.addObject(Predicates.CR_USER_REVIEW_LAST_NUMBER, objectDTO);
		
		addTriples(newValue);
		
		addResource(Predicates.CR_USER_REVIEW_LAST_NUMBER, user.getHomeUri());
		addResource(user.getHomeUri(), user.getHomeUri());
		
		return newId;
		
	}
	
	@Override
	public int addReview(ReviewDTO review, CRUser user) throws DAOException{
		int reviewId = generateNewReviewId(user);
		insertReviewToDB(review, user, reviewId);
		return reviewId;
	}
	
	@Override
	public void saveReview(int reviewId, ReviewDTO review, CRUser user) throws DAOException{
		deleteReview(user.getReviewUri(reviewId));
		insertReviewToDB(review, user, reviewId);
	}

	private void insertReviewToDB(ReviewDTO review, CRUser user, int reviewId) throws DAOException{
		
		String userReviewUri = user.getReviewUri(reviewId);
		SubjectDTO newReview = new SubjectDTO(userReviewUri, false);
		
		ObjectDTO typeObject = new ObjectDTO(Subjects.CR_FEEDBACK, false);
		typeObject.setSourceUri(userReviewUri);		
		ObjectDTO titleObject = new ObjectDTO(review.getTitle(), true);
		titleObject.setSourceUri(userReviewUri);
		ObjectDTO feedbackForObject = new ObjectDTO(review.getObjectUrl(), false);
		feedbackForObject.setSourceUri(userReviewUri);
		ObjectDTO feedbackUserObject = new ObjectDTO(user.getHomeUri(), false);
		feedbackUserObject.setSourceUri(userReviewUri);
		
		newReview.addObject(Predicates.RDF_TYPE, typeObject);
		newReview.addObject(Predicates.DC_TITLE, titleObject);
		newReview.addObject(Predicates.RDFS_LABEL, titleObject);
		newReview.addObject(Predicates.CR_FEEDBACK_FOR, feedbackForObject);
		newReview.addObject(Predicates.CR_USER, feedbackUserObject);
		
		addTriples(newReview);
		
		addResource(Subjects.CR_FEEDBACK, userReviewUri);
		addResource(Predicates.DC_TITLE, userReviewUri);
		addResource(Predicates.CR_FEEDBACK_FOR, userReviewUri);
		addResource(Predicates.CR_USER, userReviewUri);
		addResource(userReviewUri, userReviewUri);
		
		// creating a cross link to show that specific object has a review.
		SubjectDTO crossLinkSubject = new SubjectDTO(review.getObjectUrl(), false);
		ObjectDTO grossLinkObject = new ObjectDTO(userReviewUri,false);
		grossLinkObject.setSourceUri(userReviewUri);
		crossLinkSubject.addObject(Predicates.CR_HAS_FEEDBACK, grossLinkObject);
		
		addTriples(crossLinkSubject);
		
		addResource(Predicates.CR_HAS_FEEDBACK, userReviewUri);
		addResource(review.getObjectUrl(), userReviewUri);
		
	}
	
	@Override
	public List<ReviewDTO> getReviewList(CRUser user)  throws DAOException{
		
		String dbQuery = "SELECT uri, spoTitle.object AS title, spoObject.object AS object FROM spo AS spo1, spo AS spo2," +
				" spo AS spoTitle, spo AS spoObject, resource " +
				"WHERE " +
				"(spo1.subject = spo2.subject) AND (spo1.subject = spoTitle.subject) AND (spo1.subject = spoObject.subject) AND " +
				"spoObject.Predicate="+ Hashes.spoHash(Predicates.CR_FEEDBACK_FOR) + "AND " +
				"spoTitle.Predicate="+ Hashes.spoHash(Predicates.DC_TITLE) + "AND " +
				"spo1.subject=resource.uri_hash AND " +
				"(spo1.predicate = " + Hashes.spoHash(Predicates.CR_USER) + ") AND "+
				"(spo1.object_hash = " + Hashes.spoHash(user.getHomeUri()) +") AND " +
				"(spo2.predicate = " + Hashes.spoHash(Predicates.RDF_TYPE) +") AND " +
				"(spo2.object_hash = " + Hashes.spoHash(Subjects.CR_FEEDBACK) +") " +
				"ORDER BY uri ASC"
				;
		
		String resultHashResolveQuery = "SELECT uri FROM resource WHERE uri_hash IN (1,1,1,1)";
		
		List<ReviewDTO> returnList = new ArrayList();
		
		String stringList = "";
		
		int lastid = 0;
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try{
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(dbQuery);
			while (rs.next()){
				ReviewDTO tempReviewDTO = new ReviewDTO();
				tempReviewDTO.setReviewSubjectUri(rs.getString("uri"));
				tempReviewDTO.setTitle(rs.getString("title"));
				tempReviewDTO.setObjectUrl(rs.getString("object"));
				try {
					tempReviewDTO.setReviewID(Integer.parseInt( tempReviewDTO.getReviewSubjectUri().split("reviews/")[1]));
				} catch (Exception ex){
					tempReviewDTO.setReviewID(0);
				}
				returnList.add(tempReviewDTO); 
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
		
		return returnList;
	}
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getReview(eionet.cr.web.security.CRUser, int)
	 */
	public ReviewDTO getReview(CRUser user, int reviewId)  throws DAOException{
		
		String userUri = user.getReviewUri(reviewId);
		
		String dbQuery = "SELECT uri, spoTitle.object AS title, spoObject.object AS object FROM spo AS spo1, spo AS spo2," +
		" spo AS spoTitle, spo AS spoObject, resource " +
		"WHERE " +
		"(spo1.subject = " + Hashes.spoHash(userUri) + ") AND " +
		"(spo1.subject = spo2.subject) AND (spo1.subject = spoTitle.subject) AND (spo1.subject = spoObject.subject) AND " +
		"spoObject.Predicate="+ Hashes.spoHash(Predicates.CR_FEEDBACK_FOR) + "AND " +
		"spoTitle.Predicate="+ Hashes.spoHash(Predicates.DC_TITLE) + "AND " +
		"spo1.subject=resource.uri_hash AND " +
		"(spo1.predicate = " + Hashes.spoHash(Predicates.CR_USER) + ") AND "+
		"(spo1.object_hash = " + Hashes.spoHash(user.getHomeUri()) +") AND " +
		"(spo2.predicate = " + Hashes.spoHash(Predicates.RDF_TYPE) +") AND " +
		"(spo2.object_hash = " + Hashes.spoHash(Subjects.CR_FEEDBACK) +") " +
		"ORDER BY uri ASC"
		;

		ReviewDTO returnItem = new ReviewDTO();
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try{
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(dbQuery);
			if (rs.next()){
				returnItem = new ReviewDTO();
				returnItem.setReviewSubjectUri(rs.getString("uri"));
				returnItem.setTitle(rs.getString("title"));
				returnItem.setObjectUrl(rs.getString("object"));
				returnItem.setReviewID(reviewId);
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
		
		return returnItem;
	}

	/** */
	private static String sqlDeleteReview = "DELETE FROM spo WHERE subject=? OR object_hash=?" +
			"OR source=? OR obj_deriv_source=? OR obj_source_object=?";
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#deleteReview(java.lang.String)
	 */
	public void deleteReview(String reviewSubjectURI)  throws DAOException{
		
		Connection conn = null;
		Statement stmt = null;
		try{
			conn = getConnection();
			SQLUtil.executeUpdate(StringUtils.replace(
					sqlDeleteReview, "?", String.valueOf(Hashes.spoHash(reviewSubjectURI))), conn);
		}
		catch (SQLException e){
			throw new DAOException(e.toString(), e);
		}
		finally{
			SQLUtil.close(stmt);
			SQLUtil.close(conn);
		}
	}

	/** */
	private static final String deleteTriplesOfSourceSQL = "delete from SPO where SOURCE=?";
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#deleteTriplesOfSource(long)
	 */
	public void deleteTriplesOfSource(long sourceHash) throws DAOException {
		
		if (sourceHash<=0){
			throw new IllegalArgumentException("The given source hash should be >0");
		}
		
		Connection conn = null;
		Statement stmt = null;
		try{
			conn = getConnection();
			SQLUtil.executeUpdate(StringUtils.replace(
					deleteTriplesOfSourceSQL, "?", String.valueOf(sourceHash)), conn);
		}
		catch (SQLException e){
			throw new DAOException(e.toString(), e);
		}
		finally{
			SQLUtil.close(stmt);
			SQLUtil.close(conn);
		}
	}
}
