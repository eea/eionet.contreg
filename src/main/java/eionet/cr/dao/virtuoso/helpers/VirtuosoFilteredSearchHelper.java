package eionet.cr.dao.virtuoso.helpers;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import eionet.cr.common.CRRuntimeException;
import eionet.cr.dao.helpers.AbstractSearchHelper;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.URIUtil;
import eionet.cr.util.Util;
import eionet.cr.util.pagination.PagingRequest;

/**
 *
 * @author Enriko Käsper
 *
 */
public class VirtuosoFilteredSearchHelper extends AbstractSearchHelper {


    private Map<String, String> filters;
    private Set<String> literalPredicates;
    private Boolean requiresFullTextSearch = null;

    public VirtuosoFilteredSearchHelper(Map<String, String> filters, Set<String> literalPredicates,
            PagingRequest pagingRequest, SortingRequest sortingRequest) {
        super(pagingRequest, sortingRequest);
        // check the validity of filters
        if (filters == null || filters.isEmpty())
            throw new CRRuntimeException("The map of filters must not be null or empty!");
        else {
            boolean atLeastOneValidEntry = false;
            for (Map.Entry<String, String> entry : filters.entrySet()) {
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

    @Override
    protected String getOrderedQuery(List<Object> inParams) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("select distinct ?s where { ?s ?p ?o ");
        strBuilder.append(getQueryParameters(inParams));
        strBuilder.append("} ORDER BY ?s");

        return strBuilder.toString();
    }

    @Override
    public String getUnorderedQuery(List<Object> inParams) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("select distinct ?s where { ?s ?p ?o ");
        strBuilder.append(getQueryParameters(inParams));
        strBuilder.append("}");

        return strBuilder.toString();
    }

    @Override
    public String getCountQuery(List<Object> inParams) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("select count(distinct ?s) where { ?s ?p ?o ");
        strBuilder.append(getQueryParameters(inParams));
        strBuilder.append("}");

        return strBuilder.toString();
    }

    @Override
    public String getMinMaxHashQuery(List<Object> inParams) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    private String getQueryParameters(List<Object> inParams) {
        StringBuilder strBuilder = new StringBuilder();
        for (Entry<String, String> entry : filters.entrySet()) {

            String predicateUri = entry.getKey();
            String objectValue = entry.getValue();

            if (!StringUtils.isBlank(predicateUri) && !StringUtils.isBlank(objectValue)) {

                if (!requireFullTextSearch(predicateUri, objectValue)) {
                    strBuilder.append(". ?s <").append(predicateUri).append("> <").append(objectValue).append(">");
                } else {
                    strBuilder.append(". ?s <").append(predicateUri).append("> ?o . ?o bif:contains \"'").
                            append(objectValue).append("'\"");
                    inParams.add(objectValue);
                    requiresFullTextSearch = Boolean.TRUE;
                }
            }
        }
        return strBuilder.toString();

    }

    /**
     *
     * @param predicateUri
     * @param objectValue
     * @return
     */
    private boolean requireFullTextSearch(String predicateUri, String objectValue) {

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
   protected void removeFilter(String key){
       if(this.filters!=null && filters.containsKey(key)){
           filters.remove(key);
       }
   }
}
