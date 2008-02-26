package eionet.cr.dto.readers;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eionet.cr.dto.HarvestMessageDTO;
import eionet.cr.util.sql.ResultSetBaseReader;
import eionet.cr.util.sql.SQLValue;

/**
 * 
 * @author heinljab
 *
 */
public class HarvestMessageDTOReader extends ResultSetBaseReader {

	/** */
	List<HarvestMessageDTO> resultList = new ArrayList<HarvestMessageDTO>();

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.util.sql.ResultSetBaseReader#readRow(java.sql.ResultSet)
	 */
	public void readRow(ResultSet rs) throws SQLException {

		HarvestMessageDTO harvestMessageDTO = new HarvestMessageDTO();
		harvestMessageDTO.setHarvestId(new Integer(rs.getInt("HARVEST_ID")));
		harvestMessageDTO.setType(rs.getString("TYPE"));
		harvestMessageDTO.setMessage(rs.getString("MESSAGE"));
		harvestMessageDTO.setStackTrace(rs.getString("STACK_TRACE"));
		resultList.add(harvestMessageDTO);
	}

	/**
	 * @return the resultList
	 */
	public List<HarvestMessageDTO> getResultList() {
		return resultList;
	}
}
