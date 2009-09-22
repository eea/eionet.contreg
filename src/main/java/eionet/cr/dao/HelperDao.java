

package eionet.cr.dao;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import eionet.cr.dto.SubjectDTO;
import eionet.cr.search.SearchException;
import eionet.cr.search.util.SearchExpression;
import eionet.cr.util.Pair;
import eionet.cr.util.SortingRequest;

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
	 * Newly rewritten simple search.
	 * 
	 * @param expression - search expression to find.
	 * @param pageNumber - page number to get
	 * @param sortingRequest - sorting request to set
	 * @return
	 * @throws Exception
	 */
	Pair<Integer, List<SubjectDTO>> performSimpleSearch(
				SearchExpression expression,
				int pageNumber,
				SortingRequest sortingRequest) throws Exception;
	
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
	
	/**
	 * 
	 * @param subjectTypes
	 * @return
	 * @throws DAOException
	 */
	HashMap<String,String> getAddibleProperties(Collection<String> subjectTypes) throws DAOException;
	
	/**
	 * 
	 * @param subject
	 * @throws DAOException
	 */
	void deleteTriples(SubjectDTO subject) throws DAOException;
}