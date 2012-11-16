package eionet.cr.dao.virtuoso.helpers;

import java.util.List;

import org.apache.commons.lang.StringUtils;

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
    private static final String SUBJECT_TYPE_VARIABLE = "subjType";

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
     * @param expression search expression based on the query is built
     * @param virtExpression Helper object to parse Virtuoso query
     * @param exactMatch indicates if exact match is searched
     * @param pagingRequest paging request from the UI
     * @param sortingRequest sorting request from the UI
     */
    public VirtuosoFreeTextSearchHelper(SearchExpression expression, VirtuosoFullTextQuery virtExpression, boolean exactMatch,
            PagingRequest pagingRequest, SortingRequest sortingRequest) {

        super(pagingRequest, sortingRequest);
        this.expression = expression;
        this.virtExpression = virtExpression;
        this.exactMatch = exactMatch;

        bindings = new Bindings();
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.helpers.AbstractSearchHelper#getUnorderedQuery(java.util.List)
     */
    @Override
    public String getUnorderedQuery(List<Object> inParams) {

        StringBuilder buf = new StringBuilder();
        buf.append("select distinct ?s where { ?s ?p ?o");

        String typeFilterPart = buildTypeFilterPart();
        if (!StringUtils.isBlank(typeFilterPart)) {
            buf.append(" . ").append(typeFilterPart);
        }
        buf.append(" . ").append(buildTextFilterPart()).append("}");

        return buf.toString();
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.helpers.FreeTextSearchHelper#getOrderedQuery(java.util.List)
     */
    @Override
    protected String getOrderedQuery(List<Object> inParams) {

        StringBuilder buf = new StringBuilder();
        buf.append("select distinct ?s where {?s ?p ?o");

        String typeFilterPart = buildTypeFilterPart();
        if (!StringUtils.isBlank(typeFilterPart)) {
            buf.append(" . ").append(typeFilterPart);
        }
        buf.append(" . ").append(buildTextFilterPart()).append(" . optional {?s ?sortPredicate ?ord}} ORDER BY ");

        bindings.setURI("sortPredicate", sortPredicate);
        if (sortOrder != null) {
            buf.append(sortOrder);
        }

        // If sorting is done by either rdfs:label or rdf:type, and a particular subject doesn't have
        // those predicates, then the last part of subject URI must be used instead.
        if (StringUtils.equals(sortPredicate, Predicates.RDFS_LABEL)) {
            buf.append("(bif:lcase(bif:either(bif:isnull(?ord), (bif:subseq (bif:replace (?s, '/', '#'), bif:strrchr (bif:replace (?s, '/', '#'), '#')+1)), ?ord)))");
        } else if (StringUtils.equals(sortPredicate, Predicates.RDF_TYPE)) {
            buf.append("(bif:lcase(bif:subseq (bif:replace (?ord, '/', '#'), bif:strrchr (bif:replace (?ord, '/', '#'), '#')+1)))");
        } else {
            buf.append("(?ord)");
        }

        return buf.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.helpers.AbstractSearchHelper#getCountQuery(java.util.List)
     */
    @Override
    public String getCountQuery(List<Object> inParams) {

        StringBuilder buf = new StringBuilder();
        buf.append("select count(distinct ?s) where {?s ?p ?o");

        String typeFilterPart = buildTypeFilterPart();
        if (!StringUtils.isBlank(typeFilterPart)) {
            buf.append(" . ").append(typeFilterPart);
        }
        buf.append(" . ").append(buildTextFilterPart()).append("}");

        return buf.toString();
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.helpers.AbstractSearchHelper#getQueryBindings()
     */
    @Override
    public Bindings getQueryBindings() {
        return bindings;
    }

    /**
     * Builds the part of the the WHERE clause that constitutes the type filter (user can set the type of resources where he is
     * searching for his free text).
     *
     * @return The built part of the WHERE clause.
     */
    private String buildTypeFilterPart() {

        StringBuilder buf = new StringBuilder();
        if (filter != FilterType.ANY_OBJECT) {

            buf.append("?s a ?").append(SUBJECT_TYPE_VARIABLE);

            if (filter == FilterType.ANY_FILE) {
                bindings.setURI(SUBJECT_TYPE_VARIABLE, Subjects.CR_FILE);
            } else if (filter == FilterType.DATASETS) {
                bindings.setURI(SUBJECT_TYPE_VARIABLE, Predicates.DC_MITYPE_DATASET);
            } else if (filter == FilterType.IMAGES) {
                bindings.setURI(SUBJECT_TYPE_VARIABLE, Predicates.DC_MITYPE_IMAGE);
            } else if (filter == FilterType.TEXTS) {
                bindings.setURI(SUBJECT_TYPE_VARIABLE, Predicates.DC_MITYPE_TEXT);
            }
        }
        return buf.toString();
    }

    /**
     * Builds the part of the the WHERE clause that constitutes the free-text filter.
     *
     * @return The built part of the WHERE clause.
     */
    private String buildTextFilterPart() {

        StringBuilder buf = new StringBuilder();
        if (exactMatch) {

            buf.append("filter (?o = ?objectVal)");
            if (expression.isUri()) {
                bindings.setURI("objectVal", expression.toString());
            } else {
                bindings.setString("objectVal", expression.toString());
            }
        } else {
            buf.append("filter bif:contains(?o, ?objectVal)");
            bindings.setString("objectVal", virtExpression.getParsedQuery());
        }

        return buf.toString();
    }
}
