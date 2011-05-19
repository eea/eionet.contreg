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
package eionet.cr.dao.postgre.helpers;

import java.util.List;

import eionet.cr.dao.helpers.AbstractSearchHelper;
import eionet.cr.util.Hashes;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.pagination.PagingRequest;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class PostgreSearchBySourceHelper extends AbstractSearchHelper {

    private String sourceUrl;
    private long sourceHash;

    /**
     *
     * @param pagingRequest
     * @param sortingRequest
     */
    public PostgreSearchBySourceHelper(String sourceUrl,
            PagingRequest pagingRequest, SortingRequest sortingRequest) {

        super(pagingRequest, sortingRequest);

        this.sourceUrl = sourceUrl;
        this.sourceHash = Hashes.spoHash(sourceUrl);
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.postgre.helpers.AbstractSearchHelper#getUnorderedQuery(java.util.List)
     */
    public String getUnorderedQuery(List<Object> inParams) {

        StringBuffer buf = new StringBuffer().
        append("select distinct SPO.SUBJECT as SUBJECT_HASH from SPO").
        append(" where SOURCE=").append(sourceHash);

        return buf.toString();
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.postgre.helpers.AbstractSearchHelper#getOrderedQuery(java.util.List)
     */
    protected String getOrderedQuery(List<Object> inParams) {

        StringBuffer subSelect = new StringBuffer().
        append("select distinct SPO.SUBJECT as SUBJECT_HASH,").
        append(" ORDERING.OBJECT as OBJECT_ORDERED_BY from SPO");

        subSelect.append(" left join SPO as ORDERING on ").
        append("(SPO.SUBJECT=ORDERING.SUBJECT and ORDERING.PREDICATE=").
        append(Hashes.spoHash(sortPredicate)).append(")");

        subSelect.append(" where SPO.SOURCE=").append(sourceHash);

        StringBuffer buf = new StringBuffer().
        append("select * from (").append(subSelect).append(") as FOO order by OBJECT_ORDERED_BY");

        return buf.toString();

    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.postgre.helpers.AbstractSearchHelper#getCountQuery(java.util.List)
     */
    public String getCountQuery(List<Object> inParams) {

        String query = getUnorderedQuery(inParams);
        return new StringBuffer(
                "select count(*) from (").append(query).append(") as FOO").toString();
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.postgre.helpers.AbstractSearchHelper#getMinMaxHashQuery(java.util.List)
     */
    public String getMinMaxHashQuery(List<Object> inParams) {
        throw new UnsupportedOperationException("Method not implemented");
    }
}

