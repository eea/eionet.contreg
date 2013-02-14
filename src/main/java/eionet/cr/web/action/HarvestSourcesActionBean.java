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
import eionet.cr.util.pagination.Pagination;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.web.security.CRUser;
import eionet.cr.web.util.columns.GenericColumn;
import eionet.cr.web.util.columns.HarvestSourcesColumn;
import eionet.cr.web.util.columns.SearchResultColumn;

/**
 * @author altnyris
 *
 */
@UrlBinding("/sources.action")
public class HarvestSourcesActionBean extends AbstractSearchActionBean<HarvestSourceDTO> {

    /** */
    private static final String UNAVAILABLE_TYPE = "unavail";
    private static final String UNAUHTORIZED_HARVESTS = "unauth";
    private static final String PRIORITY = "priority";
    private static final String FAILED_HARVESTS = "failed";
    private static final String SCHEMAS = "schemas";
    private static final String SPARQL_ENDPOINTS = "endpoints";

    /** */
    private static final String[] EXCLUDE_FROM_SORT_AND_PAGING_URLS = {"harvest", "delete", "sourceUrl"};

    /** */
    public static final List<Pair<String, String>> SOURCE_TYPES;

    /** */
    private static final GenericColumn CHECKBOX_COLUMN;
    private static final HarvestSourcesColumn URL_COLUMN;
    private static final HarvestSourcesColumn DATE_COLUMN;

    /**
     * the string to be searched
     */
    private String searchString;

    private List<String> sources;

    /** */
    private String type;

    /** */
    private List<String> sourceUrl;

    /** */
    private List<SearchResultColumn> columnList;

    /** */
    static {
        // initialize the tabs of the harvest sources page
        SOURCE_TYPES = new LinkedList<Pair<String, String>>();
        SOURCE_TYPES.add(new Pair<String, String>(null, "Tracked files"));
        SOURCE_TYPES.add(new Pair<String, String>(PRIORITY, "Priority"));
        SOURCE_TYPES.add(new Pair<String, String>(SCHEMAS, "Schemas"));
        SOURCE_TYPES.add(new Pair<String, String>(FAILED_HARVESTS, "Failed harvests"));
        SOURCE_TYPES.add(new Pair<String, String>(UNAVAILABLE_TYPE, "Permanent failures"));
        SOURCE_TYPES.add(new Pair<String, String>(UNAUHTORIZED_HARVESTS, "Unauthorized"));
        SOURCE_TYPES.add(new Pair<String, String>(SPARQL_ENDPOINTS, "SPARQL endpoints"));

        // initialize column objects that will be used as columns in the harvest sources page

        CHECKBOX_COLUMN = new GenericColumn();
        CHECKBOX_COLUMN.setTitle("");
        CHECKBOX_COLUMN.setSortable(false);
        CHECKBOX_COLUMN.setEscapeXml(false);

        URL_COLUMN = new HarvestSourcesColumn(false);
        URL_COLUMN.setSortable(true);
        URL_COLUMN.setTitle("URL");
        URL_COLUMN.setEscapeXml(false);

        DATE_COLUMN = new HarvestSourcesColumn(true);
        DATE_COLUMN.setSortable(true);
        DATE_COLUMN.setTitle("Last harvest");
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.web.action.AbstractSearchActionBean#search()
     */
    @Override
    @DefaultHandler
    public Resolution search() throws DAOException {

        try {
            String filterString = null;
            if (!StringUtils.isEmpty(this.searchString)) {
                this.searchString = URLUtil.escapeIRI(this.searchString);
                filterString = "%" + StringEscapeUtils.escapeSql(this.searchString) + "%";
            }

            PagingRequest pagingRequest = PagingRequest.create(getPageN());
            SortingRequest sortingRequest = new SortingRequest(sortP, SortOrder.parse(sortO));

            Pair<Integer, List<HarvestSourceDTO>> pair = null;
            if (StringUtils.isBlank(type)) {
                pair = factory.getDao(HarvestSourceDAO.class).getHarvestSources(filterString, pagingRequest, sortingRequest);
            } else if (PRIORITY.equals(type)) {
                pair = factory.getDao(HarvestSourceDAO.class).getPrioritySources(filterString, pagingRequest, sortingRequest);
            } else if (UNAVAILABLE_TYPE.equals(type)) {
                pair =
                        factory.getDao(HarvestSourceDAO.class).getHarvestSourcesUnavailable(filterString, pagingRequest,
                                sortingRequest);
            } else if (FAILED_HARVESTS.equals(type)) {
                pair = factory.getDao(HarvestSourceDAO.class).getHarvestSourcesFailed(filterString, pagingRequest, sortingRequest);
            } else if (UNAUHTORIZED_HARVESTS.equals(type)) {
                pair = factory.getDao(HarvestSourceDAO.class).getHarvestSourcesUnauthorized(filterString, pagingRequest, sortingRequest);
            } else if (SCHEMAS.equals(type)) {
                // Get comma separated sources that are included into
                // inferencing ruleset
                String sourceUris = factory.getDao(HarvestSourceDAO.class).getSourcesInInferenceRules();
                pair =
                        factory.getDao(HarvestSourceDAO.class).getInferenceSources(filterString, pagingRequest, sortingRequest,
                                sourceUris);
            } else if (SPARQL_ENDPOINTS.equals(type)) {
                pair = factory.getDao(HarvestSourceDAO.class).getRemoteEndpoints(filterString, pagingRequest, sortingRequest);
            }

            if (pair != null) {
                resultList = pair.getRight();
                if (resultList == null) {
                    resultList = new LinkedList<HarvestSourceDTO>();
                }
                matchCount = pair.getLeft();
            } else {
                matchCount = 0;
            }

            setPagination(Pagination.createPagination(matchCount, pagingRequest.getPageNumber(), this));

            return new ForwardResolution("/pages/sources.jsp");
        } catch (DAOException exception) {
            throw new RuntimeException("error in search", exception);
        }
    }

    /**
     *
     * @param harvestSourceDTO
     * @return
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
     *
     * @return
     * @throws DAOException
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
     * @return Resolution
     * @throws DAOException
     * @throws HarvestException
     */
    public Resolution harvest() throws DAOException, HarvestException {

        if (isUserLoggedIn()) {
            if (sourceUrl != null && !sourceUrl.isEmpty()) {
                UrgentHarvestQueue.addPullHarvests(sourceUrl);
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
     *
     * @return List<Pair<String, String>>
     */
    public List<Pair<String, String>> getSourceTypes() {
        return SOURCE_TYPES;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @param sourceUrl the sourceUrl to set
     */
    public void setSourceUrl(List<String> sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    /**
     *
     * @return String
     */
    public String getPagingUrl() {

        String urlBinding = getUrlBinding();
        if (urlBinding.startsWith("/")) {
            urlBinding = urlBinding.substring(1);
        }

        StringBuffer buf = new StringBuffer(urlBinding);
        return buf.append("?view=").toString();
    }

    /**
     * @param searchString the searchString to set
     */
    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    /**
     * @return the searchString
     */
    public String getSearchString() {
        return searchString;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.web.action.AbstractSearchActionBean#getColumns()
     */
    @Override
    public List<SearchResultColumn> getColumns() throws DAOException {

        if (columnList == null) {

            columnList = new ArrayList<SearchResultColumn>();
            // display checkbox only when current session allows update rights in registrations ACL
            if (getUser() != null && CRUser.hasPermission(getContext().getRequest().getSession(), "/registrations", "u")) {
                columnList.add(CHECKBOX_COLUMN);
            }
            columnList.add(URL_COLUMN);
            columnList.add(DATE_COLUMN);
        }

        return columnList;
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.web.action.AbstractActionBean#excludeFromSortAndPagingUrls()
     */
    @Override
    public String[] excludeFromSortAndPagingUrls() {
        return EXCLUDE_FROM_SORT_AND_PAGING_URLS;
    }

    public List<String> getSources() {
        return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
    }
}
