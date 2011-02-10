package eionet.cr.dao.virtuoso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.readers.SubjectDataReader;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.sql.ResultSetListReader;

/**
 * 
 * @author jaanus
 *
 */
public abstract class VirtuosoBaseDAO {

	/** */
	protected Logger logger = Logger.getLogger(VirtuosoBaseDAO.class);
	
	/**
	 * 
	 * @param query
	 * @param reader
	 */
	protected <T> List<T> executeQuery(String query, ResultSetListReader<T> reader) throws DAOException {
		
		return new ArrayList<T>();
	}
	
	/**
	 * 
	 * @param reader
	 * @return
	 * @throws DAOException
	 */
	protected String getSubjectsDataQuery(Collection<String> subjectUris){
		
		return null;
	}
}
