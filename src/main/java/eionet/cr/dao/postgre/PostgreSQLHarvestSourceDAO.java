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
package eionet.cr.dao.postgre;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.readers.HarvestSourceDTOReader;
import eionet.cr.util.Pair;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.pagination.PagingRequest;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class PostgreSQLHarvestSourceDAO extends PostgreSQLBaseDAO implements HarvestSourceDAO{

	/** */
	private static final String getSourcesSQL = 
		"SELECT * FROM HARVEST_SOURCE WHERE TRACKED_FILE = 'N' AND COUNT_UNAVAIL = 0 AND URL NOT IN (SELECT URL FROM REMOVE_SOURCE_QUEUE) ";
	private static final String searchSourcesSQL = 
		"SELECT * FROM HARVEST_SOURCE WHERE TRACKED_FILE = 'N' AND COUNT_UNAVAIL = 0 AND URL like (?) AND URL NOT IN (SELECT URL FROM REMOVE_SOURCE_QUEUE) ";

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HarvestSourceDAO#getHarvestSources(java.lang.String, eionet.cr.util.PagingRequest, eionet.cr.util.SortingRequest)
	 */
    public Pair<Integer,List<HarvestSourceDTO>> getHarvestSources(String searchString, PagingRequest pagingRequest, SortingRequest sortingRequest)
    		throws DAOException {
    	
    	if (pagingRequest == null) {
    		throw new IllegalArgumentException("Pagination request cannot be null");
    	}
    	
    	return getSources(
    			StringUtils.isBlank(searchString)
    					? getSourcesSQL
    					: searchSourcesSQL,
				searchString,
				pagingRequest,
				sortingRequest);	
    }

    /**
     * 
     * @param sql
     * @param searchString
     * @param pagingRequest
     * @param sortingRequest
     * @return
     * @throws DAOException
     */
    private Pair<Integer,List<HarvestSourceDTO>> getSources(String sql, String searchString,
    		PagingRequest pagingRequest, SortingRequest sortingRequest) throws DAOException {
        	
    	List<Object> paramValues = new LinkedList<Object>();
    	if (!StringUtils.isBlank(searchString)) {
    		paramValues.add(searchString);
    	}
    	
    	if (sortingRequest!=null && sortingRequest.getSortingColumnName()!=null) {
    		sql += " ORDER BY " +
    		   sortingRequest.getSortingColumnName() + " " + sortingRequest.getSortOrder().toSQL();
    	}
    	else {
    		//in case no sorting request is present, use default one.
    		sql += " ORDER BY URL "; 
    	}
    	
//    	List<Object> limitParams =  pagingRequest.getLimitParams();
//    	if (limitParams != null) {
//    		sql += " LIMIT ?, ? ";
//    		paramValues.addAll(limitParams);
//    	}
//    	Pair<List<HarvestSourceDTO>, Integer> result = executeQueryWithRowCount(
//    			sql, paramValues, new HarvestSourceDTOReader());
//    	pagingRequest.setMatchCount(result.getRight());
//    	return result.getLeft();
    	return null;
    }

	public Integer addSource(HarvestSourceDTO source, String user)
			throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}

	public void addSourceIgnoreDuplicate(HarvestSourceDTO source, String user)
			throws DAOException {
		// TODO Auto-generated method stub
		
	}

	public void deleteHarvestHistory(int neededToRemain) throws DAOException {
		// TODO Auto-generated method stub
		
	}

	public void deleteOrphanSources() throws DAOException {
		// TODO Auto-generated method stub
		
	}

	public void deleteSourceByUrl(String url) throws DAOException {
		// TODO Auto-generated method stub
		
	}

	public void editSource(HarvestSourceDTO source) throws DAOException {
		// TODO Auto-generated method stub
		
	}

	public HarvestSourceDTO getHarvestSourceById(Integer harvestSourceID)
			throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}

	public HarvestSourceDTO getHarvestSourceByUrl(String url)
			throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HarvestSourceDAO#getHarvestSourcesFailed(java.lang.String, eionet.cr.util.PagingRequest, eionet.cr.util.SortingRequest)
	 */
	public Pair<Integer, List<HarvestSourceDTO>> getHarvestSourcesFailed(String filterString,
			PagingRequest pagingRequest, SortingRequest sortingRequest)
			throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HarvestSourceDAO#getHarvestSourcesUnavailable(java.lang.String, eionet.cr.util.PagingRequest, eionet.cr.util.SortingRequest)
	 */
	public Pair<Integer, List<HarvestSourceDTO>> getHarvestSourcesUnavailable(
			String searchString, PagingRequest pagingRequest,
			SortingRequest sortingRequest) throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HarvestSourceDAO#getHarvestTrackedFiles(java.lang.String, eionet.cr.util.PagingRequest, eionet.cr.util.SortingRequest)
	 */
	public Pair<Integer, List<HarvestSourceDTO>> getHarvestTrackedFiles(String searchString,
			PagingRequest pagingRequest, SortingRequest sortingRequest)
			throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}

	public List<HarvestSourceDTO> getNextScheduledSources(int numOfSegments)
			throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> getScheduledForDeletion() throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}

	public void queueSourcesForDeletion(List<String> urls) throws DAOException {
		// TODO Auto-generated method stub
		
	}

	public void updateHarvestFinished(int sourceId, Integer numStatements,
			Integer numResources, Boolean sourceAvailable, boolean failed)
			throws DAOException {
		// TODO Auto-generated method stub
		
	}

	public void updateHarvestStarted(int sourceId) throws DAOException {
		// TODO Auto-generated method stub
		
	}
}
