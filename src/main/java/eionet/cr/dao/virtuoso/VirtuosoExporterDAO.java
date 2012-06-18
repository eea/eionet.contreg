package eionet.cr.dao.virtuoso;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.ExporterDAO;
import eionet.cr.dao.readers.ResultSetExportReader;
import eionet.cr.dao.virtuoso.helpers.VirtuosoFilteredSearchHelper;
import eionet.cr.util.Bindings;
import eionet.cr.util.Util;
import eionet.cr.util.sesame.SPARQLQueryUtil;

/**
 * 
 * @author jaanus
 * 
 */
public class VirtuosoExporterDAO extends VirtuosoBaseDAO implements ExporterDAO {

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.ExporterDAO#exportByTypeAndFilters(java.util.Map, java.util.List,
     * eionet.cr.util.sql.ResultSetExportReader)
     */
    @Override
    public void exportByTypeAndFilters(Map<String, String> filters, List<String> selectedPredicates,
            ResultSetExportReader<Object> reader) throws DAOException {

        // create query helper
        VirtuosoFilteredSearchHelper helper = new VirtuosoFilteredSearchHelper(filters, null, null, null, true);

        String whereContents = helper.getWhereContents();
        if (whereContents != null && !whereContents.trim().startsWith(".")) {
            whereContents = "." + whereContents;
        }
        String query = getSubjectsDataQuery(whereContents, selectedPredicates, helper.getQueryBindings());
        long startTime = System.currentTimeMillis();
        logger.debug("Start exporting type search results: " + query);

        executeSPARQL(query, helper.getQueryBindings(), reader);

        logger.debug("Total export time " + Util.durationSince(startTime));

    }

    /** */
    private String getSubjectsDataQuery(String subjectsSubQuery, Collection<String> predicateUris, Bindings bindings)
            throws DAOException {
        if (subjectsSubQuery == null || subjectsSubQuery.length() == 0) {
            throw new IllegalArgumentException("Subjects sub query must not be null or empty");
        }

        // TODO does not work with multiple predicates and inferencing - check if Virtuoso issue is solved
        String sparql =
                SPARQLQueryUtil.getCrInferenceDefinitionStr() + "select distinct * where {?s ?p ?o " + subjectsSubQuery
                        + " . filter (?p IN (" + SPARQLQueryUtil.urisToCSV(predicateUris, "exportPredicateValue", bindings)
                        + "))} ORDER BY ?s";
        return sparql;
    }
}
