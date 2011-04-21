package eionet.cr.web.sparqlClient.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

import eionet.cr.util.Util;

/**
 *
 * @author jaanus
 *
 */
public class QueryResult {

    /** */
    private List<String> variables;
    private ArrayList<HashMap<String,ResultValue>> rows;
    private ArrayList<Map<String,Object>> cols;

    /**
     *
     * @param queryResult
     * @throws QueryEvaluationException
     */
    public QueryResult(TupleQueryResult queryResult) throws QueryEvaluationException {

        if (queryResult != null && queryResult.hasNext()) {

            this.variables = queryResult.getBindingNames();
            addCols();
            while (queryResult.hasNext()) {
                add(queryResult.next());
            }
        }
    }

    private void add(BindingSet bindingSet) {

        if (bindingSet == null || variables == null || variables.isEmpty()) {
            return;
        }

        HashMap<String,ResultValue> map = new HashMap<String, ResultValue>();
        for (String variable : variables) {

            ResultValue resultValue = null;
            Value value = bindingSet.getValue(variable);

            if (value != null) {

                String valueString = Util.escapeHtml(value.stringValue());
                if (value instanceof Literal) {
                    resultValue = new ResultValue(valueString, true);
                } else {
                    resultValue = new ResultValue(valueString, false);
                }
            }

            map.put(variable, resultValue);
        }

        if (rows == null) {
            rows = new ArrayList<HashMap<String,ResultValue>>();
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
                cols = new ArrayList<Map<String,Object>>();
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
}
