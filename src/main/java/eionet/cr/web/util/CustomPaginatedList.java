package eionet.cr.web.util;

import java.util.List;

import org.displaytag.pagination.PaginatedList;
import org.displaytag.properties.SortOrderEnum;

import eionet.cr.dto.SearchResultDTO;
import eionet.cr.util.Pair;
import eionet.cr.util.SortOrder;
import eionet.cr.web.action.DisplaytagSearchActionBean;

/**
 * Implementation of DisplayTag's {@link PaginatedList}. Used for feeding DisplayTag tables with all information they need for
 * displaying particular search result lists.
 *
 * @author altnyris
 * @author Jaanus
 * @param <T> Class of objects whose result list a particular instance of this class servers.
 */
public class CustomPaginatedList<T> implements PaginatedList {

    /** The full list size. */
    private int fullListSize;

    /** The list (more precisely: the list's requested page). */
    private List<T> list;

    /** Number of objects per page. */
    private int objectsPerPage;

    /** The page number. */
    private int pageNumber;

    /** The search id. */
    private String searchId;

    /** The sort criterion. */
    private String sortCriterion;

    /** The sort direction. */
    private SortOrderEnum sortDirection;

    /**
     * Simple constructor with no arguments, expecting fields to be populated via setters.
     */
    public CustomPaginatedList() {
        // Auto-generated constructor stub
    }

    /**
     * Calls {@link #CustomPaginatedList(DisplaytagSearchActionBean, int, List, int)} by obtaining the total match count and
     * the list from the given pair where the left is the total match count and the right is the list.
     *
     * @param actionBean Will be used to identify current page number, sort criterion and sort direction.
     * @param resultPair As described above.
     * @param pageSize Page size by which the result-list is expected to be chopped.
     */
    public CustomPaginatedList(DisplaytagSearchActionBean actionBean, Pair<Integer, List<T>> resultPair, int pageSize) {

        this(actionBean, resultPair == null ? 0 : resultPair.getLeft().intValue(), resultPair == null ? null : resultPair
                .getRight(), pageSize);
    }

    /**
     * Calls {@link #CustomPaginatedList(DisplaytagSearchActionBean, int, List, int)} by obtaining the total match count and
     * the list from the given {@link SearchResultDTO}.
     *
     * @param actionBean Will be used to identify current page number, sort criterion and sort direction.
     * @param searchResult Total match count and the list will be taken from here.
     * @param pageSize Page size by which the result-list is expected to be chopped.
     */
    public CustomPaginatedList(DisplaytagSearchActionBean actionBean, SearchResultDTO<T> searchResult, int pageSize) {

        this(actionBean, searchResult == null ? 0 : searchResult.getMatchCount(), searchResult == null ? null : searchResult
                .getItems(), pageSize);
    }

    /**
     * Creates an instance from the given inputs.
     *
     * @param actionBean Will be used to identify current page number, sort criterion and sort direction.
     * @param totalMatchCount Total number of matches.
     * @param list The result-list page to be displayed.
     * @param pageSize Page size by which the result-list is expected to be chopped.
     */
    public CustomPaginatedList(DisplaytagSearchActionBean actionBean, int totalMatchCount, List<T> list, int pageSize) {

        this.list = list;
        this.fullListSize = totalMatchCount > 0 ? totalMatchCount : (list == null ? 0 : list.size());
        this.objectsPerPage = pageSize;

        if (actionBean != null) {

            this.pageNumber = actionBean.getPage();
            this.sortCriterion = actionBean.getSort();
            SortOrder sortOrder = SortOrder.parse(actionBean.getDir());
            this.sortDirection = sortOrder != null ? sortOrder.toDisplayTagEnum() : SortOrderEnum.ASCENDING;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.displaytag.pagination.PaginatedList#getFullListSize()
     */
    @Override
    public int getFullListSize() {
        return fullListSize;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.displaytag.pagination.PaginatedList#getList()
     */
    @Override
    public List<T> getList() {
        return list;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.displaytag.pagination.PaginatedList#getObjectsPerPage()
     */
    @Override
    public int getObjectsPerPage() {
        return objectsPerPage;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.displaytag.pagination.PaginatedList#getPageNumber()
     */
    @Override
    public int getPageNumber() {
        return pageNumber;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.displaytag.pagination.PaginatedList#getSearchId()
     */
    @Override
    public String getSearchId() {
        return searchId;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.displaytag.pagination.PaginatedList#getSortCriterion()
     */
    @Override
    public String getSortCriterion() {
        return sortCriterion;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.displaytag.pagination.PaginatedList#getSortDirection()
     */
    @Override
    public SortOrderEnum getSortDirection() {
        return sortDirection;
    }

    /**
     * Sets the full list size.
     *
     * @param fullListSize the new full list size
     */
    public void setFullListSize(int fullListSize) {
        this.fullListSize = fullListSize;
    }

    /**
     * Sets the list.
     *
     * @param list the new list
     */
    public void setList(List<T> list) {
        this.list = list;
    }

    /**
     * Sets the objects per page.
     *
     * @param objectsPerPage the new objects per page
     */
    public void setObjectsPerPage(int objectsPerPage) {
        this.objectsPerPage = objectsPerPage;
    }

    /**
     * Sets the page number.
     *
     * @param pageNumber the new page number
     */
    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    /**
     * Sets the search id.
     *
     * @param searchId the new search id
     */
    public void setSearchId(String searchId) {
        this.searchId = searchId;
    }

    /**
     * Sets the sort criterion.
     *
     * @param sortCriterion the new sort criterion
     */
    public void setSortCriterion(String sortCriterion) {
        this.sortCriterion = sortCriterion;
    }

    /**
     * Sets the sort direction.
     *
     * @param sortDirection the new sort direction
     */
    public void setSortDirection(SortOrderEnum sortDirection) {
        this.sortDirection = sortDirection;
    }
}
