/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Jaanus Heinlaid
 */

package eionet.cr.dao.readers;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.BindingSet;

import eionet.cr.common.CRRuntimeException;
import eionet.cr.dto.FactsheetDTO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.util.URIUtil;
import eionet.cr.util.sesame.SPARQLResultSetBaseReader;
import eionet.cr.web.util.WebConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jaanus Heinlaid
 */
public class FactsheetReader extends SPARQLResultSetBaseReader<FactsheetDTO> {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(FactsheetReader.class);

    /** */
    public static final String OBJECT_DATA_SPLITTER = "<|>";

    /** */
    private FactsheetDTO factsheetDTO;

    /** */
    private String subjectUri;

    /**
     * @param subjectUri
     */
    public FactsheetReader(String subjectUri) {

        if (StringUtils.isBlank(subjectUri)) {
            throw new IllegalArgumentException("Subject URI must not be blank!");
        }
        this.subjectUri = subjectUri;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.util.sesame.SPARQLResultSetReader#readRow(org.openrdf.query.BindingSet)
     */
    @Override
    public void readRow(BindingSet bindingSet) throws ResultSetReaderException {

        if (factsheetDTO == null) {
            factsheetDTO = new FactsheetDTO(subjectUri, Integer.parseInt(getStringValue(bindingSet, "anonSubj")) > 0);
        }

        String predicateUri = getStringValue(bindingSet, "pred");
        factsheetDTO.setObjectCount(predicateUri, Integer.parseInt(getStringValue(bindingSet, "objCount")));

        ObjectDTO objectDTO = parseObjectData(getStringValue(bindingSet, "objData"), predicateUri);
        if (objectDTO != null) {
            factsheetDTO.addObject(predicateUri, objectDTO);
        }
    }

    /**
     *
     * @param objectData
     * @return
     */
    private ObjectDTO parseObjectData(String objectData, String predUri) {

        if (StringUtils.isBlank(objectData)) {
            return null;
        }

        String[] split = StringUtils.splitByWholeSeparatorPreserveAllTokens(objectData, OBJECT_DATA_SPLITTER);
        if (split == null || split.length > 8) {
            throw new CRRuntimeException("Was expecting a length <=8 for the split array");
        }

        String label = StringUtils.isBlank(split[0]) ? "" : split[0].trim();
        String language = StringUtils.isBlank(split[1]) ? null : split[1].trim();
        URI datatype = StringUtils.isBlank(split[2]) ? null : new URIImpl(split[2].trim());
        String objectUri = StringUtils.isBlank(split[3]) ? null : split[3].trim();

        boolean isLiteral = objectUri == null;
        boolean isAnonymous =
                StringUtils.isBlank(split[4]) ? false : split[4].trim().equals("1") || split[4].trim().equals("true");
        String graphUri = StringUtils.isBlank(split[5]) ? null : split[5].trim();
        int objectLength = StringUtils.isBlank(split[6]) ? 0 : Integer.parseInt(split[6].trim());
        String objectMD5 = StringUtils.isBlank(split[7]) ? "" : split[7].trim();

        ObjectDTO objectDTO = null;
        if (isLiteral) {
            objectDTO = new ObjectDTO(label, language, true, false, datatype);
        } else {

            // Virtuoso is unstable about to returning URIs of anonymous resources, so make sure it's properly sanitized.
            if (isAnonymous) {
                objectUri = URIUtil.sanitizeVirtuosoBNodeUri(objectUri);
            }

            objectDTO = new ObjectDTO(objectUri, null, false, isAnonymous);
            if (!StringUtils.isBlank(label) && !label.equals(objectUri)) {
                ObjectDTO labelObjectDTO = new ObjectDTO(label, language, true, false, datatype);
                objectDTO.setLabelObject(labelObjectDTO);
            }
        }
        objectDTO.setSourceUri(graphUri);

        // If literal object and its length in the database is actually bigger than the length
        // of the value we retrieved (because the query asks only for the N first characters),
        // then set the object's database-calculated MD5 hash, so that we can later retrieve the
        // full object value on the factsheet page. As a double measure, make also sure that the
        // database-calculated hash differs indeed from the Java-calculated hash of the first N
        // characters that we got here.
        if (isLiteral) {
            String value = objectDTO.getValue();
            if (objectLength > WebConstants.MAX_OBJECT_LENGTH && !DigestUtils.md5Hex(value).equalsIgnoreCase(objectMD5)) {
                objectDTO.setObjectMD5(objectMD5);
                LOGGER.trace("Object's database-calculated length is " + objectLength);
            }
        }

        return objectDTO;
    }

    /**
     * @return the factsheetDTO
     */
    public FactsheetDTO getFactsheetDTO() {
        return factsheetDTO;
    }
}
