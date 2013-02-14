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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Collection;
import java.util.List;

import org.openrdf.OpenRDFException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.harvest.load.ContentLoader;
import eionet.cr.harvest.statistics.dto.HarvestedUrlCountDTO;
import eionet.cr.util.Pair;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.pagination.PagingRequest;

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
     * @return List<String>
     * @throws DAOException
     *             if relational database is unavailable. fetches all scheduled source URLs, which are scheduled for removal.
     */
    List<String> getScheduledForDeletion() throws DAOException;

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
     * @return String
     * @throws DAOException
     *             if relational database is unavailable.
     */
    String getSourcesInInferenceRules() throws DAOException;

    /**
     * @param searchString
     * @param pagingRequest
     * @param sortingRequest
     * @param sourceUris
     * @return Pair<Integer, List<HarvestSourceDTO>>
     * @throws DAOException
     *             if relational database is unavailable.
     */
    Pair<Integer, List<HarvestSourceDTO>> getInferenceSources(String searchString, PagingRequest pagingRequest,
            SortingRequest sortingRequest, String sourceUris) throws DAOException;

    /**
     * @param url
     * @return boolean
     * @throws DAOException
     *             if relational database is unavailable.
     */
    boolean isSourceInInferenceRule(String url) throws DAOException;

    /**
     * Add a source to the default inference rule. The name of the default is read from the config file.
     *
     * @param url
     * @return boolean
     * @throws DAOException
     *             if relational database is unavailable.
     */
    boolean addSourceIntoInferenceRule(String url) throws DAOException;

    /**
     * Remove a source from the default inference rule. The name of the default is read from the config file.
     *
     * @param url
     * @return boolean
     * @throws DAOException
     *             if relational database is unavailable.
     */
    boolean removeSourceFromInferenceRule(String url) throws DAOException;

    /**
     * Loads the given file into the triple store (i.e. repository). File format must be supported by the triple store.
     *
     * @param file
     * @param rdfFormat
     * @param graphUrl
     * @param clearPreviousGraphContent
     * @return
     * @throws IOException
     * @throws OpenRDFException
     */
    int loadIntoRepository(File file, RDFFormat rdfFormat, String graphUrl, boolean clearPreviousGraphContent) throws IOException,
    OpenRDFException;

    /**
     * Loads the given input stream into the triple store (i.e. repository). The stream must be formatted by a format supported by
     * the triple store.
     *
     * @param inputStream
     * @param rdfFormat
     * @param graphUrl
     * @param clearPreviousGraphContent
     * @return
     * @throws IOException
     * @throws OpenRDFException
     */
    int loadIntoRepository(InputStream inputStream, RDFFormat rdfFormat, String graphUrl, boolean clearPreviousGraphContent)
            throws IOException, OpenRDFException;

    /**
     *
     * @param file
     * @param contentLoader
     * @param graphUri
     * @return
     * @throws DAOException
     */
    int loadContent(File file, ContentLoader contentLoader, String graphUri) throws DAOException;

    /**
     * Loads structured content from the given input stream into the given graph, using the given content loader object.
     *
     * @param inputStream
     * @param contentLoader
     * @param graphUri
     * @return
     * @throws DAOException
     */
    int loadContent(InputStream inputStream, ContentLoader contentLoader, String graphUri) throws DAOException;

    /**
     * Adds the meta information the harvester has collected about the source. The meta data is considered part of the harvester and
     * not the source. Therefore the meta data is stored in the harvester's named graph (or context).
     *
     * @param sourceMetadata
     * @throws DAOException
     *             if relational database is unavailable.
     * @throws RepositoryException
     *             if data repository is unavailable.
     * @throws RDFParseException
     * @throws IOException
     */
    void addSourceMetadata(SubjectDTO sourceMetadata) throws DAOException, RDFParseException, RepositoryException, IOException;

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
     *
     * @param sourceUrls
     * @throws DAOException
     */
    void removeHarvestSources(Collection<String> sourceUrls) throws DAOException;

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
     * table. The method does not use inferencing for finding new soureces. They are created by POST-HARVEST scripts with SPARQL
     * INSERT statements.
     *
     * @param sourceUrl
     *            The given source URL.
     * @return The number of sources found and inserted.
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
}
