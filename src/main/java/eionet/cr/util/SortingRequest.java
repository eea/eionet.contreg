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
package eionet.cr.util;

/**
 * Encapsulates data about table sorting.
 *
 * @author Aleksandr Ivanov <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public class SortingRequest {

    private String sortingColumnName;
    private SortOrder sortOrder = SortOrder.ASCENDING;
    private boolean sortByPredicateObjectHash = false;

    /**
     * @param sortingColumnName
     * @param sortingColumnOrder
     */
    public SortingRequest(String sortingColumnName, SortOrder sortingColumnOrder) {
        this.sortingColumnName = sortingColumnName;
        this.sortOrder = sortingColumnOrder;
    }

    /**
     * @return the sortingColumnName
     */
    public String getSortingColumnName() {
        return sortingColumnName;
    }

    /**
     * @param sortingColumnName the sortingColumnName to set
     */
    public void setSortingColumnName(String sortingColumnName) {
        this.sortingColumnName = sortingColumnName;
    }

    /**
     * @return the sortOrder
     */
    public SortOrder getSortOrder() {
        return sortOrder;
    }

    /**
     * @param sortOrder the sortOrder to set
     */
    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    /**
     * @return the sortByPredicateObjectHash
     */
    public boolean isSortByPredicateObjectHash() {
        return sortByPredicateObjectHash;
    }

    /**
     * @param sortByPredicateObjectHash the sortByPredicateObjectHash to set
     */
    public void setSortByPredicateObjectHash(boolean sortByPredicateObjectHash) {
        this.sortByPredicateObjectHash = sortByPredicateObjectHash;
    }
}
