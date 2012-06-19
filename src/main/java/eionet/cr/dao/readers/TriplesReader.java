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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

import eionet.cr.dto.TripleDTO;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class TriplesReader extends ResultSetMixedReader<TripleDTO> {

    /** */
    private List<TripleDTO> resultList = new LinkedList<TripleDTO>();

    /** */
    private HashSet<Long> distinctHashes = new HashSet<Long>();

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.util.sql.ResultSetListReader#getResultList()
     */
    public List<TripleDTO> getResultList() {
        return resultList;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.util.sql.ResultSetBaseReader#readRow(java.sql.ResultSet)
     */
    public void readRow(ResultSet rs) throws SQLException, ResultSetReaderException {

        TripleDTO dto = new TripleDTO(rs.getLong("SUBJECT"), rs.getLong("PREDICATE"), rs.getString("OBJECT"));
        dto.setObjectDerivSourceHash(rs.getLong("OBJ_DERIV_SOURCE"));

        resultList.add(dto);

        // remember distinct hashes encountered

        distinctHashes.add(Long.valueOf(dto.getSubjectHash()));
        distinctHashes.add(Long.valueOf(dto.getPredicateHash()));
        distinctHashes.add(Long.valueOf(dto.getObjectDerivSourceHash()));
    }

    /**
     * @return the distinctHashes
     */
    public HashSet<Long> getDistinctHashes() {
        return distinctHashes;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.util.sesame.SPARQLResultSetReader#readRow(org.openrdf.query .BindingSet)
     */
    @Override
    public void readRow(BindingSet bindingSet) {
        if (bindingSet != null && bindingSet.size() > 0) {
            Value subject = bindingSet.getValue("s");
            Value predicate = bindingSet.getValue("p");
            Value object = bindingSet.getValue("o");
            String strSubj = "", strPred = "", strObj = "";
            if (subject != null) {
                strSubj = subject.stringValue();
            }
            if (predicate != null) {
                strPred = predicate.stringValue();
            }
            if (object != null) {
                strObj = object.stringValue();
            }
            resultList.add(new TripleDTO(strSubj, strPred, strObj));
        }

    }
}
