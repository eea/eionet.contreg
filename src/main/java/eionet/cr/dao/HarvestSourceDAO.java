/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.cr.dao;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.UrlAuthenticationDTO;
import eionet.cr.harvest.load.ContentLoader;
import eionet.cr.harvest.statistics.dto.HarvestedUrlCountDTO;
import eionet.cr.util.Pair;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.web.sparqlClient.helpers.QueryResult;
import org.openrdf.OpenRDFException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author altnyris
 *
 */
public interface HarvestSourceDAO extends DAO {
    /**
     * @param searchString
     * @param pagingRequest
     * @param sortingRequest
     * @return list of harvesting sources (excluding unavailable sources and tracked files)
     * @throws DAOException
     *             if relational database is unavailable.
     */
    Pair<Integer, List<HarvestSourceDTO>> getHarvestSources(String searchString, PagingRequest pagingRequest,
            SortingRequest sortingRequest) throws DAOException;

    /**
     * @param searchString
     * @param pagingRequest
     * @param sortingRequest
     * @return list of priority sources
     * @throws DAOException
     *             if relational database is unavailable.
     */
    Pair<Integer, List<HarvestSourceDTO>> getPrioritySources(String searchString, PagingRequest pagingRequest,
            SortingRequest sortingRequest) throws DAOException;

    /**
     * @param searchString
     * @param pagingRequest
     * @param sortingRequest
     * @return list of unavailable harvest sources
     * @throws DAOException
     *             if relational database is unavailable.
     */
    Pair<Integer, List<HarvestSourceDTO>> getHarvestSourcesUnavailable(String searchString, PagingRequest pagingRequest,
            SortingRequest sortingRequest) throws DAOException;

    /**
     * Returns all the failed harvest sources, except the ones which are unauthorized (http error 401).
     *
     * @param searchString
     * @param pagingRequest
     * @param sortingRequest
     * @return Pair<Integer, List<HarvestSourceDTO>>
     * @throws DAOException
     *             if relational database is unavailable.
     */
    Pair<Integer, List<HarvestSourceDTO>> getHarvestSourcesFailed(String searchString, PagingRequest pagingRequest,
            SortingRequest sortingRequest) throws DAOException;

    /**
     * Returns failed harvest sources which are unauthorized (http error 401).
     *
     * @param searchString
     * @param pagingRequest
     * @param sortingRequest
     * @return
     * @throws DAOException
     */
    Pair<Integer, List<HarvestSourceDTO>> getHarvestSourcesUnauthorized(String searchString, PagingRequest pagingRequest,
            SortingRequest sortingRequest) throws DAOException;

    /**
     * Returns harvest sources that are marked as remote SPARQL endpoints.
     *
     * @param searchString
     * @param pagingRequest
     * @param sortingRequest
     * @return
     * @throws DAOException
     */
    Pair<Integer, List<HarvestSourceDTO>> getRemoteEndpoints(String searchString, PagingRequest pagingRequest,
            SortingRequest sortingRequest) throws DAOException;

    /**
     * @param harvestSourceID
     * @return harvesting sources
     * @throws DAOException
     *             if relational database is unavailable.
     */
    HarvestSourceDTO getHarvestSourceById(Integer harvestSourceID) throws DAOException;

    /**
     *
     * @param url
     * @return HarvestSourceDTO
     * @throws DAOException
     *             if relational database is unavailable.
     */
    HarvestSourceDTO getHarvestSourceByUrl(String url) throws DAOException;

    /**
     * Calculate the number of sources that need harvesting. Harvesting is done on a priority basis. The priority score is
     * calculated based on how many minutes ago the source was last harvested divided by how often it must be harvested. Any source
     * with a priority score of 1.0 or above needs to be harvested.
     *
     * @return number of sources to be harvested.
     * @throws DAOException
     *             if relational database is unavailable.
     */
    Long getUrgencySourcesCount() throws DAOException;

    /**
     * Adds all sources found by a Sparql query
     *
     * @param queryResult
     * @return inserted sources identity values list
     * @throws DAOException
     */
    public void addBulkSourcesFromSparql(QueryResult queryResult) throws DAOException;

    /**
     *
     * @param source
     * @return Integer
     * @throws DAOException
     *             if relational database is unavailable.
     */
    Integer addSource(HarvestSourceDTO source) throws DAOException;

    /**
     *
     * @param conn
     * @param source
     * @return
     * @throws DAOException
     */
    Integer addSource(Connection conn, HarvestSourceDTO source) throws DAOException;

    /**
     *
     * @param source
     * @throws DAOException
     *             if relational database is unavailable.
     */
    void addSourceIgnoreDuplicate(HarvestSourceDTO source) throws DAOException;

    /**
     *
     * @param conn
     * @param source
     * @throws DAOException
     */
    void addSourceIgnoreDuplicate(Connection conn, HarvestSourceDTO source) throws DAOException;

    /**
     * @param source
     * @throws DAOException
     *             if relational database is unavailable.
     */
    void editSource(HarvestSourceDTO source) throws DAOException;

    /**
     * Get a list of sources to harvest in the next harvesting round. The result is ordered with highest priority first.
     *
     * @param limit
     *            - max number of sources to return.
     * @return List<HarvestSourceDTO>
     * @throws DAOException
     *             if relational database is unavailable.
     */
    List<HarvestSourceDTO> getNextScheduledSources(int limit) throws DAOException;

    /**
     * Get a list of sources to harvest in the next harvesting round. The result is ordered with highest priority first.
     *
     * @param limit
     *            - max number of sources to return.
     * @return List<HarvestSourceDTO>
     * @throws DAOException
     *             if relational database is unavailable.
     */
    List<HarvestSourceDTO> getNextScheduledOnlineCsvTsv(int limit) throws DAOException;

    /**
     * @return String
     * @throws DAOException
     *             if relational database is unavailable.
     * @Deprecated Inferencing is removed from CR
     */
    @Deprecated
    String getSourcesInInferenceRules() throws DAOException;

    /**
     * @param searchString
     * @param pagingRequest
     * @param sortingRequest
     * @param sourceUris
     * @return Pair<Integer, List<HarvestSourceDTO>>
     * @throws DAOException
     *             if relational database is unavailable.
     * @Deprecated Inferencing is removed from CR
     */
    @Deprecated
    Pair<Integer, List<HarvestSourceDTO>> getInferenceSources(String searchString, PagingRequest pagingRequest,
            SortingRequest sortingRequest, String sourceUris) throws DAOException;

    /**
     * @param url
     * @return boolean
     * @throws DAOException
     *             if relational database is unavailable.
     * @Deprecated Inferencing is removed from CR
     */
    @Deprecated
    boolean isSourceInInferenceRule(String url) throws DAOException;

    /**
     * Add a source to the default inference rule. The name of the default is read from the config file.
     *
     * @param url
     * @return boolean
     * @throws DAOException
     *             if relational database is unavailable.
     *
     * @Deprecated Inferencing is removed from CR
     */
    @Deprecated
    boolean addSourceIntoInferenceRule(String url) throws DAOException;

    /**
     * Remove a source from the default inference rule. The name of the default is read from the config file.
     *
     * @param url
     * @return boolean
     * @throws DAOException
     *             if relational database is unavailable.
     *
     * @Deprecated Inferencing is removed from CR
     */
    @Deprecated
    boolean removeSourceFromInferenceRule(String url) throws DAOException;

    /**
     * Loads the given input stream into the triple store (i.e. repository).
     * The stream must be formatted by a format supported by the triple store.
     *
     * Usage of this method is deprecated, as it takes a somewhat naive approach that only works with smaller graphs.
     *
     * @param inputStream
     * @param rdfFormat
     * @param graphUrl
     * @param clearPreviousGraphContent
     * @return
     * @throws IOException
     * @throws OpenRDFException
     *
     * {@link Deprecated}
     */
    int loadContentNaive(InputStream inputStream, RDFFormat rdfFormat, String graphUrl, boolean clearPreviousGraphContent)
            throws IOException, OpenRDFException;

    /**
     * Load the content given as a map of files and corresponding loaders, into the given target graph.
     *
     * @param filesAndLoaders
     *            The given files with given loaders.
     * @param graphUri
     *            The target graph URI.
     * @return Total number of loaded triples.
     * @throws DAOException
     *             All exceptions are wrapped into this one.
     */
    int loadContent(Map<File, ContentLoader> filesAndLoaders, String graphUri) throws DAOException;

    /**
     * Returns metadata from /harvester context.
     *
     * @param harvestSourceUri
     * @param predicateUri
     * @return String
     * @throws DAOException
     *             if relational database is unavailable.
     */
    String getHarvestSourceMetadata(String harvestSourceUri, String predicateUri) throws DAOException;

    /**
     * Inserts given metadata into /harvester context.
     *
     * @param subject
     *            URL of the source.
     * @param predicate
     *            URL of property.
     * @param object
     *            value to insert.
     * @throws DAOException
     *             if relational database is unavailable.
     * @throws RepositoryException
     *             if data repository is unavailable.
     * @throws IOException
     */
    void insertUpdateSourceMetadata(String subject, String predicate, ObjectDTO... object) throws DAOException,
    RepositoryException, IOException;

    /**
     * Inserts given metadata into /harvester context.
     *
     * @param conn
     * @param subject
     * @param predicate
     * @param object
     * @throws DAOException
     * @throws RepositoryException
     * @throws IOException
     */
    void insertUpdateSourceMetadata(RepositoryConnection conn, String subject, String predicate, ObjectDTO... object)
            throws DAOException, RepositoryException, IOException;

    /**
     *
     * @param subjectUri
     * @param sourceUri
     * @throws DAOException
     */
    void deleteSubjectTriplesInSource(String subjectUri, String sourceUri) throws DAOException;

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#removeHarvestSources(java.util.Collection, boolean)
     */
    void removeHarvestSources(Collection<String> sourceUrls, boolean harvesterContextOnly, boolean clearGraphs) throws DAOException;

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#removeHarvestSources(java.util.Collection, boolean)
     */
    void removeHarvestSources(Collection<String> sourceUrls,
                              Set<String> exceptPredicates, boolean harvesterContextOnly, boolean clearGraphs) throws DAOException;

    /**
     * Clear the given graph.
     *
     * @param graphUri
     * @throws DAOException
     */
    void clearGraph(String graphUri) throws DAOException;

    /**
     * Increases COUNT_UNAVAIL by 1.
     *
     * @param sourceUrl
     * @throws DAOException
     *             if relational database is unavailable.
     */
    void increaseUnavailableCount(String sourceUrl) throws DAOException;

    /**
     * Statistics : count of last harvested URLs.
     *
     * @param days
     *            days from today backwards
     * @return Pair<Integer, List<HarvestedUrlCountDTO>>
     * @throws DAOException
     *             if query fails
     */
    Pair<Integer, List<HarvestedUrlCountDTO>> getLatestHarvestedURLs(int days) throws DAOException;

    /**
     * Returns urgency of coming harvests.
     *
     * @param limit
     *            size of list
     * @return Pair <Integer, List <HarvestUrgencyScoreDTO>>
     * @throws DAOException
     *             if query fails
     */
    List<HarvestSourceDTO> getMostUrgentHarvestSources(int limit) throws DAOException;

    /**
     *
     * @param sourceDTO
     * @throws DAOException
     */
    void updateSourceHarvestFinished(HarvestSourceDTO sourceDTO) throws DAOException;

    /**
     * Equal to calling {@link #removeHarvestSources(Collection, true)}, see JavaDoc there.
     *
     * @param sourceUrls
     *            The URLs of the harvest sources to remove.
     * @throws DAOException
     *             Any sort of error will be wrapped into this one.
     */
    void removeHarvestSources(Collection<String> sourceUrls) throws DAOException;

    /**
     * Removes harvest sources denoted by the given URLs. Records will be deleted from HARVEST_SOURCE + all related tables. Also
     * cleared will be graphs by the same URLs, and also all triples where these URLs are in subject position. If the flag is true
     * then the latter triples will be deleted only from the graph denoted by {@link GeneralConfig#HARVESTER_URI}. Otherwise they
     * will be deleted regardless of graph.
     *
     * @param sourceUrls
     *            The given URLs.
     * @param harvesterContextOnly
     *            The flag as indicated above.
     * @throws DAOException
     *             Any sort of error will be wrapped into this one.
     */
    void removeHarvestSources(Collection<String> sourceUrls, boolean harvesterContextOnly) throws DAOException;

    /**
     *
     * @param substring
     * @param limit
     *            TODO
     * @param offset
     *            TODO
     * @return
     * @throws DAOException
     */
    List<String> filter(String substring, int limit, int offset) throws DAOException;

    /**
     * Returns the number of total statements (triples).
     *
     * @return
     * @throws DAOException
     */
    int getTotalStatementsCount() throws DAOException;

    /**
     * Derives new harvest sources from the graph represented by the given source url, and inserts them into the HARVEST_SOURCE
     * table. The method does not use inferencing for finding new sources. They are created by POST-HARVEST scripts with SPARQL
     * INSERT statements.
     *
     * @param sourceUrl
     *            The given source URL.
     *
     * @throws DAOException
     */
    int deriveNewHarvestSources(String sourceUrl) throws DAOException;

    /**
     * Returns the number of harvest sources whose harvest urgency score is greater than the given threshold.
     *
     * @param threshold
     *            The given threshold.
     * @return The number of sources whose harvest urgency score is greater than the given threshold.
     * @throws DAOException
     *             When an error happens during the access of data.
     */
    int getNumberOfSourcesAboveUrgencyThreshold(double threshold) throws DAOException;

    /**
     * At first deletes triple with cr:harvestedStatements predicate and then adds it with updated value.
     *
     * @param sourceUri
     * @throws DAOException
     */
    void updateHarvestedStatementsTriple(String sourceUri) throws DAOException;

    /**
     * Returns the list of all urls which have been assigned a username and password
     *
     * @return list of authenticated urls
     * @throws DAOException
     */
    List<UrlAuthenticationDTO> getUrlAuthentications() throws DAOException;

    /**
     * Returns single url with authentication information
     *
     * @param id
     * @return url with authentication
     * @throws DAOException
     */
    UrlAuthenticationDTO getUrlAuthentication(int id) throws DAOException;

    /**
     * Returns single url with authentication information
     *
     * @param fullUrl
     * @return url with authentication
     * @throws DAOException
     */
    UrlAuthenticationDTO getUrlAuthentication(String fullUrl) throws DAOException;

    /**
     * Saves or adds urlAuthentication to database. If no id is provided, new url is added. If id is present, existing url is
     * overwritten.
     *
     * @param urlAuthentication
     * @throws DAOException
     */
    int saveUrlAuthentication(UrlAuthenticationDTO urlAuthentication) throws DAOException;

    /**
     * Deletes urlAuthentication from database.
     *
     * @param urlAuthentication
     * @throws DAOException
     */
    void deleteUrlAuthentication(int id) throws DAOException;

    /**
     * Returns a list of distinct predicates used by resources of the given rdf:type in the given graph.
     *
     * @param graphUri
     *            The graph to look in.
     * @param typeUri
     *            The rdf:type of the resources whose predicates are to be looked in.
     * @return The list of distinct predicates. Empty if no single predicate found.
     * @throws DAOException
     *             if any sort of database access error occurs.
     */
    List<String> getDistinctPredicates(String graphUri, String typeUri) throws DAOException;

    /**
     * Returns a list of all distinct predicates available for specified source
     *
     * @param sourceUri
     * @return
     * @throws DAOException
     */
    List<String> getSourceAllDistinctPredicates(String sourceUri) throws DAOException;

    /**
     * Returns a list of all distinct predicates available for specified type
     *
     * @param typeUri
     * @return
     * @throws DAOException
     */
    List<String> getTypeAllDistinctPredicates(String typeUri) throws DAOException;
}
