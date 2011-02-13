package eionet.cr.dao.virtuoso;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import virtuoso.sesame2.driver.VirtuosoRepository;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.SQLBaseDAO;
import eionet.cr.dao.readers.ResultSetMixedReader;
import eionet.cr.dao.readers.SubjectDataReader;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.Hashes;
import eionet.cr.util.sesame.SPARQLResultSetReader;
import eionet.cr.util.sesame.SesameConnectionProvider;
import eionet.cr.util.sesame.SesameUtil;

/**
 * 
 * @author jaanus
 *
 */
public abstract class VirtuosoBaseDAO extends SQLBaseDAO{

	/** */
	protected Logger logger = Logger.getLogger(VirtuosoBaseDAO.class);
	
	/**
	 * 
	 * @param <T>
	 * @param query
	 * @param reader
	 * @return
	 * @throws DAOException
	 */
	protected <T> List<T> executeSPARQL(String sparql, SPARQLResultSetReader<T> reader) throws DAOException {
		
		RepositoryConnection conn = null;
		try {
			conn = SesameUtil.getConnection();
			SesameUtil.executeQuery(sparql, reader, conn);
			return reader.getResultList();
		}
		catch (Exception e) {
			throw new DAOException(e.toString(), e);
		}
		finally{
			SesameUtil.close(conn);
		}
	}

	/**
	 * 
	 * @param <T>
	 * @param sql
	 * @param params
	 * @param reader
	 * @return
	 * @throws DAOException
	 */
	protected <T> T executeUniqueResultSPARQL(String sql, SPARQLResultSetReader<T> reader) throws DAOException {
		
		List<T> result = executeSPARQL(sql, reader);
		return (result == null || result.isEmpty()) ? null : result.get(0);
	}
	
	/**
	 * 
	 * @param subjectUris
	 * @param predicateUris
	 * @return
	 * @throws DAOException 
	 */
	protected List<SubjectDTO> getSubjectsData(
			Collection<String> subjectUris, Collection<String> predicateUris, SubjectDataReader reader) throws DAOException{
		
		if (subjectUris==null || subjectUris.isEmpty()){
			throw new IllegalArgumentException("Subjects collection must not be null or empty!");
		}
		
		String query = getSubjectsDataQuery(subjectUris, predicateUris);
		executeSPARQL(query, reader);
		
		Map<Long, SubjectDTO> subjectsMap = reader.getSubjectsMap();
		if (subjectsMap!=null && !subjectsMap.isEmpty()){
			
			for (String subjectUri : subjectUris){

				Long subjectHash = Long.valueOf(Hashes.spoHash(subjectUri));
				if (subjectsMap.get(subjectHash)==null){
					
					// TODO: don't hardcode isAnonymous to false
					SubjectDTO subjectDTO = new SubjectDTO(subjectUri, false);
					subjectsMap.put(subjectHash, subjectDTO);
				}
			}
		}
		
		return reader.getResultList();
	}
	
	/**
	 * 
	 * @param predicateUris 
	 * @param reader
	 * @return
	 * @throws DAOException
	 */
	private String getSubjectsDataQuery(Collection<String> subjectUris, Collection<String> predicateUris){
		
		if (subjectUris==null || subjectUris.isEmpty()){
			throw new IllegalArgumentException("Subjects collection must not be null or empty!");
		}
		
		StringBuilder strBuilder = new StringBuilder().
		append("select * where {graph ?g {?s ?p ?o. ").
		append("filter (");
		
		int i=0;
		for (String subjectUri : subjectUris){
			if (i>0){
				strBuilder.append(" || ");
			}
			strBuilder.append("?s = <").append(subjectUri).append(">");
			i++;
		}
		strBuilder.append(") ");
		
		if (predicateUris!=null && !predicateUris.isEmpty()){
			
			i = 0;
			strBuilder.append("filter (");
			for (String predicateUri : predicateUris){
				if (i>0){
					strBuilder.append(" || ");
				}
				strBuilder.append("?p = <").append(predicateUri).append(">");
				i++;
			}
			
			strBuilder.append(") ");
		}
		
		strBuilder.append("}}");		
		return strBuilder.toString();
	}
}
