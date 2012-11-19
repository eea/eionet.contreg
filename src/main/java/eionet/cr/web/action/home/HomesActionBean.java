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
 * The Original Code is Content Registry 3.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by Zero Technologies are Copyright
 * (C) European Environment Agency. All Rights Reserved.
 *
 * Contributor(s): Enriko Käsper
 */
package eionet.cr.web.action.home;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.UserHomeDAO;
import eionet.cr.dto.UserFolderDTO;
import eionet.cr.util.Pair;
import eionet.cr.util.SortOrder;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.pagination.Pagination;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.web.action.AbstractSearchActionBean;
import eionet.cr.web.security.CRUser;
import eionet.cr.web.util.columns.SearchResultColumn;

/**
 * Stripes action generating the list of user home folders. Potentially have search capabilities.
 *
 * @author Enriko Käsper
 *
 */

@UrlBinding("/home")
public class HomesActionBean extends AbstractSearchActionBean<UserFolderDTO> {

    /** Path to JSP. */
    private static final String HOMES_PATH = "/pages/home/homes.jsp";
    /** Columns displayed in search result. */
    private static final ArrayList<SearchResultColumn> COLUMNS;

    static {
        COLUMNS = new ArrayList<SearchResultColumn>();
        SearchResultColumn col = new SearchResultColumn("User home folders", true) {

            @Override
            public String getSortParamValue() {
                // TODO Auto-generated method stub
                return "label";
            }

            @Override
            public String format(Object object) {
                return object.toString();
            }
        };

        COLUMNS.add(col);
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.web.action.AbstractSearchActionBean#search()
     */
    @Override
    @DefaultHandler
    public Resolution search() throws DAOException {

        UserHomeDAO userHomeDAO = DAOFactory.get().getDao(UserHomeDAO.class);

        PagingRequest pagingRequest = PagingRequest.create(getPageN());
        SortingRequest sortingRequest = new SortingRequest(getSortP(), SortOrder.parse(sortO));

        Pair<Integer, List<UserFolderDTO>> folders =
                userHomeDAO.getFolderContents(CRUser.rootHomeUri(), null, pagingRequest, sortingRequest, null);

        resultList = folders.getRight();
        matchCount = folders.getLeft();

        setPagination(Pagination.createPagination(matchCount, pagingRequest.getPageNumber(), this));

        return new ForwardResolution(HOMES_PATH);
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.web.action.AbstractSearchActionBean#getColumns()
     */
    @Override
    public List<SearchResultColumn> getColumns() throws DAOException {
        return COLUMNS;
    }
}
