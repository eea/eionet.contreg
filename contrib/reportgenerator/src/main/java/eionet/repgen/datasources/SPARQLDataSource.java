package eionet.repgen.datasources;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryResult;

import eionet.repgen.Logger;
import eionet.repgen.SPARQLUtil;

/**
 * SPARQL data source implementation for iReports.
 *
 * @author Kaido Laine
 */
public class SPARQLDataSource implements JRDataSource {

    protected String countryCode;

    protected String sparql;

    protected QueryResult<BindingSet> result;

    private BindingSet currentBinding;

    public SPARQLDataSource() {

    }

    public void init(String sparql) throws Exception {
        this.sparql = sparql;
        this.result = SPARQLUtil.getSparqlQueryResult(sparql);
        Logger.log("Result is " + result);
    }

    // default implementation returns strings
    // TODO improve in the future to return required classes
    @Override
    public Object getFieldValue(JRField field) throws JRException {
        Object o = null;

        String fieldName = field.getName();
        Value value = currentBinding.getValue(fieldName);

        if (value != null) {
            o = value.stringValue();
        } else {
            o = "N/A for " + fieldName;
        }
        return o;
    }

    @Override
    public boolean next() throws JRException {
        try {
            if (result.hasNext()) {
                currentBinding = result.next();
                return true;
            }

        } catch (QueryEvaluationException e) {
            e.printStackTrace();
            throw new JRException("Error in next() " + e);
        }

        return false;
    }

}
