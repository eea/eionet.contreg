/*
 * The contents of this file are subject to the Mozilla Public
 *
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency. All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti*/
package eionet.cr.dao.readers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

import eionet.cr.common.Predicates;
import eionet.cr.dto.UploadDTO;
import eionet.cr.util.Hashes;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class UploadDTOReader extends ResultSetMixedReader<UploadDTO> {

    /** */
    private static final Logger LOGGER = Logger.getLogger(UploadDTOReader.class);

    /** */
    private static final long LABEL_HASH = Hashes.spoHash(Predicates.RDFS_LABEL);
    private static final long LAST_MODIFIED_HASH = Hashes.spoHash(Predicates.CR_LAST_MODIFIED);
    private static final long DC_TITLE_HASH = Hashes.spoHash(Predicates.DC_TITLE);

    /** */
    private LinkedHashMap<String, UploadDTO> uploadsMap = new LinkedHashMap<String, UploadDTO>();

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.util.sql.SQLResultSetReader#readRow(java.sql.ResultSet)
     */
    public void readRow(ResultSet rs) throws SQLException, ResultSetReaderException {

        String subjectUri = rs.getString("URI");
        UploadDTO uploadDTO = uploadsMap.get(subjectUri);
        if (uploadDTO == null) {
            uploadDTO = new UploadDTO(subjectUri);
            uploadsMap.put(subjectUri, uploadDTO);
        }

        long predicateHash = rs.getLong("PREDICATE");
        String objectValue = rs.getString("OBJECT");

        if (predicateHash == LABEL_HASH) {
            uploadDTO.setLabel(objectValue);
        } else if (predicateHash == LAST_MODIFIED_HASH) {
            uploadDTO.setDateModified(objectValue);
        } else if (predicateHash == DC_TITLE_HASH) {

            // label not yet set, prefer dc:title as the label
            if (StringUtils.isBlank(uploadDTO.getLabel())) {
                uploadDTO.setLabel(objectValue);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.readers.ResultSetMixedReader#getResultList()
     */
    public List<UploadDTO> getResultList() {
        return new ArrayList<UploadDTO>(uploadsMap.values());
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.util.sesame.SPARQLResultSetReader#readRow(org.openrdf.query.BindingSet)
     */
    @Override
    public void readRow(BindingSet bindingSet) throws ResultSetReaderException {

        Value subjectValue = bindingSet.getValue("s");
        String subjectUri = subjectValue.stringValue();
        Value predicateValue = bindingSet.getValue("p");
        String predicateUri = predicateValue.stringValue();
        Value objectValue = bindingSet.getValue("o");
        String objectString = objectValue.stringValue();

        UploadDTO uploadDTO = uploadsMap.get(subjectUri);
        if (uploadDTO == null) {
            uploadDTO = new UploadDTO(subjectUri);
            uploadsMap.put(subjectUri, uploadDTO);
        }

        if (predicateUri.equals(Predicates.RDFS_LABEL)) {
            uploadDTO.setLabel(objectString);

        } else if (predicateUri.equals(Predicates.CR_LAST_MODIFIED)) {
            uploadDTO.setDateModified(objectString);

        } else if (predicateUri.equals(Predicates.DC_TITLE)) {

            // label not yet set, prefer dc:title as the label
            if (StringUtils.isBlank(uploadDTO.getLabel())) {
                uploadDTO.setLabel(objectString);
            }
        }
    }
}
