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
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        jaanus
 */

package eionet.cr.dao.readers;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import eionet.cr.dao.virtuoso.VirtuosoSearchDAO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.Bindings;
import eionet.cr.util.Hashes;

/**
 * Base class for the readers that read predicate-object pairs of all or specific predicates of a given collection of subjects.
 *
 * @author jaanus
 */
public abstract class SubjectDataReader extends ResultSetMixedReader<SubjectDTO> {

    /** The subjects that should be queried for. */
    protected List<String> subjectUris;

    /** Keys represent hashes of the queried subject URIs, values represent the corresponding {@link SubjectDTO}. */
    protected Map<Long, SubjectDTO> subjectsMap;

    /**
     * Constructs an instance for reading the subjects whose URIs are given by the input list.
     *
     * @param subjectUris The URIs of the subjects to be read.
     */
    public SubjectDataReader(List<String> subjectUris) {

        if (subjectUris == null || subjectUris.isEmpty()) {
            throw new IllegalArgumentException("Subjects must not be null or empty!");
        }

        this.subjectUris = subjectUris;
        subjectsMap = new LinkedHashMap<Long, SubjectDTO>();
        for (String subjectUri : subjectUris) {
            Long subjectHash = Long.valueOf(Hashes.spoHash(subjectUri));
            subjectsMap.put(subjectHash, null);
        }

        this.blankNodeUriPrefix = VirtuosoSearchDAO.BNODE_URI_PREFIX;
    }

    /**
     * Returns the SPARQL query that this reader expects to be executed and the results of which it will read.
     *
     * @param bindings Bindings to be associated with the returned query.
     * @return The query.
     */
    public abstract String getQuery(Bindings bindings);

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.readers.ResultSetMixedReader#getResultList()
     */
    @Override
    public List<SubjectDTO> getResultList() {
        return new LinkedList<SubjectDTO>(subjectsMap.values());
    }

    /**
     * Creates an instance of {@link SubjectDataReader} based on the given inputs.
     *
     * @param subjectUris The list of subjects that will be read by this reader.
     * @param predicateUris The list of predicates whose values the reader will collect.
     * @return A {@link SubjectDataReader} as described above.
     */
    public static SubjectDataReader getInstance(List<String> subjectUris, String[] predicateUris) {

        if (predicateUris == null || predicateUris.length == 0) {
            return new GenericSubjectDataReader(subjectUris);
        } else {
            return new SpecificSubjectDataReader(subjectUris, predicateUris);
        }
    }
}
