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

/**
 * @author altnyris
 *
 */
public interface HarvestSourceDAO extends IDao{
	/**
	 * 
     * @return list of harvesting sources (excluding unavailable sources and tracked files)
     * @throws DAOException
     */
    List<HarvestSourceDTO> getHarvestSources(String searchString) throws DAOException;
    
    /**
     * @return list of harvest tracked files
     * @throws DAOException
     */
    List<HarvestSourceDTO> getHarvestTrackedFiles(String searchString) throws DAOException;
    
    /**
     * @return list of unavailable harvest sources
     * @throws DAOException
     */
    List<HarvestSourceDTO> getHarvestSourcesUnavailable(String searchString) throws DAOException;

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
    public Integer addSourceIgnoreDuplicate(HarvestSourceDTO source, String user) throws DAOException;

    /**
     * @throws DAOException
     * @param HarvestSourceDTO source
     */
    public void editSource(HarvestSourceDTO source) throws DAOException;

    /**
     * 
     * @param urls
     * @throws DAOException
     */
    public void deleteSourcesByUrl(List<String> urls) throws DAOException;

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
