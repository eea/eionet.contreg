package eionet.cr.dao.virtuoso;

import java.util.List;
import java.util.Map;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.ExporterDAO;
import eionet.cr.util.sql.ResultSetExportReader;

/**
 *
 * @author jaanus
 *
 */
public class VirtuosoExporterDAO extends VirtuosoBaseDAO implements ExporterDAO{

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.ExporterDAO#exportByTypeAndFilters(java.util.Map, java.util.List, eionet.cr.util.sql.ResultSetExportReader)
     */
    @Override
    public void exportByTypeAndFilters(Map<String, String> filters,
            List<String> selectedPredicates, ResultSetExportReader reader) throws DAOException {

        throw new UnsupportedOperationException("Method not implemented");
    }

}
