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
package eionet.cr.web.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.commons.lang.StringUtils;

import eionet.cr.common.Predicates;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dao.SearchDAO;
import eionet.cr.dto.SearchResultDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.Pair;
import eionet.cr.util.SortOrder;
import eionet.cr.util.SortStringPair;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.URIUtil;
import eionet.cr.util.Util;
import eionet.cr.util.export.ExportFormat;
import eionet.cr.util.export.Exporter;
import eionet.cr.util.export.XlsExporter;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.web.action.factsheet.FactsheetActionBean;
import eionet.cr.web.util.ApplicationCache;
import eionet.cr.web.util.columns.SearchResultColumn;
import eionet.cr.web.util.columns.SubjectPredicateColumn;

/**
 * @author Aleksandr Ivanov
 * @author Enriko Käsper
 */
@UrlBinding("/typeSearch.action")
public class TypeSearchActionBean extends AbstractSearchActionBean<SubjectDTO> {

    /**
     * Last action types on type search page.
     */
    private enum LastAction {
        ADD_FILTER, REMOVE_FILTER, APPLY_FILTERS, SET_COLUMNS, SEARCH, SORTING;
    }

    private static final String AVAILABLE_COLUMNS_CACHE = TypeSearchActionBean.class.getName() + ".availableColumnsCache";
    private static final String SELECTED_COLUMNS_CACHE = TypeSearchActionBean.class.getName() + ".selectedColumnsCache";
    private static final String SELECTED_FILTERS_CACHE = TypeSearchActionBean.class.getName() + ".selectedFiltersCache";
    private static final String RESULT_LIST_CACHED = TypeSearchActionBean.class.getName() + ".resultListCached";
    private static final String LAST_ACTION = TypeSearchActionBean.class.getName() + ".lastAction";
    private static final String MATCH_COUNT = TypeSearchActionBean.class.getName() + ".matchCount";
    private static final String EXACT_COUNT = TypeSearchActionBean.class.getName() + ".exactCount";

    /**
     * Default number of columns displayed on the search results.
     */
    private static final int DEFAULT_NUMBER_OF_SELECTED_COLUMNS = 5;

    private static final String PREVIOUS_TYPE = TypeSearchActionBean.class.getName() + ".previousType";
    /**
     * Type search jsp path.
     */
    private static final String TYPE_SEARCH_PATH = "/pages/typeSearch.jsp";
    /**
     * Type to search.
     */
    private String type;
    /**
     * Selected columns.
     */
    private List<String> selectedColumns;
    /**
     * Filter to add.
     */
    private String newFilter;
    /**
     * Filter to be removed.
     */
    private String clearFilter;
    /**
     * Selected filters.
     */
    private Map<String, String> selectedFilters;
    /**
     * Selected filters with labels.
     */
    private Map<String, Pair<String, String>> displayFilters;
    /**
     * show URI as resource identifier
     */
    private boolean uriResourceIdentifier;
    /**
     * Export format selection.
     */
    private String exportFormat;
    /**
     * Export columns.
     */
    private List<String> exportColumns;

    /**
     * Query string.
     */
    private String queryString;

    private boolean typesByName;

    /**
     *
     * @return Resolution.
     * @throws Exception
     */
    @DefaultHandler
    public Resolution preparePage() throws Exception {
        Enumeration<String> names = getContext().getRequest().getParameterNames();
        while (names.hasMoreElements()) {
            String next = names.nextElement();
            if (next.startsWith("removeFilter_")) {
                clearFilter = next.replaceFirst("removeFilter_", "");
                return removeFilter();
            }
        }
        setExportColumns(getSelectedColumnsFromCache());
        return new ForwardResolution(TYPE_SEARCH_PATH);
    }

    /**
     * @return a list of export formats.
     */
    public ExportFormat[] getExportFormats() {
        return ExportFormat.values();
    }

    /**
     * @return true if Excel export is available.
     */
    public boolean isShowExcelExport() {
        if (matchCount > XlsExporter.getRowsLimit()) {
            return false;
        }
        return true;
    }

    /**
     * Exports search result as a file.
     *
     * @return Resolution.
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public Resolution export() throws Exception {

        logger.trace("**************  START EXPORT REQUEST  ***********");

        restoreStateFromSession();
        ExportFormat format = ExportFormat.fromName(exportFormat);
        Exporter exporter = Exporter.getExporter(format);

        exporter.setLanguages(getAcceptedLanguages() != null ? getAcceptedLanguages() : Collections.EMPTY_LIST);
        List<Pair<String, String>> columnPairs = new LinkedList<Pair<String, String>>();

        exporter.setExportResourceUri(uriResourceIdentifier);
        selectedColumns = exportColumns == null || exportColumns.isEmpty() ? selectedColumns : exportColumns;
        for (String selectedColumn : selectedColumns) {
            columnPairs.add(new Pair<String, String>(selectedColumn, getAvailableColumns().get(selectedColumn)));
        }
        exporter.setSelectedColumns(columnPairs);
        Map<String, String> filters = new HashMap<String, String>();
        if (selectedFilters != null) {
            filters.putAll(selectedFilters);
        }
        filters.put(Predicates.RDF_TYPE, type);
        exporter.setSelectedFilters(filters);
        getContext().getResponse().setHeader("Content-Disposition", "attachment;filename=" + format.getFilename());
        getContext().getResponse().setHeader("Cache-Control", "no-cache, must-revalidate");

        return new StreamingResolution(format.getContentType(), exporter.export());
    }

    /**
     * @return
     */
    public Resolution introspect() {

        if (!StringUtils.isBlank(type)) {
            return new RedirectResolution(FactsheetActionBean.class).addParameter("uri", type);
        } else {
            return new ForwardResolution(TYPE_SEARCH_PATH);
        }
    }

    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    public Resolution addFilter() {

        Map<String, Map<String, String>> cache =
                (Map<String, Map<String, String>>) getSession().getAttribute(SELECTED_FILTERS_CACHE);
        if (cache == null) {
            cache = new HashMap<String, Map<String, String>>();
        }
        if (!cache.containsKey(type)) {
            Map<String, String> filters = new LinkedHashMap<String, String>();
            cache.put(type, filters);
        }
        cache.get(type).put(newFilter, null);
        getSession().setAttribute(SELECTED_FILTERS_CACHE, cache);
        getSession().setAttribute(LAST_ACTION, LastAction.ADD_FILTER);
        setExportColumns(getSelectedColumnsFromCache());
        return new RedirectResolution(getUrlBinding() + "?search=Search&type=" + Util.urlEncode(type));
    }

    /**
     * @return Resolution.
     */
    @SuppressWarnings("unchecked")
    public Resolution removeFilter() {

        Map<String, Map<String, String>> cache =
                (Map<String, Map<String, String>>) getSession().getAttribute(SELECTED_FILTERS_CACHE);
        if (cache != null && cache.containsKey(type) && cache.get(type) != null && cache.get(type).containsKey(clearFilter)) {
            cache.get(type).remove(clearFilter);
        }
        getSession().setAttribute(LAST_ACTION, LastAction.REMOVE_FILTER);
        setExportColumns(getSelectedColumnsFromCache());
        return new RedirectResolution(getUrlBinding() + "?search=Search&type=" + Util.urlEncode(type));
    }

    /**
     * @return Resolution.
     * @throws DAOException
     */
    public Resolution applyFilters() throws DAOException {
        if (selectedFilters != null && !selectedFilters.isEmpty()) {
            // save selected filters for future use
            Map<String, Map<String, String>> cache =
                    (Map<String, Map<String, String>>) getSession().getAttribute(SELECTED_FILTERS_CACHE);
            if (cache != null && cache.containsKey(type)) {
                cache.get(type).putAll(selectedFilters);
            }
        }
        getSession().setAttribute(LAST_ACTION, LastAction.REMOVE_FILTER);
        setExportColumns(getSelectedColumnsFromCache());
        return search();
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.web.action.AbstractSearchActionBean#search()
     */
    @Override
    public Resolution search() throws DAOException {
        logger.trace("**************  START SEARCH REQUEST  ***********");
        if (!StringUtils.isBlank(type)) {
            long startTime = System.currentTimeMillis();

            restoreStateFromSession();
            logger.trace("after restoreStateFromSession " + Util.durationSince(startTime));
            startTime = System.currentTimeMillis();

            LastAction lastAction = getLastAction();
            if (resultList == null || !(lastAction != null && lastAction.equals(LastAction.ADD_FILTER))) {

                Map<String, String> criteria = new HashMap<String, String>();
                criteria.put(Predicates.RDF_TYPE, type);
                if (selectedFilters != null && !selectedFilters.isEmpty()) {
                    for (Entry<String, String> entry : selectedFilters.entrySet()) {
                        if (!StringUtils.isBlank(entry.getValue())) {
                            criteria.put(entry.getKey(), StringUtils.trim(entry.getValue().trim()));
                        }
                    }
                }
                SearchResultDTO<SubjectDTO> searchResult =
                        DAOFactory
                        .get()
                        .getDao(SearchDAO.class)
                        .searchByTypeAndFilters(criteria, false, PagingRequest.create(getPageN()),
                                new SortingRequest(getSortP(), SortOrder.parse(getSortO())), selectedColumns);

                resultList = searchResult.getItems();
                matchCount = searchResult.getMatchCount();
                queryString = searchResult.getQuery();
                int exactRowCountLimit = DAOFactory.get().getDao(SearchDAO.class).getExactRowCountLimit();
                exactCount = exactRowCountLimit <= 0 || matchCount <= exactRowCountLimit;
            }

            // cache result list.
            getSession().setAttribute(RESULT_LIST_CACHED, resultList);
            getSession().setAttribute(LAST_ACTION, LastAction.SEARCH);
            getSession().setAttribute(MATCH_COUNT, matchCount);
            getSession().setAttribute(EXACT_COUNT, exactCount);
        }
        setExportColumns(getSelectedColumnsFromCache());
        return new ForwardResolution(TYPE_SEARCH_PATH);
    }

    /**
     * @return last action.
     */
    private LastAction getLastAction() {
        if (getContext().getRequest().getParameter("pageN") != null) {
            return LastAction.SORTING;
        } else {
            return (LastAction) getSession().getAttribute(LAST_ACTION);
        }
    }

    /**
     * Restores actionBean state from session.
     */
    private void restoreStateFromSession() throws DAOException {

        // restoring the result list
        resultList = (Collection<SubjectDTO>) getSession().getAttribute(RESULT_LIST_CACHED);
        Integer temp = (Integer) getSession().getAttribute(MATCH_COUNT);
        matchCount = temp == null ? 0 : temp.intValue();

        // check if user has previously selected some columns.
        Map<String, List<String>> cache = (Map<String, List<String>>) getSession().getAttribute(SELECTED_COLUMNS_CACHE);
        if (cache != null && cache.containsKey(type)) {
            selectedColumns = cache.get(type);
        } else {
            selectedColumns = new LinkedList<String>();
            int i = 1;
            for (Entry<String, String> pair : getAvailableColumns().entrySet()) {
                selectedColumns.add(pair.getKey());
                i++;
                if (i > DEFAULT_NUMBER_OF_SELECTED_COLUMNS) {
                    break;
                }
            }
        }

        // saving to session selected type.
        String previousType = (String) getSession().getAttribute(PREVIOUS_TYPE);
        getSession().setAttribute(PREVIOUS_TYPE, type);
        if (!type.equals(previousType)) {
            Map<String, Map<String, String>> filterCache =
                    (Map<String, Map<String, String>>) getSession().getAttribute(SELECTED_FILTERS_CACHE);
            if (filterCache != null && filterCache.containsKey(type)) {
                filterCache.remove(type);
            }
        } else {
            // check for selected filters and add labels to them
            Map<String, Map<String, String>> filterCache =
                    (Map<String, Map<String, String>>) getSession().getAttribute(SELECTED_FILTERS_CACHE);
            if (filterCache != null && filterCache.containsKey(type)) {
                selectedFilters = filterCache.get(type);
                displayFilters = new HashMap<String, Pair<String, String>>();
                Map<String, String> availableColumns = getAvailableColumns();
                for (Entry<String, String> entry : selectedFilters.entrySet()) {
                    if (availableColumns.containsKey(entry.getKey())) {
                        displayFilters.put(availableColumns.get(entry.getKey()),
                                new Pair<String, String>(entry.getKey(), entry.getValue()));
                    }
                }
            }
        }
    }

    /**
     *
     * @return
     */
    public Resolution setSearchColumns() {

        Map<String, List<String>> cache = (Map<String, List<String>>) getSession().getAttribute(SELECTED_COLUMNS_CACHE);
        if (cache == null) {
            cache = new HashMap<String, List<String>>();
        }
        if (selectedColumns != null) {
            // hardcode title into the selected columns
            if (!selectedColumns.contains(Predicates.RDFS_LABEL)) {
                selectedColumns.add(0, Predicates.RDFS_LABEL);
            }
            selectedColumns = new LinkedList<String>(selectedColumns.subList(0, selectedColumns.size()));
        }
        cache.put(type, selectedColumns);
        getSession().setAttribute(SELECTED_COLUMNS_CACHE, cache);
        getSession().setAttribute(LAST_ACTION, LastAction.SET_COLUMNS);
        return new RedirectResolution(getUrlBinding() + "?search=Search&type=" + Util.urlEncode(type));
    }

    private List<String> getSelectedColumnsFromCache() {
        Map<String, List<String>> cache = (Map<String, List<String>>) getSession().getAttribute(SELECTED_COLUMNS_CACHE);
        return cache == null ? selectedColumns : cache.get(type);
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.web.action.AbstractSearchActionBean#getColumns()
     */
    @Override
    public List<SearchResultColumn> getColumns() throws DAOException {

        List<SearchResultColumn> columns = new LinkedList<SearchResultColumn>();
        // setSelectedColumns uses POST to submit a form, we have to add needed parameters to the
        // column headers in order to be able to sort right.
        String actionParameter = "search=Search&amp;type=" + eionet.cr.util.Util.urlEncode(type);
        // add every selected column to the output
        if (selectedColumns != null && !selectedColumns.isEmpty()) {
            for (String string : selectedColumns) {
                for (Entry<String, String> pair : getAvailableColumns().entrySet()) {
                    if (pair.getKey().equals(string)) {
                        columns.add(new SubjectPredicateColumn(pair.getValue(), true, string, actionParameter));
                    }
                }
            }
        }

        return columns;

    }

    /**
     *
     * @return
     */
    public List<Pair<String, String>> getAvailableTypesNoGroup() {
        return SortStringPair.sortByLeftAsc(ApplicationCache.getTypes());
    }

    /**
     *
     * @return
     */
    public List<Pair<String, List<Pair<String, String>>>> getAvailableTypes() {
        List<Pair<String, String>> sourceTypes = ApplicationCache.getTypes();

        /**
         * Defining the returnTypes so that it contains group name as the outer Left and contents as outer Right Those groups form a
         * list that contains Pair's, each of them containing Pairs of types.
         */
        List<Pair<String, List<Pair<String, String>>>> returnTypes = new ArrayList<Pair<String, List<Pair<String, String>>>>();

        String previousPrefix = "";
        int i = 0;

        List<Pair<String, String>> groupedTypes;

        SortStringPair.sortByLeftAsc(sourceTypes);
        boolean itemsAdded = false;
        groupedTypes = new ArrayList<Pair<String, String>>();

        while (i < sourceTypes.size()) {

            String currentPrefix = "";
            // Disassembling the prefix
            if (sourceTypes.get(i).getLeft().replaceAll("[^#]", "").length() > 0) {
                // Meaning that the url contains #-char
                currentPrefix = sourceTypes.get(i).getLeft().split("#")[0];
            } else {
                // Must be split by last '/'
                String[] tokens = sourceTypes.get(i).getLeft().split("/");
                for (int j = 0; j < tokens.length - 1; j++) {
                    currentPrefix += tokens[j] + "/";
                }
            }

            if (!currentPrefix.equals(previousPrefix)) {
                // Adding previous group to return output, initializing new groupedTypes.
                if (itemsAdded) {
                    returnTypes.add(new Pair<String, List<Pair<String, String>>>(previousPrefix, groupedTypes));
                    itemsAdded = false;
                }
                groupedTypes = new ArrayList<Pair<String, String>>();
            }

            if (groupedTypes != null) {
                groupedTypes.add(sourceTypes.get(i));
                itemsAdded = true;
            }

            // Incrementing loop counter.
            i++;

            // The loop is over, last group shall be added manually in case there are items (itemsAdded == true).
            if (i == sourceTypes.size()) {
                if (itemsAdded) {
                    returnTypes.add(new Pair<String, List<Pair<String, String>>>(previousPrefix, groupedTypes));
                }
            }
            previousPrefix = currentPrefix;
        }

        return returnTypes;
    }

    /**
     *
     * @return
     */
    public List<Pair<String, String>> getAvailableTypesByName() {
        List<Pair<String, String>> sourceTypes = ApplicationCache.getTypes();

        SortStringPair.sortByRightAsc(sourceTypes);

        int i = 0;

        List<Pair<String, String>> result = new ArrayList<Pair<String, String>>();
        while (i < sourceTypes.size()) {

            String currentPrefix = "";
            // Disassembling the prefix
            if (sourceTypes.get(i).getLeft().replaceAll("[^#]", "").length() > 0) {
                // Meaning that the url contains #-char
                currentPrefix = sourceTypes.get(i).getLeft().split("#")[0];
            } else {
                // Must be split by last '/'
                String[] tokens = sourceTypes.get(i).getLeft().split("/");
                for (int j = 0; j < tokens.length - 1; j++) {
                    currentPrefix += tokens[j] + "/";
                }
            }
            String nameWithUri = sourceTypes.get(i).getRight() + " (" + currentPrefix + ")";
            String uri = sourceTypes.get(i).getLeft();

            result.add(new Pair<String, String>(uri, nameWithUri));

            // Incrementing loop counter.
            i++;
        }

        return result;
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
     * @return the selectedColumns
     */
    public List<String> getSelectedColumns() {
        return selectedColumns;
    }

    /**
     * @param selectedColumns the selectedColumns to set
     */
    public void setSelectedColumns(List<String> selectedColumns) {
        this.selectedColumns = selectedColumns;
    }

    /**
     *
     * @return
     * @throws DAOException
     */
    public Map<String, String> getAvailableColumnsSorted() throws DAOException {
        return sortByValueIgnoreCase(getAvailableColumns());
    }

    /**
     * @return the availableColumns
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getAvailableColumns() throws DAOException {

        Map<String, Map<String, String>> cache =
                (Map<String, Map<String, String>>) getSession().getAttribute(AVAILABLE_COLUMNS_CACHE);
        if (cache == null) {
            cache = new HashMap<String, Map<String, String>>();
        }
        if (!cache.containsKey(type)) {

            Map<String, String> result = new LinkedHashMap<String, String>();

            List<Pair<String, String>> usedPredicates = DAOFactory.get().getDao(HelperDAO.class).getPredicatesUsedForType(type);

            result.put(Predicates.RDFS_LABEL, "Title");
            if (usedPredicates != null && !usedPredicates.isEmpty()) {

                for (Pair<String, String> pair : usedPredicates) {
                    if (pair != null) {

                        String uri = pair.getLeft();
                        String label = pair.getRight();
                        if (StringUtils.isBlank(label) || label.equalsIgnoreCase("null")) {
                            label = URIUtil.extractURILabel(uri, uri);
                        }

                        if (!StringUtils.isBlank(label)) {
                            result.put(uri, label);
                        }
                    }
                }
            }
            // hardcode RDF_TYPE as one of the available values
            result.put(Predicates.RDF_TYPE, "type");

            cache.put(type, result);
            getSession().setAttribute(AVAILABLE_COLUMNS_CACHE, cache);
            return result;
        } else {
            return cache.get(type);
        }
    }

    /**
     * @return available filters.
     * @throws DAOException
     */
    public Map<String, String> getAvailableFilters() throws DAOException {
        Map<String, String> result = new HashMap<String, String>();
        result.putAll(getAvailableColumns());
        result.remove(Predicates.RDF_TYPE);
        if (selectedFilters != null) {
            for (String key : selectedFilters.keySet()) {
                result.remove(key);
            }
        }
        return result;
    }

    /**
     *
     * @return
     * @throws DAOException
     */
    public Map<String, String> getAvailableFiltersSorted() throws DAOException {

        return sortByValueIgnoreCase(getAvailableFilters());
    }

    /**
     * @return the selectedFilter
     */
    public String getNewFilter() {
        return newFilter;
    }

    /**
     * @param selectedFilter the selectedFilter to set
     */
    public void setNewFilter(String selectedFilter) {
        this.newFilter = selectedFilter;
    }

    /**
     * @return the selectedFilters
     */
    public Map<String, String> getSelectedFilters() {
        return selectedFilters;
    }

    /**
     * @param selectedFilters the selectedFilters to set
     */
    public void setSelectedFilters(Map<String, String> selectedFilters) {
        this.selectedFilters = selectedFilters;
    }

    /**
     * @return the removeFilter
     */
    public String getClearFilter() {
        return clearFilter;
    }

    /**
     * @param removeFilter the removeFilter to set
     */
    public void setClearFilter(String removeFilter) {
        this.clearFilter = removeFilter;
    }

    /**
     * @return the displayFilters
     */
    public Map<String, Pair<String, String>> getDisplayFilters() {
        return displayFilters;
    }

    /**
     * @return the uriResourceIdentifier
     */
    public boolean isUriResourceIdentifier() {
        return uriResourceIdentifier;
    }

    /**
     * @param uriResourceIdentifier the uriResourceIdentifier to set
     */
    public void setUriResourceIdentifier(boolean uriResourceIdentifier) {
        this.uriResourceIdentifier = uriResourceIdentifier;
    }

    /**
     * @return the exportColumns
     */
    public List<String> getExportColumns() {
        return exportColumns;
    }

    /**
     * @return the exportFormat
     */
    public String getExportFormat() {
        return exportFormat;
    }

    /**
     * set exportFormat parameter (xls, xml, ...)
     *
     * @param exportFormat
     */
    public void setExportFormat(String exportFormat) {
        this.exportFormat = exportFormat;
    }

    /**
     * @param exportColumns the exportColumns to set
     */
    public void setExportColumns(List<String> exportColumns) {
        this.exportColumns = exportColumns;
    }

    public String getQueryString() {
        return queryString;
    }

    /**
     *
     * @param map
     * @return
     */
    private Map<String, String> sortByValueIgnoreCase(Map<String, String> map) {

        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        if (map != null && !map.isEmpty()) {

            List<String> keys = new ArrayList<String>(map.keySet());
            List<String> values = new ArrayList<String>(map.values());

            List<String> sortedValues = new ArrayList<String>(map.values());
            Collections.sort(sortedValues, new CaseInsensitiveStringComparator());
            for (int i = 0; i < sortedValues.size(); i++) {
                result.put(keys.get(values.indexOf(sortedValues.get(i))), sortedValues.get(i));
            }
        }

        return result;
    }

    public boolean isTypesByName() {
        return typesByName;
    }

    public void setTypesByName(boolean typesByName) {
        this.typesByName = typesByName;
    }

    /**
     * Case-insensitive string comparator that handles null arguments.
     *
     * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
     *
     */
    private class CaseInsensitiveStringComparator implements Comparator<String> {

        /*
         * (non-Javadoc)
         *
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(String str1, String str2) {

            if (str1 == null && str2 == null) {
                return 0;
            } else if (str1 == null && str2 != null) {
                return -1;
            } else if (str1 != null && str2 == null) {
                return 1;
            } else {
                return (str1).compareToIgnoreCase(str2);
            }
        }
    }

}
