package eionet.cr.dao.virtuoso.helpers;

import java.util.Collection;
import java.util.List;

import eionet.cr.common.Predicates;
import eionet.cr.dao.helpers.AbstractSearchHelper;
import eionet.cr.util.Bindings;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.util.sesame.SPARQLQueryUtil;
import eionet.cr.web.util.WebConstants;
import eionet.cr.web.util.columns.ReferringPredicatesColumn;

/**
 *
 * @author jaanus
 *
 */
public class VirtuosoReferencesSearchHelper extends AbstractSearchHelper {

    /** subject uri is valid IRI. */
    private boolean isValidIRI = true;

    /** */
    private Bindings bindings;

    /** */
    private Bindings subjectDataBindings;

    /**
     * Creates a new helper object.
     *
     * @param subjectUri
     *            resource subject URI.
     * @param pagingRequest
     *            paging request from the UI
     * @param sortingRequest
     *            sorting request for theresults table
     */
    public VirtuosoReferencesSearchHelper(String subjectUri, PagingRequest pagingRequest, SortingRequest sortingRequest) {

        super(pagingRequest, sortingRequest);

        bindings = new Bindings();
        isValidIRI = SPARQLQueryUtil.isIRI(subjectUri);
        bindings.setIRI("subjectUri", subjectUri);
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.helpers.AbstractSearchHelper#getOrderedQuery(java.util.List)
     */
    @Override
    protected String getOrderedQuery(List<Object> inParams) {
        String sparql =
                "select distinct ?s where {?s ?p ?o. filter(isURI(?o) && ?o=" + (isValidIRI ? "?subjectUri" : "IRI(?subjectUri)")
                        + ")";

        if (sortPredicate != null && (Predicates.RDFS_LABEL.equals(sortPredicate) || Predicates.RDF_TYPE.equals(sortPredicate))) {
            sparql += " . OPTIONAL {?s ?sortPredicate ?oorderby }";
            bindings.setURI("sortPredicate", sortPredicate);
        }
        sparql += "} ORDER BY ";

        if (sortOrder != null) {
            sparql += sortOrder;
        }
        if (Predicates.RDFS_LABEL.equals(sortPredicate)) {

            sparql +=
                    "(bif:either( bif:isnull(?oorderby) , "
                            + "(bif:lcase(bif:subseq (bif:replace (?s, '/', '#'), bif:strrchr (bif:replace (?s, '/', '#'), '#')+1))) , "
                            + "bif:lcase(?oorderby)))";
            // kind of hack - ReferringPredicatesColumn class name is neither a predicate or URI.
            // It is not used in sparql so we can love with it for a while
        } else if (ReferringPredicatesColumn.class.getSimpleName().equals(sortPredicate)) {
            sparql += "(bif:lcase(?o))";
        } else {
            sparql += "(bif:lcase(?oorderby))";
        }

        return sparql;
    }

    /**
     * SPARQL for unordered references of the subject.
     */
    private static final String REFERENCES_UNORDERED_SPARQL = "select distinct ?s where "
            + "{?s ?p ?o. filter(isURI(?o) && ?o=?subjectUri)}";

    @Override
    public String getUnorderedQuery(List<Object> inParams) {
        return (isValidIRI ? REFERENCES_UNORDERED_SPARQL : SPARQLQueryUtil
                .parseIRIQuery(REFERENCES_UNORDERED_SPARQL, "subjectUri"));
    }

    /**
     * SPARQL for counting subject references.
     */

    private static final String REFERENCES_COUNT_SPARQL = "select count(distinct ?s) "
            + "where {?s ?p ?o. filter(isURI(?o) && ?o=?subjectUri)}";

    @Override
    public String getCountQuery(List<Object> inParams) {
        // subjectUri is set in the constructor
        return REFERENCES_COUNT_SPARQL;
    }

    /**
     * returns sparql query for getting predicates for reference subjects. If the object is literal, it will be maximum of 2000
     * characters long.
     *
     * @param subjectUris
     *            subject uris of the references
     * @param sourceUri
     *            resource subject URI.
     * @return SPARQL query for getting subjects data.
     */
    public String getSubjectsDataQuery(Collection<String> subjectUris, String sourceUri) {
        if (subjectUris == null || subjectUris.isEmpty()) {
            throw new IllegalArgumentException("Subjects collection must not be null or empty!");
        }
        subjectDataBindings = new Bindings();
        subjectDataBindings.setIRI("sourceUri", sourceUri);

        String subjectUrisCSV = SPARQLQueryUtil.urisToCSV(subjectUris, "subjectUriValue", subjectDataBindings);
        String sourceUriBinding = (isValidIRI ? "?sourceUri" : "IRI(?sourceUri)");
        String sparql =
                "select ?g ?s ?p bif:either(isLiteral(?o), bif:substring(str(?o), 1, " + WebConstants.MAX_OBJECT_LENGTH + "), ?o) as ?o "
                        + "where {graph ?g {?s ?p ?o. filter (?s IN (" + subjectUrisCSV + ")) " + ". filter(?p = <"
                        + Predicates.RDF_TYPE + "> || <" + Predicates.RDFS_LABEL + "> || (isURI(?o) && ?o=" + sourceUriBinding
                        + "))}} ORDER BY ?s";

        return sparql;
    }

    @Override
    public Bindings getQueryBindings() {
        return bindings;
    }

    /**
     * Bindings to be used in subjects data query. For safety are kept separately from main query bindings
     *
     * @return bindings for subject data query
     */
    public Bindings getSubjectDataBindings() {
        return subjectDataBindings;
    }

}
