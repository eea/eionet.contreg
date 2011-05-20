

package eionet.cr.dao;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import eionet.cr.util.ObjectLabelPair;
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
     * @return List<Pair<String, String>>
     * @throws DAOException if query fails
     */
    List<Pair<String, String>> getLatestFiles(int limit) throws DAOException;

    /**
     * Returns array of data objects of latest subjects.
     * @param rdfType String
     * @param limit max size of query result
     * @return Collection<SubjectDTO>
     * @throws DAOException if query fails
     */
    Collection<SubjectDTO> getLatestSubjects(String rdfType, int limit) throws DAOException;

    /**
     * Returns subjects newer than given timestamp.
     * @param timestamp Timestamp since what the subjects are returned
     * @param limit max size of query
     * @return List<SubjectDTO>
     * @throws DAOException if query fails
     */
    List<SubjectDTO> getSubjectsNewerThan(Date timestamp, int limit) throws DAOException;

    /**
     * @param predicateUri
     * @param extractLabels - if true, labels are extracted if label is URL
     * @return
     * @throws DAOException if query fails
     */
    Collection<ObjectLabelPair> getPicklistForPredicate(String predicateUri, boolean extractLabels) throws DAOException;

    /**
     *
     * @param subjectDTO
     * @throws DAOException if query fails
     */
    void addTriples(SubjectDTO subjectDTO) throws DAOException;

    /**
     *
     * @param uri
     * @param sourceUri
     * @throws DAOException if query fails
     */
    void addResource(String uri, String sourceUri) throws DAOException;

    /**
     *
     * @param subjectTypes
     * @return
     * @throws DAOException if query fails
     */
    HashMap<String, String> getAddibleProperties(Collection<String> subjectTypes) throws DAOException;

    /**
     *
     * @param subjectUri
     * @return
     * @throws DAOException if query fails
     */
    String getSubjectSchemaUri(String subjectUri) throws DAOException;

    /**
     *
     * @param predicateUri
     * @return
     * @throws DAOException if query fails TODO
     */
    boolean isAllowLiteralSearch(String predicateUri) throws DAOException;

    /**
     *
     * @param typeUri
     * @return
     * @throws DAOException if query fails
     */
    List<SubjectDTO> getPredicatesUsedForType(String typeUri) throws DAOException;

    /**
     * Gets sources that have some spatial content.
     *
     * @return
     * @throws DAOException if query fails
     */
    List<String> getSpatialSources() throws DAOException;

    /**
     *
     * @param subjectHash
     * @return
     * @throws DAOException if query fails
     */
    SubjectDTO getSubject(Long subjectHash) throws DAOException;

    /**
     *
     * @param subjectUri
     * @return
     * @throws DAOException if query fails
     */
    SubjectDTO getSubject(String subjectUri) throws DAOException;

    /**
     *
     * @param subjectHashes
     * @return
     * @throws DAOException if query fails
     */
    PredicateLabels getPredicateLabels(Set<Long> subjectHashes) throws DAOException;

    /**
     * @param subjects Collection<String> set of subject URIs
     * @return SubProperties subproperties of the given URIs
     * @throws DAOException if query fails
     */
    SubProperties getSubProperties(Collection<String> subjects) throws DAOException;

    /**
     *
     * @return
     * @throws DAOException if query fails
     */
    HashMap<String, ArrayList<UriLabelPair>> getDataflowSearchPicklist() throws DAOException;

    /**
     *
     * @return
     * @throws DAOException if query fails
     */
    ArrayList<Pair<String, String>> getDistinctOrderedTypes() throws DAOException;

    /**
     *
     * @param sourceHash
     * @return
     * @throws DAOException if query fails
     */
    int getSubjectCountInSource(long sourceHash) throws DAOException;

    /**
     *
     * @throws DAOException if query fails
     */
    public void updateTypeDataCache() throws DAOException;

    /**
     *
     * @param user TODO
     * @param url
     * @param isBookmark
     * @throws DAOException if query fails
     */
    public void registerUserUrl(CRUser user, String url, boolean isBookmark) throws DAOException;

    /**
     *
     * @param user
     * @param url
     * @throws DAOException if query fails
     */
    public void addUserBookmark(CRUser user, String url) throws DAOException;

    /**
     *
     * @param user
     * @param url
     * @throws DAOException if query fails
     */
    public void deleteUserBookmark(CRUser user, String url) throws DAOException;


    /**
     *
     * @param user
     * @param url
     * @throws DAOException if query fails
     */
    public List<UserBookmarkDTO> getUserBookmarks(CRUser user) throws DAOException;


    /**
     * Checks if subject is listed as user bookmark.
     * @param user Current user
     * @param subject Subject URI
     * @return boolean
     * @throws DAOException if query fails
     */
    public boolean isSubjectUserBookmark(CRUser user, String subject) throws DAOException;

    /**
     *
     * @param user
     * @param url
     * @throws DAOException if query fails
     */
    public void updateUserHistory(CRUser user, String url) throws DAOException;

    /**
     * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
     * @param user
     * @throws DAOException if query fails
     */
    public List<UserHistoryDTO> getUserHistory(CRUser user) throws DAOException;

    /**
     *
     * @param sourceUrl
     * @param pagingRequest
     * @return
     * @throws DAOException if query fails
     */
    public List<TripleDTO> getSampleTriplesInSource(String sourceUrl,
            PagingRequest pagingRequest) throws DAOException;


    /**
     *
     * @param user
     * @return int
     * @throws DAOException if query fails
     * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
     */
    public int generateNewReviewId(CRUser user) throws DAOException;

    /**
     *
     * @param user
     * @return int
     * @throws DAOException if query fails
     * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
     */
    public int getLastReviewId(CRUser user)  throws DAOException;


    /**
     *
     * @param review
     * @param user
     * @return int
     * @throws DAOException if query fails
     * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
     */
    public int addReview(ReviewDTO review, CRUser user)  throws DAOException;

    /**
     *
     * @param reviewiD
     * @param review
     * @param user
     * @throws DAOException if query fails
     * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
     */
    public void saveReview(int reviewId, ReviewDTO review, CRUser user) throws DAOException;

    /**
     *
     * @param user
     * @return List<ReviewDTO
     * @throws DAOException if query fails
     * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
     */
    public List<ReviewDTO> getReviewList(CRUser user)  throws DAOException;

    /**
     *
     * @param user
     * @param reviewId
     * @return List<ReviewDTO
     * @throws DAOException if query fails
     * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
     */
    public ReviewDTO getReview(CRUser user, int reviewId)  throws DAOException;

    /**
     *
     * @param user
     * @param reviewId
     * @return List<String>
     * @throws DAOException if query fails
     * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
     */
    public List<String> getReviewAttachmentList(CRUser user, int reviewId)  throws DAOException;



    /**
     * @param reviewSubjectURI
     * @throws DAOException if query fails
     * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
     */
    public void deleteReview(CRUser user, int reviewId, boolean deleteAttachments)  throws DAOException;

    /**
     *
     *
     * @param user
     * @param reviewId
     * @param attachmentUri
     * @throws DAOException if query fails
     * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
     */
    public void deleteAttachment(CRUser user, int reviewId, String attachmentUri)  throws DAOException;

    /**
     *
     *
     * @param attachmentUri
     * @return InputStream
     * @throws DAOException if query fails
     * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
     */
    public  DownloadFileDTO loadAttachment(String attachmentUri) throws DAOException;



    /**
     *
     * @param triples
     * @throws DAOException if query fails
     */
    public void deleteTriples(Collection<TripleDTO> triples) throws DAOException;

    /**
     *
     * @param sourceHash
     * @throws DAOException if query fails
     */
    public void deleteTriplesOfSource(long sourceHash) throws DAOException;

    /**
     *
     * @param crUser
     * @return
     * @throws DAOException if query fails
     */
    public Collection<UploadDTO> getUserUploads(CRUser crUser) throws DAOException;

    /**
     *
     * @param subjectUri
     * @return
     * @throws DAOException if query fails
     */
    public boolean isExistingSubject(String subjectUri) throws DAOException;

    /**
     *
     * @param userName TODO
     * @param subjectUris
     * @throws DAOException if query fails
     */
    public void deleteUserUploads(String userName, List<String> subjectUris) throws DAOException;

    /**
     *
     * @param renamings TODO
     * @throws DAOException if query fails
     */
    public void renameUserUploads(Map<String, String> renamings) throws DAOException;

    /**
     *
     * @param sourceHash
     * @return List<PredicateDTO>
     * @throws DAOException if query fails
     */
    public List<PredicateDTO> readDistinctPredicates(Long sourceHash) throws DAOException;

    /**
     *
     * @param sourceHash
     * @return List<String>
     * @throws DAOException if query fails
     */
    public List<String> readDistinctSubjectUrls(Long sourceHash) throws DAOException;

    /**
     * @param reader RDFExporter
     * @throws DAOException if query fails
     */
    public void outputSourceTriples(RDFExporter reader) throws DAOException;

    /**
     *
     * @param subjectUri
     * @param predicateUri
     * @param sourceUri
     * @throws DAOException if query fails
     */
    public void deleteTriples(String subjectUri, String predicateUri, String sourceUri) throws DAOException;

    /**
     * Returns total triples count.
     * @return long triples count
     * @throws DAOException if query fails
     */
     public long getTriplesCount() throws DAOException;

     /**
      *
      * @param user TODO
     * @return
      * @throws DAOException
      */
     public LinkedHashMap<URI, String> getSparqlBookmarks(CRUser user) throws DAOException;

     /**
      * Stores the user folder in CR home context.
      * @param user loggedin user
      * @throws DAOException
      */
     public void registerUserFolderInCrHomeContext(CRUser user) throws DAOException;
}
