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
     * @return list of harvesting sources
     * @throws DAOException
     * @param int harvestSourceID
     */
    public HarvestSourceDTO getHarvestSourceById(int harvestSourceID) throws DAOException;
    
    /**
     * @throws DAOException
     * @param HarvestSourceDTO source
     */
    public void addSource(HarvestSourceDTO source, String user) throws DAOException;
    
    /**
     * @throws DAOException
     * @param HarvestSourceDTO source
     */
    public void editSource(HarvestSourceDTO source) throws DAOException;
}
