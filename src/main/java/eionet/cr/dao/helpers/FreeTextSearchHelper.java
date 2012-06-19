package eionet.cr.dao.helpers;

import java.util.List;

import eionet.cr.util.SortingRequest;
import eionet.cr.util.pagination.PagingRequest;

/**
 *
 * @author jaanus
 *
 */
public abstract class FreeTextSearchHelper extends AbstractSearchHelper {

    /** */
    public enum FilterType {
        ANY_OBJECT, ANY_FILE, TEXTS, DATASETS, IMAGES
    };

    /** */
    protected FilterType filter = FilterType.ANY_OBJECT;

    /**
     *
     * @param pagingRequest
     * @param sortingRequest
     */
    public FreeTextSearchHelper(PagingRequest pagingRequest, SortingRequest sortingRequest) {
        super(pagingRequest, sortingRequest);
    }

    /**
     * @return FilterType
     */
    public FilterType getFilter() {
        return filter;
    }

    /**
     * @param filter
     */
    public void setFilter(FilterType filter) {
        this.filter = filter;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.helpers.AbstractSearchHelper#getOrderedQuery(java.util.List)
     */
    protected abstract String getOrderedQuery(List<Object> inParams);

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.helpers.AbstractSearchHelper#getUnorderedQuery(java.util.List)
     */
    public abstract String getUnorderedQuery(List<Object> inParams);

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.helpers.AbstractSearchHelper#getCountQuery(java.util.List)
     */
    public abstract String getCountQuery(List<Object> inParams);
}
