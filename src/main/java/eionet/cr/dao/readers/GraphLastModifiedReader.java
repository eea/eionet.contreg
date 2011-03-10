package eionet.cr.dao.readers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

/**
 *
 * @author risto
 */
public class GraphLastModifiedReader<T> extends ResultSetMixedReader<T>{

    private Map<String,Date> resultMap;

    public GraphLastModifiedReader() {
        resultMap = new HashMap<String, Date>();
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.util.sql.SQLResultSetReader#readRow(java.sql.ResultSet)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void readRow(ResultSet rs) throws SQLException, ResultSetReaderException {
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.util.sesame.SPARQLResultSetReader#readRow(org.openrdf.query.BindingSet)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void readRow(BindingSet bindingSet) throws ResultSetReaderException {

        Value subjectValue = bindingSet.getValue("s");

        // expecting the URI of the matching subject to be in column "s"
        String subjectUri = subjectValue.stringValue();
        String predicateValue = bindingSet.getValue("o").stringValue();

        if(StringUtils.isNotEmpty(predicateValue)){
            try {
                SimpleDateFormat lastModifiedDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                Date lastModDate = lastModifiedDateFormat.parse(predicateValue);
                resultMap.put(subjectUri, lastModDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public Map<String, Date> getResultMap() {
        return resultMap;
    }

}
