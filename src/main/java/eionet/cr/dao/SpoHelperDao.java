

package eionet.cr.dao;

import java.util.Collection;

import eionet.cr.dto.SubjectDTO;
import eionet.cr.search.SearchException;

/**
 * Helper dao to use in different searches.
 * 
 * @author Aleksandr Ivanov
 * <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public interface SpoHelperDao extends IDao {

	
	/**
	 * @param predicateUri
	 * @return
	 * @throws SearchException
	 */
	public abstract Collection<String> getPicklistForPredicate(
			String predicateUri) throws SearchException;

	/**
	 * 
	 * @param predicateUri
	 * @return
	 * @throws SearchException 
	 */
	public abstract boolean isAllowLiteralSearch(String predicateUri)
			throws SearchException;

	/**
	 * 
	 * @param subjectDTO
	 * @throws DAOException 
	 */
	public abstract void addTriples(SubjectDTO subjectDTO) throws DAOException;
	
	/**
	 * 
	 * @param uri
	 * @param firstSeenSourceUri
	 * @throws DAOException
	 */
	public abstract void addResource(String uri, String firstSeenSourceUri) throws DAOException;
}