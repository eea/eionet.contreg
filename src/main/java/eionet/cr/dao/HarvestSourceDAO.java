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
     * @throws Exception
     */
    public List<HarvestSourceDTO> getHarvestSources() throws DAOException;
    
    /**
     * @return list of harvesting sources
     * @throws Exception
     * @param int harvestSourceID
     */
    public HarvestSourceDTO getHarvestSourceById(int harvestSourceID) throws DAOException;
    
    /**
     * @throws Exception
     * @param HarvestSourceDTO source
     */
    public void addSource(HarvestSourceDTO source, String user) throws DAOException;
    
    /**
     * @throws Exception
     * @param HarvestSourceDTO source
     */
    public void editSource(HarvestSourceDTO source) throws DAOException;
}
