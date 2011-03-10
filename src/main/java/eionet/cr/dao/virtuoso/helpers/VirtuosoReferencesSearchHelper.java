package eionet.cr.dao.virtuoso.helpers;

import java.util.List;

import eionet.cr.dao.helpers.AbstractSearchHelper;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.pagination.PagingRequest;

/**
 *
 * @author jaanus
 *
 */
public class VirtuosoReferencesSearchHelper extends AbstractSearchHelper{

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
        throw new UnsupportedOperationException("Method not implemented");
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
}
