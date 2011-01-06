

package eionet.cr.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eionet.cr.dao.readers.RDFExporter;
import eionet.cr.dao.util.PredicateLabels;
import eionet.cr.dao.util.SubProperties;
import eionet.cr.dao.util.UriLabelPair;
import eionet.cr.dto.DownloadFileDTO;
import eionet.cr.dto.PredicateDTO;
import eionet.cr.dto.ReviewDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.dto.TripleDTO;
import eionet.cr.dto.UploadDTO;
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
	 * @param sourceUri
	 * @throws DAOException
	 */
	void addResource(String uri, String sourceUri) throws DAOException;
	
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
	 * @param harvestSourceId
	 * @return urgencyScore
	 * @throws DAOException
	 */
	public double getUrgencyScore(int harvestSourceId) throws DAOException;
	
	
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
	 * @param subjectHash
	 * @throws DAOException
	 */
	public boolean isSubjectUserBookmark(CRUser user, long subjectHash) throws DAOException;
	
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
	 * @param user
	 * @param reviewId
	 * @return List<String>
	 * @throws DAOException
	 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
	 */
	public List<String> getReviewAttachmentList(CRUser user, int reviewId)  throws DAOException;
	
	
	
	/**
	 * 
	 * @param reviewSubjectURI
	 * @return void
	 * @throws DAOException
	 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
	 */
	public void deleteReview(CRUser user, int reviewId, boolean deleteAttachments)  throws DAOException;
	
	/**
	 * 
	 * 
	 * @param user
	 * @param reviewId
	 * @param attachmentUri
	 * @throws DAOException
	 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
	 */
	public void deleteAttachment(CRUser user, int reviewId, String attachmentUri)  throws DAOException;
	
	/**
	 * 
	 * 
	 * @param attachmentUri
	 * @return InputStream
	 * @throws DAOException
	 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
	 */
	public  DownloadFileDTO loadAttachment(String attachmentUri) throws DAOException;
	
	
	
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
	
	/**
	 * 
	 * @param crUser
	 * @return
	 * @throws DAOException
	 */
	public Collection<UploadDTO> getUserUploads(CRUser crUser) throws DAOException;
	
	/**
	 * 
	 * @param subjectUri
	 * @return
	 * @throws DAOException
	 */
	public boolean isExistingSubject(String subjectUri) throws DAOException;
	
	/**
	 * 
	 * @param subjectUris
	 * @throws DAOException
	 */
	public void deleteSubjects(List<String> subjectUris) throws DAOException;
	
	/**
	 * 
	 * @param newUrisByOldHashes
	 * @throws DAOException
	 */
	public void renameSubjects(Map<Long,String> newUrisByOldHashes) throws DAOException;
	
	/**
	 * 
	 * @param sourceHash
	 * @return List<PredicateDTO>
	 * @throws DAOException
	 */
	public List<PredicateDTO> readDistinctPredicates(Long sourceHash) throws DAOException;
	
	/**
	 * 
	 * @param sourceHash
	 * @return List<String>
	 * @throws DAOException
	 */
	public List<String> readDistinctSubjectUrls(Long sourceHash) throws DAOException;
	
	/**
	 * 
	 * @param sourceHash
	 * @return List<PredicateDTO>
	 * @throws DAOException
	 */	
	public void outputSourceTriples(RDFExporter reader) throws DAOException;
	
	/**
	 * 
	 * @param subjectUri
	 * @param predicateUri
	 * @param sourceUri
	 * @throws DAOException
	 */
	public void deleteTriples(String subjectUri, String predicateUri, String sourceUri) throws DAOException;
}