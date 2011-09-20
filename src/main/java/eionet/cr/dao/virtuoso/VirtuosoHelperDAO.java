package eionet.cr.dao.virtuoso;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

import eionet.cr.common.Namespace;
import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dao.readers.DeliverySearchPicklistReader;
import eionet.cr.dao.readers.MapReader;
import eionet.cr.dao.readers.ObjectLabelReader;
import eionet.cr.dao.readers.RDFExporter;
import eionet.cr.dao.readers.RecentFilesReader;
import eionet.cr.dao.readers.RecentUploadsReader;
import eionet.cr.dao.readers.SPOReader;
import eionet.cr.dao.readers.SubPropertiesReader;
import eionet.cr.dao.readers.SubjectDataReader;
import eionet.cr.dao.readers.TriplesReader;
import eionet.cr.dao.readers.UploadDTOReader;
import eionet.cr.dao.util.PredicateLabels;
import eionet.cr.dao.util.SubProperties;
import eionet.cr.dao.util.UriLabelPair;
import eionet.cr.dao.virtuoso.helpers.ResourceRenameHandler;
import eionet.cr.dto.DownloadFileDTO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.PredicateDTO;
import eionet.cr.dto.ReviewDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.dto.TripleDTO;
import eionet.cr.dto.UploadDTO;
import eionet.cr.dto.UserBookmarkDTO;
import eionet.cr.dto.UserHistoryDTO;
import eionet.cr.harvest.BaseHarvest;
import eionet.cr.util.Bindings;
import eionet.cr.util.Hashes;
import eionet.cr.util.ObjectLabelPair;
import eionet.cr.util.Pair;
import eionet.cr.util.URIUtil;
import eionet.cr.util.URLUtil;
import eionet.cr.util.Util;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.util.sesame.SPARQLQueryUtil;
import eionet.cr.util.sesame.SesameUtil;
import eionet.cr.util.sql.SQLUtil;
import eionet.cr.util.sql.SingleObjectReader;
import eionet.cr.web.security.CRUser;

/**
 * Virtuoso DAO helper methods.
 */
public class VirtuosoHelperDAO extends VirtuosoBaseDAO implements HelperDAO {
    /**
     * SPARQL for last harvested files cache.
     */
    private static final String LATEST_FILES_SPARQL = "define input:inference '"
        + GeneralConfig.getProperty(GeneralConfig.VIRTUOSO_CR_RULESET_NAME) + "' SELECT DISTINCT ?s ?l ?d WHERE {?s a <"
        + Predicates.CR_FILE + "> " + ". OPTIONAL { ?s <" + Predicates.CR_FIRST_SEEN + "> ?d } . OPTIONAL { ?s <"
        + Predicates.RDFS_LABEL + "> ?l } } " + "ORDER BY DESC(?d) LIMIT ?filesCount";

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
        Bindings bindings = new Bindings();
        bindings.setInt("filesCount", limit);

        RecentFilesReader reader = new RecentFilesReader();
        reader.setBlankNodeUriPrefix(VirtuosoBaseDAO.BNODE_URI_PREFIX);

        return executeSPARQL(LATEST_FILES_SPARQL, bindings, reader);

    }

    /**
     * SPARQL for filling the latest subjects cache.
     */
    private static final String LATEST_SUBJECTS_SPARQL = "prefix dc:<" + Subjects.DUBLIN_CORE_SOURCE_URL + "> "
    + "select distinct ?s ?d where {?s a ?rdfType . " + "OPTIONAL { ?s dc:date ?d }} ORDER BY DESC(?d) LIMIT ?queryLimit";

    @Override
    public Collection<SubjectDTO> getLatestSubjects(String rdfType, int limit) throws DAOException {

        // validate arguments
        if (StringUtils.isBlank(rdfType)) {
            throw new IllegalArgumentException("rdfType must not be blank!");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be greater than 0!");
        }
        RecentUploadsReader<String> matchReader = new RecentUploadsReader<String>();
        matchReader.setBlankNodeUriPrefix(VirtuosoBaseDAO.BNODE_URI_PREFIX);
        Bindings bindings = new Bindings();
        bindings.setURI("rdfType", rdfType);
        bindings.setInt("queryLimit", limit);
        logger.trace("Recent uploads search, executing subject finder query: " + LATEST_SUBJECTS_SPARQL);
        logger.trace("Recent uploads search, executing subject finder query, bindings: " + bindings.toString());

        List<String> subjectUris = executeSPARQL(LATEST_SUBJECTS_SPARQL, bindings, matchReader);
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
                String[] neededPredicatesObl =
                {Predicates.RDFS_LABEL, Predicates.ROD_ISSUE_PROPERTY, Predicates.ROD_INSTRUMENT_PROPERTY};
                neededPredicates = neededPredicatesObl;
            } else if (rdfType.equals(Subjects.ROD_DELIVERY_CLASS)) {
                // properties for deliveries
                String[] neededPredicatesDeliveries =
                {Predicates.RDFS_LABEL, Predicates.ROD_OBLIGATION_PROPERTY, Predicates.ROD_LOCALITY_PROPERTY};
                neededPredicates = neededPredicatesDeliveries;
            }

            // get the subjects data
            resultList = getSubjectsData(subjectUris, neededPredicates, dataReader);

            // set dublin core date of found subjects
            if (pairMap != null) {
                for (SubjectDTO subject : resultList) {
                    subject.setDcDate(pairMap.get(subject.getUri()));
                }
            }
        }
        return resultList;
    }

    @Override
    public List<SubjectDTO> getSubjectsNewerThan(Date timestamp, int limit) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /**
     * SPARQL for picklist cache of the given predicate.
     */
    private static final String PREDICATE_PICKLIST_SPARQL = "SELECT DISTINCT ?label ?object WHERE { ?s ?predicateUri ?object "
        + "OPTIONAL {?object <" + Predicates.RDFS_LABEL + "> ?label }} ORDER BY ?label";

    @Override
    public Collection<ObjectLabelPair> getPicklistForPredicate(String predicateUri, boolean extractLabels) throws DAOException {
        if (StringUtils.isBlank(predicateUri)) {
            return Collections.emptyList();
        }

        Bindings bindings = new Bindings();
        bindings.setURI("predicateUri", predicateUri);

        long startTime = System.currentTimeMillis();
        logger.trace("getPicklistForPredicate query: " + PREDICATE_PICKLIST_SPARQL);
        logger.trace("getPicklistForPredicate predicate: " + bindings);

        ObjectLabelReader reader = new ObjectLabelReader(extractLabels);
        // execute the query, with the IN parameters
        executeSPARQL(PREDICATE_PICKLIST_SPARQL, bindings, reader);

        logger.trace("getPicklistForPredicate query took " + Util.durationSince(startTime));

        return reader.getResultList();

    }

    @Override
    public void addTriples(SubjectDTO subjectDTO) throws DAOException {
        RepositoryConnection conn = null;
        try {

            conn = SesameUtil.getRepositoryConnection();

            URI sub = conn.getValueFactory().createURI(subjectDTO.getUri());

            for (String predicateUri : subjectDTO.getPredicateUris()) {
                URI pred = conn.getValueFactory().createURI(predicateUri);

                Collection<ObjectDTO> objects = subjectDTO.getObjects(predicateUri);
                if (objects != null && !objects.isEmpty()) {
                    for (ObjectDTO object : objects) {

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
                }
            }
        } catch (RepositoryException e) {
            throw new DAOException(e.toString(), e);
        } finally {
            SesameUtil.close(conn);
        }
    }

    @Override
    public void addResource(String uri, String sourceUri) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /**
     * SPARQL for properties defined by Dublin Core.
     */
    private static final String PROPS_DUBLINCORE_QUERY = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
        + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " + "select distinct ?object ?label where "
        + "{?object rdfs:label ?label . ?object rdf:type rdf:Property " + ". ?object rdfs:isDefinedBy <"
        + Subjects.DUBLIN_CORE_SOURCE_URL + ">}";

    /**
     * Search for predicates that is allowed to edit on factsheet page.
     *
     * @param subjectTypes
     *            rdf:type resources of the subject
     * @return the list of properties that can be added by user.
     * @throws DAOException
     *             if query fails
     */
    @Override
    public HashMap<String, String> getAddibleProperties(Collection<String> subjectTypes) throws DAOException {

        HashMap<String, String> result = new HashMap<String, String>();

        ObjectLabelReader reader = new ObjectLabelReader(true);
        executeSPARQL(PROPS_DUBLINCORE_QUERY, reader);

        /* get the properties for given subject types */
        // TODO - actually it is a static set based on DC properties
        if (subjectTypes != null && !subjectTypes.isEmpty()) {

            Bindings bindings = new Bindings();
            String subjectTypesCSV = SPARQLQueryUtil.urisToCSV(subjectTypes, "subjectValue", bindings);
            String sparql =
                "PREFIX rdfs: <" + Namespace.RDFS.getUri() + "> "
                + "select distinct ?object ?label WHERE { ?object rdfs:label ?label . ?object rdfs:domain ?o "
                + ". FILTER (?o IN (" + subjectTypesCSV + "))}";

            // PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> select
            // distinct ?object ?label WHERE { ?object rdfs:label ?label . ?object rdfs:domain ?o .
            // FILTER (?o IN (<http://www.eea.europa.eu/portal_types/Article#Article>))}
            executeSPARQL(sparql, bindings, reader);
        }

        if (reader != null && reader.getResultList() != null) {
            for (ObjectLabelPair objectLabelPair : reader.getResultList()) {
                result.put(objectLabelPair.getLeft(), objectLabelPair.getRight());
            }
        }
        return result;
    }

    /**
     * SPARQL for getting schema URI of the subject.
     */
    private static final String SUBJECT_SCHEMA_SPARQL = "select distinct ?o " + "where { ?subjectUri <" + Predicates.CR_SCHEMA
    + "> ?o } limit 1";

    @Override
    public String getSubjectSchemaUri(String subjectUri) throws DAOException {

        if (StringUtils.isBlank(subjectUri)) {
            return null;
        }
        Bindings bindings = new Bindings();
        bindings.setURI("subjectUri", subjectUri);

        List<String> objectUri = executeSPARQL(SUBJECT_SCHEMA_SPARQL, bindings, new SingleObjectReader<String>());

        return (objectUri != null && objectUri.size() > 0) ? objectUri.get(0) : null;
    }

    /**
     * SPARQL for getting predicates used for type.
     */
    private static final String PREDICATES_FOR_TYPE_SPARQL = "SELECT distinct ?p WHERE { ?s ?p ?o . ?s a ?rdfType }";

    @Override
    public List<SubjectDTO> getPredicatesUsedForType(String typeUri) throws DAOException {
        Bindings bindings = new Bindings();
        bindings.setURI("rdfType", typeUri);

        long startTime = System.currentTimeMillis();
        logger.trace("usedPredicatesForType query: " + PREDICATES_FOR_TYPE_SPARQL);
        logger.trace("usedPredicatesForType bindings: " + bindings);

        // execute the query, with the IN parameters
        List<String> predicateUris = executeSPARQL(PREDICATES_FOR_TYPE_SPARQL, bindings, new SingleObjectReader<String>());

        logger.trace("usedPredicatesForType query took " + Util.durationSince(startTime));

        List<SubjectDTO> resultList = new ArrayList<SubjectDTO>();
        if (predicateUris != null && !predicateUris.isEmpty()) {

            // only these predicates will be queried for
            String[] neededPredicates = {Predicates.RDF_TYPE, Predicates.RDFS_LABEL};

            // get the data of all found subjects
            logger.trace("Search by filters, getting the data of the found subjects");
            resultList = getSubjectsData(predicateUris, neededPredicates, new SubjectDataReader(predicateUris));

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
        map.put(Long.valueOf(Hashes.spoHash(subjectUri)), null);

        SubjectDataReader reader = new SubjectDataReader(map);
        reader.setBlankNodeUriPrefix(VirtuosoBaseDAO.BNODE_URI_PREFIX);

        List<String> subjectUris = Collections.singletonList(subjectUri);
        List<SubjectDTO> subjects = getSubjectsData(subjectUris, null, reader, false);
        return subjects == null || subjects.isEmpty() ? null : subjects.get(0);
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#getPredicateLabels(java.util.Set)
     */
    @Override
    public PredicateLabels getPredicateLabels(Set<Long> subjectHashes) throws DAOException {

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
        Bindings bindings = new Bindings();
        String sparql =
            "select ?p ?s    WHERE { ?p <" + Predicates.RDFS_SUBPROPERTY_OF + ">  ?s " + "FILTER (?s IN ("
            + SPARQLQueryUtil.urisToCSV(subjects, "subjectValue", bindings) + ") ) }";

        SubProperties subProperties = new SubProperties();
        SubPropertiesReader reader = new SubPropertiesReader(subProperties);
        executeSPARQL(sparql, bindings, reader);

        return subProperties;
    }

    /**
     * Self-explanatory constant name.
     */
    private static final String DELIVERY_SEARCH_PICKLIST_SPARQL = "SELECT DISTINCT ?li_uri ?li_title ?ro_title ?ro_uri  WHERE "
        + "{?ro_uri <" + Predicates.ROD_INSTRUMENT_PROPERTY + "> ?li_uri " + ". ?li_uri <" + Predicates.DCTERMS_ALTERNATIVE
        + "> ?li_title " + ". ?ro_uri <" + Predicates.DCTERMS_TITLE + "> ?ro_title } ORDER BY ?li_title ?ro_title";

    /**
     * Returns picklist for delivery search.
     *
     * @return The picklist.
     * @throws DAOException
     *             if query fails.
     */
    @Override
    public HashMap<UriLabelPair, ArrayList<UriLabelPair>> getDeliverySearchPicklist() throws DAOException {

        long startTime = System.currentTimeMillis();
        logger.trace("Delivery search picklist query: " + DELIVERY_SEARCH_PICKLIST_SPARQL);

        // TODO types
        DeliverySearchPicklistReader<HashMap<String, ArrayList<UriLabelPair>>> reader =
            new DeliverySearchPicklistReader<HashMap<String, ArrayList<UriLabelPair>>>();
        executeSPARQL(DELIVERY_SEARCH_PICKLIST_SPARQL, reader);

        logger.trace("Delivery search picklist query took " + Util.durationSince(startTime));

        // FIXME
        return reader.getResultMap();

    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#registerUserUrl(eionet.cr.web.security.CRUser, java.lang.String, boolean)
     */
    @Override
    public void registerUserUrl(CRUser user, String url, boolean isBookmark) throws DAOException {

        // input arguments sanity checking
        if (user == null || StringUtils.isBlank(user.getUserName())) {
            throw new IllegalArgumentException("user must not be null and must have user name");
        }
        if (!URLUtil.isURL(url)) {
            throw new IllegalArgumentException("url must not be null and must be valid URL");
        }

        // get the subject that the user wants to register
        SubjectDTO registeredSubject = getSubject(url);

        // if subject did not exist or it isn't registered in user's registrations yet,
        // then add the necessary triples
        if (registeredSubject == null || !registeredSubject.isRegisteredBy(user)) {

            // add the rdf:type=cr:File triple into user's registrations
            registeredSubject = new SubjectDTO(url, registeredSubject == null ? false : registeredSubject.isAnonymous());
            ObjectDTO objectDTO = new ObjectDTO(Subjects.CR_FILE, false);
            objectDTO.setSourceUri(user.getRegistrationsUri());
            registeredSubject.addObject(Predicates.RDF_TYPE, objectDTO);

            addTriples(registeredSubject);

            // add the URL into user's history
            SubjectDTO userHomeItemSubject = new SubjectDTO(user.getHomeItemUri(url), false);
            objectDTO = new ObjectDTO(Util.dateToString(new Date(), "yyyy-MM-dd'T'HH:mm:ss"), true);
            objectDTO.setSourceUri(user.getHistoryUri());
            userHomeItemSubject.addObject(Predicates.CR_SAVETIME, objectDTO);

            objectDTO = new ObjectDTO(url, false);
            objectDTO.setSourceUri(user.getHistoryUri());
            userHomeItemSubject.addObject(Predicates.CR_HISTORY, objectDTO);

            // store the history and bookmark triples
            addTriples(userHomeItemSubject);

            // store user folder in CR root home context
            if (!user.isHomeFolderRegistered()) {
                registerUserFolderInCrHomeContext(user);
            }

            // let the user home item subject URI be stored in RESOURCE
            // since user registrations and history URIs were used as triple source, add them to
            // HARVEST_SOURCE (set interval minutes to 0, to avoid it being background-harvested)
            HarvestSourceDAO harvestSourceDao = DAOFactory.get().getDao(HarvestSourceDAO.class);
            harvestSourceDao.addSourceIgnoreDuplicate(HarvestSourceDTO.create(user.getRegistrationsUri(), true, 0,
                    user.getUserName()));
            harvestSourceDao.addSourceIgnoreDuplicate(HarvestSourceDTO.create(user.getHistoryUri(), true, 0, user.getUserName()));
        }

        if (isBookmark) {
            if (!isSubjectUserBookmark(user, url)) {
                addUserBookmark(user, url);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#addUserBookmark(eionet.cr.web.security.CRUser, java.lang.String)
     */
    @Override
    public void addUserBookmark(CRUser user, String url) throws DAOException {
        if (user == null || StringUtils.isBlank(user.getUserName())) {
            throw new IllegalArgumentException("user must not be null and must have user name");
        }
        if (!URLUtil.isURL(url)) {
            throw new IllegalArgumentException("url must not be null and must be valid URL");
        }

        SubjectDTO userHomeItemSubject = new SubjectDTO(user.getHomeItemUri(url), false);
        ObjectDTO objectDTO = new ObjectDTO(url, false);
        objectDTO.setSourceUri(user.getBookmarksUri());
        userHomeItemSubject.addObject(Predicates.CR_BOOKMARK, objectDTO);

        addTriples(userHomeItemSubject);

        if (!user.isHomeFolderRegistered()) {
            registerUserFolderInCrHomeContext(user);
        }
        // since user's bookmarks URI was used above as triple source, add it to HARVEST_SOURCE too
        // (but set harvest interval minutes to 0, since we don't really want it to be harvested )
        // by background harvester)
        DAOFactory.get().getDao(HarvestSourceDAO.class)
        .addSourceIgnoreDuplicate(HarvestSourceDTO.create(user.getBookmarksUri(), true, 0, user.getUserName()));

    }

    /**
     * @param user
     *            CR user
     * @throws DAOException
     *             if query fails.
     */
    public void registerUserFolderInCrHomeContext(CRUser user) throws DAOException {

        if (user == null || StringUtils.isBlank(user.getUserName())) {
            throw new IllegalArgumentException("user must not be null and must have user name");
        }

        // add triple: cr:rootHome cr:hasFolder userHomeFolderUri g=cr:rootHome
        SubjectDTO crHomeSubject = new SubjectDTO(CRUser.rootHomeUri(), false);
        ObjectDTO userHomeObject = new ObjectDTO(user.getHomeUri(), false);
        userHomeObject.setSourceUri(CRUser.rootHomeUri());
        crHomeSubject.addObject(Predicates.CR_HAS_FOLDER, userHomeObject);

        addTriples(crHomeSubject);

        // add triples: userHomeFolderUri rdf:type cr:UserFolder g=cr:rootHome;
        SubjectDTO userHomeSubject = new SubjectDTO(user.getHomeUri(), false);
        ObjectDTO typeObject = new ObjectDTO(Subjects.CR_USER_FOLDER, false);
        typeObject.setSourceUri(CRUser.rootHomeUri());
        userHomeSubject.addObject(Predicates.RDF_TYPE, typeObject);

        // add triples: userHomeFolderUri cr:allowSubObjectType cr:Folder g=cr:rootHome;
        ObjectDTO crFolderObject = new ObjectDTO(Subjects.CR_FOLDER, false);
        crFolderObject.setSourceUri(CRUser.rootHomeUri());
        userHomeSubject.addObject(Predicates.CR_ALLOW_SUBOBJECT_TYPE, crFolderObject);

        // add triples: userHomeFolderUri cr:allowSubObjectType cr:File g=cr:rootHome;
        ObjectDTO crFileObject = new ObjectDTO(Subjects.CR_FILE, false);
        crFileObject.setSourceUri(CRUser.rootHomeUri());
        userHomeSubject.addObject(Predicates.CR_ALLOW_SUBOBJECT_TYPE, crFileObject);

        addTriples(userHomeSubject);

        user.setHomeFolderRegistered(true);
        // since user's CR root home URI was used above as triple source, add it to HARVEST_SOURCE too
        // (but set harvest interval minutes to 0, since we don't really want it to be harvested )
        // by background harvester)
        DAOFactory.get().getDao(HarvestSourceDAO.class)
        .addSourceIgnoreDuplicate(HarvestSourceDTO.create(CRUser.rootHomeUri(), true, 0, null));
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#deleteUserBookmark(eionet.cr.web.security.CRUser, java.lang.String)
     */
    @Override
    public void deleteUserBookmark(CRUser user, String url) throws DAOException {

        TripleDTO triple = new TripleDTO(user.getHomeItemUri(url), Predicates.CR_BOOKMARK, url);

        triple.setSourceUri(user.getBookmarksUri());
        triple.setLiteralObject(false);

        deleteTriples(Collections.singletonList(triple));

    }

    private static final String USER_BOOKMARKS_SPARQL = "select distinct ?bookmarkUrl from ?userBookmarksUri where { ?subject <"
        + Predicates.CR_BOOKMARK + "> ?bookmarkUrl } order by ?bookmarkUrl";

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#getUserBookmarks(eionet.cr.web.security.CRUser)
     */
    @Override
    public List<UserBookmarkDTO> getUserBookmarks(CRUser user) throws DAOException {
        Bindings bindings = new Bindings();
        bindings.setURI("userBookmarksUri", user.getBookmarksUri());

        RepositoryConnection conn = null;
        TupleQueryResult queryResult = null;
        List<UserBookmarkDTO> resultList = new ArrayList<UserBookmarkDTO>();
        try {
            conn = SesameUtil.getRepositoryConnection();
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, USER_BOOKMARKS_SPARQL);
            bindings.applyTo(tupleQuery, conn.getValueFactory());

            queryResult = tupleQuery.evaluate();

            while (queryResult.hasNext()) {
                BindingSet bindingSet = queryResult.next();
                Value objectValue = bindingSet.getValue("bookmarkUrl");
                if (objectValue != null) {
                    UserBookmarkDTO bookmark = new UserBookmarkDTO();
                    bookmark.setBookmarkUrl(objectValue.stringValue());
                    resultList.add(bookmark);
                }

            }
        } catch (OpenRDFException e) {
            throw new DAOException(e.toString(), e);
        } finally {
            SesameUtil.close(queryResult);
            SesameUtil.close(conn);
        }

        return resultList;
    }

    /**
     * SPARQL for checking if the URI is listed in user bookmarks.
     */
    private static final String SUBJECT_BOOKMARK_CHECK_QUERY = "select count(1)  where "
        + "{ graph ?g {?s ?userBookmark ?o . filter( isIRI(?o)) filter (?o = ?subjectValue) "
        + "filter (?g = ?userBookmarksFolder ) } }";

    /**
     * Checks if given subject is bookmarked in user bookmarks.
     *
     * @param user
     *            Content Registry user
     * @param subject
     *            Subject URI to be checked
     * @return boolean
     * @see eionet.cr.dao.HelperDAO#isSubjectUserBookmark(eionet.cr.web.security. CRUser, long)
     * @throws DAOException
     *             if query fails.
     */
    @Override
    public boolean isSubjectUserBookmark(CRUser user, String subject) throws DAOException {
        Bindings bindings = new Bindings();
        bindings.setURI("userBookmark", Predicates.CR_BOOKMARK);
        bindings.setURI("subjectValue", subject);
        bindings.setURI("userBookmarksFolder", CRUser.bookmarksUri(user.getUserName()));

        // reader works only with Strings with Sesame/Virtuoso
        SingleObjectReader<String> reader = new SingleObjectReader<String>();
        executeSPARQL(SUBJECT_BOOKMARK_CHECK_QUERY, bindings, reader);

        // resultlist contains 1 row including count of bookmark matches
        String urlCount = reader.getResultList().get(0);
        return Integer.valueOf(urlCount) > 0;
    }

    /**
     * Updates user history for this URI.
     *
     * @param user
     *            Content Registry user
     * @param url
     *            URI which record is updated
     * @throws DAOException
     *             if query fails.
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
        objectDTO = new ObjectDTO(Util.dateToString(new Date(), "yyyy-MM-dd'T'HH:mm:ss"), true, XMLSchema.DATETIME);
        objectDTO.setSourceUri(user.getHistoryUri());
        userHomeItemSubject.addObject(Predicates.CR_SAVETIME, objectDTO);

        addTriples(userHomeItemSubject);

    }

    /**
     * SPARQL for user history items.
     */
    private static final String USER_HISTORY_QUERY = "PREFIX cr: <" + Namespace.CR.getUri() + "> "
    + "select ?url ?time FROM ?userHistoryGraph "
    + "WHERE {?s cr:userHistory ?url . ?s cr:userSaveTime ?time} order by desc(?time)";

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#getUserHistory(eionet.cr.web.security.CRUser)
     */
    @Override
    public List<UserHistoryDTO> getUserHistory(CRUser user) throws DAOException {
        Bindings bindings = new Bindings();
        bindings.setURI("userHistoryGraph", user.getHistoryUri());
        List<UserHistoryDTO> returnHistory = new ArrayList<UserHistoryDTO>();
        MapReader reader = new MapReader();

        executeSPARQL(USER_HISTORY_QUERY, bindings, reader);

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

    /**
     * SPARQL returning example triples of an harvest source.
     */
    public static final String SAMPLE_TRIPLES_QUERY = "SELECT ?s ?p ?o FROM ?source WHERE {?s ?p ?o} LIMIT 100";

    /**
     * Returns 100 sample triples of an harvest source in random order.
     *
     * @param sourceUrl
     *            harvest source url
     * @param pagingRequest
     *            PAging request of the UI
     * @throws DAOException
     *             if query fails.
     * @return list of TripleDTO objects.
     */
    @Override
    public List<TripleDTO> getSampleTriplesInSource(String sourceUrl, PagingRequest pagingRequest) throws DAOException {

        Bindings bindings = new Bindings();
        bindings.setURI("source", sourceUrl);

        TriplesReader reader = new TriplesReader();
        return executeSPARQL(SAMPLE_TRIPLES_QUERY, bindings, reader);

    }

    /**
     * SPARQL returning triples of an harvest source.
     */
    public static final String SOURCE_TRIPLES_QUERY = "SELECT ?s ?p ?o FROM ?source WHERE {?s ?p ?o}";

    /**
     * Returns triples of an harvest source.
     *
     * @param sourceUrl
     *            harvest source url
     * @throws DAOException
     *             if query fails.
     * @return list of TripleDTO objects.
     */
    @Override
    public List<TripleDTO> getTriplesInSource(String sourceUrl) throws DAOException {

        Bindings bindings = new Bindings();
        bindings.setURI("source", sourceUrl);

        TriplesReader reader = new TriplesReader();
        return executeSPARQL(SOURCE_TRIPLES_QUERY, bindings, reader);

    }

    /**
     * Returns full SPO objects of an harvest source.
     *
     * @param sourceUrl
     *            harvest source url
     * @throws DAOException
     *             if query fails.
     * @return list of SubjectDTO objects.
     */
    @Override
    public List<SubjectDTO> getSPOsInSource(String sourceUrl) throws DAOException {

        Bindings bindings = new Bindings();
        bindings.setURI("source", sourceUrl);

        SPOReader reader = new SPOReader();
        return executeSPARQL(SOURCE_TRIPLES_QUERY, bindings, reader);

    }

    /**
     * SPARQL returning triples of a subject.
     */
    public static final String SUBJECT_TRIPLES_QUERY = "SELECT ?s ?p ?o FROM ?source WHERE {?s ?p ?o . filter (?s = ?subject)}";

    /**
     * Returns full SPO objects of given subject.
     *
     * @param sourceUrl
     *            harvest source url
     * @param subject
     *            subject URI
     * @throws DAOException
     *             if query fails.
     * @return list of SubjectDTO objects.
     */
    @Override
    public List<SubjectDTO> getSPOsInSubject(String sourceUrl, String subject) throws DAOException {

        if (!StringUtils.isBlank(subject) && !StringUtils.isBlank(sourceUrl)) {
            Bindings bindings = new Bindings();
            bindings.setURI("source", sourceUrl);
            bindings.setURI("subject", subject);

            SPOReader reader = new SPOReader();
            return executeSPARQL(SUBJECT_TRIPLES_QUERY, bindings, reader);
        } else {
            return null;
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#generateNewReviewId(eionet.cr.web.security.CRUser )
     */
    @Override
    public int generateNewReviewId(CRUser user) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");
        // int currentLastId = getLastReviewId(user);
        // // Deleting from the database the old value and creating a new one.
        //
        // String deleteQuery = "DELETE FROM spo WHERE " + "PREDICATE=" +
        // Hashes.spoHash(Predicates.CR_USER_REVIEW_LAST_NUMBER)
        // + " and " + "SUBJECT=" + Hashes.spoHash(user.getHomeUri()) + "";
        // Connection conn = null;
        // Statement stmt = null;
        // try {
        // conn = getSQLConnection();
        // stmt = conn.createStatement();
        // stmt.execute(deleteQuery);
        // } catch (SQLException e) {
        // throw new DAOException(e.toString(), e);
        // } finally {
        // SQLUtil.close(stmt);
        // SQLUtil.close(conn);
        // }
        //
        // // Generating new ID
        // int newId = currentLastId + 1;
        //
        // SubjectDTO newValue = new SubjectDTO(user.getHomeUri(), false);
        // ObjectDTO objectDTO = new ObjectDTO(String.valueOf(newId), true);
        // objectDTO.setSourceUri(user.getHomeUri());
        //
        // newValue.addObject(Predicates.CR_USER_REVIEW_LAST_NUMBER, objectDTO);
        //
        // addTriples(newValue);
        //
        // addResource(Predicates.CR_USER_REVIEW_LAST_NUMBER, user.getHomeUri());
        // addResource(user.getHomeUri(), user.getHomeUri());
        //
        // // since user's home URI was used above as triple source, add it to
        // // HARVEST_SOURCE too
        // // (but set interval minutes to 0, to avoid it being
        // // background-harvested)
        // DAOFactory.get().getDao(HarvestSourceDAO.class)
        // .addSourceIgnoreDuplicate(HarvestSourceDTO.create(user.getHomeUri(), true, 0, user.getUserName()));
        //
        // return newId;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#getLastReviewId(eionet.cr.web.security.CRUser)
     */
    @Override
    public int getLastReviewId(CRUser user) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");
        // String subjectUrl = user.getHomeUri();

        // String dbQuery = "select OBJECT as lastid from SPO " + "where " + "PREDICATE="
        // + Hashes.spoHash(Predicates.CR_USER_REVIEW_LAST_NUMBER) + " and " + "SUBJECT=" + Hashes.spoHash(subjectUrl) +
        // "";

        // int lastid = 0;
        // Connection conn = null;
        // Statement stmt = null;
        // ResultSet rs = null;
        // try {
        // conn = getSQLConnection();
        // stmt = conn.createStatement();
        // rs = stmt.executeQuery(dbQuery);
        // while (rs.next()) {
        // lastid = Integer.parseInt(rs.getString("lastid"));
        // }
        // } catch (SQLException e) {
        // throw new DAOException(e.toString(), e);
        // } finally {
        // SQLUtil.close(rs);
        // SQLUtil.close(stmt);
        // SQLUtil.close(conn);
        // }

        // return lastid;

    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#addReview(eionet.cr.dto.ReviewDTO, eionet.cr.web.security.CRUser)
     */
    @Override
    public int addReview(ReviewDTO review, CRUser user) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");
        // int reviewId = generateNewReviewId(user);
        // insertReviewToDB(review, user, reviewId);
        // return reviewId;

    }

    /*
     * private void insertReviewToDB(ReviewDTO review, CRUser user, int reviewId) throws DAOException {
     *
     * String userReviewUri = user.getReviewUri(reviewId); SubjectDTO newReview = new SubjectDTO(userReviewUri, false);
     *
     * ObjectDTO typeObject = new ObjectDTO(Subjects.CR_FEEDBACK, false); typeObject.setSourceUri(userReviewUri); ObjectDTO
     * titleObject = new ObjectDTO(review.getTitle(), true); titleObject.setSourceUri(userReviewUri); ObjectDTO feedbackForObject =
     * new ObjectDTO(review.getObjectUrl(), false); feedbackForObject.setSourceUri(userReviewUri); ObjectDTO feedbackUserObject =
     * new ObjectDTO(user.getHomeUri(), false); feedbackUserObject.setSourceUri(userReviewUri);
     *
     * newReview.addObject(Predicates.RDF_TYPE, typeObject); newReview.addObject(Predicates.DC_TITLE, titleObject);
     * newReview.addObject(Predicates.RDFS_LABEL, titleObject); newReview.addObject(Predicates.CR_FEEDBACK_FOR, feedbackForObject);
     * newReview.addObject(Predicates.CR_USER, feedbackUserObject);
     *
     * addTriples(newReview);
     *
     * addResource(Subjects.CR_FEEDBACK, userReviewUri); addResource(Predicates.DC_TITLE, userReviewUri);
     * addResource(Predicates.CR_FEEDBACK_FOR, userReviewUri); addResource(Predicates.CR_USER, userReviewUri);
     * addResource(userReviewUri, userReviewUri);
     *
     * // creating a cross link to show that specific object has a review. SubjectDTO crossLinkSubject = new
     * SubjectDTO(review.getObjectUrl(), false); ObjectDTO grossLinkObject = new ObjectDTO(userReviewUri, false);
     * grossLinkObject.setSourceUri(userReviewUri); crossLinkSubject.addObject(Predicates.CR_HAS_FEEDBACK, grossLinkObject);
     *
     * addTriples(crossLinkSubject);
     *
     * addResource(Predicates.CR_HAS_FEEDBACK, userReviewUri); addResource(review.getObjectUrl(), userReviewUri);
     *
     * // since the review URI was used above as triple source, add it to // HARVEST_SOURCE too // (but set interval minutes to 0,
     * to avoid it being // background-harvested) DAOFactory.get().getDao(HarvestSourceDAO.class)
     * .addSourceIgnoreDuplicate(HarvestSourceDTO.create(userReviewUri, true, 0, user.getUserName()));
     *
     * // Adding content review to DB too.
     *
     * if (review.getReviewContent() != null && review.getReviewContent().length() > 0) {
     *
     * String contentType = review.getReviewContentType(); if (contentType == null) { contentType = ""; }
     *
     * Connection conn = null; PreparedStatement stmt = null; ResultSet rs = null; ByteArrayInputStream byteArrayInputStream = null;
     * try { conn = getSQLConnection(); stmt = conn.prepareStatement(insertReviewContentQuery); stmt.setLong(1,
     * Hashes.spoHash(user.getReviewUri(reviewId)));
     *
     * byte[] bytes = review.getReviewContent().getBytes("UTF-8"); byteArrayInputStream = new ByteArrayInputStream(bytes);
     * stmt.setBinaryStream(2, byteArrayInputStream, bytes.length); stmt.setString(3, contentType); stmt.executeUpdate(); } catch
     * (SQLException e) {
     *
     * throw new DAOException(e.getMessage(), e); } catch (UnsupportedEncodingException e) {
     *
     * throw new DAOException(e.getMessage(), e); } finally { IOUtils.closeQuietly(byteArrayInputStream); SQLUtil.close(rs);
     * SQLUtil.close(stmt); SQLUtil.close(conn); } }
     *
     * }
     */
    // private static String insertReviewContentQuery = "INSERT INTO spo_binary (SUBJECT, OBJECT, DATATYPE, MUST_EMBED)
    // VALUES (?,?,?, TRUE);";

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#saveReview(int, eionet.cr.dto.ReviewDTO, eionet.cr.web.security.CRUser)
     */
    @Override
    public void saveReview(int reviewId, ReviewDTO review, CRUser user) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");
        // deleteReview(user, reviewId, false);
        // insertReviewToDB(review, user, reviewId);

    }

    private static final String USER_REVIEWS_SPARQL = "select ?s ?p ?o where { ?s ?p ?o." + " { select distinct ?s where { ?s <"
    + Predicates.RDF_TYPE + "> <" + Subjects.CR_FEEDBACK + ">" + ". ?s <" + Predicates.CR_USER
    + "> ?userHomeUri }}} order by ?s ?p ?o";

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#getReviewList(eionet.cr.web.security.CRUser)
     */
    @Override
    public List<ReviewDTO> getReviewList(CRUser user) throws DAOException {

        Bindings bindings = new Bindings();
        bindings.setURI("userHomeUri", user.getHomeUri());

        RepositoryConnection conn = null;
        List<ReviewDTO> resultList = new ArrayList<ReviewDTO>();
        TupleQueryResult queryResult = null;
        try {
            conn = SesameUtil.getRepositoryConnection();
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, USER_REVIEWS_SPARQL);
            bindings.applyTo(tupleQuery, conn.getValueFactory());
            queryResult = tupleQuery.evaluate();

            ReviewDTO reviewDTO = null;
            while (queryResult.hasNext()) {

                BindingSet bindingSet = queryResult.next();

                String reviewUri = bindingSet.getValue("s").stringValue();
                if (reviewDTO == null || !reviewUri.equals(reviewDTO.getReviewSubjectUri())) {
                    reviewDTO = new ReviewDTO();
                    reviewDTO.setReviewSubjectUri(reviewUri);
                    resultList.add(reviewDTO);
                }

                String predicateUri = bindingSet.getValue("p").stringValue();
                if (predicateUri.equals(Predicates.DC_TITLE)) {
                    reviewDTO.setTitle(bindingSet.getValue("o").stringValue());
                }

                if (predicateUri.equals(Predicates.CR_FEEDBACK_FOR)) {
                    reviewDTO.setObjectUrl(bindingSet.getValue("o").stringValue());
                }
            }
        } catch (OpenRDFException e) {
            throw new DAOException(e.toString(), e);
        } finally {
            SesameUtil.close(queryResult);
            SesameUtil.close(conn);
        }

        return resultList;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#getReview(eionet.cr.web.security.CRUser, int)
     */
    @Override
    public ReviewDTO getReview(CRUser user, int reviewId) throws DAOException {

        throw new UnsupportedOperationException("Method not implemented");
        // String reviewUri = user.getReviewUri(reviewId);
        // ReviewDTO reviewDTO = getReviewDTO(reviewUri);
        //
        // if (reviewDTO != null) {
        //
        // }
        //
        // return reviewDTO;

        // String userUri = user.getReviewUri(reviewId);
        //
        // String dbQuery = "SELECT "
        // + "uri"
        // + ", spoTitle.object AS title"
        // + ", spoObject.object AS object"
        // + ", spo_binary.object AS reviewcontent"
        // + ", spo_binary.datatype AS datatype "
        // + " FROM spo AS spo1, spo AS spo2,"
        // +
        // " spo AS spoTitle, resource, spo AS spoObject LEFT OUTER JOIN spo_binary ON (spoObject.subject=spo_binary.subject"
        // + " AND spo_binary.must_embed = TRUE) "
        // + "WHERE " + "(spo1.subject = "
        // + Hashes.spoHash(userUri)
        // + ") AND "
        // +
        // "(spo1.subject = spo2.subject) AND (spo1.subject = spoTitle.subject) AND (spo1.subject = spoObject.subject) AND "
        // + "spoObject.Predicate="
        // + Hashes.spoHash(Predicates.CR_FEEDBACK_FOR)
        // + "AND "
        // + "spoTitle.Predicate="
        // + Hashes.spoHash(Predicates.DC_TITLE)
        // + "AND "
        // + "spo1.subject=resource.uri_hash AND "
        // + "(spo1.predicate = "
        // + Hashes.spoHash(Predicates.CR_USER)
        // + ") AND "
        // + "(spo1.object_hash = "
        // + Hashes.spoHash(user.getHomeUri())
        // + ") AND "
        // + "(spo2.predicate = "
        // + Hashes.spoHash(Predicates.RDF_TYPE)
        // + ") AND "
        // + "(spo2.object_hash = "
        // + Hashes.spoHash(Subjects.CR_FEEDBACK) + ") AND " + "(spoObject.lit_obj = 'N') " + "ORDER BY uri ASC";
        //
        // ReviewDTO result = new ReviewDTO();
        //
        // Connection conn = null;
        // Statement stmt = null;
        // ResultSet rs = null;
        // try {
        // conn = getSQLConnection();
        // stmt = conn.createStatement();
        // rs = stmt.executeQuery(dbQuery);
        // if (rs.next()) {
        // result.setReviewSubjectUri(rs.getString("uri"));
        // result.setTitle(rs.getString("title"));
        // result.setObjectUrl(rs.getString("object"));
        // result.setReviewContentType(rs.getString("datatype"));
        //
        // byte[] bytes = rs.getBytes("reviewcontent");
        // if (bytes != null && bytes.length > 0) {
        // result.setReviewContent(new String(bytes, "UTF-8"));
        // }
        // }
        // } catch (SQLException e) {
        // throw new DAOException(e.toString(), e);
        // } catch (UnsupportedEncodingException e) {
        // throw new DAOException("Unsupported encoding when fetching review content", e);
        // } finally {
        // SQLUtil.close(rs);
        // SQLUtil.close(stmt);
        // SQLUtil.close(conn);
        // }
        //
        // return result;

    }

    private static final String REVIEW_SPARQL = "select ?p ?o where { ?reviewUri ?p ?o }";

    /**
     * Get title and feedbackFor reference for a review.
     *
     * @param reviewUri
     *            - URI of review.
     * @return ReviewDTO
     * @throws DAOException
     *             if query fails
     */
    private ReviewDTO getReviewDTO(String reviewUri) throws DAOException {
        Bindings bindings = new Bindings();
        bindings.setURI("reviewUri", reviewUri);

        ReviewDTO reviewDTO = null;
        RepositoryConnection conn = null;
        TupleQueryResult queryResult = null;

        try {
            conn = SesameUtil.getRepositoryConnection();
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, REVIEW_SPARQL);
            bindings.applyTo(tupleQuery, conn.getValueFactory());
            queryResult = tupleQuery.evaluate();

            while (queryResult.hasNext()) {

                BindingSet bindingSet = queryResult.next();

                if (reviewDTO == null) {
                    reviewDTO = new ReviewDTO();
                    reviewDTO.setReviewSubjectUri(reviewUri);
                }

                String predicateUri = bindingSet.getValue("p").stringValue();
                if (predicateUri.equals(Predicates.DC_TITLE)) {
                    reviewDTO.setTitle(bindingSet.getValue("o").stringValue());
                }
                if (predicateUri.equals(Predicates.CR_FEEDBACK_FOR)) {
                    reviewDTO.setObjectUrl(bindingSet.getValue("o").stringValue());
                }
            }
        } catch (OpenRDFException e) {
            throw new DAOException(e.toString(), e);
        } finally {
            SesameUtil.close(queryResult);
            SesameUtil.close(conn);
        }

        return reviewDTO;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#getReviewAttachmentList(eionet.cr.web.security .CRUser, int)
     */
    @Override
    public List<String> getReviewAttachmentList(CRUser user, int reviewId) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

        // String dbQuery = "SELECT object FROM spo WHERE " + "(subject = " + Hashes.spoHash(user.getReviewUri(reviewId)) "
        // + ") AND "
        // + "(predicate = " + Hashes.spoHash(Predicates.CR_HAS_ATTACHMENT) + ") ORDER BY object ASC";
        //
        // List<String> returnList = new ArrayList<String>();
        //
        // Connection conn = null;
        // Statement stmt = null;
        // ResultSet rs = null;
        // try {
        // conn = getSQLConnection();
        // stmt = conn.createStatement();
        // rs = stmt.executeQuery(dbQuery);
        // while (rs.next()) {
        // returnList.add(rs.getString("object"));
        // }
        // } catch (SQLException e) {
        // throw new DAOException(e.toString(), e);
        // } finally {
        // SQLUtil.close(rs);
        // SQLUtil.close(stmt);
        // SQLUtil.close(conn);
        // }
        //
        // return returnList;

    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#deleteReview(eionet.cr.web.security.CRUser, int, boolean)
     */
    @Override
    public void deleteReview(CRUser user, int reviewId, boolean deleteAttachments) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

        // String sqlDeleteReview = "DELETE FROM spo WHERE (subject=? OR object_hash=?"
        // + "OR source=? OR obj_deriv_source=? OR obj_source_object=?)";
        //
        // if (!deleteAttachments) {
        // sqlDeleteReview += " AND (predicate <> " + Hashes.spoHash(Predicates.CR_HAS_ATTACHMENT) + ")";
        // }
        //
        // String reviewSubjectURI = user.getReviewUri(reviewId);
        //
        // Connection conn = null;
        // Statement stmt = null;
        // try {
        // conn = getSQLConnection();
        //
        // if (deleteAttachments) {
        // List<String> reviewAttachments = this.getReviewAttachmentList(user, reviewId);
        // for (int i = 0; i < reviewAttachments.size(); i++) {
        // SQLUtil.executeUpdate("DELETE FROM spo_binary WHERE subject = " + Hashes.spoHash(reviewAttachments.get(i)),
        // conn);
        // }
        // }
        //
        // SQLUtil.executeUpdate(StringUtils.replace(sqlDeleteReview, "?", String.valueOf(Hashes.spoHash(reviewSubjectURI))), conn);
        // SQLUtil.executeUpdate(
        // StringUtils.replace(sqlDeleteReviewContent, "?", String.valueOf(Hashes.spoHash(reviewSubjectURI))), conn);
        //
        // } catch (SQLException e) {
        // throw new DAOException(e.toString(), e);
        // } finally {
        // SQLUtil.close(stmt);
        // SQLUtil.close(conn);
        // }

    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#deleteAttachment(eionet.cr.web.security.CRUser, int, java.lang.String)
     */
    @Override
    public void deleteAttachment(CRUser user, int reviewId, String attachmentUri) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

        // Connection conn = null;
        // Statement stmt = null;
        // try {
        // conn = getSQLConnection();
        // SQLUtil.executeUpdate("DELETE FROM spo WHERE object_hash = " + Hashes.spoHash(attachmentUri), conn);
        // SQLUtil.executeUpdate("DELETE FROM spo_binary WHERE subject = " + Hashes.spoHash(attachmentUri), conn);
        // } catch (SQLException e) {
        // throw new DAOException(e.toString(), e);
        // } finally {
        // SQLUtil.close(stmt);
        // SQLUtil.close(conn);
        // }

    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#loadAttachment(java.lang.String)
     */
    @Override
    public DownloadFileDTO loadAttachment(String attachmentUri) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");
        // DownloadFileDTO returnFileDTO = new DownloadFileDTO();
        //
        // Connection conn = null;
        // try {
        // conn = getSQLConnection();
        // PreparedStatement ps = conn.prepareStatement("SELECT object, datatype FROM spo_binary WHERE subject = ?");
        // ps.setLong(1, Hashes.spoHash(attachmentUri));
        // ResultSet rs = ps.executeQuery();
        // while (rs.next()) {
        // returnFileDTO.setContentType(rs.getString("datatype"));
        // returnFileDTO.setInputStream(rs.getBinaryStream("object"));
        // }
        // } catch (SQLException e) {
        // throw new DAOException(e.toString(), e);
        // } finally {
        // SQLUtil.close(conn);
        // }
        // return returnFileDTO;

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

                if (triple.isLiteralObject()) {
                    Literal literalObject = null;
                    if (strObject != null) {
                        literalObject = conn.getValueFactory().createLiteral(strObject);
                    }
                    conn.remove(sub, pred, literalObject, source);
                } else {
                    URI object = null;
                    if (strObject != null) {
                        object = conn.getValueFactory().createURI(triple.getObject());
                    }
                    conn.remove(sub, pred, object, source);
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
     * @see eionet.cr.dao.HelperDAO#deleteTriplesOfSource(long)
     */
    @Override
    public void deleteTriplesOfSource(long sourceHash) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /**
     * SPARQL for user uploaded files.
     */
    private static final String USER_UPLOADS_QUERY = "select ?s ?p ?o where { ?s ?p ?o. "
        + "{ select distinct ?s where { ?userBookmarksFolder ?hasFile ?s }}} order by ?s ?p ?o";

    /**
     * User uploaded files.
     *
     * @param crUser
     *            CR user
     * @see eionet.cr.dao.HelperDAO#getUserUploads(eionet.cr.web.security.CRUser)
     * @throws DAOException
     *             if query fails.
     * @return List of user upload items.
     */
    @Override
    public Collection<UploadDTO> getUserUploads(CRUser crUser) throws DAOException {

        if (crUser == null) {
            throw new IllegalArgumentException("User object must not be null");
        }

        if (StringUtils.isBlank(crUser.getUserName())) {
            throw new IllegalArgumentException("User name must not be blank");
        }
        Bindings bindings = new Bindings();
        bindings.setURI("userBookmarksFolder", crUser.getHomeUri());
        bindings.setURI("hasFile", Predicates.CR_HAS_FILE);

        UploadDTOReader reader = new UploadDTOReader();
        executeSPARQL(USER_UPLOADS_QUERY, bindings, reader);

        // loop through all the found uploads and make sure they all have the
        // label set
        Collection<UploadDTO> uploads = reader.getResultList();
        for (UploadDTO uploadDTO : uploads) {

            String currentLabel = uploadDTO.getLabel();
            String subjectUri = uploadDTO.getSubjectUri();
            String uriLabel = URIUtil.extractURILabel(subjectUri, SubjectDTO.NO_LABEL);
            uriLabel = StringUtils.replace(uriLabel, "%20", " ");

            if (StringUtils.isBlank(currentLabel) && !StringUtils.isBlank(uriLabel))
                uploadDTO.setLabel(uriLabel);
            else
                uploadDTO.setLabel(uriLabel + " (" + currentLabel + ")");
        }

        return uploads;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#isExistingSubject(java.lang.String)
     */
    @Override
    public boolean isExistingSubject(String subjectUri) throws DAOException {

        if (StringUtils.isBlank(subjectUri)) {
            throw new IllegalArgumentException("Subject uri must not be empty");
        }

        RepositoryConnection conn = null;
        try {
            conn = SesameUtil.getRepositoryConnection();
            RepositoryResult<Statement> result =
                conn.getStatements(conn.getValueFactory().createURI(subjectUri), null, null, true);
            return result != null && result.hasNext();
        } catch (RepositoryException e) {
            throw new DAOException(e.toString(), e);
        } finally {
            SesameUtil.close(conn);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#deleteSubjects(java.util.List)
     */
    @Override
    public void deleteUserUploads(String userName, List<String> subjectUris) throws DAOException {

        if (StringUtils.isBlank(userName) || subjectUris == null || subjectUris.isEmpty()) {
            return;
        }

        Connection sqlConn = null;
        RepositoryConnection repoConn = null;
        try {
            repoConn = SesameUtil.getRepositoryConnection();
            ValueFactory valueFactory = repoConn.getValueFactory();

            StringBuilder strBuilder = new StringBuilder();
            for (String subjectUri : subjectUris) {

                URI subjectResource = valueFactory.createURI(subjectUri);
                URI userHomeContext = valueFactory.createURI(CRUser.homeUri(userName));
                URI harvesterContext = valueFactory.createURI(GeneralConfig.HARVESTER_URI);

                // JH190511: although Sesame API claims that in RepositoryConnection.remove(...)
                // the context is optional, Virtuoso requires the context (i.e. the graph) always
                // to be specified in triple removal commands. Virtuoso Sesame driver seems to
                // silently ignore the whole command, if no context specified.
                repoConn.remove(subjectResource, null, null, userHomeContext, harvesterContext);
                repoConn.remove(null, null, (Value) subjectResource, userHomeContext, harvesterContext);

                if (strBuilder.length() > 0) {
                    strBuilder.append(",");
                }
                strBuilder.append(Hashes.spoHash(subjectUri));
            }

            sqlConn = getSQLConnection();
            SQLUtil.executeUpdate("delete from SPO_BINARY where SUBJECT in (" + strBuilder + ")", sqlConn);

        } catch (RepositoryException e) {
            throw new DAOException(e.toString(), e);
        } catch (SQLException e) {
            throw new DAOException(e.toString(), e);
        } finally {
            SesameUtil.close(repoConn);
            SQLUtil.close(sqlConn);
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#renameSubjects(java.util.Map)
     */
    @Override
    public void renameUserUploads(Map<String, String> renamings) throws DAOException {

        if (renamings == null || renamings.isEmpty()) {
            return;
        }

        RepositoryConnection repoConn = null;
        try {
            repoConn = SesameUtil.getRepositoryConnection();
            ResourceRenameHandler handler = new ResourceRenameHandler(repoConn, renamings);

            ValueFactory valueFactory = repoConn.getValueFactory();
            for (Map.Entry<String, String> entry : renamings.entrySet()) {

                String oldUriString = entry.getKey();
                URI oldUri = valueFactory.createURI(oldUriString);

                logger.debug("Exporting statements with old URI " + oldUri);

                // export statements where the old URI is present in subject field
                repoConn.exportStatements(oldUri, null, (Value) null, true, handler);

                // export statements where the old URI is present in object field
                repoConn.exportStatements(null, null, oldUri, true, handler);
            }

            logger.debug("Total count of exported statments: " + handler.getHandledStatementCount());
            handler.execute();

        } catch (OpenRDFException e) {
            throw new DAOException(e.toString(), e);
        } finally {
            SesameUtil.close(repoConn);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#readDistinctPredicates(java.lang.Long)
     */
    @Override
    public List<PredicateDTO> readDistinctPredicates(Long sourceHash) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#readDistinctSubjectUrls(java.lang.Long)
     */
    @Override
    public List<String> readDistinctSubjectUrls(Long sourceHash) throws DAOException {
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
    public void deleteSubjectPredicates(Collection<String> subjectUris, Collection<String> predicateUris,
            Collection<String> sourceUris) throws DAOException {

        // If no subjects specified, then nothing to do here. Same applies to specified sources,
        // because Virtuoso requires the graph (i.e. context i.e. source) to be specified. Otherwise
        // it simply seems to ignore Sesame's triple removal API.
        if (subjectUris == null || subjectUris.isEmpty() || sourceUris == null || sourceUris.isEmpty()) {
            return;
        }

        RepositoryConnection conn = null;
        try {

            conn = SesameUtil.getRepositoryConnection();
            ValueFactory factory = conn.getValueFactory();

            if (predicateUris == null || predicateUris.isEmpty()) {

                for (String subjectUri : subjectUris) {
                    for (String sourceUri : sourceUris) {
                        // no null-pointer checking, because we don't want to allow nulls here anyway
                        URI subject = factory.createURI(subjectUri);
                        URI source = factory.createURI(sourceUri);
                        conn.remove(subject, null, null, source);
                    }
                }
            } else {
                for (String subjectUri : subjectUris) {
                    for (String predicateUri : predicateUris) {
                        for (String sourceUri : sourceUris) {

                            URI predicate = predicateUri == null ? null : factory.createURI(predicateUri);

                            // no null-pointer checking, because we don't want to allow nulls here anyway
                            URI subject = factory.createURI(subjectUri);
                            URI source = factory.createURI(sourceUri);

                            conn.remove(subject, predicate, null, source);
                        }
                    }
                }
            }
        } catch (RepositoryException e) {
            throw new DAOException(e.toString(), e);
        } finally {
            SesameUtil.close(conn);
        }
    }

    /**
     * SPARQL for all triples count in the repository.
     */
    private static final String TRIPLES_COUNT_SPARQL = "SELECT count(*) WHERE {?s ?p ?o}";

    @Override
    public long getTriplesCount() throws DAOException {

        Object resultObject = executeUniqueResultSPARQL(TRIPLES_COUNT_SPARQL, new SingleObjectReader<Long>());
        return Long.valueOf(resultObject.toString());

    }

    /** */
    private static final String SPARQL_BOOKMARKS_SPARQL = "select ?subj ?label ?queryString " + "from ?bookmarksHome " + "where {"
    + "?subj ?rdfType ?sparqlBookmark" + ". ?subj ?rdfsLabel ?label" + ". ?subj ?sparqlQuery ?queryString"
    + "} order by ?label";

    /**
     * Returns SPARQL bookmarks of the user.
     *
     * @param user
     *            CR user whose bookmarks are returned
     * @return List<Map<String, String>>
     * @throws DAOException
     *             if query fails.
     */
    public List<Map<String, String>> getSparqlBookmarks(CRUser user) throws DAOException {

        Bindings bindings = new Bindings();
        bindings.setURI("bookmarksHome", user.getBookmarksUri());
        bindings.setURI("rdfType", Predicates.RDF_TYPE);
        bindings.setURI("sparqlBookmark", Subjects.CR_SPARQL_BOOKMARK);
        bindings.setURI("rdfsLabel", Predicates.RDFS_LABEL);
        bindings.setURI("sparqlQuery", Predicates.CR_SPARQL_QUERY);

        return executeSPARQL(SPARQL_BOOKMARKS_SPARQL, bindings, new MapReader());
    }

    /**
     * @throws DAOException
     * @see eionet.cr.dao.HelperDAO#getSourceLastModifiedDates(Set)
     */
    @Override
    public Map<String, Date> getSourceLastModifiedDates(Set<String> resourceUris) throws DAOException {

        Bindings bindings = new Bindings();

        StringBuilder query = new StringBuilder();
        query.append("select distinct ?s ?date where {graph ?g {?s ?p ?o}. filter (?s in (").
        append(SPARQLQueryUtil.urisToCSV(resourceUris, "sValue", bindings)).
        append(")). ?g <").append(Predicates.CR_LAST_MODIFIED).append("> ?date}");

        HashMap<String, Date> result = new HashMap<String, Date>();
        MapReader reader = new MapReader();
        List<Map<String, String>> list = executeSPARQL(query.toString(), bindings, reader);
        if (list!=null){
            for (Map<String, String> map : list) {

                Date date = null;
                try {
                    date = BaseHarvest.DATE_FORMATTER.parse(map.get("date"));
                } catch (ParseException e) {
                    date = null;
                }

                if (date!=null){
                    String resourceUri = map.get("s");
                    Date currentDate = result.get(resourceUri);
                    if (currentDate==null || (date.compareTo(currentDate)>0)){
                        result.put(resourceUri, date);
                    }
                }
            }
        }

        return result;
    }

    /**
     * @see eionet.cr.dao.HelperDAO#getLiteralRangeSubjects(java.util.Set)
     */
    @Override
    public Set<String> getLiteralRangeSubjects(Set<String> subjectsToCheck) throws DAOException {
        // TODO Auto-generated method stub
        return new HashSet<String>();
    }
}
