package eionet.cr.dao.readers;

import org.openrdf.query.BindingSet;

/**
 * 
 * @author jaanus
 *
 */
public interface TupleResultSetReader {

	/**
	 * 
	 */
	public void setResultSetBindingNames();
	
	/**
	 * 
	 * @param bindingSet
	 */
	public void readTuple(BindingSet bindingSet);
}