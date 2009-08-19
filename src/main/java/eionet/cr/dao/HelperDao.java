

package eionet.cr.dao;

import java.util.Collection;
import java.util.List;

import eionet.cr.dto.SubjectDTO;
import eionet.cr.search.SearchException;
import eionet.cr.util.Pair;

/**
 * Helper dao to use in different searches.
 * 
 * @author Aleksandr Ivanov
 * <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public interface HelperDao extends IDao {

	
	/**
	 * performs spatial sources search.
	 * 
	 * @return
	 * @throws DAOException
	 */
	List<String> performSpatialSourcesSearch() throws DAOException;
	
	/**
	 * fetches recently discovered files.
	 * @param limit how many files to fetch
	 * @return
	 * @throws DAOException
	 */
	List<Pair<String, String>> getRecentlyDiscoveredFiles(int limit) throws DAOException;
	
	/**
	 * @param predicateUri
	 * @return
	 * @throws SearchException
	 */
	Collection<String> getPicklistForPredicate(String predicateUri) throws SearchException;

	/**
	 * 
	 * @param predicateUri
	 * @return
	 * @throws SearchException 
	 */
	boolean isAllowLiteralSearch(String predicateUri) throws SearchException;

	/**
	 * 
	 * @param subjectDTO
	 * @throws DAOException 
	 */
	void addTriples(SubjectDTO subjectDTO) throws DAOException;
	
	/**
	 * 
	 * @param uri
	 * @param firstSeenSourceUri
	 * @throws DAOException
	 */
	void addResource(String uri, String firstSeenSourceUri) throws DAOException;
}