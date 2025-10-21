package eionet.cr.dao.virtuoso.helpers;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import eionet.cr.common.CRRuntimeException;
import eionet.cr.common.Predicates;
import eionet.cr.dao.helpers.AbstractSearchHelper;
import eionet.cr.util.Bindings;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.URIUtil;
import eionet.cr.util.Util;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.util.sesame.SPARQLQueryUtil;

// TODO: Auto-generated Javadoc
/**
 *
 * @author Enriko Käsper
 *
 */
public class VirtuosoFilteredSearchHelper extends AbstractSearchHelper {

    /** */
    protected static final String SORT_OBJECT_VALUE_VARIABLE = "sortObjVal";

    /** */
    private Map<String, String> filters;

    /**
     *
     */
    private Set<String> literalRangeFilters;

    /**
     *
     */
    private Boolean requiresFullTextSearch = null;

    /**
     *
     */
    protected Bindings bindings;

    /**
     *
     * Class constructor.
     *
     * @param filters
     * @param literalRangeFilters
     * @param pagingRequest
     * @param sortingRequest
     */
    public VirtuosoFilteredSearchHelper(Map<String, String> filters, Set<String> literalRangeFilters, PagingRequest pagingRequest,
            SortingRequest sortingRequest) {

        super(pagingRequest, sortingRequest);

        // check the validity of filters
        if (filters == null || filters.isEmpty()) {
            throw new CRRuntimeException("The map of filters must not be null or empty!");
        } else {
            boolean atLeastOneValidEntry = false;
            for (Map.Entry<String, String> entry : filters.entrySet()) {
                if (!StringUtils.isBlank(entry.getKey()) && !StringUtils.isBlank(entry.getValue())) {
                    atLeastOneValidEntry = true;
                    break;
                }
            }
            if (atLeastOneValidEntry == false) {
                throw new CRRuntimeException("The map of filters must contain at least one enrty"
                        + " where key and value are not blank!");
            }
        }

        this.filters = filters;
        this.literalRangeFilters = literalRangeFilters;
        bindings = new Bindings();
    }

    /**
     *
     * Class constructor.
     *
     * @param filters
     * @param literalRangeFilters
     * @param pagingRequest
     * @param sortingRequest
     * @param useInferencing
     *
     * @Deprecated Inferencing is removed from CR
     */
    @Deprecated
    public VirtuosoFilteredSearchHelper(Map<String, String> filters, Set<String> literalRangeFilters, PagingRequest pagingRequest,
            SortingRequest sortingRequest, boolean useInferencing) {

        this(filters, literalRangeFilters, pagingRequest, sortingRequest);
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.helpers.AbstractSearchHelper#getUnorderedQuery(java.util.List)
     */
    @Override
    public String getUnorderedQuery(List<Object> inParams) {

        StringBuilder strBuilder = initQueryStringBuilder();
        strBuilder.append("select distinct ?s where {").append(getWhereContents()).append("}");

        return strBuilder.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.helpers.AbstractSearchHelper#getOrderedQuery(java.util.List)
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

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.helpers.AbstractSearchHelper#getCountQuery(java.util.List)
     */
    @Override
    public String getCountQuery(List<Object> inParams) {

        StringBuilder strBuilder = initQueryStringBuilder();
        strBuilder.append("select count(distinct ?s) where {").append(getWhereContents()).append("}");

        return strBuilder.toString();
    }

    /**
     * Builds the query 's "where contents", i.e. the part that goes in between the curly brackets in "where {}".
     *
     * @return Query parameter string for SPARQL
     */
    public String getWhereContents() {

        String result = "";
        boolean sortPredicateInWhereConents = false;
        int filterIndex = 0;

        for (Entry<String, String> entry : filters.entrySet()) {

            String predicateUri = entry.getKey();
            String objectValue = entry.getValue();

            if (!StringUtils.isBlank(predicateUri) && !StringUtils.isBlank(objectValue)) {

                filterIndex++;

                String predicateVariable = "p" + filterIndex;
                String objectVariable = "o" + filterIndex;
                String predicateValueVariable = predicateVariable + "Val";
                String objectValueVariable = objectVariable + "Val";

                if (StringUtils.equals(sortPredicate, predicateUri)) {
                    objectValueVariable = SORT_OBJECT_VALUE_VARIABLE;
                    sortPredicateInWhereConents = true;
                }

                if (filterIndex > 1) {
                    result += " . ";
                }

                result += "?s ?" + predicateVariable + " ?" + objectVariable;
                result += " . filter(?" + predicateVariable + " = ?" + predicateValueVariable + ")";
                bindings.setURI(predicateValueVariable, predicateUri);

                if (Util.isSurroundedWithQuotes(objectValue)) {
                    result += " . filter(?" + objectVariable + " = ?" + objectValueVariable + ")";
                    bindings.setString(objectValueVariable, Util.removeSurroundingQuotes(objectValue));
                } else if (URIUtil.isSchemedURI(objectValue)) {
                    result += " . filter(?" + objectVariable + " = ?" + objectValueVariable + ")";
                    bindings.setURI(objectValueVariable, objectValue);
                } else {
                    result += " . filter bif:contains(?" + objectVariable + ", ?" + objectValueVariable + ")";
                    // Quotes are added for bif:contains expression
                    objectValue = "\"" + objectValue + "\"";
                    bindings.setString(objectValueVariable, objectValue);
                    requiresFullTextSearch = Boolean.TRUE;
                }
            }
        }

        if (!result.isEmpty() && sortPredicate != null && !sortPredicateInWhereConents) {
            result += " . OPTIONAL {?s ?sortPred ?" + SORT_OBJECT_VALUE_VARIABLE + "}";
            bindings.setURI("sortPred", sortPredicate);
        }

        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.helpers.AbstractSearchHelper#getQueryBindings()
     */
    @Override
    public Bindings getQueryBindings() {
        return bindings;
    }

    /**
     * Returns StringBuilder based on useInference settings. Definition of the rule is at the beginning of the query if the helper
     * must use inferencing
     *
     * @return StringBuilder to be used for the query.
     *
     * @deprecated As inferencing is not used in CR
     */
    @Deprecated
    private StringBuilder initQueryStringBuilder() {
        return new StringBuilder("");
    }
}
