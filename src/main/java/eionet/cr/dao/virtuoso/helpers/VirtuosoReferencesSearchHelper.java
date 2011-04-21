package eionet.cr.dao.virtuoso.helpers;

import java.util.Collection;
import java.util.List;

import eionet.cr.common.Predicates;
import eionet.cr.dao.helpers.AbstractSearchHelper;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.web.util.columns.ReferringPredicatesColumn;

/**
 *
 * @author jaanus
 *
 */
public class VirtuosoReferencesSearchHelper extends AbstractSearchHelper {

    /** */
    private String subjectUri;

    /**
     *
     * @param subjectUri
     * @param pagingRequest
     * @param sortingRequest
     */
    public VirtuosoReferencesSearchHelper(String subjectUri,
            PagingRequest pagingRequest, SortingRequest sortingRequest) {

        super(pagingRequest, sortingRequest);
        this.subjectUri = subjectUri;
    }

    /* (non-Javadoc)
     * @see eionet.cr.dao.helpers.AbstractSearchHelper#getOrderedQuery(java.util.List)
     */
    @Override
    protected String getOrderedQuery(List<Object> inParams) {
        StringBuilder strBuilder = new StringBuilder().
        append("select ?s where {?s ?p ?o. filter(isURI(?o) && ?o=<").
        append(subjectUri).append(">)");

        if (sortPredicate != null && (Predicates.RDFS_LABEL.equals(sortPredicate) || Predicates.RDF_TYPE.equals(sortPredicate))) {
            strBuilder.append(" . OPTIONAL {?s <").append(sortPredicate).append("> ?oorderby }");
        }
        strBuilder.append("} ORDER BY ");

        if (sortOrder != null) {
            strBuilder.append(sortOrder);
        }
        if (Predicates.RDFS_LABEL.equals(sortPredicate)) {
            strBuilder
                    .append("(bif:either( bif:isnull(?oorderby) , (bif:lcase(bif:subseq (bif:replace (?s, '/', '#'), bif:strrchr (bif:replace (?s, '/', '#'), '#')+1))) , bif:lcase(?oorderby)))");
        } else if (ReferringPredicatesColumn.class.getSimpleName().equals(sortPredicate)) {
            strBuilder.append("(bif:lcase(?o))");
        } else {
            strBuilder.append("(bif:lcase(?oorderby))");
        }
        return strBuilder.toString();
    }

    /* (non-Javadoc)
     * @see eionet.cr.dao.helpers.AbstractSearchHelper#getUnorderedQuery(java.util.List)
     */
    @Override
    public String getUnorderedQuery(List<Object> inParams) {

        StringBuilder strBuilder = new StringBuilder().
        append("select ?s where {?s ?p ?o. filter(isURI(?o) && ?o=<").
        append(subjectUri).append(">)}");

        return strBuilder.toString();
    }

    /* (non-Javadoc)
     * @see eionet.cr.dao.helpers.AbstractSearchHelper#getCountQuery(java.util.List)
     */
    @Override
    public String getCountQuery(List<Object> inParams) {

        StringBuilder strBuilder = new StringBuilder().
        append("select count(?s) where {?s ?p ?o. filter(isURI(?o) && ?o=<").
        append(subjectUri).append(">)}");

        return strBuilder.toString();
    }

    /* (non-Javadoc)
     * @see eionet.cr.dao.helpers.AbstractSearchHelper#getMinMaxHashQuery(java.util.List)
     */
    @Override
    public String getMinMaxHashQuery(List<Object> inParams) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    //return sparql query for getting predicates for reference subjects
    public String getSubjectsDataQuery(Collection<String> subjectUris, String sourceUri) {
        if (subjectUris == null || subjectUris.isEmpty()) {
            throw new IllegalArgumentException("Subjects collection must not be null or empty!");
        }

        StringBuilder strBuilder = new StringBuilder().
        append("select * where {graph ?g {?s ?p ?o. ").
        append("filter (?s IN (");

        int i = 0;
        for (String subjectUri : subjectUris) {
            if (i > 0) {
                strBuilder.append(", ");
            }
            strBuilder.append("<").append(subjectUri).append(">");
            i++;
        }
        strBuilder.append(")) ");

        strBuilder.append(". filter(?p IN (<").append(Predicates.RDF_TYPE).append(">, <").append(Predicates.RDF_TYPE)
        .append(">) || (isURI(?o) && ?o=<").append(sourceUri).append(">))");

        strBuilder.append(". OPTIONAL {?g <").append(Predicates.CR_LAST_MODIFIED).append("> ?t} ");
        strBuilder.append("}} ORDER BY ?s");

        return strBuilder.toString();
    }
}
