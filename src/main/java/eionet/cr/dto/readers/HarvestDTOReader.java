package eionet.cr.dto.readers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import eionet.cr.dto.HarvestDTO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.util.sql.ResultSetBaseReader;

/**
 * 
 * @author altnyris
 *
 */
public class HarvestDTOReader extends ResultSetBaseReader {

	/** */
	List<HarvestDTO> resultList = new ArrayList<HarvestDTO>();

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.util.sql.ResultSetBaseReader#readRow(java.sql.ResultSet)
	 */
	public void readRow(ResultSet rs) throws SQLException {

		HarvestDTO harvestDTO = new HarvestDTO();
		
		harvestDTO.setHarvestId(new Integer(rs.getInt("HARVEST_ID")));
		harvestDTO.setHarvestSourceId(new Integer(rs.getInt("HARVEST_SOURCE_ID")));
		
		harvestDTO.setHarvestType(rs.getString("TYPE"));
		harvestDTO.setUser(rs.getString("USER"));
		harvestDTO.setStatus(rs.getString("STATUS"));
		
		harvestDTO.setDatetimeStarted(rs.getTimestamp("STARTED"));
		harvestDTO.setDatetimeFinished(rs.getTimestamp("FINISHED"));
		
		harvestDTO.setTotalResources(new Integer(rs.getInt("TOT_RESOURCES")));
		harvestDTO.setEncodingSchemes(new Integer(rs.getInt("ENC_SCHEMES")));
		harvestDTO.setTotalStatements(new Integer(rs.getInt("TOT_STATEMENTS")));
		harvestDTO.setLitObjStatements(new Integer(rs.getInt("LIT_STATEMENTS")));
		
		resultList.add(harvestDTO);
	}

	/**
	 * @return the resultList
	 */
	public List<HarvestDTO> getResultList() {
		return resultList;
	}
}
