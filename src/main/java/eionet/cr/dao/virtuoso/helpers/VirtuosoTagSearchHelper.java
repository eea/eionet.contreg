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
 *        Juhan Voolaid
 */

package eionet.cr.dao.virtuoso.helpers;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import eionet.cr.common.Predicates;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.util.sesame.SPARQLQueryUtil;

/**
 * Search helper for tagged data search.
 *
 * @author Juhan Voolaid
 */
public class VirtuosoTagSearchHelper extends VirtuosoFilteredSearchHelper {

    /** Tags to search by. */
    private List<String> tags;

    public VirtuosoTagSearchHelper(Map<String, String> filters, Set<String> literalRangeFilters, PagingRequest pagingRequest,
            SortingRequest sortingRequest, boolean useInferencing) {
        super(filters, literalRangeFilters, pagingRequest, sortingRequest, useInferencing);
    }

    /**
     * Returns sorted query for tagged data search.
     *
     * @param inParams
     * @return
     */
    @Override
    protected String getOrderedQuery(List<Object> inParams) {

        StringBuilder strBuilder = initQueryStringBuilder();
        strBuilder.append("select distinct ?s where {").append(getWhereContents()).append("} order by ");

        // In SPARQL the sort order is placed before the sort predicate
        if (sortOrder != null) {
            strBuilder.append(sortOrder);
        }

        // If sorting is done by either rdfs:label or rdf:type, and a particular subject doesn't have
        // those predicates, then the last part of subject URI must be used instead.
        if (Predicates.RDFS_LABEL.equals(sortPredicate)) {
            strBuilder.append("(bif:either( bif:isnull(?sortObject) , (bif:lcase(bif:subseq (bif:replace (?s, '/', '#'), ")
            .append("bif:strrchr (bif:replace (?s, '/', '#'), '#')+1))) , bif:lcase(?sortObject)))");
        } else if (Predicates.RDF_TYPE.equals(sortPredicate)) {
            // Replace all / with # and then get the string after last #
            strBuilder.append("(bif:lcase(bif:subseq (bif:replace (?sortObject, '/', '#'), bif:strrchr (bif:replace ").append(
                    "(?sortObject, '/', '#'), '#')+1)))");
            // sort by date
        } else {
            strBuilder.append("(bif:lcase(?sortObject))");
        }

        return StringUtils.replace(strBuilder.toString(), "?sortObject", "?" + SORT_OBJECT_VALUE_VARIABLE);
    }

    /**
     * Returns StringBuilder based on useInference settings. Definition of the rule is at the beginning of the query if the helper
     * must use inferencing
     *
     * @return StringBuilder to be used for the query.
     */
    private StringBuilder initQueryStringBuilder() {
        return new StringBuilder(useInferencing ? SPARQLQueryUtil.getCrInferenceDefinition() : "");
    }

    /**
     * Builds the query 's "where contents", i.e. the part that goes in between the curly brackets in "where {}".
     *
     * @return Query parameter string for SPARQL
     */
    @Override
    public String getWhereContents() {
        String tagPredicate = "tagPredicate";
        bindings.setURI(tagPredicate, Predicates.CR_TAG);

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < tags.size(); i++) {
            String tagLiteral = "tagLiteral" + i;
            bindings.setString(tagLiteral, tags.get(i));

            sb.append("?s ");
            sb.append("?" + tagPredicate);
            sb.append(" ?" + tagLiteral + " .");
        }
        return sb.toString();
    }

    /**
     * @return the tags
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * @param tags the tags to set
     */
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

}
