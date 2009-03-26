package eionet.cr.api.xmlrpc;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import eionet.cr.common.CRException;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public interface Services {

	/** */
	public static final String OK_RETURN_STRING = "OK";
	/**
	 * @param timestamp
	 * @return
	 * @throws CRException
	 */
	public abstract List getResourcesSinceTimestamp(Date timestamp) throws CRException;
	/**
	 * @param criteria
	 * @return
	 * @throws CRException
	 */
	public abstract List dataflowSearch(Map<String,String> criteria) throws CRException;
	/**
	 * 
	 * @param content
	 * @param sourceUri
	 * @return
	 * @throws CRException
	 */
	public abstract String pushContent(String content, String sourceUri) throws CRException;
	/**
	 * 
	 * @param attributes
	 * @return
	 * @throws CRException
	 */
	public abstract Vector getEntries(Hashtable attributes) throws CRException;
}
