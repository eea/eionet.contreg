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
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.cr.dao.readers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.Hashes;
import eionet.cr.util.YesNoBoolean;
import eionet.cr.util.export.ExportException;
import eionet.cr.util.export.SubjectExportEvent;

/**
 *
 * @author <a href="mailto:enriko.kasper@tietoenator.com">Enriko Käsper</a>
 *
 */
@SuppressWarnings("rawtypes")
public class SubjectExportReader extends ResultSetExportReader {

    /** */
    protected Logger logger = Logger.getLogger(SubjectExportReader.class);

    /** */
    private SubjectDTO currentSubject = null;
    private String currentPredicate = null;
    private Collection<ObjectDTO> currentObjects = null;

    /**
     *
     * @param exporter
     */
    public SubjectExportReader(SubjectExportEvent exporter) {
        super(exporter);
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.util.sql.ResultSetExportReader#readRow(java.sql.ResultSet)
     */
    @Override
    public void readRow(ResultSet rs) throws SQLException, ResultSetReaderException {

        if (rs.isFirst()) {
            logger.trace("start writing data");
        }
        long subjectHash = rs.getLong("SUBJECT_HASH");
        boolean newSubject = currentSubject == null || subjectHash != currentSubject.getUriHash();

        if (newSubject) {
            if (currentSubject != null) {
                exporter.writeSubjectIntoExporterOutput(currentSubject);
            }
            currentSubject = new SubjectDTO(rs.getString("SUBJECT_URI"), YesNoBoolean.parse(rs.getString("ANON_SUBJ")));
            currentSubject.setUriHash(subjectHash);
        }

        String predicateUri = rs.getString("PREDICATE_URI");
        boolean newPredicate = newSubject || currentPredicate == null || !currentPredicate.equals(predicateUri);
        if (newPredicate) {

            currentPredicate = predicateUri;
            currentObjects = new ArrayList<ObjectDTO>();
            currentSubject.getPredicates().put(predicateUri, currentObjects);
        }

        ObjectDTO object =
                new ObjectDTO(rs.getString("OBJECT"), rs.getString("OBJ_LANG"), YesNoBoolean.parse(rs.getString("LIT_OBJ")),
                        YesNoBoolean.parse(rs.getString("ANON_OBJ")));
        object.setHash(rs.getLong("OBJECT_HASH"));
        // object.setSourceUri(rs.getString("SOURCE_URI"));
        // object.setSourceHash(rs.getLong("SOURCE"));
        // object.setDerivSourceUri(rs.getString("DERIV_SOURCE_URI"));
        // object.setDerivSourceHash(rs.getLong("OBJ_DERIV_SOURCE"));
        // object.setSourceObjectHash(rs.getLong("OBJ_SOURCE_OBJECT"));

        currentObjects.add(object);

        if (rs.isLast()) {
            exporter.writeSubjectIntoExporterOutput(currentSubject);
        }
    }

    @Override
    public void readRow(BindingSet bindingSet) throws ResultSetReaderException {
        Value subjectValue = bindingSet.getValue("s");
        String subjectUri = subjectValue.stringValue();

        boolean isAnonSubject = subjectValue instanceof BNode;
        if (isAnonSubject && blankNodeUriPrefix != null) {
            subjectUri = blankNodeUriPrefix + subjectUri;
        }
        long subjectHash = Hashes.spoHash(subjectUri);

        boolean newSubject = currentSubject == null || subjectHash != currentSubject.getUriHash();
        if (newSubject) {
            if (currentSubject != null) {
                exporter.writeSubjectIntoExporterOutput(currentSubject);
            }
            currentSubject = new SubjectDTO(subjectUri, isAnonSubject);
            currentSubject.setUriHash(subjectHash);
        }

        String predicateUri = bindingSet.getValue("p").stringValue();
        boolean newPredicate = newSubject || currentPredicate == null || !currentPredicate.equals(predicateUri);
        if (newPredicate) {
            currentPredicate = predicateUri;
            currentObjects = new ArrayList<ObjectDTO>();
            currentSubject.getPredicates().put(predicateUri, currentObjects);
        }

        Value objectValue = bindingSet.getValue("o");
        boolean isLiteral = objectValue instanceof Literal;
        String objectLang = isLiteral ? ((Literal) objectValue).getLanguage() : null;

        ObjectDTO object =
                new ObjectDTO(objectValue.stringValue(), objectLang == null ? "" : objectLang, isLiteral,
                        objectValue instanceof BNode);

        object.setHash(Hashes.spoHash(objectValue.stringValue()));

        // String sourceUri = bindingSet.getValue("g").stringValue();
        // long sourceHash = Hashes.spoHash(sourceUri);

        // object.setSourceUri(sourceUri);
        // object.setSourceHash(sourceHash);
        // object.setDerivSourceUri(sourceUri);
        // object.setDerivSourceHash(sourceHash);

        // TODO: what about object's source object
        // object.setSourceObjectHash(rs.getLong("OBJ_SOURCE_OBJECT"));

        currentObjects.add(object);

    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.readers.ResultSetReader#endResultSet()
     */
    @Override
    public void endResultSet() {
        if (currentSubject != null) {
            try {
                exporter.writeSubjectIntoExporterOutput(currentSubject);
            } catch (ExportException e) {
                e.printStackTrace();
            }
        }
    }
}
