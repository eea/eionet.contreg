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
}
