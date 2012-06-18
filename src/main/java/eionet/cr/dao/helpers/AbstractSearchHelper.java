/*
 * The contents of this file are subject to the Mozilla Public
 *
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
 * Agency. Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency. All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti*/
package eionet.cr.dao.helpers;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import eionet.cr.util.Bindings;
import eionet.cr.util.SortOrder;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.pagination.PagingRequest;

/**
 * Abstract helper class for constructing queries. Contains methods for building paging, sorting and inference parts.
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 * 
 */
public abstract class AbstractSearchHelper implements SearchHelper {

    /** */
    protected PagingRequest pagingRequest;
    protected SortingRequest sortingRequest;

    /** */
    protected String sortPredicate;
    protected String sortOrder;

    /**
     * 
     * @param pagingRequest
     * @param sortingRequest
     */
    protected AbstractSearchHelper(PagingRequest pagingRequest, SortingRequest sortingRequest) {

        this.pagingRequest = pagingRequest;
        this.sortingRequest = sortingRequest;
        if (sortingRequest != null) {
            sortPredicate = sortingRequest.getSortingColumnName();
            sortOrder =
                    sortingRequest.getSortOrder() == null ? SortOrder.ASCENDING.toSQL() : sortingRequest.getSortOrder().toSQL();
        }
    }

    /**
     * 
     * @param inParams
     * @return String
     */
    public String getQuery(List<Object> inParams) {

        String query = StringUtils.isBlank(sortPredicate) ? getUnorderedQuery(inParams) : getOrderedQuery(inParams);

        if (pagingRequest != null) {
            return new StringBuffer(query).append(" limit ").append(pagingRequest.getItemsPerPage()).append(" offset ")
                    .append(pagingRequest.getOffset()).toString();
        } else {
            return query;
        }
    }

    /**
     * 
     * @param inParams
     * @return
     */
    protected abstract String getOrderedQuery(List<Object> inParams);

    /**
     * 
     * @param inParams
     * @return String
     */
    public abstract String getUnorderedQuery(List<Object> inParams);

    /**
     * 
     * @param inParams
     * @return String
     */
    public abstract String getCountQuery(List<Object> inParams);

    /**
     * Default implementation of getQueryBindings. Returns null if not specified in the helper implementation.
     * 
     * @return prepared query bindings
     */
    @Override
    public Bindings getQueryBindings() {
        return null;
    }

}
