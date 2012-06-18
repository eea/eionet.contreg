package eionet.cr.dao.readers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

import eionet.cr.common.Predicates;
import eionet.cr.dto.DeliveryDTO;

/**
 * Reads delivery objects from Resultset.
 * 
 * @author altnyris
 * 
 */
public class DeliverySearchReader extends ResultSetMixedReader<DeliveryDTO> {

    private HashMap<String, DeliveryDTO> deliveries = new HashMap<String, DeliveryDTO>();
    private int fileCount = 0;

    /**
     * converts Bindingset row to DeliveryDTO.
     * 
     * @param bindingSet - BindingSet
     * @throws ResultSetReaderException - if error in reading
     */
    @Override
    public void readRow(final BindingSet bindingSet) throws ResultSetReaderException {
        if (bindingSet != null && bindingSet.size() > 0) {
            Value subjectValue = bindingSet.getValue("s");
            String subjectUri = subjectValue.stringValue();

            if (!StringUtils.isBlank(subjectUri)) {
                DeliveryDTO delivery = deliveries.get(subjectUri);
                if (delivery == null) {
                    delivery = new DeliveryDTO(subjectUri);
                    fileCount = 0;
                }

                String predicateUri = bindingSet.getValue("p") != null ? bindingSet.getValue("p").stringValue() : null;
                String object = bindingSet.getValue("o") != null ? bindingSet.getValue("o").stringValue() : null;

                if (predicateUri != null && predicateUri.equals(Predicates.DC_TITLE)) {
                    delivery.setTitle(object);
                } else if (predicateUri != null && predicateUri.equals(Predicates.ROD_COVERAGE_NOTE)) {
                    delivery.setCoverageNote(object);
                } else if (predicateUri != null && predicateUri.equals(Predicates.ROD_HAS_FILE)) {
                    fileCount++;
                    delivery.setFileCnt(fileCount);
                } else if (predicateUri != null && predicateUri.equals(Predicates.ROD_PERIOD)) {
                    delivery.setPeriod(object);
                } else if (predicateUri != null && predicateUri.equals(Predicates.ROD_START_OF_PERIOD)) {
                    if (object != null && object.length() > 4) {
                        delivery.setStartYear(object.substring(0, 4));
                    }
                } else if (predicateUri != null && predicateUri.equals(Predicates.ROD_END_OF_PERIOD)) {
                    if (object != null && object.length() > 4) {
                        delivery.setEndYear(object.substring(0, 4));
                    }
                } else if (predicateUri != null && predicateUri.equals(Predicates.ROD_LOCALITY_PROPERTY)) {
                    Value cname = bindingSet.getValue("cname");
                    if (cname != null) {
                        String cnameValue = cname.stringValue();
                        delivery.setLocality(cnameValue);
                    }
                } else if (predicateUri != null && predicateUri.equals(Predicates.ROD_RELEASED)) {
                    delivery.setDate(object);
                }
                deliveries.put(subjectUri, delivery);
            }
        }
    }

    public HashMap<String, DeliveryDTO> getMap() {
        return deliveries;
    }

    @Override
    public void readRow(final ResultSet rs) throws SQLException, ResultSetReaderException {
        // TODO Auto-generated method stub
    }

}
