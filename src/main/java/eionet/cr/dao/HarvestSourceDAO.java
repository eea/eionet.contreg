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
import java.sql.Timestamp;
import java.util.List;

import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFParseException;

import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.harvest.statistics.dto.HarvestUrgencyScoreDTO;
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
     * @throws DAOException if relational database is unavailable.
     */
    Pair<Integer, List<HarvestSourceDTO>> getHarvestSources(String searchString, PagingRequest pagingRequest,
            SortingRequest sortingRequest) throws DAOException;

    /**
     * @param searchString
     * @param pagingRequest
     * @param sortingRequest
     * @return list of priority sources
     * @throws DAOException if relational database is unavailable.
     */
    Pair<Integer, List<HarvestSourceDTO>> getPrioritySources(String searchString, PagingRequest pagingRequest,
            SortingRequest sortingRequest) throws DAOException;

    /**
     * @param searchString
     * @param pagingRequest
     * @param sortingRequest
     * @return list of unavailable harvest sources
     * @throws DAOException if relational database is unavailable.
     */
    Pair<Integer, List<HarvestSourceDTO>> getHarvestSourcesUnavailable(String searchString, PagingRequest pagingRequest,
            SortingRequest sortingRequest) throws DAOException;

    /**
     * @param searchString
     * @param pagingRequest
     * @param sortingRequest
     * @return Pair<Integer, List<HarvestSourceDTO>>
     * @throws DAOException if relational database is unavailable.
     */
    Pair<Integer, List<HarvestSourceDTO>> getHarvestSourcesFailed(String searchString, PagingRequest pagingRequest,
            SortingRequest sortingRequest) throws DAOException;

    /**
     * @param harvestSourceID
     * @return harvesting sources
     * @throws DAOException if relational database is unavailable.
     */
    public HarvestSourceDTO getHarvestSourceById(Integer harvestSourceID) throws DAOException;

    /**
     *
     * @param url
     * @return HarvestSourceDTO
     * @throws DAOException if relational database is unavailable.
     */
    public HarvestSourceDTO getHarvestSourceByUrl(String url) throws DAOException;

    /**
     * Calculate the number of sources that need harvesting. Harvesting is done on a priority basis.
     * The priority score is calculated based on how many minutes ago the source was last harvested
     * divided by how often it must be harvested. Any source with a priority score of 1.0 or above
     * needs to be harvested.
     *
     * @return number of sources to be harvested.
     * @throws DAOException if relational database is unavailable.
     */
    public Long getUrgencySourcesCount() throws DAOException;

    /**
     *
     * @param source
     * @return Integer
     * @throws DAOException if relational database is unavailable.
     */
    public Integer addSource(HarvestSourceDTO source) throws DAOException;

    /**
     *
     * @param source
     * @throws DAOException if relational database is unavailable.
     */
    public void addSourceIgnoreDuplicate(HarvestSourceDTO source) throws DAOException;

    /**
     * @param source
     * @throws DAOException if relational database is unavailable.
     */
    public void editSource(HarvestSourceDTO source) throws DAOException;

    /**
     * adds references to sources into queue for removal.
     *
     * @param urls
     *            - urls to add
     * @throws DAOException if relational database is unavailable.
     */
    public void queueSourcesForDeletion(List<String> urls) throws DAOException;

    /**
     * @return List<String>
     * @throws DAOException if relational database is unavailable.
     *             fetches all scheduled source URLs, which are scheduled for removal.
     */
    List<String> getScheduledForDeletion() throws DAOException;

    /**
     * Deletes selected source from the DB.
     *
     * @param url
     *            - source url
     * @throws DAOException if relational database is unavailable.
     */
    public void deleteSourceByUrl(String url) throws DAOException;

    /**
     *
     * @param sourceId
     * @param numStatements
     * @param numResources
     * @param sourceAvailable
     * @param failed
     * @param permanentError
     * @param lastHarvest
     * @throws DAOException if relational database is unavailable.
     */
    public void updateHarvestFinished(int sourceId, Integer numStatements, Boolean sourceAvailable, boolean failed,
            boolean permanentError, Timestamp lastHarvest) throws DAOException;

    /**
     * Get a list of sources to harvest in the next harvesting round. The result is ordered with highest priority first.
     *
     * @param limit - max number of sources to return.
     * @return List<HarvestSourceDTO>
     * @throws DAOException if relational database is unavailable.
     */
    public List<HarvestSourceDTO> getNextScheduledSources(int limit) throws DAOException;

    /**
     * Deletes orphans from SPO table. Orphan is a record inside SPO, which has an non-existent source in HARVEST_SOURCE.
     *
     * @throws DAOException if relational database is unavailable.
     *
     */
    void deleteTriplesOfMissingSources() throws DAOException;

    /**
     * @param neededToRemain
     * @throws DAOException if relational database is unavailable.
     */
    void deleteHarvestHistory(int neededToRemain) throws DAOException;

    /**
     * @return String
     * @throws DAOException if relational database is unavailable.
     */
    String getSourcesInInferenceRules() throws DAOException;

    /**
     * @param searchString
     * @param pagingRequest
     * @param sortingRequest
     * @param sourceUris
     * @return Pair<Integer, List<HarvestSourceDTO>>
     * @throws DAOException if relational database is unavailable.
     */
    Pair<Integer, List<HarvestSourceDTO>> getInferenceSources(String searchString, PagingRequest pagingRequest,
            SortingRequest sortingRequest, String sourceUris) throws DAOException;

    /**
     * @param url
     * @return boolean
     * @throws DAOException if relational database is unavailable.
     */
    boolean isSourceInInferenceRule(String url) throws DAOException;

    /**
     * Add a source to the default inference rule. The name of the default is read from the config file.
     *
     * @param url
     * @return boolean
     * @throws DAOException if relational database is unavailable.
     */
    boolean addSourceIntoInferenceRule(String url) throws DAOException;

    /**
     * Remove a source from the default inference rule. The name of the default is read from the config file.
     *
     * @param url
     * @return boolean
     * @throws DAOException if relational database is unavailable.
     */
    boolean removeSourceFromInferenceRule(String url) throws DAOException;

    /**
     * Loads the file into the repository (triple store). File is required to be RDF.
     *
     * @param file
     * @param sourceUrlString
     * @return int
     * @throws DAOException if relational database is unavailable.
     * @throws RepositoryException if data repository is unavailable.
     * @throws RDFParseException
     * @throws IOException
     */
    public int addSourceToRepository(File file, String sourceUrlString) throws DAOException, RepositoryException,
            RDFParseException, IOException;

    /**
     * Adds the meta information the harvester has collected about the source. The meta data is considered part of the
     * harvester and not the source. Therefore the meta data is stored in the harvester's named graph (or context).
     *
     * @param sourceMetadata
     * @throws DAOException if relational database is unavailable.
     * @throws RepositoryException if data repository is unavailable.
     * @throws RDFParseException
     * @throws IOException
     */
    public void addSourceMetadata(SubjectDTO sourceMetadata) throws DAOException, RDFParseException, RepositoryException,
            IOException;

    /**
     * Returns new harvest sources found by HarvestingJob.
     *
     * @param sourceUrl - The newly harvested source, which can contain new resources of cr:File type.
     * @return List<String> - List of new sources in form of URLs.
     * @throws DAOException if relational database is unavailable.
     * @throws RepositoryException if data repository is unavailable.
     * @throws RDFParseException
     * @throws IOException
     */
    public List<String> getNewSources(String sourceUrl) throws DAOException, RDFParseException, RepositoryException, IOException;

    /**
     * Returns metadata from /harvester context.
     *
     * @param subject
     * @param predicate
     * @return String
     * @throws DAOException if relational database is unavailable.
     * @throws RepositoryException if data repository is unavailable.
     * @throws IOException
     */
    public String getSourceMetadata(String subject, String predicate) throws DAOException, RepositoryException, IOException;

    /**
     * Inserts given metadata into /harvester context.
     *
     * @param subject URL of the source.
     * @param predicate URL of property.
     * @param object value to  insert.
     * @throws DAOException if relational database is unavailable.
     * @throws RepositoryException if data repository is unavailable.
     * @throws IOException
     */
    public void insertUpdateSourceMetadata(String subject, String predicate, ObjectDTO object)
            throws DAOException, RepositoryException, IOException;

    /**
     * @param url
     * @param lastHarvest
     * @param failed
     * @throws DAOException if relational database is unavailable.
     */
    public void editRedirectedSource(String url, Timestamp lastHarvest, boolean failed) throws DAOException;

    /**
     * Removes all predicates from /harvester context for given subject.
     *
     * @param subject
     * @throws DAOException if relational database is unavailable.
     * @throws RepositoryException if data repository is unavailable.
     * @throws IOException
     */
    public void removeAllPredicatesFromHarvesterContext(String subject) throws DAOException, RepositoryException, IOException;

    /**
     * Removes all triples for given source. Doesn't remove triples from /harvester context.
     *
     * @param url
     * @throws DAOException if relational database is unavailable.
     */
    public void deleteSourceTriples(String url) throws DAOException;

    /**
     * Increases COUNT_UNAVAIL by 1.
     *
     * @param sourceId
     * @throws DAOException if relational database is unavailable.
     */
    public void increaseUnavailableCount(int sourceId) throws DAOException;

    /**
    * Statistics : count of last harvested URLs.
    * @param days days from today backwards
    * @return  Pair<Integer, List<HarvestedUrlCountDTO>>
    * @throws DAOException if query fails
    */
   Pair<Integer, List<HarvestedUrlCountDTO>> getLatestHarvestedURLs(int days) throws DAOException;

   /**
    * Returns   urgency of coming harvests.
    * @param amount size of list
    * @return  Pair <Integer, List <HarvestUrgencyScoreDTO>>
    * @throws DAOException if query fails
    */

   Pair<Integer, List<HarvestUrgencyScoreDTO>> getUrgencyOfComingHarvests(int amount) throws DAOException;

   /**
    * Updates source LAST_HARVEST date.
    * @param sourceUrl
    * @param lastHarvest
    * @throws DAOException if relational database is unavailable.
    */
   public void updateLastHarvest(String sourceUrl, Timestamp lastHarvest) throws DAOException;

}
