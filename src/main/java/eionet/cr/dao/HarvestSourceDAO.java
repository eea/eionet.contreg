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
import java.util.List;

import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFParseException;

import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.SubjectDTO;
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
     * @return list of harvesting sources (excluding unavailable sources and
     *         tracked files)
     * @throws DAOException
     */
    Pair<Integer, List<HarvestSourceDTO>> getHarvestSources(
            String searchString, PagingRequest pagingRequest,
            SortingRequest sortingRequest) throws DAOException;

    /**
     * @param searchString
     * @param pagingRequest
     * @param sortingRequest
     * @return list of priority sources
     * @throws DAOException
     */
    Pair<Integer, List<HarvestSourceDTO>> getPrioritySources(
            String searchString, PagingRequest pagingRequest,
            SortingRequest sortingRequest) throws DAOException;

    /**
     * @param searchString
     * @param pagingRequest
     * @param sortingRequest
     * @return list of unavailable harvest sources
     * @throws DAOException
     */
    Pair<Integer, List<HarvestSourceDTO>> getHarvestSourcesUnavailable(
            String searchString, PagingRequest pagingRequest,
            SortingRequest sortingRequest) throws DAOException;

    /**
     * @param searchString
     * @param pagingRequest
     * @param sortingRequest
     * @return Pair<Integer, List<HarvestSourceDTO>>
     * @throws DAOException
     */
    Pair<Integer, List<HarvestSourceDTO>> getHarvestSourcesFailed(
            String searchString, PagingRequest pagingRequest,
            SortingRequest sortingRequest) throws DAOException;

    /**
     * @param harvestSourceID
     * @return harvesting sources
     * @throws DAOException
     */
    public HarvestSourceDTO getHarvestSourceById(Integer harvestSourceID)
            throws DAOException;

    /**
     * 
     * @param url
     * @return HarvestSourceDTO
     * @throws DAOException
     */
    public HarvestSourceDTO getHarvestSourceByUrl(String url)
            throws DAOException;

    /**
     * @return Long
     * @throws DAOException
     */
    public Long getUrgencySourcesCount() throws DAOException;

    /**
     * 
     * @param source
     * @return Integer
     * @throws DAOException
     */
    public Integer addSource(HarvestSourceDTO source) throws DAOException;

    /**
     * 
     * @param source
     * @throws DAOException
     */
    public void addSourceIgnoreDuplicate(HarvestSourceDTO source)
            throws DAOException;

    /**
     * @param source
     * @throws DAOException
     */
    public void editSource(HarvestSourceDTO source) throws DAOException;

    /**
     * adds references to sources into queue for removal.
     * 
     * @param urls
     *            - urls to add
     * @throws DAOException
     */
    public void queueSourcesForDeletion(List<String> urls) throws DAOException;

    /**
     * @return List<String>
     * @throws DAOException
     *             fetches all scheduled source URLs, which are scheduled for
     *             removal.
     */
    List<String> getScheduledForDeletion() throws DAOException;

    /**
     * Deletes selected source from the DB.
     * 
     * @param url
     *            - source url
     * @throws DAOException
     */
    public void deleteSourceByUrl(String url) throws DAOException;

    /**
     * 
     * @param sourceId
     * @param numStatements
     * @param numResources
     * @param sourceAvailable
     * @param failed
     * @throws DAOException
     */
    public void updateHarvestFinished(int sourceId, Integer numStatements,
            Boolean sourceAvailable, boolean failed) throws DAOException;

    /**
     * 
     * @param limit
     * @return List<HarvestSourceDTO>
     * @throws DAOException
     */
    public List<HarvestSourceDTO> getNextScheduledSources(int limit)
            throws DAOException;

    /**
     * deletes orphans from SPO table. Orphan is a record inside SPO, which has
     * an non-existent source in HARVEST_SOURCE.
     * 
     * @throws DAOException
     * 
     */
    void deleteTriplesOfMissingSources() throws DAOException;

    /**
     * @param neededToRemain
     * @throws DAOException
     */
    void deleteHarvestHistory(int neededToRemain) throws DAOException;

    /**
     * 
     * @param harvestSourceId
     * @return urgencyScore
     * @throws DAOException
     */
    public double getUrgencyScore(int harvestSourceId) throws DAOException;

    /**
     * @return String
     * @throws DAOException
     */
    String getSourcesInInferenceRules() throws DAOException;

    /**
     * @param searchString
     * @param pagingRequest
     * @param sortingRequest
     * @param sourceUris
     * @return Pair<Integer, List<HarvestSourceDTO>>
     * @throws DAOException
     */
    Pair<Integer, List<HarvestSourceDTO>> getInferenceSources(
            String searchString, PagingRequest pagingRequest,
            SortingRequest sortingRequest, String sourceUris)
            throws DAOException;

    /**
     * @param url
     * @return boolean
     * @throws DAOException
     */
    boolean isSourceInInferenceRule(String url) throws DAOException;

    /**
     * @param url
     * @return boolean
     * @throws DAOException
     */
    boolean addSourceIntoInferenceRule(String url) throws DAOException;

    /**
     * @param url
     * @return boolean
     * @throws DAOException
     */
    boolean removeSourceFromInferenceRule(String url) throws DAOException;

    /**
     * Loads the file into the repository (triple store). File is required to be
     * RDF.
     * 
     * @param file
     * @param sourceUrlString
     * @return int
     * @throws DAOException
     * @throws RepositoryException
     * @throws RDFParseException
     * @throws IOException
     */
    public int addSourceToRepository(File file, String sourceUrlString)
            throws DAOException, RepositoryException, RDFParseException,
            IOException;

    /**
     * Adds the meta information the harvester has collected about the source.
     * The meta data is considered part of the harvester and not the source.
     * Therefore the meta data is stored in the harvester's named graph (or
     * context)
     * 
     * @param sourceMetadata
     * @throws DAOException
     * @throws RepositoryException
     * @throws RDFParseException
     * @throws IOException
     */
    public void addSourceMetadata(SubjectDTO sourceMetadata)
            throws DAOException, RDFParseException, RepositoryException,
            IOException;
    
    /**
     * Returns new harvest sources found by HarvestingJob
     * 
     * @param sourceUrl
     * @return List<String>
     * @throws DAOException
     * @throws RepositoryException
     * @throws RDFParseException
     * @throws IOException
     */
    public List<String> getNewSources(String sourceUrl)
            throws DAOException, RDFParseException, RepositoryException,
            IOException;
    
   
    /**
     * Returns metadata from /harvester context
     * 
     * @param subject
     * @param predicate
     * @return String
     * @throws DAOException
     * @throws RepositoryException
     */
    public String getSourceMetadata(String subject, String predicate) throws DAOException, RepositoryException;
    
    /**
     * Inserts given metadata into /harvester context
     * 
     * @param subject
     * @param predicate
     * @param value
     * @throws DAOException
     * @throws RepositoryException
     */
    public void insertUpdateSourceMetadata(String subject, String predicate, String value) throws DAOException, RepositoryException;
    
    /**
     * @param source
     * @throws DAOException
     */
    public void editRedirectedSource(HarvestSourceDTO source) throws DAOException;

}
