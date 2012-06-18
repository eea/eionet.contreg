package eionet.cr.dao.readers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

import eionet.cr.dto.TagDTO;

/**
 * Reads tag names and counts from Resultset.
 * 
 * @author kaido
 * 
 */
public class TagCloudReader extends ResultSetMixedReader<TagDTO> {
    /**
     * Holder of max tag count.
     */
    private int maxTagCount = 0;

    /**
     * private flag to show if max tag count is got.
     */
    private boolean maxTagCountUpdated = false;

    /**
     * converts Bindingset row to TagDTO.
     * 
     * @param bindingSet BindingSet
     * @throws ResultSetReaderException if error in reading
     */
    @Override
    public void readRow(final BindingSet bindingSet) throws ResultSetReaderException {
        if (bindingSet != null && bindingSet.size() > 0) {
            Value count = bindingSet.getValue("c");
            Value object = bindingSet.getValue("o");

            String strObj = "";
            int tagCount = 0;
            if (object != null) {
                strObj = object.stringValue();
            }
            if (count != null) {
                tagCount = Integer.valueOf(count.stringValue());
                // max tag count from 1st row
                if (!maxTagCountUpdated) {
                    maxTagCountUpdated = true;
                    maxTagCount = tagCount;
                }
            }
            resultList.add(new TagDTO(strObj, tagCount, maxTagCount));
        }
    }

    @Override
    public void readRow(final ResultSet rs) throws SQLException, ResultSetReaderException {
        // TODO Auto-generated method stub
    }

}
