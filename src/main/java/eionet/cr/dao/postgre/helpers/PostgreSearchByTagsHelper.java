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
* Enriko Käsper, Tieto Estonia*/
package eionet.cr.dao.postgre.helpers;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import eionet.cr.common.Predicates;
import eionet.cr.dao.helpers.AbstractSearchHelper;
import eionet.cr.util.Hashes;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.pagination.PagingRequest;

/**
 *
 * @author <a href="mailto:enriko.kasper@tieto.com">Enriko Käsper</a>
 *
 */
public class PostgreSearchByTagsHelper extends AbstractSearchHelper {

    private List<String> tags;

    /**
     *
     * @param pagingRequest
     * @param sortingRequest
     */
    public PostgreSearchByTagsHelper(List<String> tags,
            PagingRequest pagingRequest, SortingRequest sortingRequest) {

        super(pagingRequest, sortingRequest);

        this.tags = tags;
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.postgre.helpers.AbstractSearchHelper#getUnorderedQuery(java.util.List)
     */
    public String getUnorderedQuery(List<Object> inParams) {

        StringBuffer buf = new StringBuffer().
        append("select distinct SPO1.SUBJECT as SUBJECT_HASH from SPO as SPO1 ");

        int index=2;
        StringBuffer whereBuffer = new StringBuffer();
        for (String tag : tags) {
            String spoAlias = "SPO" + index;
            whereBuffer.append(whereBuffer.length() > 0 ? " and " : "");

            whereBuffer.append(spoAlias).append(".PREDICATE=").append(Hashes.spoHash(Predicates.CR_TAG)).
            append(" AND ").append(spoAlias).append(".OBJECT_HASH=");
            whereBuffer.append(Hashes.spoHash(StringUtils.strip(tag, "\"")));

            index++;
        }
        // continue the query by extending the "from" part with inner joins
        // to as many aliases as where put into the above-created "where" part
        // (SPO1 is already present in the form part)
        for (int i=2 ; i<index; i++) {
            buf.
            append(" inner join SPO as SPO").append(i).
            append(" on SPO1.SUBJECT = SPO").append(i).append(".SUBJECT ");
        }

        // finish the query by adding the above-created "where" part
        if (whereBuffer.length() > 0) {
            buf.append(" where ").append(whereBuffer);
        }

        return buf.toString();
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.postgre.helpers.AbstractSearchHelper#getOrderedQuery(java.util.List)
     */
    protected String getOrderedQuery(List<Object> inParams) {

        StringBuffer subSelect = new StringBuffer().
        append("select distinct SPO1.SUBJECT as SUBJECT_HASH,").
        append(" ORDERING.OBJECT as OBJECT_ORDERED_BY from SPO as SPO1");

        subSelect.append(" left join SPO as ORDERING on ").
        append("(SPO1.SUBJECT=ORDERING.SUBJECT and ORDERING.PREDICATE=").
        append(Hashes.spoHash(sortPredicate)).append(")");

        int index=2;
        StringBuffer whereBuffer = new StringBuffer();
        for (String tag : tags) {
            String spoAlias = "SPO" + index;
            whereBuffer.append(whereBuffer.length() > 0 ? " and " : "");

            whereBuffer.append(spoAlias).append(".PREDICATE=").append(Hashes.spoHash(Predicates.CR_TAG)).
            append(" AND ").append(spoAlias).append(".OBJECT_HASH=");
            whereBuffer.append(Hashes.spoHash(StringUtils.strip(tag, "\"")));

            index++;
        }
        // continue the query by extending the "from" part with inner joins
        // to as many aliases as where put into the above-created "where" part
        // (SPO1 is already present in the form part)
        for (int i=2 ; i<index; i++) {
            subSelect.append(" inner join SPO as SPO").append(i).
            append(" on SPO1.SUBJECT = SPO").append(i).append(".SUBJECT ");
        }

        // finish the query by adding the above-created "where" part
        if (whereBuffer.length() > 0) {
            subSelect.append(" where ").append(whereBuffer);
        }


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
