

package eionet.cr.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import eionet.cr.dao.util.PredicateLabels;
import eionet.cr.dao.util.SubProperties;
import eionet.cr.dao.util.UriLabelPair;
import eionet.cr.dto.ReviewDTO;
import eionet.cr.dto.TripleDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.dto.UserBookmarkDTO;
import eionet.cr.dto.UserHistoryDTO;
import eionet.cr.harvest.statistics.dto.HarvestUrgencyScoreDTO;
import eionet.cr.harvest.statistics.dto.HarvestedUrlCountDTO;
import eionet.cr.util.Pair;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.web.security.CRUser;

/**
 * Helper dao to use in different searches.
 * 
 * @author Aleksandr Ivanov
 * <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public interface HelperDAO extends DAO {

	
	/**
	 * fetches recently discovered files.
	 * @param limit how many files to fetch
	 * @return
	 * @throws DAOException
	 */
	List<Pair<String, String>> getLatestFiles(int limit) throws DAOException;
	
	/**
	 * 
	 * @param rdfType
	 * @param limit
	 * @return
	 * @throws DAOException
	 */
	Collection<SubjectDTO> getLatestSubjects(String rdfType, int limit) throws DAOException;
	
	/**
	 * 
	 * @param timestamp
	 * @param limit
	 * @return
	 * @throws DAOException
	 */
	List<SubjectDTO> getSubjectsNewerThan(Date timestamp, int limit) throws DAOException;
	
	/**
	 * @param predicateUri
	 * @return
	 * @throws DAOException 
	 */
	Collection<String> getPicklistForPredicate(String predicateUri) throws DAOException;

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
	 * @param subjectUri
	 * @return
	 * @throws DAOException
	 */
	String getSubjectSchemaUri(String subjectUri) throws DAOException;
	
	/**
	 * 
	 * @param predicateUri
	 * @return
	 * @throws DAOException TODO
	 */
	boolean isAllowLiteralSearch(String predicateUri) throws DAOException;
	
	/**
	 * 
	 * @param typeUri
	 * @return
	 * @throws DAOException
	 */
	List<SubjectDTO> getPredicatesUsedForType(String typeUri) throws DAOException;
	
	/**
	 * Gets sources that have some spatial content.
	 * 
	 * @return
	 * @throws DAOException
	 */
	List<String> getSpatialSources() throws DAOException;

	/**
	 * 
	 * @param subjectHash
	 * @return
	 * @throws DAOException
	 */
	SubjectDTO getSubject(Long subjectHash) throws DAOException;
	
	/**
	 * 
	 * @param subjectHashes
	 * @return
	 * @throws DAOException
	 */
	PredicateLabels getPredicateLabels(Set<Long> subjectHashes) throws DAOException;
	
	/**
	 * 
	 * @param subjectHashes
	 * @return
	 * @throws DAOException
	 */
	SubProperties getSubProperties(Set<Long> subjectHashes) throws DAOException;
	
	/**
	 * 
	 * @return
	 * @throws DAOException
	 */
	HashMap<String, ArrayList<UriLabelPair>> getDataflowSearchPicklist() throws DAOException;
	
	/**
	 * 
	 * @return
	 * @throws DAOException
	 */
	ArrayList<Pair<String,String>> getDistinctOrderedTypes() throws DAOException;
	
	/**
	 * 
	 * @param sourceHash
	 * @return
	 * @throws DAOException
	 */
	int getSubjectCountInSource(long sourceHash) throws DAOException;
	
	/**
	 * 
	 * @param days
	 * @return
	 * @throws DAOException
	 */
	Pair<Integer, List<HarvestedUrlCountDTO>> getLatestHarvestedURLs(int days) throws DAOException;

	/**
	 * 
	 * @param amount
	 * @return
	 * @throws DAOException
	 */
	
	Pair <Integer, List <HarvestUrgencyScoreDTO>> getUrgencyOfComingHarvests(int amount) throws DAOException;
	
	/**
	 * 
	 * @param amount
	 * @return
	 * @throws DAOException
	 */
	boolean isUrlInHarvestSource(String url) throws DAOException;

	/**
	 * 
	 * @throws DAOException
	 */
	public void updateTypeDataCache() throws DAOException;
	
	/**
	 * 
	 * @param user TODO
	 * @param url 
	 * @param isBookmark 
	 * @throws DAOException
	 */
	public void registerUserUrl(CRUser user, String url, boolean isBookmark) throws DAOException;
	
	/**
	 * 
	 * @param user
	 * @param url
	 * @throws DAOException
	 */
	public void addUserBookmark(CRUser user, String url) throws DAOException;
	
	/**
	 * 
	 * @param user
	 * @param url
	 * @throws DAOException
	 */
	public void deleteUserBookmark(CRUser user, String url) throws DAOException;

	
	/**
	 * 
	 * @param user
	 * @param url
	 * @throws DAOException
	 */
	public List<UserBookmarkDTO> getUserBookmarks(CRUser user) throws DAOException;
	
	
	/**
	 * 
	 * @param user
	 * @param url
	 * @throws DAOException
	 */
	public boolean isUrlUserBookmark(CRUser user, String url) throws DAOException;
	
	/**
	 * 
	 * @param user
	 * @param url
	 * @throws DAOException
	 */
	public void updateUserHistory(CRUser user, String url) throws DAOException;

	/**
	 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
	 * @param user
	 * @throws DAOException
	 */
	public List<UserHistoryDTO> getUserHistory(CRUser user) throws DAOException;

	/**
	 * 
	 * @param sourceUrl
	 * @param pagingRequest
	 * @return
	 * @throws DAOException
	 */
	public List<TripleDTO> getSampleTriplesInSource(String sourceUrl,
			PagingRequest pagingRequest) throws DAOException;

	
	/**
	 * 
	 * @param user
	 * @return int
	 * @throws DAOException
	 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
	 */
	public int generateNewReviewId(CRUser user) throws DAOException;
	
	/**
	 * 
	 * @param user
	 * @return int
	 * @throws DAOException
	 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
	 */
	public int getLastReviewId(CRUser user)  throws DAOException;
	
	
	/**
	 * 
	 * @param review
	 * @param user
	 * @return int
	 * @throws DAOException
	 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
	 */
	public int addReview(ReviewDTO review, CRUser user)  throws DAOException;
	
	/**
	 * 
	 * @param reviewiD
	 * @param review
	 * @param user
	 * @throws DAOException
	 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
	 */	
	public void saveReview(int reviewId, ReviewDTO review, CRUser user) throws DAOException;
	
	/**
	 * 
	 * @param user
	 * @return List<ReviewDTO
	 * @throws DAOException
	 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
	 */
	public List<ReviewDTO> getReviewList(CRUser user)  throws DAOException;
	
	/**
	 * 
	 * @param user
	 * @param reviewId
	 * @return List<ReviewDTO
	 * @throws DAOException
	 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
	 */
	public ReviewDTO getReview(CRUser user, int reviewId)  throws DAOException;
	
	
	/**
	 * 
	 * @param reviewSubjectURI
	 * @return void
	 * @throws DAOException
	 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
	 */
	public void deleteReview(String reviewSubjectURI)  throws DAOException;
	
	/**
	 * 
	 * @param triples
	 * @throws DAOException
	 */
	public void deleteTriples(Collection<TripleDTO> triples) throws DAOException;
	
	/**
	 * 
	 * @param sourceHash
	 * @throws DAOException
	 */
	public void deleteTriplesOfSource(long sourceHash) throws DAOException;
	
	
}