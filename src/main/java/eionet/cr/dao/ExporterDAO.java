package eionet.cr.dao;

import java.util.List;
import java.util.Map;

import eionet.cr.dao.readers.ResultSetExportReader;

public interface ExporterDAO extends DAO {

    public void exportByTypeAndFilters(
            Map<String, String> filters,
            List<String> selectedPredicates,
            ResultSetExportReader<Object> reader) throws DAOException;

}
