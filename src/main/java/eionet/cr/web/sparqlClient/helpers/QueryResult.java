package eionet.cr.web.sparqlClient.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

import eionet.cr.config.GeneralConfig;

/**
 *
 * @author jaanus
 *
 */
public class QueryResult {

    /** */
    private List<String> variables;
    private ArrayList<HashMap<String, ResultValue>> rows;
    private ArrayList<Map<String, Object>> cols;

    /** When true, literal objects have language and type added at the end of the value. */
    private boolean virtuosoFormat;

    /**
     * If max rows count is not defined in properties, this constant value is used.
     */
    private static final int DEFAULT_MAX_ROWS_COUNT = 2000;

    /**
     * Maximum Rows count that is returned in HTML.
     */
    private static final int MAX_ROWS_COUNT = GeneralConfig.getIntProperty(GeneralConfig.SPARQLENDPOINT_MAX_ROWS_COUNT,
            DEFAULT_MAX_ROWS_COUNT);

    /** */
    private boolean allRowsReturned = true;

    /**
     * Local logger.
     */
    private static final Log LOGGER = LogFactory.getLog(QueryResult.class);

    /**
     *
     * @param queryResult
     * @throws QueryEvaluationException
     */
    public QueryResult(TupleQueryResult queryResult, boolean virtuosoFormat) throws QueryEvaluationException {
        this.virtuosoFormat = virtuosoFormat;

        if (queryResult != null && queryResult.hasNext()) {

            this.variables = queryResult.getBindingNames();
            addCols();
            int counter = 0;
            while (queryResult.hasNext()) {
                add(queryResult.next());
                counter++;
                // if query result exceeds max rows count, return the resultset
                if (counter == MAX_ROWS_COUNT) {
                    LOGGER.debug("Maximum rows count exceeded, returning first rows");
                    allRowsReturned = false;
                    break;
                }
            }
        }
    }

    private void add(BindingSet bindingSet) {

        if (bindingSet == null || variables == null || variables.isEmpty()) {
            return;
        }

        HashMap<String, ResultValue> map = new HashMap<String, ResultValue>();
        for (String variable : variables) {

            ResultValue resultValue = null;
            Value value = bindingSet.getValue(variable);

            if (value != null) {

                String valueString = value.stringValue();
                if (value instanceof Literal) {
                    if (virtuosoFormat) {
                        if (((Literal) value).getLanguage() != null) {
                            valueString = "\"" + valueString + "\"@" + ((Literal) value).getLanguage();
                        }
                        if (((Literal) value).getDatatype() != null) {
                            String type = ((Literal) value).getDatatype().stringValue();
                            type = type.replaceFirst("http://www.w3.org/2001/XMLSchema#", "xsd:");
                            if (!type.startsWith("xsd:")) {
                                type = "<" + type + ">";
                            }

                            valueString = "\"" + valueString + "\"^^" + type;
                        }
                    }
                    resultValue = new ResultValue(valueString, true);
                } else {
                    resultValue = new ResultValue(valueString, false);
                }
            }

            map.put(variable, resultValue);
        }

        if (rows == null) {
            rows = new ArrayList<HashMap<String, ResultValue>>();
        }
        rows.add(map);
    }

    /**
     *
     */
    private void addCols() {

        if (variables == null || variables.isEmpty()) {
            return;
        }

        for (String variable : variables) {

            Map<String, Object> col = new HashMap<String, Object>();
            col.put("property", variable);
            col.put("title", variable);
            col.put("sortable", Boolean.TRUE);

            if (cols == null) {
                cols = new ArrayList<Map<String, Object>>();
            }
            cols.add(col);
        }
    }

    /**
     * @return the variables
     */
    public List<String> getVariables() {
        return variables;
    }

    /**
     * @return the rows
     */
    public ArrayList<HashMap<String, ResultValue>> getRows() {
        return rows;
    }

    /**
     * @return the cols
     */
    public ArrayList<Map<String, Object>> getCols() {
        return cols;
    }

    /**
     * Shows if all rows were returned (did not exceed maximum rowcount.
     *
     * @return boolean
     */
    public boolean isAllRowsReturned() {
        return allRowsReturned;
    }

    /**
     * Setter of allRowsReturned.
     *
     * @param allRowsReturned to indicate if full query is returned.
     */
    public void setAllRowsReturned(final boolean allRowsReturned) {
        this.allRowsReturned = allRowsReturned;
    }

}
