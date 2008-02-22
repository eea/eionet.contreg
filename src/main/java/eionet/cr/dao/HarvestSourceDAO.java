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
     * @throws Exception
     * @param SourceDTO source
     */
    public void addSource(HarvestSourceDTO source, String user) throws DAOException;
}
