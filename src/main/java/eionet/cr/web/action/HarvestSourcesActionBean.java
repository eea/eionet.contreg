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
import eionet.cr.search.SearchException;
import eionet.cr.search.util.SortOrder;
import eionet.cr.util.Pair;
import eionet.cr.util.QueryString;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.pagination.Pagination;
import eionet.cr.util.pagination.PaginationRequest;
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
	
	/** */
	public static final List<Pair<String, String>> sourceTypes;
	private static final List<SearchResultColumn> columnList;
	
	static {
		sourceTypes = new LinkedList<Pair<String,String>>();
		sourceTypes.add(new Pair<String, String>(null, "Sources"));
		sourceTypes.add(new Pair<String, String>(TRACKED_FILES, "Tracked files"));
		sourceTypes.add(new Pair<String, String>(UNAVAILABLE_TYPE, "Unavaliable"));
		sourceTypes.add(new Pair<String, String>(FAILED_HARVESTS, "Failed harvests"));
		
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
	 * the string to be searched 
	 */
	private String searchString;
	

	/** */
	private String type;

	/** */
	private List<String> sourceUrl;


	/** */
	public HarvestSourcesActionBean(){
	}
	
	/** 
	 * @see eionet.cr.web.action.AbstractSearchActionBean#search()
	 * {@inheritDoc}
	 */
	@DefaultHandler
	public Resolution search() {
		try {
			String filterString = null; 
			if(!StringUtils.isEmpty(this.searchString)) {
				filterString = "%" + StringEscapeUtils.escapeSql(this.searchString) + "%";
			}
			PaginationRequest pageRequest = new PaginationRequest(getPageN(), Pagination.DEFAULT_ITEMS_PER_PAGE);
			SortingRequest sortingRequest = new SortingRequest(sortP, SortOrder.parse(sortO));
			if(StringUtils.isBlank(type)) {
				setResultList(factory.getDao(HarvestSourceDAO.class).getHarvestSources(filterString, pageRequest, sortingRequest));
			} else if (TRACKED_FILES.equals(type)) {
					setResultList(factory.getDao(HarvestSourceDAO.class).getHarvestTrackedFiles(filterString, pageRequest, sortingRequest));
			} else if (UNAVAILABLE_TYPE.equals(type)) {
					setResultList(factory.getDao(HarvestSourceDAO.class).getHarvestSourcesUnavailable(filterString, pageRequest, sortingRequest));
			} else if (FAILED_HARVESTS.equals(type)) {
				setResultList(factory.getDao(HarvestSourceDAO.class).getHarvestSourcesFailed(filterString, pageRequest, sortingRequest));
			}
			matchCount = pageRequest.getMatchCount();
			setPagination(Pagination.getPagination(
					pageRequest,
					getUrlBinding(),
					QueryString.createQueryString(getContext().getRequest())));
			
			return new ForwardResolution("/pages/sources.jsp");
		} catch (DAOException exception) {
			throw new RuntimeException("error in search", exception);
		}
	}

	
	/**
	 * 
	 * @return
	 * @throws DAOException 
	 */
	public Resolution delete() throws DAOException{
		
		if(isUserLoggedIn()){
			if (sourceUrl!=null && !sourceUrl.isEmpty()){
				factory.getDao(HarvestSourceDAO.class).queueSourcesForDeletion(sourceUrl);
				addSystemMessage("Harvest source(s) sheduled for removal!");
			}
		}
		else
			addWarningMessage(getBundle().getString("not.logged.in"));
		return search();
	}
	
	/**
	 * @throws DAOException 
	 * @throws HarvestException 
	 * 
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
	 * @return
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
	 * @return
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

	/** 
	 * @see eionet.cr.web.action.AbstractSearchActionBean#getColumns()
	 * {@inheritDoc}
	 */
	@Override
	public List<SearchResultColumn> getColumns() throws SearchException {
		return columnList;
	}

}
