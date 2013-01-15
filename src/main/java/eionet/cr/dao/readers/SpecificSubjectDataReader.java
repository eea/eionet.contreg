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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

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
import eionet.cr.util.sesame.SPARQLQueryUtil;
import eionet.cr.web.util.WebConstants;

/**
 * Implementation of {@link SubjectDataReader} that reads specific predicates given through the constructor(s).
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 */
public class SpecificSubjectDataReader extends SubjectDataReader {

    /** The current subject. i.e. the result set is expected to be ordered by subject URI. */
    private SubjectDTO currentSubject = null;

    /** The URIs of predicates to be queried for. */
    private String[] selectedPredicates;

    /** The current subject's predicate-object pairs. Keys are predicate URIs, values are sets of the predicate's values. */
    private HashMap<String, HashSet<Value>> currentSubjectPredicateValues = new HashMap<String, HashSet<Value>>();

    /**
     * Calls {@link SubjectDataReader#SubjectDataReader(List)} first, then sets the given predicates to be queried for.. Class
     * constructor.
     *
     * @param subjectUris
     * @param selectedPredicates
     */
    public SpecificSubjectDataReader(List<String> subjectUris, String[] selectedPredicates) {

        super(subjectUris);
        setSelectedPredicates(selectedPredicates);
    }

    /**
     * Convenience method for setting the selected predicates and ensuring they're not null nor empty.
     *
     * @param selectedPredicates The URIs of predicates to set.
     */
    private void setSelectedPredicates(String[] selectedPredicates) {

        if (selectedPredicates == null || selectedPredicates.length == 0) {
            throw new IllegalArgumentException("Selected predicates must not be null nor empty!");
        }
        this.selectedPredicates = selectedPredicates;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.util.sql.SQLResultSetReader#readRow(java.sql.ResultSet)
     */
    @Override
    public void readRow(ResultSet rs) throws SQLException, ResultSetReaderException {
        throw new UnsupportedOperationException("Method not implemented");
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

            if (currentSubject != null) {
                endOfSubject();
            }
            currentSubject = new SubjectDTO(subjectUri, isAnonSubject);
            currentSubject.setUriHash(subjectHash);
            subjectsMap.put(Long.valueOf(subjectHash), currentSubject);
        }

        for (int i = 0; i < selectedPredicates.length; i++) {

            String predicateUri = selectedPredicates[i];

            Value value = bindingSet.getValue("val" + i);
            if (value != null) {
                HashSet<Value> valueSet = currentSubjectPredicateValues.get(predicateUri);
                if (valueSet == null) {
                    valueSet = new HashSet<Value>();
                    currentSubjectPredicateValues.put(predicateUri, valueSet);
                }
                valueSet.add(value);
            }
        }
    }

    /**
     * Utility method called upon end of a subject in the result set. Collects the subject's predicate-value pairs and constructs
     * and places them into the subject's {@link SubjectDTO}.
     */
    private void endOfSubject() {

        for (Entry<String, HashSet<Value>> entry : currentSubjectPredicateValues.entrySet()) {

            String predicateUri = entry.getKey();
            HashSet<Value> values = entry.getValue();
            ArrayList<ObjectDTO> objects = new ArrayList<ObjectDTO>();
            for (Value value : values) {

                boolean isLiteral = value instanceof Literal;
                String objectLang = isLiteral ? ((Literal) value).getLanguage() : null;
                URI dataType = isLiteral ? ((Literal) value).getDatatype() : null;

                String strObjectValue = value.stringValue();
                boolean isAnonObject = value instanceof BNode;
                if (!isLiteral && isAnonObject && blankNodeUriPrefix != null) {
                    strObjectValue = blankNodeUriPrefix.concat(strObjectValue);
                }

                ObjectDTO object =
                        new ObjectDTO(strObjectValue, objectLang == null ? "" : objectLang, isLiteral, isAnonObject, dataType);
                object.setHash(Hashes.spoHash(strObjectValue));
                objects.add(object);
            }

            if (objects != null && !objects.isEmpty()) {
                currentSubject.getPredicates().put(predicateUri, objects);
            }
        }

        currentSubjectPredicateValues = new HashMap<String, HashSet<Value>>();
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.readers.SubjectDataReader#getQuery(java.util.Collection, eionet.cr.util.Bindings)
     */
    @Override
    public String getQuery(Bindings bindings) {

        StringBuilder qry = new StringBuilder();
        qry.append("select distinct ?s");

        String predSelector = " bif:either(isLiteral(?o), bif:substring(str(?o), 1, MAX), ?o) as ?val";
        for (int i = 0; i < selectedPredicates.length; i++) {
            String index = String.valueOf(i);
            String s = StringUtils.replace(predSelector, "?o", "?o" + index);
            s = StringUtils.replace(s, "?val", "?val" + index);
            s = StringUtils.replace(s, "MAX", "" + WebConstants.MAX_OBJECT_LENGTH);
            qry.append(s);
        }

        qry.append(" where {?s ?p ?o.");
        for (int i = 0; i < selectedPredicates.length; i++) {
            String bindingName = "p" + i;
            qry.append(" optional {?s ?").append(bindingName).append(" ?o").append(i).append("}");
            bindings.setURI(bindingName, selectedPredicates[i]);
        }

        String commaSeparatedSubjects = SPARQLQueryUtil.urisToCSV(subjectUris, "subjectValue", bindings);
        qry.append(" filter (?s IN (").append(commaSeparatedSubjects).append(")) } order by ?s");

        return qry.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.readers.ResultSetMixedReader#endResultSet()
     */
    @Override
    public void endResultSet() {
        endOfSubject();
    }
}
