package eionet.cr.web.action;

import java.util.List;
import java.util.Map;

import eionet.cr.search.util.DefaultColumnList;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public abstract class AbstractSearchActionBean extends AbstractCRActionBean{

	/** */
	protected List columns = null;
	
	/** */
	protected List<Map<String,String>> resultList;

	/**
	 * @return the columns
	 */
	public List getColumns() {
		if (columns==null)
			columns = getDefaultColumns();
		return columns;
	}
	
	/**
	 * 
	 */
	protected List getDefaultColumns(){
		return new DefaultColumnList();
	}

	/**
	 * @return the resultList
	 */
	public List<Map<String, String>> getResultList() {
		return resultList;
	}
}
