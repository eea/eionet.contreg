package eionet.cr.dao;

import java.util.List;
import java.util.Map;

import eionet.cr.dao.readers.ResultSetExportReader;

/**
 * Interface for DAO methods dealing with the export of search result lists.
 *
 * @author Enriko Käsper
 * @author Jaanus Heinlaid
 */
public interface ExporterDAO extends DAO {

    /**
     *
     * @param filters
     * @param selectedPredicates
     * @param reader
     * @throws DAOException
     */
    void
            exportByTypeAndFilters(Map<String, String> filters, List<String> selectedPredicates,
                    ResultSetExportReader<Object> reader) throws DAOException;
}
