package eionet.cr.dao.virtuoso;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dao.readers.DataflowPicklistReader;
import eionet.cr.dao.readers.ObjectLabelReader;
import eionet.cr.dao.readers.RDFExporter;
import eionet.cr.dao.readers.RecentUploadsReader;
import eionet.cr.dao.readers.SubjectDataReader;
import eionet.cr.dao.readers.TriplesReader;
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
import eionet.cr.util.ObjectLabelPair;
import eionet.cr.util.Util;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.util.sesame.SesameUtil;
import eionet.cr.util.sql.SQLUtil;
import eionet.cr.util.sql.SingleObjectReader;
import eionet.cr.web.security.CRUser;

/**
 * 
 * @author jaanus
 * 
 */
public class VirtuosoHelperDAO extends VirtuosoBaseDAO implements HelperDAO {

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HelperDAO#getLatestFiles(int)
     */
    @Override
    public List<Pair<String, String>> getLatestFiles(int limit)
            throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HelperDAO#getLatestSubjects(java.lang.String, int)
     */
    @Override
    public Collection<SubjectDTO> getLatestSubjects(String rdfType, int limit)
            throws DAOException {

        // validate arguments
        if (StringUtils.isBlank(rdfType))
            throw new IllegalArgumentException("rdfType must not be blank!");
        if (limit <= 0)
            throw new IllegalArgumentException("limit must be greater than 0!");

        StringBuffer sqlBuf = new StringBuffer().append(
                "prefix dc: <http://purl.org/dc/elements/1.1/> ").append(
                "select distinct ?s ?d where { ").append("?s a <").append(
                rdfType).append("> . ").append("OPTIONAL { ?s dc:date ?d } ")
                .append("} ORDER BY DESC(?d) LIMIT " + limit);

        logger.trace("Recent uploads search, executing subject finder query: "
                + sqlBuf.toString());

        RecentUploadsReader<String> matchReader = new RecentUploadsReader<String>();
        matchReader.setBlankNodeUriPrefix(VirtuosoBaseDAO.BNODE_URI_PREFIX);
        List<String> subjectUris = executeSPARQL(sqlBuf.toString(), matchReader);
        Map<String, Date> pairMap = matchReader.getResultMap();

        List<SubjectDTO> resultList = new ArrayList<SubjectDTO>();

        // if subjectUris not empty, get the subjects data and set their dates
        if (subjectUris != null && !subjectUris.isEmpty()) {

            logger
                    .trace("Recent uploads search, getting the data of the found subjects");

            // get the data of all found subjects
            SubjectDataReader dataReader = new SubjectDataReader(subjectUris);
            dataReader.setBlankNodeUriPrefix(VirtuosoBaseDAO.BNODE_URI_PREFIX);

            // only these predicates will be queried for
            String[] neededPredicates = null;
            if (rdfType.equals(Subjects.ROD_OBLIGATION_CLASS)) {
                // properties for obligations
                String[] neededPredicatesObl = { Predicates.RDFS_LABEL,
                        Predicates.ROD_ISSUE_PROPERTY,
                        Predicates.ROD_INSTRUMENT_PROPERTY };
                neededPredicates = neededPredicatesObl;
            } else if (rdfType.equals(Subjects.ROD_DELIVERY_CLASS)) {
                // properties for deliveries
                String[] neededPredicatesDeliveries = { Predicates.RDFS_LABEL,
                        Predicates.ROD_OBLIGATION_PROPERTY,
                        Predicates.ROD_LOCALITY_PROPERTY };
                neededPredicates = neededPredicatesDeliveries;
            }

            // get the subjects data
            resultList = getSubjectsData(subjectUris, neededPredicates,
                    dataReader, null);

            // set dublin core date of found subjects
            if (pairMap != null) {
                for (SubjectDTO subject : resultList) {
                    subject.setDcDate(pairMap.get(subject.getUri()));
                }
            }
        }
        return resultList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HelperDAO#getSubjectsNewerThan(java.util.Date, int)
     */
    @Override
    public List<SubjectDTO> getSubjectsNewerThan(Date timestamp, int limit)
            throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HelperDAO#getPicklistForPredicate(java.lang.String)
     */
    @Override
    public Collection<ObjectLabelPair> getPicklistForPredicate(
            String predicateUri, boolean extractLabels) throws DAOException {
        if (StringUtils.isBlank(predicateUri)) {
            return Collections.emptyList();
        }
        // TODO use CustomSearch instead?
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("SELECT DISTINCT ?label ?object WHERE { ?s <")
                .append(predicateUri).append("> ?object ").append(
                        "OPTIONAL {?object <").append(Predicates.RDFS_LABEL)
                .append("> ?label }} ORDER BY ?label");

        long startTime = System.currentTimeMillis();
        logger.trace("getPicklistForPredicate query: " + strBuilder.toString());

        ObjectLabelReader reader = new ObjectLabelReader(extractLabels);
        // execute the query, with the IN parameters
        executeSPARQL(strBuilder.toString(), reader);

        logger.trace("getPicklistForPredicate query took "
                + Util.durationSince(startTime));

        return reader.getResultList();

    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HelperDAO#addTriples(eionet.cr.dto.SubjectDTO)
     */
    @Override
    public void addTriples(SubjectDTO subjectDTO) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HelperDAO#addResource(java.lang.String,
     * java.lang.String)
     */
    @Override
    public void addResource(String uri, String sourceUri) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HelperDAO#getAddibleProperties(java.util.Collection)
     */
    @Override
    public HashMap<String, String> getAddibleProperties(
            Collection<String> subjectTypes) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HelperDAO#getSubjectSchemaUri(java.lang.String)
     */
    @Override
    public String getSubjectSchemaUri(String subjectUri) throws DAOException {

        if (StringUtils.isBlank(subjectUri)) {
            return null;
        }
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("select distinct ?o where { ").append("<").append(
                subjectUri).append("> <").append(Predicates.CR_SCHEMA).append(
                "> ?o ").append("} limit 1");

        List<String> objectUri = executeSPARQL(strBuilder.toString(),
                new SingleObjectReader<String>());

        return (objectUri != null && objectUri.size() > 0) ? objectUri.get(0)
                : null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HelperDAO#isAllowLiteralSearch(java.lang.String)
     */
    @Override
    public boolean isAllowLiteralSearch(String predicateUri)
            throws DAOException {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HelperDAO#getPredicatesUsedForType(java.lang.String)
     */
    @Override
    public List<SubjectDTO> getPredicatesUsedForType(String typeUri)
            throws DAOException {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("SELECT distinct ?p WHERE { ?s ?p ?o . ?s <").append(
                Predicates.RDF_TYPE);
        strBuilder.append("> <").append(typeUri).append("> }");

        long startTime = System.currentTimeMillis();
        logger.trace("usedPredicatesForType query: " + strBuilder.toString());

        // execute the query, with the IN parameters
        List<String> predicateUris = executeSPARQL(strBuilder.toString(),
                new SingleObjectReader<String>());

        logger.trace("usedPredicatesForType query took "
                + Util.durationSince(startTime));

        List<SubjectDTO> resultList = new ArrayList<SubjectDTO>();
        if (predicateUris != null && !predicateUris.isEmpty()) {

            // only these predicates will be queried for
            String[] neededPredicates = { Predicates.RDF_TYPE,
                    Predicates.RDFS_LABEL };

            // get the data of all found subjects
            logger
                    .trace("Search by filters, getting the data of the found subjects");
            resultList = getSubjectsData(predicateUris, neededPredicates,
                    new SubjectDataReader(predicateUris), null);

            // since a used predicate may not appear as a subject in SPO,
            // there might unfound SubjectDTO objects
            List<String> unfoundSubjects = new ArrayList<String>();
            for (String entry : predicateUris) {
                SubjectDTO newSubject = new SubjectDTO(entry, false);
                if (!resultList.contains(newSubject)) {
                    unfoundSubjects.add(entry);
                }
            }

            // if there were indeed any unfound SubjectDTO objects, find URIs
            // for those predicates
            // and create dummy SubjectDTO objects from those URIs
            if (!unfoundSubjects.isEmpty()) {
                for (String entry : unfoundSubjects) {
                    if (!StringUtils.isBlank(entry)) {
                        unfoundSubjects.remove(entry);
                        resultList.add(new SubjectDTO(entry, false));
                    }
                }
            }
        }

        return resultList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HelperDAO#getSpatialSources()
     */
    @Override
    public List<String> getSpatialSources() throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HelperDAO#getSubject(java.lang.String)
     */
    @Override
    public SubjectDTO getSubject(String subjectUri) throws DAOException {

        if (StringUtils.isBlank(subjectUri)) {
            return null;
        }

        Map<Long, SubjectDTO> map = new LinkedHashMap<Long, SubjectDTO>();
        long subjectHash = Hashes.spoHash(subjectUri);
        map.put(Long.valueOf(subjectHash), null);
        SubjectDataReader reader = new SubjectDataReader(map);
        reader.setBlankNodeUriPrefix(VirtuosoBaseDAO.BNODE_URI_PREFIX);

        List<SubjectDTO> subjects = getSubjectsData(Collections
                .singletonList(subjectUri), null, reader, null, false);
        return subjects == null || subjects.isEmpty() ? null : subjects.get(0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HelperDAO#getSubject(java.lang.Long)
     */
    @Override
    public SubjectDTO getSubject(Long subjectHash) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HelperDAO#getPredicateLabels(java.util.Set)
     */
    @Override
    public PredicateLabels getPredicateLabels(Set<Long> subjectHashes)
            throws DAOException {

        // TODO: implement this method
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HelperDAO#getSubProperties(java.util.Set)
     */
    @Override
    public SubProperties getSubProperties(Set<Long> subjectHashes)
            throws DAOException {
        // TODO: implement this method
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HelperDAO#getDataflowSearchPicklist()
     */
    @Override
    public HashMap<String, ArrayList<UriLabelPair>> getDataflowSearchPicklist()
            throws DAOException {
        // throw new UnsupportedOperationException("Method not implemented");
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("SELECT ?li_title ?ro_title ?ro_uri  WHERE { ")
                .append("?ro_uri <").append(Predicates.ROD_INSTRUMENT_PROPERTY)
                .append("> ?li_uri . ").append("?li_uri <").append(
                        Predicates.DCTERMS_ALTERNATIVE)
                .append("> ?li_title . ").append("?ro_uri <").append(
                        Predicates.DCTERMS_TITLE).append("> ?ro_title ")
                .append("} ORDER BY ?li_title ?ro_title");

        long startTime = System.currentTimeMillis();
        logger.trace("getDataflowSearchPicklist query: "
                + strBuilder.toString());

        // TODO types
        DataflowPicklistReader<HashMap<String, ArrayList<UriLabelPair>>> reader = new DataflowPicklistReader<HashMap<String, ArrayList<UriLabelPair>>>();
        executeSPARQL(strBuilder.toString(), reader);

        logger.trace("getDataflowSearchPicklist query took "
                + Util.durationSince(startTime));

        // FIXME
        return reader.getResultMap();

    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HelperDAO#getDistinctOrderedTypes()
     */
    @Override
    public ArrayList<Pair<String, String>> getDistinctOrderedTypes()
            throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HelperDAO#getSubjectCountInSource(long)
     */
    @Override
    public int getSubjectCountInSource(long sourceHash) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HelperDAO#getLatestHarvestedURLs(int)
     */
    @Override
    public Pair<Integer, List<HarvestedUrlCountDTO>> getLatestHarvestedURLs(
            int days) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HelperDAO#getUrgencyOfComingHarvests(int)
     */
    @Override
    public Pair<Integer, List<HarvestUrgencyScoreDTO>> getUrgencyOfComingHarvests(
            int amount) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HelperDAO#isUrlInHarvestSource(java.lang.String)
     */
    @Override
    public boolean isUrlInHarvestSource(String url) throws DAOException {

        // TODO: implement this method
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HelperDAO#updateTypeDataCache()
     */
    @Override
    public void updateTypeDataCache() throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * eionet.cr.dao.HelperDAO#registerUserUrl(eionet.cr.web.security.CRUser,
     * java.lang.String, boolean)
     */
    @Override
    public void registerUserUrl(CRUser user, String url, boolean isBookmark)
            throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * eionet.cr.dao.HelperDAO#addUserBookmark(eionet.cr.web.security.CRUser,
     * java.lang.String)
     */
    @Override
    public void addUserBookmark(CRUser user, String url) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * eionet.cr.dao.HelperDAO#deleteUserBookmark(eionet.cr.web.security.CRUser,
     * java.lang.String)
     */
    @Override
    public void deleteUserBookmark(CRUser user, String url) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * eionet.cr.dao.HelperDAO#getUserBookmarks(eionet.cr.web.security.CRUser)
     */
    @Override
    public List<UserBookmarkDTO> getUserBookmarks(CRUser user)
            throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * eionet.cr.dao.HelperDAO#isSubjectUserBookmark(eionet.cr.web.security.
     * CRUser, long)
     */
    @Override
    public boolean isSubjectUserBookmark(CRUser user, long subjectHash)
            throws DAOException {

        // TODO: implement actual logic
        return false;

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * eionet.cr.dao.HelperDAO#updateUserHistory(eionet.cr.web.security.CRUser,
     * java.lang.String)
     */
    @Override
    public void updateUserHistory(CRUser user, String url) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * eionet.cr.dao.HelperDAO#getUserHistory(eionet.cr.web.security.CRUser)
     */
    @Override
    public List<UserHistoryDTO> getUserHistory(CRUser user) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HelperDAO#getSampleTriplesInSource(java.lang.String,
     * eionet.cr.util.pagination.PagingRequest)
     */
    @Override
    public List<TripleDTO> getSampleTriplesInSource(String sourceUrl,
            PagingRequest pagingRequest) throws DAOException {

        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("SELECT ?s ?p ?o FROM <").append(sourceUrl).append(
                "> WHERE {?s ?p ?o} LIMIT 100");

        TriplesReader reader = new TriplesReader();
        return executeSPARQL(strBuilder.toString(), reader);

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * eionet.cr.dao.HelperDAO#generateNewReviewId(eionet.cr.web.security.CRUser
     * )
     */
    @Override
    public int generateNewReviewId(CRUser user) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * eionet.cr.dao.HelperDAO#getLastReviewId(eionet.cr.web.security.CRUser)
     */
    @Override
    public int getLastReviewId(CRUser user) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HelperDAO#addReview(eionet.cr.dto.ReviewDTO,
     * eionet.cr.web.security.CRUser)
     */
    @Override
    public int addReview(ReviewDTO review, CRUser user) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HelperDAO#saveReview(int, eionet.cr.dto.ReviewDTO,
     * eionet.cr.web.security.CRUser)
     */
    @Override
    public void saveReview(int reviewId, ReviewDTO review, CRUser user)
            throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HelperDAO#getReviewList(eionet.cr.web.security.CRUser)
     */
    @Override
    public List<ReviewDTO> getReviewList(CRUser user) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HelperDAO#getReview(eionet.cr.web.security.CRUser,
     * int)
     */
    @Override
    public ReviewDTO getReview(CRUser user, int reviewId) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * eionet.cr.dao.HelperDAO#getReviewAttachmentList(eionet.cr.web.security
     * .CRUser, int)
     */
    @Override
    public List<String> getReviewAttachmentList(CRUser user, int reviewId)
            throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HelperDAO#deleteReview(eionet.cr.web.security.CRUser,
     * int, boolean)
     */
    @Override
    public void deleteReview(CRUser user, int reviewId,
            boolean deleteAttachments) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * eionet.cr.dao.HelperDAO#deleteAttachment(eionet.cr.web.security.CRUser,
     * int, java.lang.String)
     */
    @Override
    public void deleteAttachment(CRUser user, int reviewId, String attachmentUri)
            throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HelperDAO#loadAttachment(java.lang.String)
     */
    @Override
    public DownloadFileDTO loadAttachment(String attachmentUri)
            throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HelperDAO#deleteTriples(java.util.Collection)
     */
    @Override
    public void deleteTriples(Collection<TripleDTO> triples)
            throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HelperDAO#deleteTriplesOfSource(long)
     */
    @Override
    public void deleteTriplesOfSource(long sourceHash) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * eionet.cr.dao.HelperDAO#getUserUploads(eionet.cr.web.security.CRUser)
     */
    @Override
    public Collection<UploadDTO> getUserUploads(CRUser crUser)
            throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HelperDAO#isExistingSubject(java.lang.String)
     */
    @Override
    public boolean isExistingSubject(String subjectUri) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HelperDAO#deleteSubjects(java.util.List)
     */
    @Override
    public void deleteSubjects(List<String> subjectUris) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HelperDAO#renameSubjects(java.util.Map)
     */
    @Override
    public void renameSubjects(Map<Long, String> newUrisByOldHashes)
            throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HelperDAO#readDistinctPredicates(java.lang.Long)
     */
    @Override
    public List<PredicateDTO> readDistinctPredicates(Long sourceHash)
            throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HelperDAO#readDistinctSubjectUrls(java.lang.Long)
     */
    @Override
    public List<String> readDistinctSubjectUrls(Long sourceHash)
            throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * eionet.cr.dao.HelperDAO#outputSourceTriples(eionet.cr.dao.readers.RDFExporter
     * )
     */
    @Override
    public void outputSourceTriples(RDFExporter reader) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HelperDAO#deleteTriples(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public void deleteTriples(String subjectUri, String predicateUri,
            String sourceUri) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HelperDAO#getTriplesCount()
     */
    @Override
    public long getTriplesCount() throws DAOException {

        String query = "SELECT count(*) WHERE {?s ?p ?o}";
        Object resultObject = executeUniqueResultSPARQL(query,
                new SingleObjectReader<Long>());
        return Long.valueOf(resultObject.toString());

    }
    
}
