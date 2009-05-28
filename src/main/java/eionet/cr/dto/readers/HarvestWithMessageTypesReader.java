package eionet.cr.dto.readers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import eionet.cr.dto.HarvestDTO;
import eionet.cr.util.sql.ResultSetBaseReader;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class HarvestWithMessageTypesReader extends ResultSetBaseReader{
	
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
			curHarvest.setUser(rs.getString("HARVEST.USER"));
			curHarvest.setStatus(rs.getString("HARVEST.STATUS"));
			
			curHarvest.setDatetimeStarted(rs.getTimestamp("HARVEST.STARTED"));
			curHarvest.setDatetimeFinished(rs.getTimestamp("HARVEST.FINISHED"));
			
			curHarvest.setTotalResources(new Integer(rs.getInt("HARVEST.TOT_RESOURCES")));
			curHarvest.setEncodingSchemes(new Integer(rs.getInt("HARVEST.ENC_SCHEMES")));
			curHarvest.setTotalStatements(new Integer(rs.getInt("HARVEST.TOT_STATEMENTS")));
			curHarvest.setLitObjStatements(new Integer(rs.getInt("HARVEST.LIT_STATEMENTS")));
		}
		
		curHarvest.addMessageType(curHarvest, rs.getString("HARVEST_MESSAGE.TYPE"));
	}

	/**
	 * @return the resultList
	 */
	public List<HarvestDTO> getResultList() {
		return resultList;
	}
}
