package eionet.cr.dao.virtuoso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dao.readers.RDFExporter;
import eionet.cr.dao.readers.SubjectDataReader;
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
import eionet.cr.util.Hashes;
import eionet.cr.util.Pair;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.util.sql.SingleObjectReader;
import eionet.cr.web.security.CRUser;

/**
 * 
 * @author jaanus
 *
 */
public class VirtuosoHelperDAO extends VirtuosoBaseDAO implements HelperDAO{

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getLatestFiles(int)
	 */
	@Override
	public List<Pair<String, String>> getLatestFiles(int limit)
			throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getLatestSubjects(java.lang.String, int)
	 */
	@Override
	public Collection<SubjectDTO> getLatestSubjects(String rdfType, int limit)
			throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getSubjectsNewerThan(java.util.Date, int)
	 */
	@Override
	public List<SubjectDTO> getSubjectsNewerThan(Date timestamp, int limit)
			throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getPicklistForPredicate(java.lang.String)
	 */
	@Override
	public Collection<String> getPicklistForPredicate(String predicateUri)
			throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#addTriples(eionet.cr.dto.SubjectDTO)
	 */
	@Override
	public void addTriples(SubjectDTO subjectDTO) throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#addResource(java.lang.String, java.lang.String)
	 */
	@Override
	public void addResource(String uri, String sourceUri) throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getAddibleProperties(java.util.Collection)
	 */
	@Override
	public HashMap<String, String> getAddibleProperties(
			Collection<String> subjectTypes) throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getSubjectSchemaUri(java.lang.String)
	 */
	@Override
	public String getSubjectSchemaUri(String subjectUri) throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#isAllowLiteralSearch(java.lang.String)
	 */
	@Override
	public boolean isAllowLiteralSearch(String predicateUri)
			throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getPredicatesUsedForType(java.lang.String)
	 */
	@Override
	public List<SubjectDTO> getPredicatesUsedForType(String typeUri)
			throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getSpatialSources()
	 */
	@Override
	public List<String> getSpatialSources() throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getSubject(java.lang.String)
	 */
	@Override
	public SubjectDTO getSubject(String subjectUri) throws DAOException {
		
		if (StringUtils.isBlank(subjectUri)){
			return null;
		}
		
		Map<Long,SubjectDTO> map = new LinkedHashMap<Long, SubjectDTO>();
		long subjectHash = Hashes.spoHash(subjectUri);
		map.put(Long.valueOf(subjectHash), null);
		SubjectDataReader reader = new SubjectDataReader(map);
		
		List<SubjectDTO> subjects = getSubjectsData(Collections.singletonList(subjectUri), null, reader);
		return subjects==null || subjects.isEmpty() ? null : subjects.get(0);
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getSubject(java.lang.Long)
	 */
	@Override
	public SubjectDTO getSubject(Long subjectHash) throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getPredicateLabels(java.util.Set)
	 */
	@Override
	public PredicateLabels getPredicateLabels(Set<Long> subjectHashes) throws DAOException {
		
		// TODO: implement this method
		return null;
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getSubProperties(java.util.Set)
	 */
	@Override
	public SubProperties getSubProperties(Set<Long> subjectHashes) throws DAOException {
		// TODO: implement this method
		return null;
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getDataflowSearchPicklist()
	 */
	@Override
	public HashMap<String, ArrayList<UriLabelPair>> getDataflowSearchPicklist()
			throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getDistinctOrderedTypes()
	 */
	@Override
	public ArrayList<Pair<String, String>> getDistinctOrderedTypes()
			throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getSubjectCountInSource(long)
	 */
	@Override
	public int getSubjectCountInSource(long sourceHash) throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getLatestHarvestedURLs(int)
	 */
	@Override
	public Pair<Integer, List<HarvestedUrlCountDTO>> getLatestHarvestedURLs(
			int days) throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getUrgencyOfComingHarvests(int)
	 */
	@Override
	public Pair<Integer, List<HarvestUrgencyScoreDTO>> getUrgencyOfComingHarvests(
			int amount) throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#isUrlInHarvestSource(java.lang.String)
	 */
	@Override
	public boolean isUrlInHarvestSource(String url) throws DAOException {
		
		// TODO: implement this method
		return false;
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#updateTypeDataCache()
	 */
	@Override
	public void updateTypeDataCache() throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#registerUserUrl(eionet.cr.web.security.CRUser, java.lang.String, boolean)
	 */
	@Override
	public void registerUserUrl(CRUser user, String url, boolean isBookmark)
			throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#addUserBookmark(eionet.cr.web.security.CRUser, java.lang.String)
	 */
	@Override
	public void addUserBookmark(CRUser user, String url) throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#deleteUserBookmark(eionet.cr.web.security.CRUser, java.lang.String)
	 */
	@Override
	public void deleteUserBookmark(CRUser user, String url) throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getUserBookmarks(eionet.cr.web.security.CRUser)
	 */
	@Override
	public List<UserBookmarkDTO> getUserBookmarks(CRUser user)
			throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#isSubjectUserBookmark(eionet.cr.web.security.CRUser, long)
	 */
	@Override
	public boolean isSubjectUserBookmark(CRUser user, long subjectHash) throws DAOException {
		
		// TODO: implement actual logic
		return false;
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#updateUserHistory(eionet.cr.web.security.CRUser, java.lang.String)
	 */
	@Override
	public void updateUserHistory(CRUser user, String url) throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getUserHistory(eionet.cr.web.security.CRUser)
	 */
	@Override
	public List<UserHistoryDTO> getUserHistory(CRUser user) throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getSampleTriplesInSource(java.lang.String, eionet.cr.util.pagination.PagingRequest)
	 */
	@Override
	public List<TripleDTO> getSampleTriplesInSource(String sourceUrl,
			PagingRequest pagingRequest) throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#generateNewReviewId(eionet.cr.web.security.CRUser)
	 */
	@Override
	public int generateNewReviewId(CRUser user) throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getLastReviewId(eionet.cr.web.security.CRUser)
	 */
	@Override
	public int getLastReviewId(CRUser user) throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#addReview(eionet.cr.dto.ReviewDTO, eionet.cr.web.security.CRUser)
	 */
	@Override
	public int addReview(ReviewDTO review, CRUser user) throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#saveReview(int, eionet.cr.dto.ReviewDTO, eionet.cr.web.security.CRUser)
	 */
	@Override
	public void saveReview(int reviewId, ReviewDTO review, CRUser user)
			throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getReviewList(eionet.cr.web.security.CRUser)
	 */
	@Override
	public List<ReviewDTO> getReviewList(CRUser user) throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getReview(eionet.cr.web.security.CRUser, int)
	 */
	@Override
	public ReviewDTO getReview(CRUser user, int reviewId) throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getReviewAttachmentList(eionet.cr.web.security.CRUser, int)
	 */
	@Override
	public List<String> getReviewAttachmentList(CRUser user, int reviewId)
			throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#deleteReview(eionet.cr.web.security.CRUser, int, boolean)
	 */
	@Override
	public void deleteReview(CRUser user, int reviewId,
			boolean deleteAttachments) throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#deleteAttachment(eionet.cr.web.security.CRUser, int, java.lang.String)
	 */
	@Override
	public void deleteAttachment(CRUser user, int reviewId, String attachmentUri)
			throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#loadAttachment(java.lang.String)
	 */
	@Override
	public DownloadFileDTO loadAttachment(String attachmentUri)
			throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#deleteTriples(java.util.Collection)
	 */
	@Override
	public void deleteTriples(Collection<TripleDTO> triples)
			throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#deleteTriplesOfSource(long)
	 */
	@Override
	public void deleteTriplesOfSource(long sourceHash) throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getUserUploads(eionet.cr.web.security.CRUser)
	 */
	@Override
	public Collection<UploadDTO> getUserUploads(CRUser crUser)
			throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#isExistingSubject(java.lang.String)
	 */
	@Override
	public boolean isExistingSubject(String subjectUri) throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#deleteSubjects(java.util.List)
	 */
	@Override
	public void deleteSubjects(List<String> subjectUris) throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#renameSubjects(java.util.Map)
	 */
	@Override
	public void renameSubjects(Map<Long, String> newUrisByOldHashes)
			throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#readDistinctPredicates(java.lang.Long)
	 */
	@Override
	public List<PredicateDTO> readDistinctPredicates(Long sourceHash)
			throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#readDistinctSubjectUrls(java.lang.Long)
	 */
	@Override
	public List<String> readDistinctSubjectUrls(Long sourceHash)
			throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#outputSourceTriples(eionet.cr.dao.readers.RDFExporter)
	 */
	@Override
	public void outputSourceTriples(RDFExporter reader) throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#deleteTriples(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void deleteTriples(String subjectUri, String predicateUri,
			String sourceUri) throws DAOException {
		throw new UnsupportedOperationException("Method not implemented");
		
	}
	
	/* (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getTriplesCount()
	 */
	@Override
	public long getTriplesCount() throws DAOException {
		
		String query = "SELECT count(*) WHERE {?s ?p ?o}";
		Object resultObject = executeUniqueResultSPARQL(query, new SingleObjectReader<Long>());
		return Long.valueOf(resultObject.toString());
		
	}
	
}
