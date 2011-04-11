package eionet.cr.dao.virtuoso.helpers;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import eionet.cr.common.CRRuntimeException;
import eionet.cr.common.Predicates;
import eionet.cr.config.GeneralConfig;
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

    private static final String inferenceDef = "DEFINE input:inference";

    public VirtuosoFilteredSearchHelper(Map<String, String> filters,
            Set<String> literalPredicates, PagingRequest pagingRequest,
            SortingRequest sortingRequest) {
        super(pagingRequest, sortingRequest);
        // check the validity of filters
        if (filters == null || filters.isEmpty())
            throw new CRRuntimeException(
                    "The map of filters must not be null or empty!");
        else {
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
    }

    @Override
    protected String getOrderedQuery(List<Object> inParams) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(inferenceDef).append("'").append(
                GeneralConfig
                        .getProperty(GeneralConfig.VIRTUOSO_CR_RULESET_NAME))
                .append("' ");
        strBuilder.append("select distinct ?s where { ?s ?p ?o ");
        strBuilder.append(getQueryParameters(inParams));
        strBuilder.append("} ORDER BY ");
        if (sortOrder != null) {
            strBuilder.append(sortOrder);
        }
        if (Predicates.RDFS_LABEL.equals(sortPredicate)) {
            strBuilder
                    .append("(bif:either( bif:isnull(?oorderby) , (bif:lcase(bif:subseq (bif:replace (?s, '/', '#'), bif:strrchr (bif:replace (?s, '/', '#'), '#')+1))) , bif:lcase(?oorderby)))");
        } else {
            strBuilder.append("(bif:lcase(?oorderby))");
        }

        return strBuilder.toString();
    }

    @Override
    public String getUnorderedQuery(List<Object> inParams) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(inferenceDef).append("'").append(
                GeneralConfig
                        .getProperty(GeneralConfig.VIRTUOSO_CR_RULESET_NAME))
                .append("' ");
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

    public String getQueryParameters(List<Object> inParams) {
        StringBuilder strBuilder = new StringBuilder();
        int i = 1;
        boolean hasSortingPredicate = false;

        for (Entry<String, String> entry : filters.entrySet()) {

            String predicateUri = entry.getKey();
            String objectValue = entry.getValue();

            if (!StringUtils.isBlank(predicateUri)
                    && !StringUtils.isBlank(objectValue)) {

                String objectAlias = "?o".concat(String.valueOf(i));
                String predicateAlias = "?p".concat(String.valueOf(i));

                if (sortPredicate != null && predicateUri.equals(sortPredicate)
                        && !hasSortingPredicate) {
                    objectAlias = "?oorderby";
                    hasSortingPredicate = true;
                }
                strBuilder.append(" . {{?s ").append(predicateAlias)
                        .append(" ").append(objectAlias).append(" . ?s <")
                        .append(predicateUri).append("> ").append(objectAlias)
                        .append("} . { ?s ").append(predicateAlias).append(" ")
                        .append(objectAlias);

                if (Util.isSurroundedWithQuotes(objectValue)) {
                    strBuilder.append(" . FILTER (").append(objectAlias)
                            .append(" = ").append(objectValue).append(")");
                } else if (URIUtil.isSchemedURI(objectValue)) {
                    strBuilder.append(" . FILTER (").append(objectAlias)
                            .append(" = <").append(objectValue).append("> || ")
                            .append(objectAlias).append(" = \"").append(
                                    objectValue).append("\")");
                    // TODO check if it is a number??
                } else {
                    strBuilder.append(" . FILTER bif:contains(").append(
                            objectAlias).append(", \"'").append(objectValue)
                            .append("'\")");
                    inParams.add(objectValue);
                    // TODO is it really needed in Virtuoso
                    requiresFullTextSearch = Boolean.TRUE;
                }
                strBuilder.append("}}");
                i++;
            }
        }
        if (!hasSortingPredicate && sortPredicate != null) {
            strBuilder.append(" . OPTIONAL {?s <").append(sortPredicate)
                    .append("> ?oorderby }");
        }
        return strBuilder.toString();

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
}
