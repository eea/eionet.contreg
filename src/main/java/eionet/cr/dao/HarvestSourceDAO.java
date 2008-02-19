package eionet.cr.dao;

import java.util.List;
import java.util.Map;

import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.util.sql.SQLValue;

/**
 * @author altnyris
 *
 */
public interface HarvestSourceDAO {
	/**
     * @return list of harvesting sources
     * @throws Exception
     */
    public List<Map<String,SQLValue>> getSources() throws DAOException;
    
    /**
     * @throws Exception
     * @param SourceDTO source
     */
    public void addSource(HarvestSourceDTO source) throws DAOException;
}
