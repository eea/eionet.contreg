package eionet.cr.dto.readers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import eionet.cr.dto.UrgentHarvestQueueItemDTO;
import eionet.cr.util.sql.ResultSetBaseReader;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class HarvestQueueItemDTOReader extends ResultSetBaseReader{

	/** */
	List<UrgentHarvestQueueItemDTO> resultList = new ArrayList<UrgentHarvestQueueItemDTO>();

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.util.sql.ResultSetBaseReader#readRow(java.sql.ResultSet)
	 */
	public void readRow(ResultSet rs) throws SQLException {
		
		UrgentHarvestQueueItemDTO dto = new UrgentHarvestQueueItemDTO();
		dto.setUrl(rs.getString("URL"));
		dto.setTimeAdded(rs.getTimestamp("TIMESTAMP"));
		dto.setPushedContent(rs.getString("PUSHED_CONTENT"));
		
		resultList.add(dto);
	}

	/**
	 * @return the resultList
	 */
	public List<UrgentHarvestQueueItemDTO> getResultList() {
		return resultList;
	}
}
