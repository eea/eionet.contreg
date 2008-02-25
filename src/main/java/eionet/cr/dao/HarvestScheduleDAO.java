package eionet.cr.dao;

import eionet.cr.dto.HarvestScheduleDTO;

/**
 * @author altnyris
 *
 */
public interface HarvestScheduleDAO {
	    
    /**
     * @return harvesting sources schedule
     * @throws DAOException
     * @param int harvestSourceID
     */
    public HarvestScheduleDTO getHarvestScheduleBySourceId(int harvestSourceID) throws DAOException;
    
    /**
     * @throws DAOException
     * @param HarvestScheduleDTO schedule
     */
    public void addSchedule(HarvestScheduleDTO schedule) throws DAOException;
    
    /**
     * @throws DAOException
     * @param HarvestScheduleDTO schedule
     */
    public void editSchedule(HarvestScheduleDTO schedule) throws DAOException;
    
    /**
     * @throws DAOException
     * @param Integer sourceId
     */
    public void deleteSchedule(Integer sourceId) throws DAOException;
}
