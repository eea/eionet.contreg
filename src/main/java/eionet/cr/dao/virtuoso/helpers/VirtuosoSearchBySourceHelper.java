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
public class VirtuosoSearchBySourceHelper extends AbstractSearchHelper{

    /** */
    private String sourceUrl;

    /**
     *
     * @param sourceUrl
     * @param pagingRequest
     * @param sortingRequest
     */
    public VirtuosoSearchBySourceHelper(String sourceUrl,
            PagingRequest pagingRequest, SortingRequest sortingRequest) {

        super(pagingRequest, sortingRequest);
        this.sourceUrl = sourceUrl;
    }

    /* (non-Javadoc)
     * @see eionet.cr.dao.helpers.AbstractSearchHelper#getUnorderedQuery(java.util.List)
     */
    @Override
    public String getUnorderedQuery(List<Object> inParams) {

        StringBuilder strBuilder = new StringBuilder().
        append("select distinct ?s from <").append(sourceUrl).append("> where {?s ?p ?o}");

        return strBuilder.toString();
    }

    /* (non-Javadoc)
     * @see eionet.cr.dao.helpers.AbstractSearchHelper#getOrderedQuery(java.util.List)
     */
    @Override
    protected String getOrderedQuery(List<Object> inParams) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /* (non-Javadoc)
     * @see eionet.cr.dao.helpers.AbstractSearchHelper#getCountQuery(java.util.List)
     */
    @Override
    public String getCountQuery(List<Object> inParams) {

        StringBuilder strBuilder = new StringBuilder().
        append("select count(distinct ?s) from <").append(sourceUrl).append("> where {?s ?p ?o}");

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
