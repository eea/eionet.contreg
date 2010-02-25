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
package eionet.cr.dao.readers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import eionet.cr.dto.HarvestBaseDTO;
import eionet.cr.dto.HarvestDTO;
import eionet.cr.util.sql.ResultSetListReader;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class HarvestWithMessageTypesReader extends ResultSetListReader<HarvestDTO> {
	
	/** */
	private List<HarvestDTO> resultList = new ArrayList<HarvestDTO>();
	
	/** */
	private HarvestDTO curHarvest;
	
	/** */
	private int maxDistinctHarvests;

	/**
	 * 
	 * @param maxDistinctHarvests
	 */
	public HarvestWithMessageTypesReader(int maxDistinctHarvests){
		this.maxDistinctHarvests = maxDistinctHarvests;
	}
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.util.sql.ResultSetBaseReader#readRow(java.sql.ResultSet)
	 */
	public void readRow(ResultSet rs) throws SQLException {
		
		Integer harvestId = rs.getInt("HARVEST.HARVEST_ID");
		if (curHarvest==null || curHarvest.getHarvestId().equals(harvestId)==false){ // if first row or new harvest
			
			// create new harvest object and add it to the list, but only if the list has not reached it's max allowed size yet
			if (resultList.size() >= maxDistinctHarvests){
				return;
			}
				
			curHarvest = new HarvestDTO();
			resultList.add(curHarvest);
			
			curHarvest.setHarvestId(harvestId);
			curHarvest.setHarvestSourceId(new Integer(rs.getInt("HARVEST.HARVEST_SOURCE_ID")));
			
			curHarvest.setHarvestType(rs.getString("HARVEST.TYPE"));
			curHarvest.setUser(rs.getString("HARVEST_USER"));
			curHarvest.setStatus(rs.getString("HARVEST.STATUS"));
			
			curHarvest.setDatetimeStarted(rs.getTimestamp("HARVEST.STARTED"));
			curHarvest.setDatetimeFinished(rs.getTimestamp("HARVEST.FINISHED"));
			
			curHarvest.setTotalResources(new Integer(rs.getInt("HARVEST.TOT_RESOURCES")));
			curHarvest.setEncodingSchemes(new Integer(rs.getInt("HARVEST.ENC_SCHEMES")));
			curHarvest.setTotalStatements(new Integer(rs.getInt("HARVEST.TOT_STATEMENTS")));
			curHarvest.setLitObjStatements(new Integer(rs.getInt("HARVEST.LIT_STATEMENTS")));
		}
		
		HarvestBaseDTO.addMessageType(curHarvest, rs.getString("HARVEST_MESSAGE.TYPE"));
	}

	/**
	 * @return the resultList
	 */
	public List<HarvestDTO> getResultList() {
		return resultList;
	}
}
