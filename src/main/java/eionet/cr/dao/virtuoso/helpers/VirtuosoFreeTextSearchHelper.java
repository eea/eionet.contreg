package eionet.cr.dao.virtuoso.helpers;

import java.util.List;

import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.dao.helpers.FreeTextSearchHelper;
import eionet.cr.dao.util.SearchExpression;
import eionet.cr.util.Bindings;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.util.sql.VirtuosoFullTextQuery;

/**
 *
 * @author risto
 *
 */
public class VirtuosoFreeTextSearchHelper extends FreeTextSearchHelper {

    /** */
    private SearchExpression expression;
    /** */
    private VirtuosoFullTextQuery virtExpression;
    /** */
    private boolean exactMatch;
    /** Query bindings . */
    private Bindings bindings;

    /**
     * Creates a new helper object.
     *
     * @param expression
     *            search expression based on the query is built
     * @param virtExpression
     *            Helper object to parse Virtuoso query
     * @param exactMatch
     *            indicates if exact match is searched
     * @param pagingRequest
     *            paging request from the UI
     * @param sortingRequest
     *            sorting request from the UI
     */
    public VirtuosoFreeTextSearchHelper(SearchExpression expression, VirtuosoFullTextQuery virtExpression, boolean exactMatch,
            PagingRequest pagingRequest, SortingRequest sortingRequest) {

        super(pagingRequest, sortingRequest);
        this.expression = expression;
        this.virtExpression = virtExpression;
        this.exactMatch = exactMatch;

        bindings = new Bindings();
    }

    /**
     *
     */
    @Override
    protected String getOrderedQuery(List<Object> inParams) {

        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("select distinct ?s where {?s ?p ?o . ").append(addTypeFilterParams());
        strBuilder.append(addTextFilterClause());

        strBuilder.append("optional {?s ?sortPredicate ?ord} }");
        bindings.setURI("sortPredicate", sortPredicate);

        strBuilder.append("ORDER BY ");
        if (sortOrder != null)
            strBuilder.append(sortOrder);
        if (sortPredicate != null && sortPredicate.equals(Predicates.RDFS_LABEL)) {
            // If Label is not null then use Label. Otherwise use subject where we replace all / with # and then get
            // the string after last #.
            strBuilder
            .append("(bif:lcase(bif:either(bif:isnull(?ord), (bif:subseq (bif:replace (?s, '/', '#'), bif:strrchr (bif:replace (?s, '/', '#'), '#')+1)), ?ord)))");
        } else if (sortPredicate != null && sortPredicate.equals(Predicates.RDF_TYPE)) {
            // Replace all / with # and then get the string after last #
            strBuilder
            .append("(bif:lcase(bif:subseq (bif:replace (?ord, '/', '#'), bif:strrchr (bif:replace (?ord, '/', '#'), '#')+1)))");
        } else {
            strBuilder.append("(?ord)");
        }

        return strBuilder.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.helpers.AbstractSearchHelper#getUnorderedQuery(java.util.List)
     */
    @Override
    public String getUnorderedQuery(List<Object> inParams) {

        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("select distinct ?s where { ?s ?p ?o . ").append(addTypeFilterParams());
        strBuilder.append(addTextFilterClause());

        strBuilder.append("}");

        return strBuilder.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.helpers.AbstractSearchHelper#getCountQuery(java.util.List)
     */
    @Override
    public String getCountQuery(List<Object> inParams) {

        StringBuilder strBuilder = new StringBuilder();
        // TODO shouldn't it use inferencing?
        strBuilder.append("select count(distinct ?s) where { ?s ?p ?o . ").append(addTypeFilterParams());

        strBuilder.append(addTextFilterClause());
        strBuilder.append("}");

        return strBuilder.toString();
    }

    /**
     * Adds Type Search filter parameteres to the query if type is not any object. Bindings is filled with relevant value.
     *
     * @return filter part for the SPARQL query
     */
    private String addTypeFilterParams() {

        StringBuffer buf = new StringBuffer();
        buf.append(" ");

        if (filter != FilterType.ANY_OBJECT) {

            buf.append("?s a ?subjectType");

            if (filter == FilterType.ANY_FILE) {
                bindings.setURI("subjectType", Subjects.CR_FILE);
            } else if (filter == FilterType.DATASETS) {
                bindings.setURI("subjectType", Predicates.DC_MITYPE_DATASET);
            } else if (filter == FilterType.IMAGES) {
                bindings.setURI("subjectType", Predicates.DC_MITYPE_IMAGE);
            } else if (filter == FilterType.TEXTS) {
                bindings.setURI("subjectType", Predicates.DC_MITYPE_TEXT);
            }

            buf.append(" . ");
        }
        return buf.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.helpers.AbstractSearchHelper#getMinMaxHashQuery(java.util.List)
     */
    @Override
    public String getMinMaxHashQuery(List<Object> inParams) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /**
     * Builds relevant FILTER conditions based on the SearchExpression.
     *
     * @return FILTER that can will be added to the query
     */
    private String addTextFilterClause() {
        StringBuilder strBuilder = new StringBuilder();
        if (exactMatch) {
            if (expression.isUri()) {
                strBuilder.append(" FILTER (?o = ?objectValueUri || ?o = ?objectValueLit).");
                bindings.setURI("objectValueUri", expression.toString());
                bindings.setString("objectValueLit", expression.toString());
            } else {
                strBuilder.append(" FILTER (?o = ?objectValue).");
                bindings.setString("objectValue", expression.toString());
            }
        } else {
            strBuilder.append(" FILTER bif:contains(?o, ?objectValue). ");
            bindings.setString("objectValue", virtExpression.getParsedQuery());
        }

        return strBuilder.toString();
    }

    @Override
    public Bindings getQueryBindings() {
        return bindings;
    }
}
