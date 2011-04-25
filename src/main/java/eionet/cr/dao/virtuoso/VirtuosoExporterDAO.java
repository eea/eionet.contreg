package eionet.cr.dao.virtuoso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.ExporterDAO;
import eionet.cr.dao.readers.ResultSetExportReader;
import eionet.cr.dao.virtuoso.helpers.VirtuosoFilteredSearchHelper;
import eionet.cr.util.Util;

/**
 *
 * @author jaanus
 *
 */
public class VirtuosoExporterDAO extends VirtuosoBaseDAO implements ExporterDAO {

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.ExporterDAO#exportByTypeAndFilters(java.util.Map,
     * java.util.List, eionet.cr.util.sql.ResultSetExportReader)
     */
    @Override
    public void exportByTypeAndFilters(Map<String, String> filters,
            List<String> selectedPredicates, ResultSetExportReader<Object> reader) throws DAOException {

        // create query helper
        VirtuosoFilteredSearchHelper helper = new VirtuosoFilteredSearchHelper(filters, null,
                null, null);

        // limit predicates
        String[] predicateUris = null;
        if (selectedPredicates != null && !selectedPredicates.isEmpty()) {
            predicateUris = (String[]) selectedPredicates.toArray(new String[selectedPredicates.size()]);
        }

        String query = getSubjectsDataQuery(helper.getQueryParameters(new ArrayList<Object>()), predicateUris);
        long startTime = System.currentTimeMillis();
        logger.debug("Start exporting type search results: " + query);

        executeSPARQL(query, reader);

        logger.debug("Total export time " + Util.durationSince(startTime));

    }

    private String getSubjectsDataQuery(String subjectsSubQuery,
            String[] predicateUris) throws DAOException {

        if (subjectsSubQuery == null || subjectsSubQuery.length() == 0)
            throw new IllegalArgumentException("Subjects sub query must not be null or empty");

        StringBuilder strBuilder = new StringBuilder().
                append("select distinct * where {?s ?p ?o ").
                append(subjectsSubQuery);

        // if only certain predicates needed, add relevant filter
        if (predicateUris != null && predicateUris.length > 0) {

            int i = 0;
            strBuilder.append(" . filter (");
            for (String predicateUri : predicateUris) {
                if (i > 0) {
                    strBuilder.append(" || ");
                }
                strBuilder.append("?p = <").append(predicateUri).append(">");
                i++;
            }

            strBuilder.append(") ");
        }

        strBuilder.append("} ORDER BY ?s");
        return strBuilder.toString();
    }
}
