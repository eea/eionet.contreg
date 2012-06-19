/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.cr.util.pagination;

import java.util.ArrayList;
import java.util.List;

import eionet.cr.util.QueryString;
import eionet.cr.web.action.AbstractActionBean;
import eionet.cr.web.action.AbstractSearchActionBean;

/**
 *
 * @author <a urlPath="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public final class Pagination {

    /** */
    public static final String PAGE_NUM_PARAM = "pageN";

    /** */
    public static final int DEFAULT_ITEMS_PER_PAGE = 15;

    /** */
    private int matchCount;
    private int numOfPages;
    private int curPageNum;
    private String urlPath;
    private QueryString queryString;

    /** */
    private Page first;
    private Page prev;
    private Page last;
    private Page next;
    private List<Page> group;

    /**
     *
     * @param matchCount
     * @param curPageNum
     * @param urlPath
     */
    private Pagination(int matchCount, int numOfPages, int curPageNum, String urlPath, QueryString queryString) {

        this.matchCount = matchCount;
        this.numOfPages = numOfPages;
        this.curPageNum = curPageNum;
        this.urlPath = urlPath;
        this.queryString = queryString == null ? QueryString.createQueryString() : queryString;

        constructPages();
    }

    /**
     *
     * @param matchCount
     * @param curPageNum
     * @param urlPath
     * @return
     */
    public static Pagination createPagination(int matchCount, int curPageNum, String urlPath, QueryString queryString) {

        int numOfPages = matchCount / pageLength();
        if (matchCount % pageLength() != 0)
            numOfPages = numOfPages + 1;

        if (urlPath.startsWith("/")) {
            urlPath = urlPath.substring(1);
        }

        if (numOfPages > 1)
            return new Pagination(matchCount, numOfPages, Math.min(numOfPages, Math.max(1, curPageNum)), urlPath, queryString);
        else
            return null;
    }

    /**
     * Gets pagination based on {@link PagingRequest}.
     *
     * @param pagingRequest
     * @param actionBean
     * @return
     */
    public static Pagination createPagination(int matchCount, int pageNumber, AbstractActionBean actionBean) {

        return createPagination(
                matchCount,
                pageNumber,
                actionBean.getUrlBinding(),
                QueryString.createQueryString(actionBean.getContext().getRequest()).removeParameters(
                        actionBean.excludeFromSortAndPagingUrls()));
    }

    /**
     *
     * @param actionBean
     * @return
     */
    public static Pagination createPagination(AbstractSearchActionBean actionBean) {

        return Pagination.createPagination(
                actionBean.getMatchCount(),
                actionBean.getPageN(),
                actionBean.getUrlBinding(),
                QueryString.createQueryString(actionBean.getContext().getRequest()).removeParameters(
                        actionBean.excludeFromSortAndPagingUrls()));
    }

    /**
     *
     */
    private void constructPages() {

        if (curPageNum > 1) {
            first = createPage(1);
            prev = createPage(curPageNum - 1);
        }

        if (curPageNum < numOfPages) {
            last = createPage(numOfPages);
            next = createPage(curPageNum + 1);
        }

        int startPage = Math.max(Math.min(curPageNum - groupSize() / 2, numOfPages - (groupSize() - 1)), 1);
        int endPage = Math.min(startPage + groupSize() - 1, numOfPages);

        group = new ArrayList<Page>();
        for (int i = startPage; i <= endPage; i++) {
            group.add(createPage(i));
        }
    }

    /**
     * Creates the query string to append to a link for a given page number.
     *
     * @param newPageNum
     * @return
     */
    private String getPageHref(int pageNum) {

        StringBuffer buf = new StringBuffer(urlPath);
        return buf.append("?").append(queryString.setParameterValue(PAGE_NUM_PARAM, String.valueOf(pageNum)).toURLFormat())
        .toString();
    }

    /**
     *
     * @param pageNum
     * @return
     */
    private Page createPage(int pageNum) {

        Page page = new Page();
        page.setNumber(pageNum);
        page.setSelected(curPageNum == pageNum);
        page.setHref(getPageHref(pageNum));

        return page;
    }

    /**
     * @return the matchCount
     */
    public int getMatchCount() {
        return matchCount;
    }

    /**
     * @return the curPageNum
     */
    public int getCurPageNum() {
        return curPageNum;
    }

    /**
     * @return the numOfPages
     */
    public int getNumOfPages() {
        return numOfPages;
    }

    /**
     *
     * @return
     */
    public static int pageLength() {
        return DEFAULT_ITEMS_PER_PAGE; // TODO should probably be made dynamic somehow.
    }

    /**
     *
     * @return
     */
    public static int groupSize() {
        return 9; // TODO should probably not be hard-coded
    }

    /**
     * @return the rowsFrom
     */
    public int getRowsFrom() {
        return (curPageNum - 1) * pageLength() + 1;
    }

    /**
     * @return the rowsTo
     */
    public int getRowsTo() {
        return Math.min(curPageNum * pageLength(), matchCount);
    }

    /**
     * @return the first
     */
    public Page getFirst() {
        return first;
    }

    /**
     * @return the prev
     */
    public Page getPrev() {
        return prev;
    }

    /**
     * @return the last
     */
    public Page getLast() {
        return last;
    }

    /**
     * @return the next
     */
    public Page getNext() {
        return next;
    }

    /**
     * @return the group
     */
    public List<Page> getGroup() {
        return group;
    }
}
