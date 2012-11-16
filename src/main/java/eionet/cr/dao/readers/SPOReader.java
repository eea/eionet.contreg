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
 * Risto Alt, Eesti
 */
package eionet.cr.dao.readers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.Hashes;

/**
 *
 * @author altnyris
 *
 */
public class SPOReader extends ResultSetMixedReader<SubjectDTO> {

    /** */
    private SubjectDTO currentSubject = null;
    private String currentPredicate = null;
    private Collection<ObjectDTO> currentObjects = null;

    private List<SubjectDTO> resultlist = null;

    /**
     *
     * @param subjectsMap
     */
    public SPOReader() {
        resultlist = new ArrayList<SubjectDTO>();
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.util.sql.ResultSetBaseReader#readRow(java.sql.ResultSet)
     */
    @Override
    public void readRow(ResultSet rs) throws SQLException, ResultSetReaderException {
    }

    /**
     *
     * @param subjectHash
     * @param subjectDTO
     */
    protected void addNewSubject(SubjectDTO subjectDTO) {
        resultlist.add(currentSubject);
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.util.sesame.SPARQLResultSetReader#readRow(org.openrdf.query.BindingSet)
     */
    @Override
    public void readRow(BindingSet bindingSet) {

        Value subjectValue = bindingSet.getValue("s");
        String subjectUri = subjectValue.stringValue();

        boolean isAnonSubject = subjectValue instanceof BNode;
        if (isAnonSubject && blankNodeUriPrefix != null) {
            subjectUri = blankNodeUriPrefix + subjectUri;
        }
        long subjectHash = Hashes.spoHash(subjectUri);

        boolean newSubject = currentSubject == null || subjectHash != currentSubject.getUriHash();
        if (newSubject) {
            currentSubject = new SubjectDTO(subjectUri, isAnonSubject);
            currentSubject.setUriHash(subjectHash);
            addNewSubject(currentSubject);
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
        URI dataType = isLiteral ? ((Literal) objectValue).getDatatype() : null;

        String strObjectValue = objectValue.stringValue();
        boolean isAnonObject = objectValue instanceof BNode;
        if (!isLiteral && isAnonObject && blankNodeUriPrefix != null) {
            strObjectValue = blankNodeUriPrefix.concat(strObjectValue);
        }

        ObjectDTO object =
                new ObjectDTO(strObjectValue, objectLang == null ? "" : objectLang, isLiteral, objectValue instanceof BNode,
                        dataType);

        object.setHash(Hashes.spoHash(strObjectValue));
        currentObjects.add(object);
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.readers.ResultSetMixedReader#getResultList()
     */
    @Override
    public List<SubjectDTO> getResultList() {
        return resultlist;
    }

}
