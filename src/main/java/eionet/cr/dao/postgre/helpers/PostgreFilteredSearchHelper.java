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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import eionet.cr.common.CRRuntimeException;
import eionet.cr.dao.helpers.AbstractSearchHelper;
import eionet.cr.util.Hashes;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.URIUtil;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.web.util.columns.SubjectLastModifiedColumn;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class PostgreFilteredSearchHelper extends AbstractSearchHelper {

    /** */
    private Map<String, String> filters;
    private Set<String> literalPredicates;
    private boolean useCache = false;
    private Boolean requiresFullTextSearch = null;

    /**
     *
     * @param pagingRequest
     * @param sortingRequest
     */
    public PostgreFilteredSearchHelper(Map<String, String> filters, Set<String> literalPredicates,
            PagingRequest pagingRequest, SortingRequest sortingRequest) {

        super(pagingRequest, sortingRequest);

        // check the validity of filters
        if (filters == null || filters.isEmpty())
            throw new CRRuntimeException("The map of filters must not be null or empty!");
        else {
            boolean atLeastOneValidEntry = false;
            for (Map.Entry<String,String> entry : filters.entrySet()) {
                if (!StringUtils.isBlank(entry.getKey()) && !StringUtils.isBlank(entry.getValue())) {
                    atLeastOneValidEntry = true;
                    break;
                }
            }
            if (atLeastOneValidEntry == false) {
                throw new CRRuntimeException("The map of filters must contain at least one enrty" +
                        " where key and value are not blank!");
            }
        }

        this.filters = filters;
        this.literalPredicates = literalPredicates;
    }

    /* (non-Javadoc)
     * @see eionet.cr.dao.postgre.helpers.AbstractSearchHelper#getOrderedQuery(java.util.List)
     */
    @Override
    protected String getOrderedQuery(List<Object> inParams) {

        // start the query with the selection of fields and the join to ordering table

        StringBuffer subSelect = new StringBuffer().
        append("select distinct SPO1.SUBJECT as SUBJECT_HASH,");

        if (sortPredicate.equals(SubjectLastModifiedColumn.class.getSimpleName())) {
            subSelect.append(" RESOURCE.LASTMODIFIED_TIME as OBJECT_ORDERED_BY from " + getSpoTableName()+ " as SPO1").
            append(" left join RESOURCE on (SPO1.SUBJECT=RESOURCE.URI_HASH)");
        } else {
            subSelect.append(" ORDERING.OBJECT as OBJECT_ORDERED_BY from " + getSpoTableName()+ " as SPO1").
            append(" left join SPO as ORDERING").
            append(" on (SPO1.SUBJECT=ORDERING.SUBJECT and ORDERING.PREDICATE=").
            append(Hashes.spoHash(sortPredicate)).append(")");
        }

        // build the "where" part into a separate buffer
        // validations in constructor ensure that there is at least one valid where-filter

        int index = getSpoTableIndex();
        StringBuffer whereBuf = new StringBuffer(getSpoTableCriteria());
        for (Entry<String,String> entry : filters.entrySet()) {

            String predicateUri = entry.getKey();
            String objectValue = entry.getValue();

            if (!StringUtils.isBlank(predicateUri) && !StringUtils.isBlank(objectValue)) {

                String spoAlias = "SPO" + index;
                whereBuf.append(whereBuf.length() > 0 ? " and " : "").

                append(spoAlias).append(".PREDICATE=").append(Hashes.spoHash(predicateUri)).
                append(" and ");

                if (!requireFullTextSearch(predicateUri, objectValue)) {
                    whereBuf.append(spoAlias).append(".OBJECT_HASH=").
                    append(Hashes.spoHash(StringUtils.strip(objectValue, "\"")));
                } else {
                    whereBuf.append("to_tsvector('simple', ").append(spoAlias).
                    append(".OBJECT) @@ to_tsquery('simple', ?)").
                    append(" and ").append(spoAlias).append(".LIT_OBJ='Y'");
                    inParams.add(objectValue);

                    requiresFullTextSearch = Boolean.TRUE;
                }
            }

            index++;
        }

        // continue the query by extending the "from" part with inner joins
        // to as many aliases as where put into the above-created "where" part
        // (SPO1 is already present in the form part)
        for (int i=2 ; i<index; i++) {
            subSelect.
            append(" inner join SPO as SPO").append(i).
            append(" on SPO1.SUBJECT = SPO").append(i).append(".SUBJECT ");
        }

        // finish the query by adding the above-created "where" part
        if (whereBuf.length() > 0) {
            subSelect.append(" where ").append(whereBuf);
        }

        // the final query selects all from the above-created sub-select
        StringBuffer buf = new StringBuffer().
        append("select * from (").append(subSelect).append(") as FOO order by OBJECT_ORDERED_BY");
        return buf.toString();
    }

    /* (non-Javadoc)
     * @see eionet.cr.dao.postgre.helpers.AbstractSearchHelper#getUnorderedQuery(java.util.List)
     */
    @Override
    public String getUnorderedQuery(List<Object> inParams) {
        return getUnorderedQuery(inParams, "select distinct SPO1.SUBJECT as SUBJECT_HASH from " + getSpoTableName()+ " as SPO1 ");
    }
    protected String getUnorderedQuery(List<Object> inParams, String selectPart) {
        // start the query with the selection of fields and the join to ordering table

        StringBuffer query = new StringBuffer().
        append(selectPart);

        // build the "where" part into a separate buffer
        // validations in constructor ensure that there is at least one valid where-filter

        int index = getSpoTableIndex();
        StringBuffer whereBuf = new StringBuffer(getSpoTableCriteria());
        for (Entry<String,String> entry : filters.entrySet()) {

            String predicateUri = entry.getKey();
            String objectValue = entry.getValue();

            if (!StringUtils.isBlank(predicateUri) && !StringUtils.isBlank(objectValue)) {

                String spoAlias = "SPO" + index;
                whereBuf.append(whereBuf.length() > 0 ? " and " : "").
                append(spoAlias).append(".PREDICATE=").append(Hashes.spoHash(predicateUri)).
                append(" and ");

                if (!requireFullTextSearch(predicateUri, objectValue)) {

                    whereBuf.append(spoAlias).append(".OBJECT_HASH=").
                    append(Hashes.spoHash(StringUtils.strip(objectValue, "\"")));
                } else {
                    whereBuf.append("to_tsvector('simple', ").append(spoAlias).
                    append(".OBJECT) @@ to_tsquery('simple', ?)").
                    append(" and ").append(spoAlias).append(".LIT_OBJ='Y'");
                    inParams.add(objectValue);

                    requiresFullTextSearch = Boolean.TRUE;
                }
            }

            index++;
        }

        // continue the query by extending the "from" part with inner joins
        // to as many aliases as where put into the above-created "where" part
        // (SPO1 is already present in the form part)
        for (int i=2; i<index; i++) {
            query.
            append(" inner join SPO as SPO").append(i).
            append(" on SPO1.SUBJECT = SPO").append(i).append(".SUBJECT ");
        }

        // finish the query by adding the above-created "where" part
        if (whereBuf.length() > 0) {
            query.append(" where ").append(whereBuf);
        }

        // return the query
        return query.toString();
    }

    /* (non-Javadoc)
     * @see eionet.cr.dao.postgre.helpers.AbstractSearchHelper#getCountQuery(java.util.List)
     */
    @Override
    public String getCountQuery(List<Object> inParams) {

        String query = getUnorderedQuery(inParams);
        return new StringBuffer(
                "select count(*) from (").append(query).append(") as FOO").toString();
    }

    @Override
    public String getMinMaxHashQuery(List<Object> inParams) {
        String query = getUnorderedQuery(inParams, "select min(SPO1.SUBJECT) as LCOL, max(SPO1.SUBJECT) as RCOL from " + getSpoTableName()+ " as SPO1 ");
        return query;
    }

    /**
     *
     * @param s
     * @return
     */
    private boolean isSurroundedWithQuotes(String s) {
        return s.startsWith("\"") && s.endsWith("\"");
    }

    /**
     *
     * @param s
     * @return
     */
    private boolean isLiteralPredicate(String s) {
        return literalPredicates != null && literalPredicates.contains(s);
    }

    /**
     *
     * @return
     */
    public boolean isUseCache() {
        return useCache;
    }

    /**
     *
     * @param useCache
     */
    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }

    /**
     *
     * @return
     */
    protected String getSpoTableName() {
        return "SPO";
    }

    /**
     *
     * @return
     */
    protected String getSpoTableCriteria() {
        return "";
    }

    /**
     *
     * @param key
     * @param value
     */
    protected void addFilter(String key, String value) {
        if (this.filters == null) {
            this.filters = new HashMap<String, String>();
        }
        filters.put(key, value);
    }

    /**
     *
     * @param key
     */
    protected void removeFilter(String key) {
        if (this.filters != null && filters.containsKey(key)) {
            filters.remove(key);
        }
    }

    /**
     *
     * @return
     */
    protected int getSpoTableIndex() {
        return 1;
    }

    /**
     *
     * @param predicateUri
     * @param objectValue
     * @return
     */
    private boolean requireFullTextSearch(String predicateUri, String objectValue) {

        return (isSurroundedWithQuotes(objectValue)
            || URIUtil.isSchemedURI(objectValue)
            || !isLiteralPredicate(predicateUri)) == false;
    }

    /**
     *
     * @return
     */
    public boolean requiresFullTextSearch() {

        if (requiresFullTextSearch == null) {

            requiresFullTextSearch = Boolean.FALSE;
            for (Entry<String,String> entry : filters.entrySet()) {

                String predicateUri = entry.getKey();
                String objectValue = entry.getValue();

                if (requireFullTextSearch(predicateUri, objectValue)) {
                    requiresFullTextSearch = Boolean.TRUE;
                    break;
                }
            }
        }

        return requiresFullTextSearch;
    }
}
