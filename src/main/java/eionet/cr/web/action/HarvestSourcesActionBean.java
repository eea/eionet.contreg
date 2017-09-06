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
package eionet.cr.web.action;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.harvest.CurrentHarvests;
import eionet.cr.harvest.HarvestException;
import eionet.cr.harvest.scheduled.UrgentHarvestQueue;
import eionet.cr.util.Pair;
import eionet.cr.util.SortOrder;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.URLUtil;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.web.action.factsheet.FactsheetActionBean;
import eionet.cr.web.action.source.ViewSourceActionBean;
import eionet.cr.web.security.CRUser;
import eionet.cr.web.util.CustomPaginatedList;
import eionet.cr.web.util.columns.GenericColumn;
import eionet.cr.web.util.columns.HarvestSourcesColumn;
import eionet.cr.web.util.columns.SearchResultColumn;

/**
 * @author altnyris
 *
 */
@UrlBinding("/sources.action")
public class HarvestSourcesActionBean extends DisplaytagSearchActionBean {

    /** Default size of the deletion queue result list page. */
    public static final int RESULT_LIST_PAGE_SIZE = 15;

    /** Max number of rows where a paging request's offset can start from in Virtuoso. */
    private static final int VIRTUOSO_MAX_PAGING_ROWS = 10000;

    /** Request parameters to exclude from paging and sorting URLs. */
    private static final String[] EXCLUDE_FROM_SORT_AND_PAGING_URLS = {"harvest", "delete", "sourceUrl"};

    /** The string to search harvest sources by. */
    private String searchString;

    /** Indicates the currently selected tab on harvest sources list. */
    private HarvestSourceType type = HarvestSourceType.TRACKED;

    /** The list of source URLs selected by user for deletion or possible other operations. */
    private List<String> sourceUrl;

    /** The result list of harvest sources matching the filtering criteria and requested paging+sorting. */
    private List<HarvestSourceDTO> resultList = new ArrayList<HarvestSourceDTO>();

    /** Paginated list object based on {@link #resultList} and fed into DisplayTag's table tag in JSP. */
    private CustomPaginatedList<HarvestSourceDTO> paginatedList = new CustomPaginatedList<HarvestSourceDTO>();

    /**
     * Default action.
     *
     * @return The resolution to go to.
     * @throws DAOException If any sort of data access error happens.
     */
    @DefaultHandler
    public Resolution search() throws DAOException {

        try {
            PagingRequest pagingRequest = PagingRequest.create(getPage(), RESULT_LIST_PAGE_SIZE);
            SortingRequest sortingRequest = new SortingRequest(getSort(), SortOrder.parse(getDir()));

            if (pagingRequest != null) {
                boolean pageAllowed = isPageAllowed(pagingRequest);
                if (!pageAllowed) {
                    addCautionMessage("The requested page number exceeds the backend's maximum number of rows "
                            + "that can be paged through! Please narrow your search by using the tabs and filter below.");
                    return new RedirectResolution(getClass());
                }
            }

            String filterString = null;
            if (!StringUtils.isEmpty(this.searchString)) {
                this.searchString = URLUtil.escapeIRI(this.searchString);
                filterString = "%" + StringEscapeUtils.escapeSql(this.searchString) + "%";
            }

            Pair<Integer, List<HarvestSourceDTO>> pair = null;
            HarvestSourceDAO harvestSourceDAO = factory.getDao(HarvestSourceDAO.class);
            if (type == null || HarvestSourceType.TRACKED.equals(type)) {
                if (type == null) {
                    type = HarvestSourceType.TRACKED;
                }
                pair = harvestSourceDAO.getHarvestSources(filterString, pagingRequest, sortingRequest);
            } else if (HarvestSourceType.PRIORITY.equals(type)) {
                pair = harvestSourceDAO.getPrioritySources(filterString, pagingRequest, sortingRequest);
            } else if (HarvestSourceType.UNAVAILABLE.equals(type)) {
                pair = harvestSourceDAO.getHarvestSourcesUnavailable(filterString, pagingRequest, sortingRequest);
            } else if (HarvestSourceType.FAILED.equals(type)) {
                pair = harvestSourceDAO.getHarvestSourcesFailed(filterString, pagingRequest, sortingRequest);
            } else if (HarvestSourceType.UNAUHTORIZED.equals(type)) {
                pair = harvestSourceDAO.getHarvestSourcesUnauthorized(filterString, pagingRequest, sortingRequest);
            } else if (HarvestSourceType.ENDPOINT.equals(type)) {
                pair = harvestSourceDAO.getRemoteEndpoints(filterString, pagingRequest, sortingRequest);
            }

            int matchCount = 0;
            if (pair != null) {
                resultList = pair.getRight();
                if (resultList == null) {
                    resultList = new ArrayList<HarvestSourceDTO>();
                }
                matchCount = pair.getLeft() == null ? 0 : pair.getLeft().intValue();
            }

            paginatedList = new CustomPaginatedList<HarvestSourceDTO>(this, matchCount, resultList, RESULT_LIST_PAGE_SIZE);
            return new ForwardResolution("/pages/sources.jsp");
        } catch (DAOException exception) {
            throw new RuntimeException("error in search", exception);
        }
    }

    /**
     * Indicates if user can delete the given harvest source.
     *
     * @param harvestSourceDTO The given harvest source.
     * @return True/false flag.
     */
    public boolean userCanDelete(HarvestSourceDTO harvestSourceDTO) {

        boolean result = false;

        // only non-priority sources can be deleted
        // (a priority source must be first turned into non-priority to delete it)
        if (harvestSourceDTO != null && !harvestSourceDTO.isPrioritySource()) {

            String sourceOwner = harvestSourceDTO.getOwner();
            CRUser user = getUser();

            // should the source's owner be unspecified, any authenticated user can delete it,
            // otherwise an authenticated user can delete if he is an administrator or the source's owner
            if (StringUtils.isBlank(sourceOwner) && user != null) {
                result = true;
            } else if (user != null && (user.isAdministrator() || user.getUserName().equals(sourceOwner))) {
                result = true;
            }
        }

        return result;
    }

    /**
     * Delete action for deleting user-selected sources.
     *
     * @return The resolution to go to.
     * @throws DAOException If data access error occurs.
     */
    public Resolution delete() throws DAOException {

        if (isUserLoggedIn()) {
            if (sourceUrl != null && !sourceUrl.isEmpty()) {

                // An authenticated user can delete sources he own. An
                // administrator can delete any source.
                // A priority source can not be deleted. The administrator must
                // first change it to a non-priority source, then delete it.

                LinkedHashSet<String> sourcesToDelete = new LinkedHashSet<String>();
                LinkedHashSet<String> notOwnedSources = new LinkedHashSet<String>();
                LinkedHashSet<String> prioritySources = new LinkedHashSet<String>();
                LinkedHashSet<String> currentlyHarvested = new LinkedHashSet<String>();

                for (String url : sourceUrl) {

                    HarvestSourceDTO source = factory.getDao(HarvestSourceDAO.class).getHarvestSourceByUrl(url);
                    if (source != null) {

                        if (CurrentHarvests.contains(url)) {
                            currentlyHarvested.add(url);
                        } else {
                            if (userCanDelete(source)) {
                                sourcesToDelete.add(url);
                            } else if (source.isPrioritySource()) {
                                prioritySources.add(url);
                            } else if (!getUserName().equals(source.getOwner())) {
                                notOwnedSources.add(url);
                            }
                        }
                    }
                }

                logger.debug("Deleting the following sources: " + sourcesToDelete);
                factory.getDao(HarvestSourceDAO.class).removeHarvestSources(sourcesToDelete);

                if (!sourcesToDelete.isEmpty()) {
                    StringBuffer msg = new StringBuffer();
                    msg.append("The following sources were successfully removed from the system: <ul>");
                    for (String uri : sourcesToDelete) {
                        msg.append("<li>").append(uri).append("</li>");
                    }
                    msg.append("</ul>");
                    addSystemMessage(msg.toString());
                }

                StringBuffer warnings = new StringBuffer();
                if (!prioritySources.isEmpty()) {
                    warnings.append("The following sources could not be deleted, because they are priority sources: <ul>");
                    for (String url : prioritySources) {
                        warnings.append("<li>").append(url).append("</li>");
                    }
                    warnings.append("</ul>");
                }
                if (!notOwnedSources.isEmpty()) {
                    warnings.append("The following sources could not be deleted, because you are not their owner: <ul>");
                    for (String url : notOwnedSources) {
                        warnings.append("<li>").append(url).append("</li>");
                    }
                    warnings.append("</ul>");
                }
                if (!currentlyHarvested.isEmpty()) {
                    warnings.append("The following sources could not be deleted, because they are curently being harvested: <ul>");
                    for (String url : currentlyHarvested) {
                        warnings.append("<li>").append(url).append("</li>");
                    }
                    warnings.append("</ul>");
                }

                if (warnings.length() > 0) {
                    addWarningMessage(warnings.toString());
                }
            }
        } else {
            addWarningMessage(getBundle().getString("not.logged.in"));
        }
        return search();
    }

    /**
     * The harvest action for harvesting selected source(s).
     *
     * @return Resolution The resolution to go to.
     * @throws DAOException If data access error occurs.
     * @throws HarvestException If harvest exception occurs.
     */
    public Resolution harvest() throws DAOException, HarvestException {

        if (isUserLoggedIn()) {
            if (sourceUrl != null && !sourceUrl.isEmpty()) {
                UrgentHarvestQueue.addPullHarvests(sourceUrl, getUserName());
                if (sourceUrl.size() == 1) {
                    addSystemMessage("The source has been scheduled for urgent harvest!");
                } else {
                    addSystemMessage("The sources have been scheduled for urgent harvest!");
                }
            }
        } else {
            addWarningMessage(getBundle().getString("not.logged.in"));
        }

        return search();
    }

    /**
     * Returns all possible values of {@link HarvestSourceType}.
     *
     * @return The array of possible values.
     */
    public HarvestSourceType[] getSourceTypes() {
        return HarvestSourceType.values();
    }

    /**
     * Sets the user-selected URLs.
     *
     * @param sourceUrl the new source url
     */
    public void setSourceUrl(List<String> sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    /**
     * Sets the search string.
     *
     * @param searchString the new search string
     */
    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    /**
     * Gets the search string.
     *
     * @return the search string
     */
    public String getSearchString() {
        return searchString;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.web.action.AbstractActionBean#excludeFromSortAndPagingUrls()
     */
    @Override
    public String[] excludeFromSortAndPagingUrls() {
        return EXCLUDE_FROM_SORT_AND_PAGING_URLS;
    }

    /**
     * Checks if Virtuoso is capable of handling the given paging request.
     * Virtuoso allows to page no more than 10000 rows. So if the given paging reuqest is such that the offset would start after
     * 10000 then Virtuoso will throw an error like this:
     * SR353: Sorted TOP clause specifies more than 361110 rows to sort. Only 10000 are allowed.
     * Either decrease the offset and/or row count or use a scrollable cursor.
     *
     * So the idea of this function is to check whether the given paging request's offset will be <= 10000 rows.
     * Returns true if yes, otherwise returns false.
     *
     * @param pagingRequest The paging request to check.
     * @return Boolean as indicated above.
     */
    private boolean isPageAllowed(PagingRequest pagingRequest) {

        if (pagingRequest == null) {
            return false;
        }

        int pageNumber = pagingRequest.getPageNumber();
        int itemsPerPage = pagingRequest.getItemsPerPage();

        int rowCount = pageNumber * itemsPerPage;
        return rowCount <= VIRTUOSO_MAX_PAGING_ROWS;
    }

    /**
     * Dynamic getter for {@link #RESULT_LIST_PAGE_SIZE}.
     *
     * @return The value.
     */
    public int getResultListPageSize() {
        return RESULT_LIST_PAGE_SIZE;
    }

    /**
     * Gets the paginated list.
     *
     * @return the paginated list
     */
    public CustomPaginatedList<HarvestSourceDTO> getPaginatedList() {
        return paginatedList;
    }

    /**
     * Returns true if the current user is allowed to make updates to the harvest sources. Otherwise returns false.
     *
     * @return True/false.
     */
    public boolean isUserAllowedUpdates() {
        CRUser user = getUser();
        return user != null && CRUser.hasPermission(getContext().getRequest().getSession(), "/registrations", "u");
    }

    /**
     * Returns the class of {@link HarvestSourceActionBean} for refactoring-safe access from JSP.
     *
     * @return The class.
     */
    public Class<HarvestSourceActionBean> getHarvestSourceBeanClass() {
        return HarvestSourceActionBean.class;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public HarvestSourceType getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type the new type
     */
    public void setType(HarvestSourceType type) {
        this.type = type;
    }

    /**
     * Classifies harvest sources by the different tabs by which the list of harevst sourecs is displayed to users.
     *
     * @author Jaanus
     */
    public static enum HarvestSourceType {

        /** The tracked files. */
        TRACKED("Tracked files"),

        /** The priority sources. */
        PRIORITY("Priority"),

        /** Sources with failed latest harvest. */
        FAILED("Failed harvests"),

        /** Unavailable sources. */
        UNAVAILABLE("Permanent failures"),

        /** Unauhtorized sources. */
        UNAUHTORIZED("Unauthorized"),

        /** Remote SPARQL endpoints. */
        ENDPOINT("SPARQL endpoints");

        /** The classifier's human-readable title. */
        private String title;

        /**
         * Simple constructor with given human-readable title.
         *
         * @param title The title.
         */
        HarvestSourceType(String title) {

            if (title == null || title.trim().length() == 0) {
                this.title = this.name();
            } else {
                this.title = title;
            }
        }

        /**
         * Gets the title.
         *
         * @return the title
         */
        public String getTitle() {
            return title;
        }
    }
}
