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
 * Aleksandr Ivanov, Tieto Eesti
 */
package eionet.cr.util.pagination;

/**
 * @author Aleksandr Ivanov <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public final class PagingRequest {

    /** */
    private int pageNumber;
    private int itemsPerPage;
    private int offset;

    /**
     * @param pageNumber
     * @param itemsPerPage
     */
    private PagingRequest(int pageNumber, int itemsPerPage) {

        this.pageNumber = Math.max(1, pageNumber);
        this.itemsPerPage = Math.max(1, itemsPerPage);
        this.offset = (this.pageNumber - 1) * this.itemsPerPage;
    }

    /**
     *
     * @param pageNumber
     * @return
     */
    public static PagingRequest create(int pageNumber) {

        return create(pageNumber, Pagination.DEFAULT_ITEMS_PER_PAGE);
    }

    /**
     *
     * @param pageNumber
     * @param itemsPerPage
     * @return
     */
    public static PagingRequest create(int pageNumber, int itemsPerPage) {

        return itemsPerPage <= 0 ? null : new PagingRequest(pageNumber, itemsPerPage);
    }

    /**
     * @return the pageNumber
     */
    public int getPageNumber() {
        return pageNumber;
    }

    /**
     * @return the itemsPerPage
     */
    public int getItemsPerPage() {
        return itemsPerPage;
    }

    /**
     *
     * @return
     */
    public int getOffset() {
        return offset;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return new StringBuffer().append("pageNumber=").append(pageNumber).append(",itemsPerPage=").append(itemsPerPage)
        .toString();
    }
}
