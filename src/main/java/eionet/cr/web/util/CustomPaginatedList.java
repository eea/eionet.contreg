package eionet.cr.web.util;

import java.util.List;

import org.displaytag.pagination.PaginatedList;
import org.displaytag.properties.SortOrderEnum;

public class CustomPaginatedList<T> implements PaginatedList {

    private int fullListSize;
    private List<T> list;
    private int objectsPerPage;
    private int pageNumber;
    private String searchId;
    private String sortCriterion;
    private SortOrderEnum sortDirection;

    @Override
    public int getFullListSize() {
        return fullListSize;
    }

    @Override
    public List<T> getList() {
        return list;
    }

    @Override
    public int getObjectsPerPage() {
        return objectsPerPage;
    }

    @Override
    public int getPageNumber() {
        return pageNumber;
    }

    @Override
    public String getSearchId() {
        return searchId;
    }

    @Override
    public String getSortCriterion() {
        return sortCriterion;
    }

    @Override
    public SortOrderEnum getSortDirection() {
        return sortDirection;
    }

    public void setFullListSize(int fullListSize) {
        this.fullListSize = fullListSize;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public void setObjectsPerPage(int objectsPerPage) {
        this.objectsPerPage = objectsPerPage;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public void setSearchId(String searchId) {
        this.searchId = searchId;
    }

    public void setSortCriterion(String sortCriterion) {
        this.sortCriterion = sortCriterion;
    }

    public void setSortDirection(SortOrderEnum sortDirection) {
        this.sortDirection = sortDirection;
    }

}
