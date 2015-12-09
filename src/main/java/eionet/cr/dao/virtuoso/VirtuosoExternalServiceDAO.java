package eionet.cr.dao.virtuoso;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.readers.ExternalServiceDTOReader;
import eionet.cr.dto.ExternalServiceDTO;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Virtuoso implementation for the {@link VirtuosoExternalServiceDAO}.
 */
public class VirtuosoExternalServiceDAO extends VirtuosoBaseDAO implements eionet.cr.dao.ExternalServiceDAO {
    
    /** */
    private static final Logger LOGGER = Logger.getLogger(VirtuosoExternalServiceDAO.class);

    /** */
    private static final String LIST_ALL_SERVICES =
            "select * from EXTERNAL_SERVICE order by SERVICE_URL";

    private static final String FETCH_SERVICE =
            "select * from EXTERNAL_SERVICE where SERVICE_ID = ?";

    @Override
    public List<ExternalServiceDTO> getExternalServices() throws DAOException {

        List<ExternalServiceDTO> list = executeSQL(LIST_ALL_SERVICES, null, new ExternalServiceDTOReader());
        return list;
    }

    @Override
    public ExternalServiceDTO fetch(Integer id) throws DAOException {
        ArrayList<Object> values = new ArrayList<Object>();
        values.add(Integer.valueOf(id));
        return executeUniqueResultSQL(FETCH_SERVICE, values, new ExternalServiceDTOReader());

    }
}
