package eionet.cr.dto.readers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.util.sql.ResultSetBaseReader;

/**
 * 
 * @author altnyris
 *
 */
public class HarvestSourceDTOReader extends ResultSetBaseReader {

	/** */
	List<HarvestSourceDTO> resultList = new ArrayList<HarvestSourceDTO>();

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.util.sql.ResultSetBaseReader#readRow(java.sql.ResultSet)
	 */
	public void readRow(ResultSet rs) throws SQLException {

		HarvestSourceDTO harvestSourceDTO = new HarvestSourceDTO();
		harvestSourceDTO.setSourceId(new Integer(rs.getInt("HARVEST_SOURCE_ID")));
		harvestSourceDTO.setIdentifier(rs.getString("IDENTIFIER"));
		harvestSourceDTO.setPullUrl(rs.getString("PULL_URL"));
		harvestSourceDTO.setType(rs.getString("TYPE"));
		harvestSourceDTO.setEmails(rs.getString("EMAILS"));
		harvestSourceDTO.setDateCreated(rs.getDate("DATE_CREATED"));
		harvestSourceDTO.setCreator(rs.getString("CREATOR"));
		harvestSourceDTO.setStatements(new Integer(rs.getInt("STATEMENTS")));
		resultList.add(harvestSourceDTO);
	}

	/**
	 * @return the resultList
	 */
	public List<HarvestSourceDTO> getResultList() {
		return resultList;
	}
}
