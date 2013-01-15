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
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.Bindings;
import eionet.cr.util.Hashes;
import eionet.cr.util.YesNoBoolean;
import eionet.cr.util.sesame.SPARQLQueryUtil;
import eionet.cr.web.util.WebConstants;

/**
 * Implementation of {@link SubjectDataReader} that reads <b>all</b> predicates of the queried subjects.
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class GenericSubjectDataReader extends SubjectDataReader {

    /** The current subject. i.e. the result set is expected to be ordered by subject URI and then predicate URI. */
    private SubjectDTO currentSubject = null;

    /** URI of the current predicate. i.e. the result set is expected to be ordered by subject URI and then predicate URI. */
    private String currentPredicate = null;

    /** The current subject's current predicate's objects. */
    private Collection<ObjectDTO> currentObjects = null;

    /**
     * Constructs by calling {@link SubjectDataReader#SubjectDataReader(List)}.
     *
     * @param subjectUris See above comment.
     */
    public GenericSubjectDataReader(List<String> subjectUris) {
        super(subjectUris);
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.util.sql.SQLResultSetReader#readRow(java.sql.ResultSet)
     */
    @Override
    public void readRow(ResultSet rs) throws SQLException, ResultSetReaderException {

        long subjectHash = rs.getLong("SUBJECT_HASH");
        boolean newSubject = currentSubject == null || subjectHash != currentSubject.getUriHash();
        if (newSubject) {
            currentSubject = new SubjectDTO(rs.getString("SUBJECT_URI"), YesNoBoolean.parse(rs.getString("ANON_SUBJ")));
            currentSubject.setUriHash(subjectHash);
            currentSubject.setLastModifiedDate(new Date(rs.getLong("SUBJECT_MODIFIED")));
            addNewSubject(subjectHash, currentSubject);
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
        object.setSourceUri(rs.getString("SOURCE_URI"));
        object.setSourceHash(rs.getLong("SOURCE"));
        object.setDerivSourceUri(rs.getString("DERIV_SOURCE_URI"));
        object.setDerivSourceHash(rs.getLong("OBJ_DERIV_SOURCE"));
        object.setSourceObjectHash(rs.getLong("OBJ_SOURCE_OBJECT"));

        currentObjects.add(object);
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
            addNewSubject(subjectHash, currentSubject);
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

        Value graphValue = bindingSet.getValue("g");
        if (graphValue != null) {
            String sourceUri = graphValue.stringValue();
            if (StringUtils.isNotEmpty(sourceUri)) {

                object.setSourceUri(sourceUri);
                object.setDerivSourceUri(sourceUri);

                long sourceHash = Hashes.spoHash(sourceUri);
                object.setSourceHash(sourceHash);
                object.setDerivSourceHash(sourceHash);
            }
        }

        // TODO: what about object's source object
        // object.setSourceObjectHash(rs.getLong("OBJ_SOURCE_OBJECT"));

        currentObjects.add(object);
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.readers.SubjectDataReader#getQuery(eionet.cr.util.Bindings)
     */
    @Override
    public String getQuery(Bindings bindings) {

        String commaSeparatedSubjects = SPARQLQueryUtil.urisToCSV(subjectUris, "subjectValue", bindings);

        // For very long literals, only first 2000 characters will be returned,
        // to prevent VirtuosoException: SR319: Max row length * exceeded exception
        String query =
                "select ?s ?p bif:either(isLiteral(?obj), bif:substring(str(?obj), 1, " + WebConstants.MAX_OBJECT_LENGTH
                + "), ?obj) as ?o where {?s ?p ?obj. filter (?s IN (" + commaSeparatedSubjects + ")) ";

        query += "} ORDER BY ?s ?p";
        return query;
    }

    /**
     * Utility method called upon getting to a new subject in the result list (ordered by subject and predicate).
     *
     * @param subjectHash
     * @param subjectDTO
     */
    private void addNewSubject(long subjectHash, SubjectDTO subjectDTO) {
        subjectsMap.put(Long.valueOf(subjectHash), currentSubject);
    }

}
