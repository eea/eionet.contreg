package eionet.cr.dao.virtuoso.helpers;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import eionet.cr.common.CRRuntimeException;
import eionet.cr.common.Predicates;
import eionet.cr.dao.helpers.AbstractSearchHelper;
import eionet.cr.util.Bindings;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.URIUtil;
import eionet.cr.util.Util;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.util.sesame.SPARQLQueryUtil;

/**
 *
 * @author Enriko Käsper
 *
 */
public class VirtuosoFilteredSearchHelper extends AbstractSearchHelper {

    private Map<String, String> filters;
    private Set<String> literalPredicates;
    private Boolean requiresFullTextSearch = null;
    private Bindings bindings;

    private static final String SORTPREDICATE_VALUE_ALIAS = "sortPredicateValue";

    public VirtuosoFilteredSearchHelper(Map<String, String> filters,
            Set<String> literalPredicates, PagingRequest pagingRequest,
            SortingRequest sortingRequest) {
        super(pagingRequest, sortingRequest);
        // check the validity of filters
        if (filters == null || filters.isEmpty()) {
            throw new CRRuntimeException(
                    "The map of filters must not be null or empty!");
        } else {
            boolean atLeastOneValidEntry = false;
            for (Map.Entry<String, String> entry : filters.entrySet()) {
                if (!StringUtils.isBlank(entry.getKey())
                        && !StringUtils.isBlank(entry.getValue())) {
                    atLeastOneValidEntry = true;
                    break;
                }
            }
            if (atLeastOneValidEntry == false) {
                throw new CRRuntimeException(
                        "The map of filters must contain at least one enrty"
                                + " where key and value are not blank!");
            }
        }

        this.filters = filters;
        this.literalPredicates = literalPredicates;
        bindings = new Bindings();
    }

    @Override
    protected String getOrderedQuery(List<Object> inParams) {
        //sorting by date needs including the graph into query: sorting is done by graph's cr:contentLastmodified
        StringBuilder strBuilder = new StringBuilder(SPARQLQueryUtil.getCrInferenceDefinition());
        if (Predicates.CR_LAST_MODIFIED.equals(sortPredicate)) {
            strBuilder.append("select distinct ?s max(?time) AS ?oorderby where {graph ?g { ?s ?p ?o ");
        } else {
            strBuilder.append("select distinct ?s where { ?s ?p ?o ");
        }
        strBuilder.append(getQueryParameters(inParams));
        if (Predicates.CR_LAST_MODIFIED.equals(sortPredicate)) {
            strBuilder.append("}");
        }
        strBuilder.append("} ORDER BY ");
        if (sortOrder != null) {
            strBuilder.append(sortOrder);
        }
        //if we do not have real labels / types in the query sort by last part of URI
        if (Predicates.RDFS_LABEL.equals(sortPredicate)) {
            strBuilder.append("(bif:either( bif:isnull(?oorderby) , (bif:lcase(bif:subseq (bif:replace (?s, '/', '#'), ")
                .append("bif:strrchr (bif:replace (?s, '/', '#'), '#')+1))) , bif:lcase(?oorderby)))");
        } else if (Predicates.RDF_TYPE.equals(sortPredicate)) {
            //Replace all / with # and then get the string after last #
            strBuilder.append("(bif:lcase(bif:subseq (bif:replace (?oorderby, '/', '#'), bif:strrchr (bif:replace ")
                .append("(?oorderby, '/', '#'), '#')+1)))");
        //sort by date
        } else if (sortPredicate.equals(Predicates.CR_LAST_MODIFIED)) {
            strBuilder.append("(?oorderby)");
        } else {
            strBuilder.append("(bif:lcase(?oorderby))");
        }

        return strBuilder.toString();
    }

    @Override
    public String getUnorderedQuery(List<Object> inParams) {

        StringBuilder strBuilder = new StringBuilder(SPARQLQueryUtil.getCrInferenceDefinition());
        strBuilder.append("select distinct ?s where { ?s ?p ?o ");
        strBuilder.append(getQueryParameters(inParams));
        strBuilder.append("}");

        return strBuilder.toString();
    }

    @Override
    public String getCountQuery(List<Object> inParams) {
        StringBuilder strBuilder = new StringBuilder(SPARQLQueryUtil.getCrInferenceDefinition());
        strBuilder.append("select count(distinct ?s) where { ?s ?p ?o ");
        strBuilder.append(getQueryParameters(inParams));
        strBuilder.append("}");

        return strBuilder.toString();
    }

    @Override
    public String getMinMaxHashQuery(List<Object> inParams) {
        throw new UnsupportedOperationException("Method not implemented");
    }


    /**
     * Builds query parameter String bsaed on given filters.
     *
     * @param inParams
     *            useless parameter was used for SQL preparedstatemenst
     * @return Query parameter string for SPARQL
     */
    public String getQueryParameters(List<Object> inParams) {
        String s = "";
        // TODO remove inParams that were used for SQL prepared statement handling
        int i = 1;

        //shows if sorting predicate is in the filter
        boolean hasSortingPredicate = false;

        for (Entry<String, String> entry : filters.entrySet()) {

            String predicateUri = entry.getKey();
            String objectValue = entry.getValue();

            if (!StringUtils.isBlank(predicateUri) && !StringUtils.isBlank(objectValue)) {

                String o = "?o".concat(String.valueOf(i)); // ?o1
                String p = "?p".concat(String.valueOf(i)); // ?p1
                // NB! value aliases without question mark
                // predicateValue1
                String predicateValueAlias = "predicateValue".concat(String.valueOf(i));
                // objectValue1
                String objectValueAlias = "objectValue".concat(String.valueOf(i));

                //sorting predicate exists in the query include in in select  ?oorderby instead of regular (?o1) alias
                if (sortPredicate != null && predicateUri.equals(sortPredicate) && !hasSortingPredicate) {
                    o = "?oorderby";
                    hasSortingPredicate = true;
                }

                // " . {{?s ?p1 ?o1 . ?s ?predicateValue1 ?o1} . { ?s ?p1 ?o1
                s += " . {{?s " + p + " " + o + " . ?s ?" + predicateValueAlias + " " + o + "} . { ?s " + p + " " + o;

                bindings.setURI(predicateValueAlias, predicateUri);

                if (Util.isSurroundedWithQuotes(objectValue)) {
                    // ". FILTER (?o1= ?objectValue1)";
                    s += ". FILTER (" + o + " = ?" + objectValueAlias + ")";

                    bindings.setString(objectValueAlias, Util.removeSurroundingQuotes(objectValue));

                } else if (URIUtil.isSchemedURI(objectValue)) {
                    // compare both because it is not known if the object is
                    // literal or URI

                    // " . FILTER (?o1 = ?objectValue1Uri || ?o1=?objectValue1Lit)
                    s += " . FILTER (" + o + " = ?" + objectValueAlias + "Uri || ?o" + i + " = ?" + objectValueAlias + "Lit)";
                    bindings.setURI(objectValueAlias + "Uri", objectValue);
                    bindings.setString(objectValueAlias + "Lit", objectValue);
                } else {
                    // . FILTER bif:contains(?o1, ?objectValue1)
                    s += " . FILTER bif:contains(" + o + ", ?" + objectValueAlias + ")";

                    bindings.setString(objectValueAlias, "'" + objectValue + "'");

                    // inParams.add(objectValue);
                    // TODO is it really needed in Virtuoso
                    requiresFullTextSearch = Boolean.TRUE;
                }

                s += "}}";
                i++;
            }
        }
        if (!hasSortingPredicate && sortPredicate != null) {
            if (Predicates.CR_LAST_MODIFIED.equals(sortPredicate)) {
                s += " . OPTIONAL {?g ?sortPredicateValue ?time}";
            } else {
                s += " . OPTIONAL {?s ?sortPredicateValue ?oorderby}";
            }
            bindings.setURI(SORTPREDICATE_VALUE_ALIAS, sortPredicate);
        }
        return s;
    }

    /**
     *
     * @param predicateUri
     * @param objectValue
     * @return
     */
    private boolean requireFullTextSearch(String predicateUri,
            String objectValue) {

        return (Util.isSurroundedWithQuotes(objectValue)
                || URIUtil.isSchemedURI(objectValue) || !isLiteralPredicate(predicateUri)) == false;
    }

    /**
     *
     * @return
     */
    public boolean requiresFullTextSearch() {

        if (requiresFullTextSearch == null) {

            requiresFullTextSearch = Boolean.FALSE;
            for (Entry<String, String> entry : filters.entrySet()) {

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
     * @param key
     */
    protected void removeFilter(String key) {
        if (this.filters != null && filters.containsKey(key)) {
            filters.remove(key);
        }
    }

    @Override
    public Bindings getQueryBindings() {
        return bindings;
    }
}
