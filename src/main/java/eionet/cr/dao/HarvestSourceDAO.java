package eionet.cr.dao;

import java.util.List;

import eionet.cr.dto.HarvestSourceDTO;

/**
 * @author altnyris
 *
 */
public interface HarvestSourceDAO {
	/**
     * @return list of harvesting sources
     * @throws DAOException
     */
    public List<HarvestSourceDTO> getHarvestSources() throws DAOException;

    /**
     * 
     * @param type
     * @return
     * @throws DAOException
     */
    public List<HarvestSourceDTO> getHarvestSourcesByType(String type) throws DAOException;
    
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
     * @return
     * @throws DAOException
     */
    public List<String> getDistinctSchedules() throws DAOException;

    /**
     * 
     * @param schedule
     * @return
     * @throws DAOException
     */
    public List<HarvestSourceDTO> getHarvestSourcesBySchedule(String schedule) throws DAOException;
    
    /**
     * 
     * @return
     * @throws DAOException
     */
    public List<HarvestSourceDTO> getHarvestSourcesUnavailable() throws DAOException;
}
