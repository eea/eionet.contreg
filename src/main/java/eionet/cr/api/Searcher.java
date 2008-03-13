package eionet.cr.api;

import eionet.cr.util.CRException;

/**
 * 
 * @author heinljab
 *
 */
public interface Searcher {

	public java.util.List getResourcesSinceTimestamp(java.util.Date timestamp) throws CRException;
}
