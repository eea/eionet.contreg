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
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import eionet.cr.common.Namespace;
import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dao.readers.DataflowPicklistReader;
import eionet.cr.dao.readers.MapReader;
import eionet.cr.dao.readers.ObjectLabelReader;
import eionet.cr.dao.readers.RDFExporter;
import eionet.cr.dao.readers.RecentFilesReader;
import eionet.cr.dao.readers.RecentUploadsReader;
import eionet.cr.dao.readers.SubPropertiesReader;
import eionet.cr.dao.readers.SubjectDataReader;
import eionet.cr.dao.readers.TriplesReader;
import eionet.cr.dao.util.PredicateLabels;
import eionet.cr.dao.util.SubProperties;
import eionet.cr.dao.util.UriLabelPair;
import eionet.cr.dto.DownloadFileDTO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.PredicateDTO;
import eionet.cr.dto.ReviewDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.dto.TripleDTO;
import eionet.cr.dto.UploadDTO;
import eionet.cr.dto.UserBookmarkDTO;
import eionet.cr.dto.UserHistoryDTO;
import eionet.cr.util.Hashes;
import eionet.cr.util.ObjectLabelPair;
import eionet.cr.util.Pair;
import eionet.cr.util.Util;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.util.sesame.SesameUtil;
import eionet.cr.util.sql.SingleObjectReader;
import eionet.cr.web.security.CRUser;

/**
 *
 * @author jaanus
 *
 */
public class VirtuosoHelperDAO extends VirtuosoBaseDAO implements HelperDAO {

    /**
     * Returns latest harvested files (type=cr:File) in descending order (cr:firstSeen).
     *
     * @param limit
     *            count of latest files
     * @return List of Pair containing URL and date
     * @see eionet.cr.dao.HelperDAO#getLatestFiles(int)
     * @throws DAOException
     *             if query fails
     */
    @Override
    public List<Pair<String, String>> getLatestFiles(final int limit) throws DAOException {

        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("define input:inference '").append(
                GeneralConfig.getProperty(GeneralConfig.VIRTUOSO_CR_RULESET_NAME)).append("' ").append(
                "SELECT DISTINCT ?s ?l ?d WHERE ").append("{?s a <").append(Predicates.CR_FILE).append("> ").append(
                ". OPTIONAL { ?s <").append(Predicates.CR_FIRST_SEEN).append("> ?d } ").append(
                        ". OPTIONAL { ?s <").append(Predicates.RDFS_LABEL).append("> ?l } ").append(
                "} ORDER BY DESC(?d) LIMIT ")
                .append(limit);

        RecentFilesReader reader = new RecentFilesReader();
        reader.setBlankNodeUriPrefix(VirtuosoBaseDAO.BNODE_URI_PREFIX);

        return executeSPARQL(strBuilder.toString(), reader);

    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#getLatestSubjects(java.lang.String, int)
     */
    @Override
    public Collection<SubjectDTO> getLatestSubjects(String rdfType, int limit) throws DAOException {

        // validate arguments
        if (StringUtils.isBlank(rdfType))
            throw new IllegalArgumentException("rdfType must not be blank!");
        if (limit <= 0)
            throw new IllegalArgumentException("limit must be greater than 0!");

        StringBuffer sqlBuf = new StringBuffer()
                .append("prefix dc: <http://purl.org/dc/elements/1.1/> ")
                .append("select distinct ?s ?d where { ").append("?s a <")
                .append(rdfType).append("> . ")
                .append("OPTIONAL { ?s dc:date ?d } ")
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

            logger.trace("Recent uploads search, getting the data of the found subjects");

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
                .append(predicateUri).append("> ?object ")
                .append("OPTIONAL {?object <").append(Predicates.RDFS_LABEL)
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
        RepositoryConnection conn = null;
        try {

            conn = SesameUtil.getRepositoryConnection();

            URI sub = conn.getValueFactory().createURI(subjectDTO.getUri());

            for (String predicateUri : subjectDTO.getPredicateUris()) {
                URI pred = conn.getValueFactory().createURI(predicateUri);
                ObjectDTO object = subjectDTO.getObject(predicateUri);

                String sourceUri = object.getSourceUri();
                URI source = conn.getValueFactory().createURI(sourceUri);

                if (object.isLiteral()) {
                    Literal literalObject = conn.getValueFactory().createLiteral(object.toString(), object.getDatatype());
                    conn.add(sub, pred, literalObject, source);
                } else {
                    URI resourceObject = conn.getValueFactory().createURI(object.toString());
                    conn.add(sub, pred, resourceObject, source);
                }
            }
        } catch (RepositoryException e) {
            throw new DAOException(e.toString(), e);
        } finally {
            SesameUtil.close(conn);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#addResource(java.lang.String, java.lang.String)
     */
    @Override
    public void addResource(String uri, String sourceUri) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /**
     * Search for predicates that is allowed to edit on factsheet page.
     *
     * @param subjectTypes
     * @return the list of properties that can be added by user.
     * @throws DAOException
     *             if query fails
     */
    public HashMap<String, String> getAddibleProperties(Collection<String> subjectTypes)
                                                                        throws DAOException {

        HashMap<String, String> result = new HashMap<String, String>();

        StringBuilder strBuilder = new StringBuilder();
        strBuilder
                .append("PREFIX rdf: <")
                .append(Namespace.RDF.getUri())
                .append("> PREFIX rdfs: <")
                .append(Namespace.RDFS.getUri())
                .append("> select distinct ?object ?label where {?object rdfs:label ?label ")
                .append(". ?object rdf:type rdf:Property . ?object rdfs:isDefinedBy <")
                .append(Subjects.DUBLIN_CORE_SOURCE_URL).append(">}");

        ObjectLabelReader reader = new ObjectLabelReader(true);
        executeSPARQL(strBuilder.toString(), reader);

        /* get the properties for given subject types */
        if (subjectTypes != null && !subjectTypes.isEmpty()) {

            strBuilder = new StringBuilder();
            strBuilder
                    .append("PREFIX rdfs: <")
                    .append(Namespace.RDFS.getUri())
                    .append("> select distinct ?object ?label WHERE { ?object rdfs:label ?label ")
                    .append(". ?object rdfs:domain ?o . FILTER (?o IN (")
                    .append(Util.sparqlUrisToCsv(subjectTypes)).append("))}");

            executeSPARQL(strBuilder.toString(), reader);
        }

        if (reader != null && reader.getResultList() != null) {
            for (ObjectLabelPair objectLabelPair : reader.getResultList()) {
                result.put(objectLabelPair.getLeft(), objectLabelPair.getRight());
            }
        }
        return result;

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
        strBuilder.append("select distinct ?o where { ").append("<")
                .append(subjectUri).append("> <").append(Predicates.CR_SCHEMA)
                .append("> ?o ").append("} limit 1");

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
            logger.trace("Search by filters, getting the data of the found subjects");
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

        List<SubjectDTO> subjects = getSubjectsData(
                Collections.singletonList(subjectUri), null, reader, null,
                false, false);
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

    /**
     * Finds SubProperties of given subject list.
     *
     * @param subjects
     *            Collection<String> subject URIs
     * @return SubProperties
     * @throws DAOException
     *             if query fails
     */
    @Override
    public SubProperties getSubProperties(final Collection<String> subjects) throws DAOException {

        StringBuilder sparql = new StringBuilder();
        sparql.append("select ?p ?s    WHERE { ?p <").append(Predicates.RDFS_SUBPROPERTY_OF).append(
                ">  ?s ");
        sparql.append("FILTER (?s IN ( ");
        int i = 0;
        for (String subject : subjects) {
            if (i > 0) {
                sparql.append(", ");
            }
            sparql.append("<").append(subject).append("> ");
            i++;
        }
        sparql.append(") ) }");
        SubProperties subProperties = new SubProperties();
        SubPropertiesReader reader = new SubPropertiesReader(subProperties);
        executeSPARQL(sparql.toString(), reader);

        return subProperties;
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
                .append("> ?li_uri . ").append("?li_uri <")
                .append(Predicates.DCTERMS_ALTERNATIVE)
                .append("> ?li_title . ").append("?ro_uri <")
                .append(Predicates.DCTERMS_TITLE).append("> ?ro_title ")
                .append("} ORDER BY ?li_title ?ro_title");

        long startTime = System.currentTimeMillis();
        logger.trace("getDataflowSearchPicklist query: "
                + strBuilder.toString());

        // TODO types
        DataflowPicklistReader<HashMap<String, ArrayList<UriLabelPair>>> reader =
                new DataflowPicklistReader<HashMap<String, ArrayList<UriLabelPair>>>();
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
    // @Override
    // public Pair<Integer, List<HarvestedUrlCountDTO>> getLatestHarvestedURLs(
    // int days) throws DAOException {
    // throw new UnsupportedOperationException("Method not implemented");
    //
    // }

    // /*
    // * (non-Javadoc)
    // *
    // * @see eionet.cr.dao.HelperDAO#getUrgencyOfComingHarvests(int)
    // */
    // @Override
    // public Pair<Integer, List<HarvestUrgencyScoreDTO>> getUrgencyOfComingHarvests(
    // int amount) throws DAOException {
    // throw new UnsupportedOperationException("Method not implemented");
    //
    // }

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
     * @see eionet.cr.dao.HelperDAO#registerUserUrl(eionet.cr.web.security.CRUser, java.lang.String, boolean)
     */
    @Override
    public void registerUserUrl(CRUser user, String url, boolean isBookmark)
            throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#addUserBookmark(eionet.cr.web.security.CRUser, java.lang.String)
     */
    @Override
    public void addUserBookmark(CRUser user, String url) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#deleteUserBookmark(eionet.cr.web.security.CRUser, java.lang.String)
     */
    @Override
    public void deleteUserBookmark(CRUser user, String url) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#getUserBookmarks(eionet.cr.web.security.CRUser)
     */
    @Override
    public List<UserBookmarkDTO> getUserBookmarks(CRUser user)
            throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#isSubjectUserBookmark(eionet.cr.web.security. CRUser, long)
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
     * @see eionet.cr.dao.HelperDAO#updateUserHistory(eionet.cr.web.security.CRUser, java.lang.String)
     */
    /**
     *
     */
    @Override
    public void updateUserHistory(CRUser user, String url) throws DAOException {

        // if URL not yet in user history, add it there
        SubjectDTO userHomeItemSubject = new SubjectDTO(user.getHomeItemUri(url), false);
        ObjectDTO objectDTO = new ObjectDTO(url, false);
        objectDTO.setSourceUri(user.getHistoryUri());
        userHomeItemSubject.addObject(Predicates.CR_HISTORY, objectDTO);

        addTriples(userHomeItemSubject);

        // delete old history object
        List<TripleDTO> triples = new ArrayList<TripleDTO>();
        TripleDTO triple = new TripleDTO(user.getHomeItemUri(url), Predicates.CR_SAVETIME, null);
        triple.setSourceUri(user.getHistoryUri());
        triples.add(triple);

        deleteTriples(triples);

        // now add new save-time
        userHomeItemSubject = new SubjectDTO(user.getHomeItemUri(url), false);
        objectDTO = new ObjectDTO(
                Util.dateToString(new Date(), "yyyy-MM-dd'T'HH:mm:ss"), true, XMLSchema.DATETIME);
        objectDTO.setSourceUri(user.getHistoryUri());
        userHomeItemSubject.addObject(Predicates.CR_SAVETIME, objectDTO);

        addTriples(userHomeItemSubject);

    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#getUserHistory(eionet.cr.web.security.CRUser)
     */
    @Override
    public List<UserHistoryDTO> getUserHistory(CRUser user) throws DAOException {

        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("PREFIX cr: <").append(Namespace.CR.getUri()).append("> select ?url ?time FROM <")
                .append(user.getHistoryUri())
                .append("> WHERE {?s cr:userHistory ?url . ?s cr:userSaveTime ?time} order by desc(?time)");

        List<UserHistoryDTO> returnHistory = new ArrayList<UserHistoryDTO>();
        MapReader reader = new MapReader();
        executeSPARQL(strBuilder.toString(), reader);

        if (reader.getResultList() != null && reader.getResultList().size() > 0) {
            for (Map<String, String> resultItem : reader.getResultList()) {
                UserHistoryDTO historyItem = new UserHistoryDTO();
                historyItem.setUrl(resultItem.get("url"));
                historyItem.setLastOperation(resultItem.get("time"));
                returnHistory.add(historyItem);
            }
        }
        return returnHistory;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#getSampleTriplesInSource(java.lang.String, eionet.cr.util.pagination.PagingRequest)
     */
    @Override
    public List<TripleDTO> getSampleTriplesInSource(String sourceUrl,
            PagingRequest pagingRequest) throws DAOException {

        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("SELECT ?s ?p ?o FROM <").append(sourceUrl)
                .append("> WHERE {?s ?p ?o} LIMIT 100");

        TriplesReader reader = new TriplesReader();
        return executeSPARQL(strBuilder.toString(), reader);

    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#generateNewReviewId(eionet.cr.web.security.CRUser )
     */
    @Override
    public int generateNewReviewId(CRUser user) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#getLastReviewId(eionet.cr.web.security.CRUser)
     */
    @Override
    public int getLastReviewId(CRUser user) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#addReview(eionet.cr.dto.ReviewDTO, eionet.cr.web.security.CRUser)
     */
    @Override
    public int addReview(ReviewDTO review, CRUser user) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#saveReview(int, eionet.cr.dto.ReviewDTO, eionet.cr.web.security.CRUser)
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
     * @see eionet.cr.dao.HelperDAO#getReview(eionet.cr.web.security.CRUser, int)
     */
    @Override
    public ReviewDTO getReview(CRUser user, int reviewId) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#getReviewAttachmentList(eionet.cr.web.security .CRUser, int)
     */
    @Override
    public List<String> getReviewAttachmentList(CRUser user, int reviewId)
            throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#deleteReview(eionet.cr.web.security.CRUser, int, boolean)
     */
    @Override
    public void deleteReview(CRUser user, int reviewId,
            boolean deleteAttachments) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#deleteAttachment(eionet.cr.web.security.CRUser, int, java.lang.String)
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
    public void deleteTriples(Collection<TripleDTO> triples) throws DAOException {

        RepositoryConnection conn = null;
        try {

            conn = SesameUtil.getRepositoryConnection();

            for (TripleDTO triple : triples) {
                URI sub = conn.getValueFactory().createURI(triple.getSubjectUri());
                URI pred = conn.getValueFactory().createURI(triple.getPredicateUri());
                URI source = conn.getValueFactory().createURI(triple.getSourceUri());

                String strObject = triple.getObject();
                Literal literalObject = null;
                if (strObject != null) {
                    conn.getValueFactory().createLiteral(strObject);
                }

                conn.remove(sub, pred, literalObject, source);
            }
        } catch (RepositoryException e) {
            throw new DAOException(e.toString(), e);
        } finally {
            SesameUtil.close(conn);
        }

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
     * @see eionet.cr.dao.HelperDAO#getUserUploads(eionet.cr.web.security.CRUser)
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
     * @see eionet.cr.dao.HelperDAO#outputSourceTriples(eionet.cr.dao.readers.RDFExporter )
     */
    @Override
    public void outputSourceTriples(RDFExporter reader) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#deleteTriples(java.lang.String, java.lang.String, java.lang.String)
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
