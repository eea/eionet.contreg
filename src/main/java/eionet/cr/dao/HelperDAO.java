package eionet.cr.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import eionet.cr.dao.readers.RDFExporter;
import eionet.cr.dao.util.PredicateLabels;
import eionet.cr.dao.util.SubProperties;
import eionet.cr.dao.util.UriLabelPair;
import eionet.cr.dto.FactsheetDTO;
import eionet.cr.dto.PredicateDTO;
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
 * @author Aleksandr Ivanov <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 */
public interface HelperDAO extends DAO {

    /**
     * Fetches recently discovered files.
     *
     * @param limit how many files to fetch
     * @return List<Pair<String, String>>
     * @throws DAOException if query fails
     */
    List<Pair<String, String>> getLatestFiles(int limit) throws DAOException;

    /**
     * Returns array of data objects of latest subjects.
     *
     * @param rdfType String
     * @param limit max size of query result
     * @return Collection<SubjectDTO>
     * @throws DAOException if query fails
     */
    Collection<SubjectDTO> getLatestSubjects(String rdfType, int limit) throws DAOException;

    /**
     * Returns subjects newer than given timestamp.
     *
     * @param timestamp Timestamp since what the subjects are returned
     * @param limit max size of query
     * @return List<SubjectDTO>
     * @throws DAOException if query fails
     */
    List<SubjectDTO> getSubjectsNewerThan(Date timestamp, int limit) throws DAOException;

    /**
     * @param predicateUri
     * @param extractLabels - if true, labels are extracted if label is URL
     * @return Collection<ObjectLabelPair>
     * @throws DAOException if query fails
     */
    Collection<ObjectLabelPair> getPicklistForPredicate(String predicateUri, boolean extractLabels) throws DAOException;

    /**
     * Adds triples to triplestore.
     *
     * @param subjectDTO
     * @throws DAOException if query fails
     */
    void addTriples(SubjectDTO subjectDTO) throws DAOException;

    /**
     * Adds triples to triplestore.
     *
     * @param conn
     * @param subjectDTO
     * @throws DAOException
     */
    void addTriples(RepositoryConnection conn, SubjectDTO subjectDTO) throws DAOException, RepositoryException;

    /**
     * Store CONSTRUCT query result into given context.
     *
     * @param constructQuery
     * @param context
     * @param defaultGraphUris
     * @param namedGraphUris
     * @param limit - max number of triples inserted
     * @return int - number of triples inserted
     * @throws DAOException - if query fails
     */
    int addTriples(String constructQuery, String context, String[] defaultGraphUris, String[] namedGraphUris, int limit)
            throws DAOException;

    /**
     * @param uri
     * @param sourceUri
     * @return
     * @throws DAOException if query fails
     */
    void addResource(String uri, String sourceUri) throws DAOException;

    /**
     * @param subjectUri
     * @return HashMap<String, String>
     * @throws DAOException if query fails
     */
    HashMap<String, String> getAddibleProperties(String subjectUri) throws DAOException;

    /**
     * @param subjectUri
     * @return String
     * @throws DAOException if query fails
     */
    String getSubjectSchemaUri(String subjectUri) throws DAOException;

    /**
     *
     * @param typeUri
     * @return
     * @throws DAOException if query fails
     */
    List<Pair<String, String>> getPredicatesUsedForType(String typeUri) throws DAOException;

    /**
     * Gets sources that have some spatial content.
     *
     * @return List<String>
     * @throws DAOException if query fails
     */
    List<String> getSpatialSources() throws DAOException;

    /**
     * @param subjectUri
     * @return SubjectDTO
     * @throws DAOException if query fails
     */
    SubjectDTO getSubject(String subjectUri) throws DAOException;

    /**
     * @param subjectHashes
     * @return PredicateLabels
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
     * @return HashMap<UriLabelPair, ArrayList<UriLabelPair>> map where key is instrument and value list of related obligations
     * @throws DAOException if query fails
     */
    HashMap<UriLabelPair, ArrayList<UriLabelPair>> getDeliverySearchPicklist() throws DAOException;

    /**
     * @param user
     * @param url
     * @param isBookmark
     * @param label
     * @return
     * @throws DAOException if query fails
     */
    void registerUserUrl(CRUser user, String url, boolean isBookmark, String label) throws DAOException;

    /**
     * @param user
     * @param url
     * @param label
     * @return
     * @throws DAOException if query fails
     */
    void addUserBookmark(CRUser user, String url, String label) throws DAOException;

    /**
     * @param user
     * @param uri
     * @return
     * @throws DAOException if query fails
     */
    void deleteUserBookmark(CRUser user, String uri) throws DAOException;

    /**
     * @param userBookmarksUri
     * @return List<UserBookmarkDTO>
     * @throws DAOException if query fails
     */
    List<UserBookmarkDTO> getUserBookmarks(String userBookmarksUri) throws DAOException;

    /**
     * Checks if subject is listed as user bookmark.
     *
     * @param user Current user
     * @param subject Subject URI
     * @return boolean
     * @throws DAOException if query fails
     */
    boolean isSubjectUserBookmark(CRUser user, String subject) throws DAOException;

    /**
     * @param user
     * @param url
     * @return
     * @throws DAOException if query fails
     */
    void updateUserHistory(CRUser user, String url) throws DAOException;

    /**
     * @param historyUri
     * @return List<UserHistoryDTO>
     * @throws DAOException if query fails
     */
    List<UserHistoryDTO> getUserHistory(String historyUri) throws DAOException;

    /**
     * @param sourceUrl
     * @param pagingRequest
     * @return List<TripleDTO>
     * @throws DAOException if query fails
     */
    List<TripleDTO> getSampleTriplesInSource(String sourceUrl, PagingRequest pagingRequest) throws DAOException;

    /**
     * @param triples
     * @throws DAOException - if query fails
     */
    void deleteTriples(Collection<TripleDTO> triples) throws DAOException;

    /**
     * @param triple
     * @throws DAOException - if query fails
     */
    void deleteTriple(TripleDTO triple) throws DAOException;

    /**
     * @param sourceHash
     * @return
     * @throws DAOException if query fails
     */
    void deleteTriplesOfSource(long sourceHash) throws DAOException;

    /**
     * @param crUser
     * @return Collection<UploadDTO>
     * @throws DAOException if query fails
     */
    Collection<UploadDTO> getUserUploads(CRUser crUser) throws DAOException;

    /**
     * Returns all the uploads that are rdf source files.
     *
     * @return
     * @throws DAOException
     */
    List<UploadDTO> getAllRdfUploads() throws DAOException;

    /**
     * @param subjectUri
     * @return boolean
     * @throws DAOException if query fails
     */
    boolean isExistingSubject(String subjectUri) throws DAOException;

    /**
     * @param userName TODO
     * @param subjectUris
     * @return
     * @throws DAOException if query fails
     */
    void deleteUserUploads(String userName, List<String> subjectUris) throws DAOException;

    /**
     * @param renamings TODO
     * @return
     * @throws DAOException if query fails
     */
    void renameUserUploads(Map<String, String> renamings) throws DAOException;

    /**
     * @param sourceHash
     * @return List<PredicateDTO>
     * @throws DAOException if query fails
     */
    List<PredicateDTO> readDistinctPredicates(Long sourceHash) throws DAOException;

    /**
     * @param sourceHash
     * @return List<String>
     * @throws DAOException if query fails
     */
    List<String> readDistinctSubjectUrls(Long sourceHash) throws DAOException;

    /**
     * @param reader RDFExporter
     * @return
     * @throws DAOException if query fails
     */
    void outputSourceTriples(RDFExporter reader) throws DAOException;

    /**
     * @param subjectUris
     * @param predicateUris
     * @param sourceUris
     * @return
     * @throws DAOException if query fails
     */
    void deleteSubjectPredicates(Collection<String> subjectUris, Collection<String> predicateUris, Collection<String> sourceUris)
            throws DAOException;

    /**
     * @param user
     * @return List<Map<String, String>>
     * @throws DAOException
     */
    List<Map<String, String>> getSparqlBookmarks(CRUser user) throws DAOException;

    /**
     * Returns bookmarks of the projects where the user has View permission.
     * @param user current user
     * @return list of bookmark objects
     * @throws DAOException if query fails
     */
    List<Map<String, String>> getProjectSparqlBookmarks(CRUser user) throws DAOException;

    /**
     * Returns shared SPARQL bookmark queries.
     *
     * @return List<Map<String, String>>
     * @throws DAOException
     */
    List<Map<String, String>> getSharedSparqlBookmarks() throws DAOException;

    /**
     * Returns triples of an harvest source.
     *
     * @param sourceUrl harvest source url
     * @throws DAOException if query fails.
     * @return list of TripleDTO objects.
     */
    List<TripleDTO> getTriplesInSource(String sourceUrl) throws DAOException;

    /**
     * Returns full SPO objects of an harvest source.
     *
     * @param sourceUrl harvest source url
     * @throws DAOException if query fails.
     * @return list of SubjectDTO objects.
     */
    List<SubjectDTO> getSPOsInSource(String sourceUrl) throws DAOException;

    /**
     * Returns full SPO objects of given subject.
     *
     * @param subjectUri
     *
     * @throws DAOException if query fails.
     * @return list of SubjectDTO objects.
     */
    List<SubjectDTO> getSPOsInSubject(String subjectUri) throws DAOException;

    /**
     * @param resourceUris
     * @return
     * @throws DAOException
     */
    Map<String, Date> getSourceLastModifiedDates(Set<String> resourceUris) throws DAOException;

    /**
     *
     * @param subjectsToCheck
     * @return
     * @throws DAOException
     */
    Set<String> getLiteralRangeSubjects(Set<String> subjectsToCheck) throws DAOException;

    /**
     *
     * @param subjectUri
     * @param acceptedLanguages TODO
     * @param predicatePages TODO
     * @return
     * @throws DAOException
     */
    FactsheetDTO getFactsheet(String subjectUri, List<String> acceptedLanguages, Map<String, Integer> predicatePages)
            throws DAOException;

    /**
     * Returns number of harvested triples from the harvest source.
     *
     * @param sourceUri
     * @return
     * @throws DAOException
     */
    int getHarvestedStatements(String sourceUri) throws DAOException;

    /**
     *
     * @param subjectUri
     * @return
     * @throws DAOException
     */
    boolean isTabularDataSubject(String subjectUri) throws DAOException;

    /**
     *
     * @param subjectUri
     * @param predicateUri
     * @param objectMD5
     * @param graphUri
     * @return
     */
    String getLiteralObjectValue(String subjectUri, String predicateUri, String objectMD5, String graphUri);

    /**
     * Returns true if a graph by the given URI exists in the triple store. Otherwise returns false.
     *
     * @param grpahUri The graph in question.
     * @return The boolean as described above.
     * @throws DAOException When an error happens.
     */
    boolean isGraphExists(String grpahUri) throws DAOException;

    /**
     * deletes project bookmark.
     * @param uri full bookmark uri
     * @throws DAOException if deleting fails
     */
    void deleteProjectBookmark(String uri) throws DAOException;
}
