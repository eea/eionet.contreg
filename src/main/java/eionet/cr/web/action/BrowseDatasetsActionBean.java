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
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        jaanus
 *        sofiageo
 */

package eionet.cr.web.action;

import eionet.cr.dao.BrowseVoidDatasetsDAO;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.util.VoidDatasetsResultRow;
import eionet.cr.util.Pair;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.web.action.factsheet.FactsheetActionBean;
import eionet.cr.web.sparqlClient.helpers.QueryCachedMap;
import eionet.cr.web.util.CustomPaginatedList;
import net.sourceforge.stripes.action.*;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Action bean that provides functions for browsing VoID (Vocabulary of Interlinked Datasets) datasets. Browsing done by two facets:
 * dct:creator and dct:subject, where "dct" stands for DublinCore Terms (http://purl.org/dc/terms/).
 *
 * @author jaanus, George Sofianos
 */
@UrlBinding("/browseDatasets.action")
public class BrowseDatasetsActionBean extends DisplaytagSearchActionBean {

    /** */
    private static final Logger LOGGER = Logger.getLogger(BrowseDatasetsActionBean.class);

    /** Default size of the result list page. */
    public static final int RESULT_LIST_PAGE_SIZE = 20;

    /** Max number of rows where a paging request's offset can start from in Virtuoso. */
    private static final int VIRTUOSO_MAX_PAGING_ROWS = 10000;

    /** Forward path to the JSP that handles the display and faceted browsing of VoID datasets. */
    private static final String BROWSE_DATASETS_JSP = "/pages/browseDatasets.jsp";

    /** The creator (http://purl.org/dc/terms/creator) to search by. */
    private List<String> creator;

    /** The subject (http://purl.org/dc/terms/subject) to search by. */
    private List<String> subject;

    /** Paginated list object based on {@link #datasets} and fed into DisplayTag's table tag in JSP. */
    private CustomPaginatedList<VoidDatasetsResultRow> paginatedList = new CustomPaginatedList<VoidDatasetsResultRow>();

    /** The found datasets */
    private List<VoidDatasetsResultRow> datasets;

    /** The list of available creators (http://purl.org/dc/terms/creator) to search by. */
    private List<String> availableCreators;

    /** The list of available subjects (http://purl.org/dc/terms/creator) to search by. */
    private List<String> availableSubjects;

    /** Indicates the number of user-selected creators before he changed his selection on the form. */
    private int prevCrtSize;

    /** Indicates the number of user-selected subjects before he changes his selection on the form. */
    private int prevSbjSize;

    /** A dummy hash-map that is easy to access in JSP in order to determine whether a creator has been selected. */
    private HashMap<String, String> selectedCreators = new HashMap<String, String>();

    /** A dummy hash-map that is easy to access in JSP in order to determine whether a subject has been selected. */
    private HashMap<String, String> selectedSubjects = new HashMap<String, String>();

    /** The substring that dcterms:title should contain if specified by user. */
    private String titleFilter;

    /** The facet option to show only harvested sources **/
    private boolean harvestedCheck;

    /**
     *
     * @return
     * @throws DAOException
     */
    @DefaultHandler
    public Resolution defaultEvent() throws DAOException {

        try {
            PagingRequest pagingRequest = PagingRequest.create(getPage(), RESULT_LIST_PAGE_SIZE);
            SortingRequest sortingRequest = new SortingRequest(getSort(), eionet.cr.util.SortOrder.parse(getDir()));

            if (pagingRequest != null) {
                boolean pageAllowed = isPageAllowed(pagingRequest);
                if (!pageAllowed) {
                    addCautionMessage("The requested page number exceeds the backend's maximum number of rows "
                            + "that can be paged through! Please narrow your search by using the tabs and filter below.");
                    return new RedirectResolution(getClass());
                }
            }
            LOGGER.trace(" - ");
            LOGGER.trace("Searching for VoID datasets, creators = " + creator + ", subjects = " + subject);

            BrowseVoidDatasetsDAO dao = DAOFactory.get().getDao(BrowseVoidDatasetsDAO.class);

            Pair<Integer, List<VoidDatasetsResultRow>> pair = null;
            pair = dao.findDatasets(creator, subject, titleFilter, harvestedCheck, pagingRequest, sortingRequest);
            int matchCount = 0;
            if (pair != null) {
                datasets = pair.getRight();
                if (datasets == null) {
                    datasets = new ArrayList<VoidDatasetsResultRow>();
                }
                matchCount = pair.getLeft() == null ? 0 : pair.getLeft().intValue();
            }
            paginatedList = new CustomPaginatedList<VoidDatasetsResultRow>(this, matchCount, datasets, RESULT_LIST_PAGE_SIZE);

            LOGGER.trace(datasets.size() + " datasets found!");
            LOGGER.trace("Populating available creators and subjects");

            // create a buffer to keep the last (n) keys
            Map availableCreatorsMap = (Map) getSession().getAttribute("availableCreatorsMap");
            Map availableSubjectsMap = (Map) getSession().getAttribute("availableSubjectsMap");

            // create session available data, simulating a simple cache
            if (availableCreatorsMap == null) {
                availableCreatorsMap = new QueryCachedMap<List<String>, List<String>>();
            }
            if (availableSubjectsMap == null) {
                availableSubjectsMap = new QueryCachedMap<List<String>, List<String>>();
            }

            if (isCreatorsChanged()) {
                LOGGER.trace("Creators changed");

                if (!availableSubjectsMap.containsKey(creator)) {
                    availableSubjectsMap.put(creator,dao.findSubjects(creator));
                }

                if (!availableCreatorsMap.containsKey(null)) {
                    availableCreatorsMap.put(null,dao.findCreators(null));
                }
                availableCreators = (List) availableCreatorsMap.get(null);
                availableSubjects = (List) availableSubjectsMap.get(creator);

            } else if (isSubjectsChanged()) {
                LOGGER.trace("Subjects changed");

                if (!availableCreatorsMap.containsKey(subject)) {
                    availableCreatorsMap.put(subject,dao.findCreators(subject));
                }

                if (!availableSubjectsMap.containsKey(null)) {
                    availableSubjectsMap.put(null,dao.findSubjects(null));
                }
                availableCreators = (List) availableCreatorsMap.get(subject);
                availableSubjects = (List) availableSubjectsMap.get(null);

            } else {
                if (!availableCreatorsMap.containsKey(subject)) {
                    availableCreatorsMap.put(subject,dao.findCreators(subject));
                }

                if (!availableSubjectsMap.containsKey(creator)) {
                    availableSubjectsMap.put(creator,dao.findSubjects(creator));
                }
                availableCreators = (List) availableCreatorsMap.get(subject);
                availableSubjects = (List) availableSubjectsMap.get(creator);
            }

            getSession().setAttribute("availableCreatorsMap", availableCreatorsMap);
            getSession().setAttribute("availableSubjectsMap", availableSubjectsMap);

            beforeRender();
            return new ForwardResolution(BROWSE_DATASETS_JSP);
        } catch (DAOException exception) {
            throw new RuntimeException("error in search", exception);
        }
    }

    /**
     * Assign and prepare some necessary fields for the JSP render.
     */
    private void beforeRender() {

        if (creator != null) {
            for (String crt : creator) {
                selectedCreators.put(crt, crt);
            }
        }

        if (subject != null) {
            for (String sbj : subject) {
                selectedSubjects.put(sbj, sbj);
            }
        }

        prevSbjSize = subject == null ? 0 : subject.size();
        prevCrtSize = creator == null ? 0 : creator.size();

        Collections.sort(availableCreators);
        Collections.sort(availableSubjects);
    }

    /**
     * Returns true if user changed his creators' selection, otherwise return false.
     *
     * @return the boolean as described
     */
    private boolean isCreatorsChanged() {
        int crtSize = creator == null ? 0 : creator.size();
        return crtSize != prevCrtSize;
    }

    /**
     * Returns true if user changed his subjects' selection, otherwise return false.
     *
     * @return the boolean as described
     */
    private boolean isSubjectsChanged() {
        int sbjSize = subject == null ? 0 : subject.size();
        return sbjSize != prevSbjSize;
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
    public CustomPaginatedList<VoidDatasetsResultRow> getPaginatedList() {
        return paginatedList;
    }

    /**
     * @return the datasets
     */
    public List<VoidDatasetsResultRow> getDatasets() {
        return datasets;
    }

    /**
     * Returns the Java class object of the {@link FactsheetActionBean}. Used for building refactoring-safe links to that bean in
     * JSP page(s).
     *
     * @return
     */
    public Class getFactsheetActionBeanClass() {
        return FactsheetActionBean.class;
    }

    /**
     * @return the availableCreators
     */
    public List<String> getAvailableCreators() {
        return availableCreators;
    }

    /**
     * @return the availableSubjects
     */
    public List<String> getAvailableSubjects() {
        return availableSubjects;
    }

    /**
     * @return the creator
     */
    public List<String> getCreator() {
        return creator;
    }

    /**
     * @param creator the creator to set
     */
    public void setCreator(List<String> creator) {
        this.creator = creator;
    }

    /**
     * @return the subject
     */
    public List<String> getSubject() {
        return subject;
    }

    /**
     * @param subject the subject to set
     */
    public void setSubject(List<String> subject) {
        this.subject = subject;
    }

    /**
     * @return the prevCrtSize
     */
    public int getPrevCrtSize() {
        return prevCrtSize;
    }

    /**
     * @param prevCrtSize the prevCrtSize to set
     */
    public void setPrevCrtSize(int prevCrtSize) {
        this.prevCrtSize = prevCrtSize;
    }

    /**
     * @return the prevSbjSize
     */
    public int getPrevSbjSize() {
        return prevSbjSize;
    }

    /**
     * @param prevSbjSize the prevSbjSize to set
     */
    public void setPrevSbjSize(int prevSbjSize) {
        this.prevSbjSize = prevSbjSize;
    }

    /**
     * @return the selectedCreators
     */
    public HashMap<String, String> getSelectedCreators() {
        return selectedCreators;
    }

    /**
     * @return the selectedSubjects
     */
    public HashMap<String, String> getSelectedSubjects() {
        return selectedSubjects;
    }

    /**
     * @return the titleFilter
     */
    public String getTitleFilter() {
        return titleFilter;
    }

    /**
     * @param titleFilter the titleFilter to set
     */
    public void setTitleFilter(String titleFilter) {
        this.titleFilter = titleFilter;
    }

    /**
     * @return true or false for already harvested state
     */
    public boolean getHarvestedCheck() { return harvestedCheck; }

    /**
     * @param harvestedCheck set harvested state check true or false
     */
    public void setHarvestedCheck(boolean harvestedCheck) { this.harvestedCheck = harvestedCheck; }

}
