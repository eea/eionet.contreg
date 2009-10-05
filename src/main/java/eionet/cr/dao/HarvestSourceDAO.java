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

import java.util.List;

import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.pagination.PaginationRequest;

/**
 * @author altnyris
 *
 */
public interface HarvestSourceDAO extends IDao{
	/**
	 * 
     * @param sortingRequest 
	 * @return list of harvesting sources (excluding unavailable sources and tracked files)
     * @throws DAOException
     */
    List<HarvestSourceDTO> getHarvestSources(String searchString, PaginationRequest pageRequest, SortingRequest sortingRequest) throws DAOException;
    
    /**
     * @param sortingRequest 
     * @return list of harvest tracked files
     * @throws DAOException
     */
    List<HarvestSourceDTO> getHarvestTrackedFiles(String searchString, PaginationRequest pageRequest, SortingRequest sortingRequest) throws DAOException;
    
    /**
     * @param sortingRequest 
     * @return list of unavailable harvest sources
     * @throws DAOException
     */
    List<HarvestSourceDTO> getHarvestSourcesUnavailable(String searchString, PaginationRequest pageRequest, SortingRequest sortingRequest) throws DAOException;

    /**
     * @return harvesting sources
     * @throws DAOException
     * @param int harvestSourceID
     */
    public HarvestSourceDTO getHarvestSourceById(Integer harvestSourceID) throws DAOException;
    
    /**
     * 
     * @param url
     * @return
     * @throws DAOException
     */
    public HarvestSourceDTO getHarvestSourceByUrl(String url) throws DAOException;
    
    /**
     * @throws DAOException
     * @param HarvestSourceDTO source
     */
    public Integer addSource(HarvestSourceDTO source, String user) throws DAOException;

    /**
     * @throws DAOException
     * @param HarvestSourceDTO source
     */
    public void addSourceIgnoreDuplicate(HarvestSourceDTO source, String user) throws DAOException;

    /**
     * @throws DAOException
     * @param HarvestSourceDTO source
     */
    public void editSource(HarvestSourceDTO source) throws DAOException;

    /**
     * adds references to sources into queue for removal.
     * 
     * @param urls - urls to add
     * @throws DAOException
     */
    public void queueSourcesForDeletion(List<String> urls) throws DAOException;
    
    /**
     * fetches all scheduled source URLs, which are scheduled for removal.
     * 
     * @return
     */
    List<String> getScheduledForDeletion() throws DAOException;
    /**
     * Deletes selected source from the DB.
     * 
     * @param url - source url
     * @throws DAOException
     */
    public void deleteSourceByUrl(String url) throws DAOException;

    /**
     * 
     * @param sourceId
     * @param numStatements
     * @param numResources
     * @param sourceAvailable 
     * @throws DAOException
     */
    public void updateHarvestFinished(int sourceId, Integer numStatements, Integer numResources, Boolean sourceAvailable) throws DAOException;
    
    /**
     * 
     * @param sourceId
     * @throws DAOException
     */
    public void updateHarvestStarted(int sourceId) throws DAOException;
    
    /**
     * 
     * @param numOfSegments
     * @return
     * @throws DAOException
     */
    public List<HarvestSourceDTO> getNextScheduledSources(int numOfSegments) throws DAOException;

}
