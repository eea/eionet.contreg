package eionet.cr.dao;

import java.util.List;
import java.util.Map;

import eionet.cr.util.sql.SQLValue;

/**
 * 
 * @author heinljab
 *
 */
public class DAOUsageSample {

	/**
	 * 
	 */
	public static void sampleUsage(){
		
		try {
			SampleDAO sampleDAO = DAOFactory.getDAOFactory().getSampleDAO();
			List<Map<String,SQLValue>> list = sampleDAO.executeSampleQuery();
			System.out.println(list);
			
		} catch (DAOException e) {
			e.printStackTrace();
		}
	}
}
