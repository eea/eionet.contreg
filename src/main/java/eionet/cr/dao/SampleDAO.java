package eionet.cr.dao;

import java.util.List;
import java.util.Map;

import eionet.cr.util.sql.SQLValue;

/**
 * 
 * @author heinljab
 *
 */
public interface SampleDAO {

	/**
	 * 
	 */
	public List<Map<String,SQLValue>> executeSampleQuery() throws DAOException;
}
