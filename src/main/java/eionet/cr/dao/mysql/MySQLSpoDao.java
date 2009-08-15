package eionet.cr.dao.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.SpoHelperDao;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.search.SearchException;
import eionet.cr.util.Hashes;
import eionet.cr.util.Util;
import eionet.cr.util.YesNoBoolean;
import eionet.cr.util.sql.SQLUtil;
import eionet.cr.util.sql.SQLValue;
import eionet.cr.util.sql.SQLValueReader;


/**
 *	Mysql implementation of {@link MySQLSpoDao}.
 * 
 * @author Aleksandr Ivanov
 * <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public class MySQLSpoDao extends MySQLBaseDAO implements SpoHelperDao {
	
	/** */
	private static final String sqlQuery = "select distinct OBJECT from SPO where PREDICATE=? and LIT_OBJ='Y' and ANON_OBJ='N' order by OBJECT asc";
	
	/**
	 * 
	 */
	MySQLSpoDao() {
		//reducing visibility
	}

	/** 
	 * @see eionet.cr.dao.SpoHelperDao#getPicklistForPredicate(java.lang.String)
	 * {@inheritDoc}
	 */
	public Collection<String> getPicklistForPredicate(String predicateUri) throws SearchException {
		if (StringUtils.isBlank(predicateUri)) {
			return Collections.emptyList();
		}
		try{
			Collection<String> result = new LinkedList<String>();
			List<Map<String,SQLValue>> resultList = executeQuery(
					sqlQuery,
					Collections.singletonList((Object)Hashes.spoHash(predicateUri)),
					new SQLValueReader()); 
			if (resultList!=null && !resultList.isEmpty()){
				for (int i=0; i<resultList.size(); i++){
					SQLValue sqlValue = resultList.get(i).get("OBJECT");
					if (sqlValue!=null){
						result.add(sqlValue.getString());
					}
				}
			}
			return result;
		}
		catch (DAOException e){
			throw new SearchException(e.toString(), e);
		}
	}

	/** */
	private static final String allowLiteralSearchQuery = "select distinct OBJECT from SPO where SUBJECT=? and PREDICATE=? and LIT_OBJ='N' and ANON_OBJ='N'";
	
	/** 
	 * @see eionet.cr.dao.SpoHelperDao#isAllowLiteralSearch(java.lang.String)
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
			
			List<Map<String,SQLValue>> resultList = executeQuery(allowLiteralSearchQuery, values, new SQLValueReader());
			if (resultList == null || resultList.isEmpty()) {
				return true; // if not rdfs:domain specified at all, then lets allow literal search
			}
			
			for (Map<String, SQLValue> result : resultList){
				SQLValue sqlValue = result.get("OBJECT");
				if (sqlValue!=null){
					String strValue = sqlValue.getString();
					if (strValue!=null && strValue.equals(Subjects.RDFS_LITERAL)){
						return true; // rdfs:Literal is present in the specified rdfs:domain
					}
				}
			}
			
			return false;
		}
		catch(DAOException exception) {
			throw new SearchException();
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
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		try{
			conn = getConnection();
			pstmt = conn.prepareStatement(tripleInsertSQL);

			boolean doExecuteBatch = false;
			long subjectHash = Long.parseLong(subjectDTO.getUriHash());
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
}
