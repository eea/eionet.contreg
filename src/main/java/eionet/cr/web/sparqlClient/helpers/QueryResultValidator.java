package eionet.cr.web.sparqlClient.helpers;

import java.util.Map;
import java.util.Map.Entry;

import eionet.cr.util.URIUtil;

/**
 *
 * Contains methods to validate SparqlQuery results to different scenarios.
 *
 * @author Jaak
 */
public class QueryResultValidator {

    public static final String PROPER_BULK_SOURCE_OK = "ok";
    public static final String PROPER_BULK_SOURCE_FAIL_TOO_MANY_COLUMNS = "Query must return only single column with sources.";
    public static final String PROPER_BULK_SOURCE_FAIL_RESULT_EMPTY = "Query did not return any results.";
    public static final String PROPER_BULK_SOURCE_FAIL_RESULT_CONTAINS_NON_URLS =
            "Query results include results that are not URIs.";

    /**
     * Validates whether the Sparql query result returned is proper for sources
     *
     * @param queryResult
     * @return
     */
    public static String isProperBulkSourceResult(QueryResult queryResult) {

        if (queryResult != null) {
            if (queryResult.getCols() == null || queryResult.getCols().size() != 1) {
                return PROPER_BULK_SOURCE_FAIL_TOO_MANY_COLUMNS;
            }

            if (queryResult.getRows() == null || queryResult.getRows().size() == 0) {
                return PROPER_BULK_SOURCE_FAIL_RESULT_EMPTY;
            }

            if (queryResult.getRows() != null && queryResult.getRows().size() > 0) {
                for (Map<String, ResultValue> row : queryResult.getRows()) {
                    if (row.isEmpty()) {
                        return PROPER_BULK_SOURCE_FAIL_RESULT_CONTAINS_NON_URLS;
                    }

                    for (Entry<String, ResultValue> entry : row.entrySet()){
                        if (!URIUtil.isURI(entry.getValue().getValue())){
                            return PROPER_BULK_SOURCE_FAIL_RESULT_CONTAINS_NON_URLS;
                        }
                    }
                }

            }
        }

        return PROPER_BULK_SOURCE_OK;
    }

}
