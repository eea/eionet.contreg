package eionet.cr.web.sparqlClient.helpers;

import java.util.Map;

import org.apache.commons.validator.routines.UrlValidator;

/**
 *
 * Contains methods to validate SparqlQuery results to different scenarios.
 *
 * @author Jaak
 */
public class QueryResultValidator {

    public static final String PROPER_BULK_SOURCE_OK = "ok";
    public static final String PROPER_BULK_SOURCE_FAIL_RESULT_EMPTY = "Query did not return any results.";
    public static final String PROPER_BULK_SOURCE_FAIL_RESULT_CONTAINS_NON_URLS =
            "Query results first column includes results that are not URIs.";

    /**
     * Validates whether the Sparql query result returned is proper for sources
     *
     * @param queryResult
     * @return
     */
    public static String isProperBulkSourceResult(QueryResult queryResult) {

        if (queryResult != null) {

            if (queryResult.getRows() == null || queryResult.getRows().size() == 0) {
                return PROPER_BULK_SOURCE_FAIL_RESULT_EMPTY;
            }

            if (queryResult.getRows() != null && queryResult.getRows().size() > 0) {
                for (Map<String, ResultValue> row : queryResult.getRows()) {
                    if (row.isEmpty()) {
                        return PROPER_BULK_SOURCE_FAIL_RESULT_CONTAINS_NON_URLS;
                    }

                    String firstColumn = (String) queryResult.getCols().get(0).get("property");
                    ResultValue resultValue = row.get(firstColumn);

                    if (!UrlValidator.getInstance().isValid(resultValue.getValue())){
                        return PROPER_BULK_SOURCE_FAIL_RESULT_CONTAINS_NON_URLS;
                    }
                }

            }
        }

        return PROPER_BULK_SOURCE_OK;
    }

}
