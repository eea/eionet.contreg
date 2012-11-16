package eionet.cr.dao.readers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

import eionet.cr.dto.DeliveryFilesDTO;
import eionet.cr.dto.FileDTO;
import eionet.cr.util.URIUtil;

/**
 * Reads delivery files from Resultset.
 *
 * @author altnyris
 *
 */
public class DeliveryFilesReader extends ResultSetMixedReader<DeliveryFilesDTO> {

    private HashMap<String, DeliveryFilesDTO> deliveries = new HashMap<String, DeliveryFilesDTO>();

    /**
     * converts Bindingset row to DeliveryFilesDTO.
     *
     * @param bindingSet BindingSet
     * @throws ResultSetReaderException if error in reading
     */
    @Override
    public void readRow(final BindingSet bindingSet) throws ResultSetReaderException {
        if (bindingSet != null && bindingSet.size() > 0) {
            Value deliveryValue = bindingSet.getValue("s");
            String deliveryUri = deliveryValue.stringValue();
            String deliveryTitle = bindingSet.getValue("title").stringValue();

            if (!StringUtils.isBlank(deliveryUri)) {
                DeliveryFilesDTO del = deliveries.get(deliveryUri);
                if (del == null) {
                    del = new DeliveryFilesDTO(deliveryUri);
                }
                del.setTitle(deliveryTitle);

                String fileUri = bindingSet.getValue("o") != null ? bindingSet.getValue("o").stringValue() : null;
                if (!StringUtils.isBlank(fileUri)) {
                    FileDTO file = new FileDTO(fileUri);

                    String title = URIUtil.extractURILabel(fileUri);
                    if (!StringUtils.isBlank(title)) {
                        file.setTitle(title);
                    }
                    String triplesCnt =
                            bindingSet.getValue("triplesCnt") != null ? bindingSet.getValue("triplesCnt").stringValue() : null;
                            if (!StringUtils.isBlank(triplesCnt)) {
                                file.setTriplesCnt(new Integer(triplesCnt).intValue());
                            }
                            List<FileDTO> files = del.getFiles();
                            if (!files.contains(file)) {
                                files.add(file);
                                del.setFiles(files);
                            }
                }
                deliveries.put(deliveryUri, del);
            }
        }
    }

    @Override
    public List<DeliveryFilesDTO> getResultList() {
        return new ArrayList<DeliveryFilesDTO>(deliveries.values());
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.util.sql.SQLResultSetReader#readRow(java.sql.ResultSet)
     */
    @Override
    public void readRow(final ResultSet rs) throws SQLException, ResultSetReaderException {
        // TODO Auto-generated method stub
    }

}
