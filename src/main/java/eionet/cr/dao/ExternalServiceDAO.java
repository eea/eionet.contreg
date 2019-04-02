package eionet.cr.dao;

import eionet.cr.dto.ExternalServiceDTO;

import java.util.List;

/**
 * DAO for external services.
 */
public interface ExternalServiceDAO extends DAO {

    /**
     * Returns all valid external services.
     * @return list of ExternalServiceDTO objects.
     * @throws DAOException if query fails
     */
    List<ExternalServiceDTO> getExternalServices() throws DAOException;

    /**
     * returns the service with the given ID.
     * @param id service id
     * @return Service DTO
     * @throws DAOException if db query fails
     */
    ExternalServiceDTO fetch(Integer id) throws DAOException;
}
