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
package eionet.cr.dao;

import java.util.List;

import eionet.cr.dto.HarvestDTO;

/**
 * 
 * @author heinljab
 *
 */
public interface HarvestDAO extends DAO {

	/**
	 * 
	 * @param harvestDTO
	 */
	public int insertStartedHarvest(int harvestSourceId, String harvestType, String user, String status) throws DAOException;
	
	/**
	 * 
	 * @param harvestDTO
	 * @throws DAOException
	 */
	public void updateFinishedHarvest(int harvestId, String status, int totStatements,
			int totResources, int litStatements, int encSchemes) throws DAOException;
	
	/**
	 * 
	 * @param harvestSourceId
	 */
	public List<HarvestDTO> getHarvestsBySourceId(Integer harvestSourceId) throws DAOException;

	/**
	 * 
	 * @param harvestId
	 * @return
	 * @throws DAOException
	 */
    public HarvestDTO getHarvestById(Integer harvestId) throws DAOException;
    
    /**
     * 
     * @param harvestSourceId
     * @return
     * @throws DAOException
     */
    public HarvestDTO getLastHarvestBySourceId(Integer harvestSourceId) throws DAOException;
}
