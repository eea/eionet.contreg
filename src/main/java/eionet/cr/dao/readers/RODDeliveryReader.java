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
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import org.openrdf.query.BindingSet;

import eionet.cr.common.Predicates;
import eionet.cr.util.Hashes;
import eionet.cr.util.YesNoBoolean;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 * 
 */
@SuppressWarnings("rawtypes")
public class RODDeliveryReader extends ResultSetMixedReader {

    /** */
    private static HashMap<Long, String> predicateUrisByHashes;

    /**
     *
     */
    static {
        String[] ss =
                {Predicates.DC_IDENTIFIER, Predicates.DC_TITLE, Predicates.DC_DATE, Predicates.ROD_PERIOD,
                        Predicates.ROD_LOCALITY_PROPERTY, Predicates.ROD_OBLIGATION_PROPERTY, Predicates.RDF_TYPE};

        predicateUrisByHashes = new HashMap<Long, String>();
        for (int i = 0; i < ss.length; i++) {
            predicateUrisByHashes.put(Long.valueOf(Hashes.spoHash(ss[i])), ss[i]);
        }
    }

    /** */
    private HashMap<Long, Hashtable<String, Vector<String>>> subjectsMap = new HashMap<Long, Hashtable<String, Vector<String>>>();

    public RODDeliveryReader() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.util.sql.ResultSetBaseReader#readRow(java.sql.ResultSet)
     */
    public void readRow(ResultSet rs) throws SQLException, ResultSetReaderException {

        // get (or create and put) the subject's hashtable
        Long subjectHash = Long.valueOf(rs.getLong("SUBJECT_HASH"));
        Hashtable<String, Vector<String>> subjectHashtable = subjectsMap.get(subjectHash);
        if (subjectHashtable == null) {
            subjectHashtable = new Hashtable<String, Vector<String>>();
            subjectsMap.put(subjectHash, subjectHashtable);
        }

        // skip if predicate not required
        String predicateUri = predicateUrisByHashes.get(Long.valueOf(rs.getLong("PREDICATE_HASH")));
        if (predicateUri == null || predicateUri.equals(Predicates.RDF_TYPE)) {
            return;
        }

        // skip literal objects if Predicates.ROD_OBLIGATION_PROPERTY
        boolean isLiteral = YesNoBoolean.parse(rs.getString("LIT_OBJ"));
        if (isLiteral && predicateUri.equals(Predicates.ROD_OBLIGATION_PROPERTY)) {
            return;
        } else if (!isLiteral && !predicateUri.equals(Predicates.ROD_OBLIGATION_PROPERTY)) {
            return;
        }

        // get (or create and put) the predicate's value vector from subject's hashtable
        Vector<String> valueVector = subjectHashtable.get(predicateUri);
        if (valueVector == null) {
            valueVector = new Vector<String>();
            subjectHashtable.put(predicateUri, valueVector);
        }

        // add this object value to the value vector
        valueVector.add(rs.getString("OBJECT"));
    }

    /**
     * 
     * @return
     */
    public Vector<Hashtable<String, Vector<String>>> getResultVector() {

        return new Vector<Hashtable<String, Vector<String>>>(subjectsMap.values());
    }

    /**
     * 
     * @return
     */
    public static Collection<Long> getPredicateHashes() {
        return predicateUrisByHashes.keySet();
    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.util.sesame.SPARQLResultSetReader#readRow(org.openrdf.query.BindingSet)
     */
    @Override
    public void readRow(BindingSet bindingSet) {

        // TODO Auto-generated method stub
    }
}
