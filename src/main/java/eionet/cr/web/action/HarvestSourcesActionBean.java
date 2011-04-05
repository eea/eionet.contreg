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
import eionet.cr.harvest.HarvestException;
import eionet.cr.harvest.scheduled.UrgentHarvestQueue;
import eionet.cr.util.Pair;
import eionet.cr.util.SortOrder;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.pagination.Pagination;
import eionet.cr.util.pagination.PagingRequest;
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
    private static final String TRACKED_FILES = "tracked_file";
    private static final String FAILED_HARVESTS = "failed";
    private static final String SCHEMAS = "schemas";

    /** */
    private static final String[] EXCLUDE_FROM_SORT_AND_PAGING_URLS = {"harvest", "delete", "sourceUrl"};

    /** */
    public static final List<Pair<String, String>> sourceTypes;
    private static final List<SearchResultColumn> columnList;

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
    static {
        sourceTypes = new LinkedList<Pair<String,String>>();
        sourceTypes.add(new Pair<String, String>(null, "Priority"));
        sourceTypes.add(new Pair<String, String>(TRACKED_FILES, "Tracked files"));
        sourceTypes.add(new Pair<String, String>(UNAVAILABLE_TYPE, "Unavaliable"));
        sourceTypes.add(new Pair<String, String>(FAILED_HARVESTS, "Failed harvests"));
        sourceTypes.add(new Pair<String, String>(SCHEMAS, "Schemas"));

        columnList = new LinkedList<SearchResultColumn>();
        GenericColumn checkbox = new GenericColumn();
        checkbox.setTitle("");
        checkbox.setSortable(false);
        checkbox.setEscapeXml(false);
        columnList.add(checkbox);

        HarvestSourcesColumn urlColumn = new HarvestSourcesColumn(false);
        urlColumn.setSortable(true);
        urlColumn.setTitle("URL");
        urlColumn.setEscapeXml(false);
        columnList.add(urlColumn);

        HarvestSourcesColumn dateColumn= new HarvestSourcesColumn(true);
        dateColumn.setSortable(true);
        dateColumn.setTitle("Last harvest");
        columnList.add(dateColumn);

    }

    /**
     * @see eionet.cr.web.action.AbstractSearchActionBean#search()
     */
    @DefaultHandler
    public Resolution search() throws DAOException {

        try {
            String filterString = null;
            if(!StringUtils.isEmpty(this.searchString)) {
                filterString = "%" + StringEscapeUtils.escapeSql(this.searchString) + "%";
            }

            PagingRequest pagingRequest = PagingRequest.create(getPageN());
            SortingRequest sortingRequest = new SortingRequest(sortP, SortOrder.parse(sortO));

            Pair<Integer,List<HarvestSourceDTO>> pair = null;
            if(StringUtils.isBlank(type)) {
                pair = factory.getDao(HarvestSourceDAO.class).getHarvestSources(
                        filterString, pagingRequest, sortingRequest);
            }
            else if (TRACKED_FILES.equals(type)) {
                pair = factory.getDao(HarvestSourceDAO.class).getHarvestTrackedFiles(
                        filterString, pagingRequest, sortingRequest);
            }
            else if (UNAVAILABLE_TYPE.equals(type)) {
                pair = factory.getDao(HarvestSourceDAO.class).getHarvestSourcesUnavailable(
                        filterString, pagingRequest, sortingRequest);
            }
            else if (FAILED_HARVESTS.equals(type)) {
                pair = factory.getDao(HarvestSourceDAO.class).getHarvestSourcesFailed(
                        filterString, pagingRequest, sortingRequest);
            }
            else if (SCHEMAS.equals(type)) {
                //Get comma separated sources that are included into inferencing ruleset
                String sourceUris = factory.getDao(HarvestSourceDAO.class).getSourcesInInferenceRules();
                pair = factory.getDao(HarvestSourceDAO.class).getInferenceSources(
                        filterString, pagingRequest, sortingRequest, sourceUris);
            }

            if (pair!=null){
                resultList = pair.getRight();
                if (resultList==null){
                    resultList = new LinkedList<HarvestSourceDTO>();
                }
                matchCount = pair.getLeft();
            }
            else{
                matchCount = 0;
            }

            setPagination(Pagination.createPagination(
                    matchCount, pagingRequest.getPageNumber(), this));

            return new ForwardResolution("/pages/sources.jsp");
        }
        catch (DAOException exception) {
            throw new RuntimeException("error in search", exception);
        }
    }


    /**
     *
     * @return Resolution
     * @throws DAOException
     */
    public Resolution delete() throws DAOException{

        if(isUserLoggedIn()){
            if (sourceUrl!=null && !sourceUrl.isEmpty()){
                
                //An authenticated user can delete sources he own. An administrator can delete any source. 
                //A priority source can not be deleted. The administrator must first change it to a non-priority source, then delete it. 
                List<String> sourcesToBeDeleted = new ArrayList<String>();
                List<String> notOwner = new ArrayList<String>();
                List<String> prioritySources = new ArrayList<String>();
                
                for (String uri : sourceUrl) {
                    HarvestSourceDTO source = factory.getDao(HarvestSourceDAO.class).getHarvestSourceByUrl(uri);
                    if (source != null) {
                        if (source.isPrioritySource()) {
                            prioritySources.add(uri);
                        } else if (getUser().isAdministrator() || (source.getOwner() != null && source.getOwner().equals(getUserName()))) { 
                            sourcesToBeDeleted.add(uri);
                        } else {
                            notOwner.add(uri);
                        }
                    }
                }
                
                factory.getDao(HarvestSourceDAO.class).queueSourcesForDeletion(sourcesToBeDeleted);
                
                if (sourcesToBeDeleted != null && !sourcesToBeDeleted.isEmpty()) {
                    StringBuffer msg = new StringBuffer();
                    msg.append("Following source(s) were sheduled for removal: <ul>");
                    for (String uri : sourcesToBeDeleted) {
                        msg.append("<li>").append(uri).append("</li>");
                    }
                    msg.append("</ul>");
                    addSystemMessage(msg.toString());
                }
                
                StringBuffer warnings = new StringBuffer();
                if (prioritySources != null && !prioritySources.isEmpty()) {
                    warnings.append("Following source(s) could not be deleted because they are Priority sources: <ul>");
                    for (String uri : prioritySources) {
                        warnings.append("<li>").append(uri).append("</li>");
                    }
                    warnings.append("</ul>");
                }
                if (notOwner != null && !notOwner.isEmpty()) {
                    warnings.append("Following source(s) could not be deleted because you are not the owner of these sources: <ul>");
                    for (String uri : notOwner) {
                        warnings.append("<li>").append(uri).append("</li>");
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
    public Resolution harvest() throws DAOException, HarvestException{

        if(isUserLoggedIn()){
            if (sourceUrl!=null && !sourceUrl.isEmpty()){
                UrgentHarvestQueue.addPullHarvests(sourceUrl);
                if (sourceUrl.size()==1)
                    addSystemMessage("The source has been scheduled for urgent harvest!");
                else
                    addSystemMessage("The sources have been scheduled for urgent harvest!");
            }
        }
        else
            addWarningMessage(getBundle().getString("not.logged.in"));

        return search();
    }

    /**
     *
     * @return List<Pair<String, String>>
     */
    public List<Pair<String, String>> getSourceTypes(){
        return sourceTypes;
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
    public String getPagingUrl(){

        String urlBinding = getUrlBinding();
        if (urlBinding.startsWith("/")){
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
     * @see eionet.cr.web.action.AbstractSearchActionBean#getColumns()
     */
    public List<SearchResultColumn> getColumns() throws DAOException {
        return columnList;
    }

    /**
     *
     */
    public String[] excludeFromSortAndPagingUrls(){
        return EXCLUDE_FROM_SORT_AND_PAGING_URLS;
    }


    public List<String> getSources() {
        return sources;
    }


    public void setSources(List<String> sources) {
        this.sources = sources;
    }
}
