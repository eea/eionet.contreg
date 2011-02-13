package eionet.cr.dao.readers;

import java.util.List;

/**
 * 
 * @author jaanus
 *
 * @param <T>
 */
public interface ResultSetReader<T> {

	/**
	 * 
	 * @return
	 */
	public List<T> getResultList();
	
	/**
	 * 
	 */
	public void endResultSet();
}
