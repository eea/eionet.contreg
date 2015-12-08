package eionet.cr.dao.virtuoso;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.readers.ExternalServiceDTOReader;
import eionet.cr.dto.ExternalServiceDTO;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Virtuoso implementation for the {@link eionet.cr.dao.virtuoso.ExternalServiceDAO}.
 */
public class ExternalServiceDAO extends VirtuosoBaseDAO implements eionet.cr.dao.ExternalServiceDAO {
    
    /** */
    private static final Logger LOGGER = Logger.getLogger(ExternalServiceDAO.class);

    /** */
    private static final String LIST_ALL_SERVICES =
            "select * from EXTERNAL_SERVICE order by SERVICE_URL";


    @Override
    public List<ExternalServiceDTO> getExternalServices() throws DAOException {

        List<ExternalServiceDTO> list = executeSQL(LIST_ALL_SERVICES, null, new ExternalServiceDTOReader());
        return list;
    }
}
