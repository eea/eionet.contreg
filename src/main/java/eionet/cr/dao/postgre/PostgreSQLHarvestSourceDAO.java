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

import java.util.List;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.pagination.PaginationRequest;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class PostgreSQLHarvestSourceDAO extends PostgreSQLBaseDAO implements HarvestSourceDAO{

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

	public List<HarvestSourceDTO> getHarvestSources(String searchString,
			PaginationRequest pageRequest, SortingRequest sortingRequest)
			throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}

	public List<HarvestSourceDTO> getHarvestSourcesFailed(String filterString,
			PaginationRequest pageRequest, SortingRequest sortingRequest)
			throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}

	public List<HarvestSourceDTO> getHarvestSourcesUnavailable(
			String searchString, PaginationRequest pageRequest,
			SortingRequest sortingRequest) throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}

	public List<HarvestSourceDTO> getHarvestTrackedFiles(String searchString,
			PaginationRequest pageRequest, SortingRequest sortingRequest)
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
