package eionet.cr.dao.readers;

import java.util.LinkedHashMap;
import java.util.Map;

import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;

import eionet.cr.dto.ObjectDTO;
import eionet.cr.util.sesame.SPARQLResultSetBaseReader;

/**
 *
 * @author jaanus
 *
 */
public class PostHarvestScriptTestResultsReader extends SPARQLResultSetBaseReader<Map<String, ObjectDTO>> {

    /*
     * (non-Javadoc)
     * @see eionet.cr.util.sesame.SPARQLResultSetReader#readRow(org.openrdf.query.BindingSet)
     */
    @Override
    public void readRow(BindingSet bindingSet) throws ResultSetReaderException {

        LinkedHashMap<String, ObjectDTO> rowMap = new LinkedHashMap<String, ObjectDTO>();
        if (bindingSet != null && bindingSet.size() > 0 && bindingNames != null && !bindingNames.isEmpty()) {

            for (String bindingName : bindingNames) {

                Binding binding = bindingSet.getBinding(bindingName);
                Value bindingValue = binding.getValue();
                if (bindingValue != null) {

                    String stringValue = bindingValue.stringValue();
                    if (stringValue == null) {
                        stringValue = "";
                    }

                    if (bindingValue instanceof Literal) {
                        rowMap.put(bindingName, ObjectDTO.createLiteral(stringValue));
                    } else {
                        rowMap.put(bindingName, ObjectDTO.createResource(stringValue));
                    }
                }
            }
        }

        resultList.add(rowMap);
    }
}
