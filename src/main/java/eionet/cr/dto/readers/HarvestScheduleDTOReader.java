package eionet.cr.dto.readers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import eionet.cr.dto.HarvestScheduleDTO;
import eionet.cr.util.sql.ResultSetBaseReader;

/**
 * 
 * @author altnyris
 *
 */
public class HarvestScheduleDTOReader extends ResultSetBaseReader {

	/** */
	List<HarvestScheduleDTO> resultList = new ArrayList<HarvestScheduleDTO>();

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.util.sql.ResultSetBaseReader#readRow(java.sql.ResultSet)
	 */
	public void readRow(ResultSet rs) throws SQLException {

		HarvestScheduleDTO harvestScheduleDTO = new HarvestScheduleDTO();
		harvestScheduleDTO.setHarvestSourceId(new Integer(rs.getInt("HARVEST_SOURCE_ID")));
		harvestScheduleDTO.setWeekday(rs.getString("WEEKDAY"));
		harvestScheduleDTO.setHour(new Integer(rs.getInt("HOUR")));
		harvestScheduleDTO.setPeriod(new Integer(rs.getInt("PERIOD_WEEKS")));
		resultList.add(harvestScheduleDTO);
	}

	/**
	 * @return the resultListAAA
	 */
	public List<HarvestScheduleDTO> getResultList() {
		return resultList;
	}
}
