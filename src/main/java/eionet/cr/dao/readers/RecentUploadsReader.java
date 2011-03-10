package eionet.cr.dao.readers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.BNode;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;


/**
 * 
 * @author risto
 *
 * @param <T>
 */
public class RecentUploadsReader<T> extends ResultSetMixedReader<T>{

    private Map<String,Date> resultMap;

    public RecentUploadsReader() {
        resultMap = new HashMap<String, Date>();
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.util.sql.SQLResultSetReader#readRow(java.sql.ResultSet)
     */
    @Override
    public void readRow(ResultSet rs) throws SQLException, ResultSetReaderException {
        throw new UnsupportedOperationException("Method not implemented");
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
        if (subjectValue instanceof BNode && blankNodeUriPrefix!=null){
            if (!subjectUri.startsWith(blankNodeUriPrefix)){
                subjectUri = blankNodeUriPrefix + subjectUri;
            }
        }
        resultList.add((T)subjectUri);

        // expecting the column "d" to contain the date
        Value dateValue = bindingSet.getValue("d");
        if(dateValue != null){
            try {
                String dateStr = dateValue.stringValue();
                if(dateStr != null && dateStr.length() > 0){
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    Date date = dateFormat.parse(dateStr);
                    resultMap.put(subjectUri, date);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }	

    }

    public Map<String, Date> getResultMap() {
        return resultMap;
    }

}
